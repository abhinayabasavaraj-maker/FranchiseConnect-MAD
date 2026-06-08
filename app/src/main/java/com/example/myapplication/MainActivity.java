package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvBrandCount, tvUserCount, tvEmail1, tvEmail2, tvEmail3, tvEmail4;
    private Button btnGetStarted, btnNavRegister, btnNavLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adjust for Edge-to-Edge Status Bar Insets
        View navbar = findViewById(R.id.layoutNavbar);
        if (navbar != null) {
            int originalPaddingTop = navbar.getPaddingTop();
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(navbar, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top + originalPaddingTop, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        // Initialize Views
        tvBrandCount = findViewById(R.id.tvBrandCount);
        tvUserCount = findViewById(R.id.tvUserCount);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvEmail1 = findViewById(R.id.tvEmail1);
        tvEmail2 = findViewById(R.id.tvEmail2);
        tvEmail3 = findViewById(R.id.tvEmail3);
        tvEmail4 = findViewById(R.id.tvEmail4);
        btnNavLogin = findViewById(R.id.btnNavLogin);
        btnNavRegister = findViewById(R.id.btnNavRegister);

        // Set Click Listeners
        btnNavLogin.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening Login...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        btnNavRegister.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening Registration...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        tvEmail1.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:abbaskhan.cs25@bmsce.ac.in"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        tvEmail2.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:abhinaya.cs24@bmsce.ac.in"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        tvEmail3.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:abhishek.cs25@bmsce.ac.in"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        tvEmail4.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:abhinayaks.cs25@bmsce.ac.in"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        // Fetch Stats from API
        fetchStats();
    }

    private void fetchStats() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Fetch Brand Count
        apiService.getBrandCount().enqueue(new Callback<CountResponse>() {
            @Override
            public void onResponse(Call<CountResponse> call, Response<CountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvBrandCount.setText(String.valueOf(response.body().getCount()));
                }
            }

            @Override
            public void onFailure(Call<CountResponse> call, Throwable t) {
                tvBrandCount.setText("0");
            }
        });

        // Fetch User Count
        apiService.getUserCount().enqueue(new Callback<CountResponse>() {
            @Override
            public void onResponse(Call<CountResponse> call, Response<CountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvUserCount.setText(String.valueOf(response.body().getCount()));
                }
            }

            @Override
            public void onFailure(Call<CountResponse> call, Throwable t) {
                tvUserCount.setText("0");
            }
        });
    }
}
