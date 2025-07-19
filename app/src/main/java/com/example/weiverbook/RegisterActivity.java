package com.example.weiverbook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnSignUp;
    private TextView tvSignInLink;
    private DatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.et_username_register);
        etPassword = findViewById(R.id.et_password_register);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvSignInLink = findViewById(R.id.tv_sign_in_link);

        btnSignUp.setOnClickListener(v -> {
            registerUser();
        });

        tvSignInLink.setOnClickListener(v -> {
            // Kembali ke halaman login
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            // Cek apakah username sudah ada
            boolean userExists = dbHelper.checkUserExists(username);

            runOnUiThread(() -> {
                if (userExists) {
                    Toast.makeText(this, "Username sudah digunakan, coba yang lain", Toast.LENGTH_SHORT).show();
                } else {
                    // Jika belum ada, tambahkan user baru
                    dbHelper.addUser(username, password);
                    Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                    finish(); // Kembali ke halaman login setelah berhasil
                }
            });
        });
    }
}