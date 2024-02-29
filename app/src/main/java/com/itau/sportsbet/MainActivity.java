package com.itau.sportsbet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.opencv.core.Size;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button phoneSetBtn = findViewById(R.id.phone_set_btn);
        phoneSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.db_name, Context.MODE_PRIVATE);
                EditText txtInputPhoneId =(EditText) findViewById(R.id.editPhoneId);
                String phoneId = txtInputPhoneId.toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("PhoneId", phoneId);
                Config.phoneId = phoneId;

                setResult(1);
            }
        });
    }
}