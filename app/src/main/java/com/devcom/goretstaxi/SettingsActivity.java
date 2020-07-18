package com.devcom.goretstaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private EditText nameInput, phoneInput, carInput;
    private ImageView closeBtn, saveBtn;
    private TextView imageChange;
    private String getType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getType = getIntent().getStringExtra("type");

        circleImageView = findViewById(R.id.profile_image);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        carInput = findViewById(R.id.carNameInput);
        closeBtn = findViewById(R.id.closeBtn);
        saveBtn = findViewById(R.id.saveBtn);
        imageChange = findViewById(R.id.changePhoto);

        if(getType.equals("Drivers")) {
            carInput.setVisibility(View.VISIBLE);
        }

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getType.equals("Drivers")) {
                    startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
                } else {
                    startActivity(new Intent(SettingsActivity.this, PassengerMapsActivity.class));
                }
            }
        });
    }
}