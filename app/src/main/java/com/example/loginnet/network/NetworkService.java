package com.example.loginnet.network;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkService {
    Map<String, String> params = new HashMap<>();
    String urlPath = "https://p.njupt.edu.cn:802/eportal/portal/login/";
    String TAG = "NetworkService";

    public NetworkService(){
        params.put("callback", "dr1003");
        params.put("login_method", "1");
        params.put("user_account", "");
        params.put("user_password", "");
        params.put("wlan_user_ip", "");
        params.put("wlan_user_mac", "000000000000");
        params.put("jsVersion", "4.1.3");
        params.put("terminal_type", "1");
        params.put("lang", "zh-cn");
        params.put("v", "5614");
    }

    public int networkRequest(String userId, String password, String supplier){
        HttpURLConnection connection;
        String supplier_c = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d("wlan_user_ip", getWlanIpv4());
                params.replace("wlan_user_ip", "10.161.164.49", getWlanIpv4());
                params.replace("user_password", password);
                if(supplier.equals("CMCC")){
                    supplier_c = "cmcc";
                } else if (supplier.equals("CHINANER")) {
                    supplier_c = "njxy";
                }
                params.replace("user_account", ",0," + userId + "@" + supplier_c);
                Log.d(TAG, "networkRequest: " + params);
                setParams();
                URL url = new URL(urlPath);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.46");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("networkRequest", Objects.requireNonNull(getStringByStream(connection.getInputStream())));
                }
                String result = getStringByStream(connection.getInputStream());
                assert result != null;
                Log.d(TAG, result);
                return getRebackInfo(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "networkRequest(Connection): " + e);
        }
        return 0;
    }

    private String getStringByStream(InputStream inputStream){
        Reader reader;
        try {
            reader=new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] rawBuffer=new char[512];
            StringBuilder buffer=new StringBuilder();
            int length;
            while ((length=reader.read(rawBuffer))!=-1){
                buffer.append(rawBuffer,0,length);
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "getStringByStream: " + e.getMessage());
        }
        return null;
    }

    private void setParams(){
        AtomicBoolean isFirst = new AtomicBoolean(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            params.forEach((key, value) -> {
                if(isFirst.get()){
                    isFirst.set(false);
                    urlPath += "?" + key + "=" + value;
                }
                else {
                    urlPath += "&" + key + "=" + value;
                }
            });
        }
    }

    private String getWlanIpv4(){
        HttpURLConnection connection;
        String ipv4 = "";
        try {
            URL url = new URL("https://p.njupt.edu.cn/a79.htm");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.46");
                connection.setRequestProperty("Referer", "https://p.njupt.edu.cn/a79.htm");
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "getWlanIpv4: " + getStringByStream(connection.getInputStream()));
                }
                String result = getStringByStream(connection.getInputStream());

                Pattern r = Pattern.compile("v46ip='(.+?)'");
                assert result != null;
                Matcher m = r.matcher(result);
                if(m.find()){
                    ipv4 =  m.group(1);
                }
                else {
                    Log.e(TAG, "getWlanIpv4: NO IPV4");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getWlanIpv4: " + e.getMessage());
        }
        return ipv4;
    }

    private int getRebackInfo(String result) {
        Pattern r = Pattern.compile("dr1003\\((.+?)\\);");
        assert result != null;
        Matcher m = r.matcher(result);
        if(m.find()){
            try {
                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(m.group(1)));
                if (Integer.valueOf(String.valueOf(jsonObject.get("result"))).equals(1)) {
                    return 1;
                } else {
                    if (Integer.valueOf(String.valueOf(jsonObject.get("ret_code"))).equals(2)) {
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }catch (JSONException e){
                Log.e(TAG, "getRebackInfo: " + e.getMessage());
            }
        }
        else {
            Log.e(TAG, "getWlanIpv4: NO IPV4");
        }
        return 0;
    }
}
