package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvDisplayMobile, tvDisplayGender, tvDisplayLocation, tvDisplayQualification;
    private Button btnEditProfile, btnProfileLogout, btnAddBrand;
    private TextView tvNoBrands;
    private LinearLayout layoutBrandsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvDisplayMobile = findViewById(R.id.tvDisplayMobile);
        tvDisplayGender = findViewById(R.id.tvDisplayGender);
        tvDisplayLocation = findViewById(R.id.tvDisplayLocation);
        tvDisplayQualification = findViewById(R.id.tvDisplayQualification);
        
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);
        btnAddBrand = findViewById(R.id.btnAddBrand);
        tvNoBrands = findViewById(R.id.tvNoBrands);
        layoutBrandsList = findViewById(R.id.layoutBrandsList);

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        btnAddBrand.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, UploadBrandActivity.class));
        });

        btnProfileLogout.setOnClickListener(v -> {
            getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProfile();
    }

    private void fetchProfile() {
        String userId = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("userId", null);
        String token = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("token", null);

        if (userId == null || token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProfile("Bearer " + token, userId).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    
                    String fullName = user.getFirstName();
                    if (user.getMiddleName() != null && !user.getMiddleName().isEmpty()) {
                        fullName += " " + user.getMiddleName();
                    }
                    if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                        fullName += " " + user.getLastName();
                    }
                    
                    tvProfileName.setText(fullName);
                    tvProfileEmail.setText(user.getEmail());
                    tvDisplayMobile.setText(user.getMobile() != null && !user.getMobile().isEmpty() ? user.getMobile() : "Not provided");
                    tvDisplayGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Not provided");
                    
                    getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit()
                        .putString("userEmail", user.getEmail())
                        .putString("userMobile", user.getMobile())
                        .apply();
                    
                    String location = "";
                    if (user.getState() != null && !user.getState().isEmpty()) {
                        location += user.getState();
                    }
                    if (user.getCity() != null && !user.getCity().isEmpty()) {
                        if (!location.isEmpty()) location += ", ";
                        location += user.getCity();
                    }
                    tvDisplayLocation.setText(location.isEmpty() ? "Not provided" : location);
                    tvDisplayQualification.setText(user.getQualification() != null && !user.getQualification().isEmpty() ? user.getQualification() : "Not provided");

                    // Fetch and display user's uploaded brands
                    fetchUserBrands(user.getEmail(), user.getMobile());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile details", Toast.LENGTH_SHORT).show();
                    loadMockData(); // Fallback to mock data on server error
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadMockData(); // Fallback to mock data on network error
            }
        });
    }

    private void fetchUserBrands(String email, String mobile) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBrands().enqueue(new retrofit2.Callback<List<Brand>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Brand>> call, retrofit2.Response<List<Brand>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Brand> allBrands = response.body();
                    List<Brand> userBrands = new ArrayList<>();
                    for (Brand brand : allBrands) {
                        boolean matchesEmail = email != null && !email.isEmpty() && email.equalsIgnoreCase(brand.getOwnerEmail());
                        boolean matchesMobile = mobile != null && !mobile.isEmpty() && mobile.equals(brand.getOwnerMobile());
                        if (matchesEmail || matchesMobile) {
                            userBrands.add(brand);
                        }
                    }
                    displayUserBrands(userBrands);
                } else {
                    layoutBrandsList.removeAllViews();
                    tvNoBrands.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Brand>> call, Throwable t) {
                layoutBrandsList.removeAllViews();
                tvNoBrands.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayUserBrands(List<Brand> brands) {
        layoutBrandsList.removeAllViews();
        if (brands == null || brands.isEmpty()) {
            tvNoBrands.setVisibility(View.VISIBLE);
        } else {
            tvNoBrands.setVisibility(View.GONE);
            for (Brand brand : brands) {
                View brandView = getLayoutInflater().inflate(R.layout.item_brand, layoutBrandsList, false);
                
                ImageView ivBrandLogo = brandView.findViewById(R.id.ivBrandLogo);
                ImageView btnAddToFav = brandView.findViewById(R.id.btnAddToFav);
                TextView tvBrandName = brandView.findViewById(R.id.tvBrandName);
                TextView tvBrandCategory = brandView.findViewById(R.id.tvBrandCategory);
                TextView tvBrandInvestment = brandView.findViewById(R.id.tvBrandInvestment);
                Button btnViewDetails = brandView.findViewById(R.id.btnViewDetails);
                
                if (btnAddToFav != null) {
                    btnAddToFav.setVisibility(View.GONE);
                }
                
                if (tvBrandName != null) {
                    tvBrandName.setText(brand.getName());
                }
                if (tvBrandCategory != null) {
                    tvBrandCategory.setText(brand.getCategory());
                }
                if (tvBrandInvestment != null) {
                    tvBrandInvestment.setText("Investment: " + brand.getInvestment());
                }
                
                if (ivBrandLogo != null) {
                    int logoRes = brand.getLogoResId() != 0 ? brand.getLogoResId() : R.drawable.logo;
                    String logoUrl = RetrofitClient.getAbsoluteUrl(brand.getLogoUrl());
                    if (logoUrl != null && !logoUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(logoUrl)
                                .placeholder(logoRes)
                                .error(logoRes)
                                .into(ivBrandLogo);
                    } else {
                        ivBrandLogo.setImageResource(logoRes);
                    }
                }
                
                View.OnClickListener detailsClickListener = v -> {
                    Intent intent = new Intent(ProfileActivity.this, SecondActivity.class);
                    intent.putExtra("brandName", brand.getName());
                    intent.putExtra("brandCategory", brand.getCategory());
                    intent.putExtra("brandInvestment", brand.getInvestment());
                    intent.putExtra("brandLogoUrl", brand.getLogoUrl());
                    intent.putExtra("ownerEmail", brand.getOwnerEmail());
                    intent.putExtra("ownerMobile", brand.getOwnerMobile());
                    startActivity(intent);
                };
                
                brandView.setOnClickListener(detailsClickListener);
                if (btnViewDetails != null) {
                    btnViewDetails.setOnClickListener(detailsClickListener);
                }
                
                layoutBrandsList.addView(brandView);
            }
        }
    }

    private void loadMockData() {
        tvProfileName.setText("Abbas Khan");
        tvProfileEmail.setText("abbaskhan.cs25@bmsce.ac.in");
        tvDisplayMobile.setText("9876543210");
        tvDisplayGender.setText("Male");
        tvDisplayLocation.setText("Karnataka, Bengaluru");
        tvDisplayQualification.setText("B.E/B.Tech");
        
        getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit()
            .putString("userEmail", "abbaskhan.cs25@bmsce.ac.in")
            .putString("userMobile", "9876543210")
            .apply();

        fetchUserBrands("abbaskhan.cs25@bmsce.ac.in", "9876543210");
    }
}
