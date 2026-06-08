package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ImageView ivAdCarousel, btnNavFavorites, btnNavProfile;
    private TextView tvWelcomeDashboard;
    private LinearLayout layoutCategories;
    private RecyclerView rvBrands;
    private GridLayout gridSimilarSites;
    private Button btnDashUploadBrand;
    private View btnNavLogout;

    private List<Brand> allBrands = new ArrayList<>();
    private BrandAdapter adapter;
    private List<Button> categoryButtons = new ArrayList<>();

    private int currentAdIndex = 0;
    private int[] adImages = {
        R.drawable.ad1, 
        R.drawable.ad2, 
        R.drawable.ad3,
        R.drawable.ad4,
        R.drawable.ad5
    };
    private String[] adUrls = {
        "https://franchising.bk.com/franchise-fee",
        "https://www.vrlgroup.in/vrl_investors_desk.aspx",
        "https://franchise.tealogy.in/",
        "https://www.lakmesalon.in/",
        "https://www.franchiseindia.com/brands/t-r-autoworks.104371"
    };

    private String[][] similarSites = {
        {"Entrepreneur", "https://www.entrepreneur.com"},
        {"Indian Retailer", "https://www.indianretailer.com"},
        {"Restaurant India", "https://www.restaurantindia.in/"},
        {"Franchise Nepal", "https://www.franchisenepal.com/"},
        {"Franchise BD", "https://www.franchisebangladesh.com/"},
        {"Brand License", "https://www.indianretailer.com/brandlicense/"},
        {"BusinessEx", "https://www.businessex.com/"},
        {"Bradford", "https://www.bradfordlicenseindia.com/"},
        {"Francorp", "https://www.francorp.in/"},
        {"FranGlobal", "https://www.franglobal.com/"},
        {"Gaurav Marya", "https://www.gauravmarya.com/"},
        {"MSME", "https://www.msme.in/"}
    };

    private int[] similarSiteImages = {
        R.drawable.sim1, R.drawable.sim2, R.drawable.sim3, R.drawable.sim4,
        R.drawable.sim5, R.drawable.sim6, R.drawable.sim7, R.drawable.sim8,
        R.drawable.sim9, R.drawable.sim10, R.drawable.sim11, R.drawable.sim12
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
        ivAdCarousel = findViewById(R.id.ivAdCarousel);
        tvWelcomeDashboard = findViewById(R.id.tvWelcomeDashboard);
        layoutCategories = findViewById(R.id.layoutCategories);
        rvBrands = findViewById(R.id.rvBrands);
        gridSimilarSites = findViewById(R.id.gridSimilarSites);
        btnDashUploadBrand = findViewById(R.id.btnDashUploadBrand);
        btnNavFavorites = findViewById(R.id.btnNavFavorites);
        btnNavProfile = findViewById(R.id.btnNavProfile);
        btnNavLogout = findViewById(R.id.btnNavLogout);

        setupCategories();
        setupAdCarousel();
        setupSimilarSites();
        setupBrands();

        // Navigation
        btnNavProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
        });
        
        btnNavFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Favorites...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, FavoritesActivity.class));
        });
        
        btnDashUploadBrand.setOnClickListener(v -> startActivity(new Intent(this, UploadBrandActivity.class)));
        
        btnNavLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnAdNext).setOnClickListener(v -> nextAd());
        findViewById(R.id.btnAdPrev).setOnClickListener(v -> prevAd());
        
        ivAdCarousel.setImageResource(adImages[0]);
        tvWelcomeDashboard.setText("Welcome, User!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the brands list every time the user returns to this screen
        setupBrands();
    }

    private void setupBrands() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBrands().enqueue(new retrofit2.Callback<List<Brand>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Brand>> call, retrofit2.Response<List<Brand>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBrands = response.body();
                    // Copy to prevent modifications directly to allBrands list
                    List<Brand> displayList = new ArrayList<>(allBrands);
                    adapter = new BrandAdapter(displayList, new BrandAdapter.OnBrandClickListener() {
                        @Override
                        public void onBrandClick(Brand brand) {
                            Intent intent = new Intent(DashboardActivity.this, SecondActivity.class);
                            intent.putExtra("brandName", brand.getName());
                            intent.putExtra("brandCategory", brand.getCategory());
                            intent.putExtra("brandInvestment", brand.getInvestment());
                            intent.putExtra("brandLogoUrl", brand.getLogoUrl());
                            intent.putExtra("ownerEmail", brand.getOwnerEmail());
                            intent.putExtra("ownerMobile", brand.getOwnerMobile());
                            startActivity(intent);
                        }

                        @Override
                        public void onFavoriteClick(Brand brand) {
                            addToFavorites(brand);
                        }
                    });
                    rvBrands.setLayoutManager(new GridLayoutManager(DashboardActivity.this, 2));
                    rvBrands.setAdapter(adapter);
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load brands", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Brand>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToFavorites(Brand brand) {
        String userId = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        FavoriteRequest request = new FavoriteRequest(userId, brand.getId());
        
        apiService.addFavorite(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, brand.getName() + " added to favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Already in favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategories() {
        String[] categories = {
            "All Categories", "Food & Beverages", "Retail & Fashion", 
            "Health & Wellness", "Education", "Beauty & Salon", 
            "Automobiles", "Electronics", "Real Estate"
        };
        categoryButtons.clear();
        layoutCategories.removeAllViews();
        
        for (String cat : categories) {
            Button btn = new Button(this);
            btn.setText(cat);
            btn.setAllCaps(false);
            btn.setTextColor(getResources().getColor(android.R.color.white));
            
            // Set first button "All Categories" as default selected
            if (cat.equals("All Categories")) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6"))); // Solid Blue
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A60A5FA"))); // Semi-transparent
            }
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(v -> {
                // Update button visual states
                for (Button b : categoryButtons) {
                    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A60A5FA")));
                }
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6")));
                
                // Filter logic
                if (adapter != null) {
                    if (cat.equals("All Categories")) {
                        adapter.updateList(new ArrayList<>(allBrands));
                    } else {
                        List<Brand> filtered = new ArrayList<>();
                        for (Brand b : allBrands) {
                            if (b.getCategory() != null && b.getCategory().equalsIgnoreCase(cat)) {
                                filtered.add(b);
                            }
                        }
                        adapter.updateList(filtered);
                        if (filtered.isEmpty()) {
                            Toast.makeText(this, "No brands found in " + cat, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            categoryButtons.add(btn);
            layoutCategories.addView(btn);
        }
    }

    private void setupAdCarousel() {
        ivAdCarousel.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adUrls[currentAdIndex]));
            startActivity(intent);
        });
        
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                nextAd();
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void nextAd() {
        currentAdIndex = (currentAdIndex + 1) % adImages.length;
        ivAdCarousel.setImageResource(adImages[currentAdIndex]);
    }

    private void prevAd() {
        currentAdIndex = (currentAdIndex - 1 + adImages.length) % adImages.length;
        ivAdCarousel.setImageResource(adImages[currentAdIndex]);
    }

    private void setupSimilarSites() {
        gridSimilarSites.removeAllViews();
        for (int i = 0; i < similarSites.length; i++) {
            final int index = i;
            ImageView iv = new ImageView(this);
            iv.setImageResource(similarSiteImages[i]);
            iv.setAdjustViewBounds(true);
            iv.setPadding(8, 8, 8, 8);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            iv.setLayoutParams(params);

            iv.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(similarSites[index][1]));
                startActivity(intent);
            });
            gridSimilarSites.addView(iv);
        }
    }
}
