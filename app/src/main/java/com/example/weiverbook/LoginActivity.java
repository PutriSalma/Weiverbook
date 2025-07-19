package com.example.weiverbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private TextView tvSignUp;
    private DatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        dbHelper = new DatabaseHelper(this);

        tvSignUp = findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        executorService.execute(() -> {
            addDummyUser();
        });

        btnSignIn.setOnClickListener(view -> {
            String username = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                boolean isLoggedIn = dbHelper.checkUser(username, password);
                runOnUiThread(() -> {
                    if (isLoggedIn) {
                        int userId = dbHelper.getUserId(username);
                        SessionManager.login(userId, username);

                        Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Username atau Password Salah", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void addDummyUser() {
        dbHelper.addUser("putri", "putri123");
        dbHelper.addUser("user", "user123");
    }
}