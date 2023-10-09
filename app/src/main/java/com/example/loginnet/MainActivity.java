package com.example.loginnet;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginnet.network.NetworkService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private EditText user_id_edittext;
    private EditText password_edittext;
    private Spinner supplier_spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout);
        String[] infoArr = readDataFile();
        Button connection_button = findViewById(R.id.connectiontest_button);
        Button save_button = findViewById(R.id.saveInfo_button);
        user_id_edittext = findViewById(R.id.userId);
        password_edittext = findViewById(R.id.userPassword);
        supplier_spinner = findViewById(R.id.sender_spinner);
        connection_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        onClickTestButton(v);
                    }
                }.start();
            }
        });

        save_button.setOnClickListener(v -> {
            String filename = "cuser.dat";
            String userId = String.valueOf(user_id_edittext.getText());
            String password = String.valueOf(password_edittext.getText());
            String supplier = supplier_spinner.getSelectedItem().toString();

            if(userId.isEmpty()){
                Toast.makeText(this, "请填写学号！", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "请填写密码！", Toast.LENGTH_SHORT).show();
                return;
            }

            if (supplier.isEmpty()) {
                Toast.makeText(this, "请选择运营商！", Toast.LENGTH_SHORT).show();
                return;
            }

            String outputData = userId+";"+password+";"+supplier;

            try (FileOutputStream fos = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(outputData.getBytes());
                Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("FileOpen", "onCreate: " + e.getMessage());
            }
        });

        if (infoArr != null){
            user_id_edittext.setText(infoArr[0]);
            password_edittext.setText(infoArr[1]);
            for (int i = 0; i<supplier_spinner.getCount(); i++){
                if(String.valueOf(supplier_spinner.getItemAtPosition(i)).equals(infoArr[2])){
                    supplier_spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onClickTestButton(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    String[] infoArr = readDataFile();
                    String content;
                    if(infoArr != null){
                        NetworkService networkService = new NetworkService();
                        int rt = networkService.networkRequest(infoArr[0], infoArr[1], infoArr[2]);
                        if(rt == 1){
                            content = "连接成功";
                        } else if (rt == 2) {
                            content = "已登录";
                        } else {
                            content = "登录失败，请检查用户名和密码重新连接!";
                        }
                    } else {
                        content = "请填写学号和密码，并且保存!";
                    }
                    Intent intent = new Intent();
                    intent.setAction("com.java.androidtest.SEND_MESSAGE");
                    intent.putExtra("content",content);
                    intent.setPackage(getPackageName());
                    sendBroadcast(intent);
                }
            }.start();
        }
    }

    private String[] readDataFile(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileInputStream fis = null;
            try {
                fis = this.openFileInput("cuser.dat");
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "请填写学号和密码，并且保存!", Toast.LENGTH_SHORT).show();
            }
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                String[] infoArr = stringBuilder.toString().split(";");
                if(infoArr.length == 3){
                    return infoArr;
                } else {
                    Toast.makeText(this, "请填写学号和密码，并且保存!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "储存文件错误，请重新保存文件!", Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }
}
