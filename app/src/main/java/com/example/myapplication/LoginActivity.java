package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLoginSubmit.setOnClickListener(v -> {
            performLogin();
        });

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        User loginUser = new User(email, password);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        apiService.login(loginUser).enqueue(new retrofit2.Callback<UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    // Save User ID and Token in SharedPreferences
                    String userId = response.body().getUser().getId();
                    String token = response.body().getToken();
                    getSharedPreferences("FranchiseConnect", MODE_PRIVATE)
                        .edit()
                        .putString("userId", userId)
                        .putString("userName", response.body().getUser().getName())
                        .putString("userEmail", response.body().getUser().getEmail())
                        .putString("userMobile", response.body().getUser().getMobile())
                        .putString("token", token)
                        .apply();

                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Invalid Email or Password";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
