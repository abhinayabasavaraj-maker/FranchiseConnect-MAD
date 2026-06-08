package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmptyState;
    private FavoriteAdapter adapter;
    private List<Brand> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

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

        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        View btnBack = findViewById(R.id.btnBack);
        View btnFavLogout = findViewById(R.id.btnFavLogout);

        btnBack.setOnClickListener(v -> finish());
        
        btnFavLogout.setOnClickListener(v -> {
            getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupFavorites();
    }

    private void setupFavorites() {
        String userId = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("userId", null);
        if (userId == null) {
            tvEmptyState.setText("Please login to see favorites");
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getFavorites(userId).enqueue(new retrofit2.Callback<List<Brand>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Brand>> call, retrofit2.Response<List<Brand>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteList = response.body();
                    updateUI(userId);
                } else {
                    Toast.makeText(FavoritesActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Error loading favorites");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Brand>> call, Throwable t) {
                Toast.makeText(FavoritesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Network error");
            }
        });
    }

    private void updateUI(String userId) {
        if (favoriteList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
            
            adapter = new FavoriteAdapter(favoriteList, new FavoriteAdapter.OnFavoriteActionListener() {
                @Override
                public void onRemove(int position) {
                    Brand brand = favoriteList.get(position);
                    removeFromBackend(userId, brand.getId(), position);
                }

                @Override
                public void onViewDetails(Brand brand) {
                    Intent intent = new Intent(FavoritesActivity.this, SecondActivity.class);
                    intent.putExtra("brandName", brand.getName());
                    intent.putExtra("brandCategory", brand.getCategory());
                    intent.putExtra("brandInvestment", brand.getInvestment());
                    intent.putExtra("brandLogoUrl", brand.getLogoUrl());
                    intent.putExtra("ownerEmail", brand.getOwnerEmail());
                    intent.putExtra("ownerMobile", brand.getOwnerMobile());
                    startActivity(intent);
                }
            });

            rvFavorites.setLayoutManager(new GridLayoutManager(FavoritesActivity.this, 2));
            rvFavorites.setAdapter(adapter);
        }
    }

    private void removeFromBackend(String userId, String brandId, int position) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.removeFavorite(userId, brandId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    favoriteList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, favoriteList.size());
                    
                    if (favoriteList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                    Toast.makeText(FavoritesActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(FavoritesActivity.this, "Failed to remove", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
