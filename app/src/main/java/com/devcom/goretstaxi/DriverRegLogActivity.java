package com.devcom.goretstaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegLogActivity extends AppCompatActivity {

    private Button signInBtn, signUpBtn;
    private EditText emailInput, passInput;
    private TextView driverStatus, question;
    private FirebaseAuth mAuth;
    private DatabaseReference driverDatabaseRef;

    private String onlineDriverID;
    private ProgressDialog loadingBar;

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

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passInput.getText().toString();

                registerDriver(email, password);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passInput.getText().toString();

                signInDriver(email, password);
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInBtn.setVisibility(View.INVISIBLE);
                question.setVisibility(View.INVISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                driverStatus.setText("Регистрация водителя");
            }
        });
    }

    private void registerDriver(String email, String password) {
        loadingBar.setTitle("Регистрация водителя");
        loadingBar.setMessage("Пожалуйста подождите");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onlineDriverID = mAuth.getCurrentUser().getUid();
                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Drivers").child(onlineDriverID);
                    driverDatabaseRef.setValue(true);

                    Intent driverMapIntent = new Intent(DriverRegLogActivity.this, DriverMapActivity.class);
                    startActivity(driverMapIntent);
                    Toast.makeText(DriverRegLogActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(DriverRegLogActivity.this, "Произошла ошибка регистрации " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInDriver(String email, String password) {
        loadingBar.setTitle("Вход водителя");
        loadingBar.setMessage("Пожалуйста подождите");
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent driverMapIntent = new Intent(DriverRegLogActivity.this, DriverMapActivity.class);
                    startActivity(driverMapIntent);
                    Toast.makeText(DriverRegLogActivity.this, "Вход прошел успешно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(DriverRegLogActivity.this, "Произошла ошибка входа " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent driverMapIntent = new Intent(DriverRegLogActivity.this, DriverMapActivity.class);
                    startActivity(driverMapIntent);
                }
            }
        });
    }
}