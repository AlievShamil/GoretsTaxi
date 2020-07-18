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

public class PassengerRegLogActivity extends AppCompatActivity {

    private Button signInBtn, signUpBtn;
    private EditText emailInput, passInput;
    private TextView passengerStatus, question;

    private FirebaseAuth mAuth;
    private DatabaseReference passengerDatabaseRef;

    private String onlinePassengerID;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_reg_log);

        signInBtn = findViewById(R.id.signInPassenger);
        signUpBtn = findViewById(R.id.singUpPassenger);
        emailInput = findViewById(R.id.passengerEmail);
        passInput = findViewById(R.id.passengerPassword);
        passengerStatus = findViewById(R.id.statusPassenger);
        question = findViewById(R.id.accountCreate);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passInput.getText().toString();

                registerPassenger(email, password);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passInput.getText().toString();

                signInPassenger(email, password);
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInBtn.setVisibility(View.INVISIBLE);
                question.setVisibility(View.INVISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                passengerStatus.setText("Регистрация пассажира");
            }
        });
    }

    private void registerPassenger(String email, String password) {
        loadingBar.setTitle("Регистрация пассажира");
        loadingBar.setMessage("Пожалуйста подождите");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onlinePassengerID = mAuth.getCurrentUser().getUid();
                    passengerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Passengers").child(onlinePassengerID);
                    passengerDatabaseRef.setValue(true);

                    Intent passengerIntent = new Intent(PassengerRegLogActivity.this, PassengerMapsActivity.class);
                    startActivity(passengerIntent);
                    Toast.makeText(PassengerRegLogActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                } else {
                    loadingBar.dismiss();
                    Toast.makeText(PassengerRegLogActivity.this, "Произошла ошибка регистрации " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInPassenger(String email, String password) {
        loadingBar.setTitle("Вход пассажира");
        loadingBar.setMessage("Пожалуйста подождите");
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    Toast.makeText(PassengerRegLogActivity.this, "Вход прошел успешно", Toast.LENGTH_SHORT).show();
                    Intent passengerIntent = new Intent(PassengerRegLogActivity.this, PassengerMapsActivity.class);
                    startActivity(passengerIntent);
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(PassengerRegLogActivity.this, "Произошла ошибка входа " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}