package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Adjust for Edge-to-Edge Status Bar Insets (using a safe margin/padding setup)
        android.view.View root = findViewById(R.id.btnBack);
        
        // Bind Views
        ImageView ivDetailLogo = findViewById(R.id.ivDetailLogo);
        TextView tvDetailName = findViewById(R.id.tvDetailName);
        TextView tvDetailCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDetailCategoryOverlay = findViewById(R.id.tvDetailCategoryOverlay);
        TextView tvDetailInvestment = findViewById(R.id.tvDetailInvestment);
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnApplyNow = findViewById(R.id.btnApplyNow);

        // Get Data from Intent
        String name = getIntent().getStringExtra("brandName");
        String category = getIntent().getStringExtra("brandCategory");
        String investment = getIntent().getStringExtra("brandInvestment");
        String logoUrl = getIntent().getStringExtra("brandLogoUrl");
        String ownerEmail = getIntent().getStringExtra("ownerEmail");
        String ownerMobile = getIntent().getStringExtra("ownerMobile");

        // Set content
        if (name != null) tvDetailName.setText(name);
        if (category != null) {
            tvDetailCategory.setText(category);
            if (tvDetailCategoryOverlay != null) {
                tvDetailCategoryOverlay.setText(category);
            }
        }
        if (investment != null) tvDetailInvestment.setText(investment);

        // Load image using Glide with dynamic host resolution
        int logoRes = R.drawable.logo;
        String absoluteLogoUrl = RetrofitClient.getAbsoluteUrl(logoUrl);
        if (absoluteLogoUrl != null && !absoluteLogoUrl.isEmpty()) {
            Glide.with(this)
                    .load(absoluteLogoUrl)
                    .placeholder(logoRes)
                    .error(logoRes)
                    .into(ivDetailLogo);
        } else {
            ivDetailLogo.setImageResource(logoRes);
        }

        // Set Click Listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        if (btnApplyNow != null) {
            btnApplyNow.setOnClickListener(v -> 
                showInquiryDialog(name, category, investment, ownerEmail, ownerMobile)
            );
        }
    }

    private void showInquiryDialog(final String brandName, final String brandCategory, final String brandInvestment, final String ownerEmail, final String ownerMobile) {
        final String finalOwnerEmail = (ownerEmail == null || ownerEmail.isEmpty()) ? "abhinayabasavaraj@gmail.com" : ownerEmail;
        final String finalOwnerMobile = (ownerMobile == null || ownerMobile.isEmpty()) ? "9876543210" : ownerMobile;

        final SharedPreferences prefs = getSharedPreferences("FranchiseConnect", MODE_PRIVATE);
        final String investorName = prefs.getString("userName", "Investor");
        String investorEmail = prefs.getString("userEmail", "");
        String investorMobile = prefs.getString("userMobile", "");

        if (investorEmail.isEmpty()) {
            String userId = prefs.getString("userId", null);
            String token = prefs.getString("token", null);
            if (userId != null && token != null) {
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.getProfile("Bearer " + token, userId).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            String fullName = user.getFirstName();
                            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                                fullName += " " + user.getLastName();
                            }
                            prefs.edit()
                                .putString("userName", fullName)
                                .putString("userEmail", user.getEmail())
                                .putString("userMobile", user.getMobile())
                                .apply();
                            
                            showInquiryDialogActual(brandName, brandCategory, brandInvestment, finalOwnerEmail, finalOwnerMobile, fullName, user.getEmail(), user.getMobile());
                        } else {
                            showInquiryDialogActual(brandName, brandCategory, brandInvestment, finalOwnerEmail, finalOwnerMobile, investorName, "", "");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        showInquiryDialogActual(brandName, brandCategory, brandInvestment, finalOwnerEmail, finalOwnerMobile, investorName, "", "");
                    }
                });
                return;
            }
        }
        
        showInquiryDialogActual(brandName, brandCategory, brandInvestment, finalOwnerEmail, finalOwnerMobile, investorName, investorEmail, investorMobile);
    }

    private void showInquiryDialogActual(final String brandName, final String brandCategory, final String brandInvestment, 
                                         final String ownerEmail, final String ownerMobile, 
                                         final String investorName, final String investorEmail, final String investorMobile) {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Franchise Inquiry - " + brandName);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("You are inquiring about " + brandName + ". An email will be composed to the brand owner (" + ownerEmail + ").");
        tvInfo.setTextColor(android.graphics.Color.WHITE);
        tvInfo.setAlpha(0.8f);
        tvInfo.setTextSize(14);
        tvInfo.setPadding(0, 0, 0, 30);
        layout.addView(tvInfo);

        final CheckBox cbShareMobile = new CheckBox(this);
        cbShareMobile.setText("Share my mobile number");
        cbShareMobile.setChecked(true);
        cbShareMobile.setTextColor(android.graphics.Color.WHITE);
        cbShareMobile.setTextSize(15);
        layout.addView(cbShareMobile);

        final EditText etMobile = new EditText(this);
        etMobile.setHint("Mobile number");
        etMobile.setHintTextColor(android.graphics.Color.GRAY);
        etMobile.setTextColor(android.graphics.Color.WHITE);
        etMobile.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        etMobile.setText(investorMobile != null ? investorMobile : "");
        etMobile.setPadding(0, 20, 0, 20);
        layout.addView(etMobile);

        cbShareMobile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etMobile.setEnabled(isChecked);
            if (isChecked) {
                etMobile.setAlpha(1.0f);
            } else {
                etMobile.setAlpha(0.5f);
            }
        });

        builder.setView(layout);

        builder.setPositiveButton("Send Mail", (dialog, which) -> {
            String sharedMobile = "";
            if (cbShareMobile.isChecked()) {
                sharedMobile = etMobile.getText().toString().trim();
                if (!sharedMobile.isEmpty()) {
                    getSharedPreferences("FranchiseConnect", MODE_PRIVATE).edit().putString("userMobile", sharedMobile).apply();
                }
            }
            sendEmailViaIntent(brandName, brandCategory, brandInvestment, ownerEmail, investorName, investorEmail, sharedMobile);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        AlertDialog alertDialog = builder.create();
        
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.drawable.dialog_holo_dark_frame);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#60a5fa"));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.GRAY);
        });
        
        alertDialog.show();
    }

    private void sendEmailViaIntent(String brandName, String brandCategory, String brandInvestment, String ownerEmail,
                                    String investorName, String investorEmail, String sharedMobile) {
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear Brand Owner,\n\n");
        bodyBuilder.append("I am interested in acquiring a franchise for your brand, ").append(brandName).append(".\n\n");
        bodyBuilder.append("Brand Details:\n");
        bodyBuilder.append("- Category: ").append(brandCategory).append("\n");
        bodyBuilder.append("- Investment Range: ").append(brandInvestment).append("\n\n");
        bodyBuilder.append("Investor Details:\n");
        bodyBuilder.append("- Name: ").append(investorName).append("\n");
        if (investorEmail != null && !investorEmail.isEmpty()) {
            bodyBuilder.append("- Email: ").append(investorEmail).append("\n");
        }
        if (!sharedMobile.isEmpty()) {
            bodyBuilder.append("- Mobile: ").append(sharedMobile).append("\n");
        }
        bodyBuilder.append("\nPlease share details regarding the franchise application process, requirements, and next steps.\n\n");
        bodyBuilder.append("Best regards,\n");
        bodyBuilder.append(investorName);

        String emailBody = bodyBuilder.toString();
        String emailSubject = "Franchise Inquiry - " + brandName;

        Uri uri = Uri.parse("mailto:" + Uri.encode(ownerEmail) +
                "?subject=" + Uri.encode(emailSubject) +
                "&body=" + Uri.encode(emailBody));
        
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(uri);

        try {
            startActivity(Intent.createChooser(intent, "Send email via..."));
            Toast.makeText(this, "Opening Mail Client...", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found on your device.", Toast.LENGTH_LONG).show();
        }
    }
}
