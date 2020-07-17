package com.devcom.goretstaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button driverBtn, consumerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        driverBtn = findViewById(R.id.driverBtn);
        consumerBtn = findViewById(R.id.consumerBtn);

        driverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driverIntent = new Intent(WelcomeActivity.this, DriverRegLogActivity.class);
                startActivity(driverIntent);
            }
        });

        consumerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent consumerIntent = new Intent(WelcomeActivity.this, ConsumerRegLogActivity.class);
                startActivity(consumerIntent);
            }
        });
    }
}