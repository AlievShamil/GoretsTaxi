package com.devcom.goretstaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(welcomeIntent);

//                    Intent welcomeIntent = new Intent(MainActivity.this, DriverMapActivity.class);
//                    startActivity(welcomeIntent);
                }
            }
        };

        thread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}