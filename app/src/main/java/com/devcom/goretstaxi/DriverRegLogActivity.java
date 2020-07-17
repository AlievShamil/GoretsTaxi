package com.devcom.goretstaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DriverRegLogActivity extends AppCompatActivity {

    private Button signInBtn, signUpBtn;
    private EditText emailInput,passInput;
    private TextView driverStatus, question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_reg_log);

        signInBtn = findViewById(R.id.signInDriver);
        signUpBtn = findViewById(R.id.singUpDriver);
        emailInput = findViewById(R.id.driverEmail);
        passInput = findViewById(R.id.driverPassword);
        driverStatus = findViewById(R.id.statusDriver);
        question = findViewById(R.id.accountCreate);
        
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DriverRegLogActivity.this, "Click", Toast.LENGTH_SHORT).show();
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInBtn.setVisibility(View.INVISIBLE);
                question.setVisibility(View.INVISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                driverStatus.setText("Регистрация для водителей");
            }
        });
    }
}