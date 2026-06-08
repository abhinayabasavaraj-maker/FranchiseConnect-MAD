package com.example.myapplication;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadBrandActivity extends AppCompatActivity {

    private EditText etBrandName, etSubCategory, etAboutBrand, etMinInvestment, etMaxInvestment;
    private EditText etBrandFee, etAnticipatedReturn, etPaybackPeriod, etAreaRequired;
    private EditText etFranchiseOutlets, etEstablishedYear, etOperationCommenced, etFranchiseCommenced;
    private Spinner spCategory;
    private CheckBox cbIsRenewable;
    private Button btnSubmitBrand, btnCancel, btnUploadLogo;
    private ImageView ivUploadedLogoPreview;
    private CardView cvUploadedLogoPreview;
    private String selectedLogoUrl = "";

    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    ivUploadedLogoPreview.setImageURI(uri);
                    cvUploadedLogoPreview.setVisibility(View.VISIBLE);
                    uploadLogoToServer(uri);
                }
            });

    private String[] categories = {
            "Food & Beverages", "Retail & Fashion", "Health & Wellness",
            "Education", "Beauty & Salon", "Automobiles",
            "Electronics", "Real Estate"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_brand);

        // Initialize Views
        etBrandName = findViewById(R.id.etBrandName);
        spCategory = findViewById(R.id.spCategory);
        etSubCategory = findViewById(R.id.etSubCategory);
        etAboutBrand = findViewById(R.id.etAboutBrand);
        etMinInvestment = findViewById(R.id.etMinInvestment);
        etMaxInvestment = findViewById(R.id.etMaxInvestment);
        etBrandFee = findViewById(R.id.etBrandFee);
        etAnticipatedReturn = findViewById(R.id.etAnticipatedReturn);
        etPaybackPeriod = findViewById(R.id.etPaybackPeriod);
        etAreaRequired = findViewById(R.id.etAreaRequired);
        etFranchiseOutlets = findViewById(R.id.etFranchiseOutlets);
        etEstablishedYear = findViewById(R.id.etEstablishedYear);
        etOperationCommenced = findViewById(R.id.etOperationCommenced);
        etFranchiseCommenced = findViewById(R.id.etFranchiseCommenced);
        cbIsRenewable = findViewById(R.id.cbIsRenewable);
        btnSubmitBrand = findViewById(R.id.btnSubmitBrand);
        btnCancel = findViewById(R.id.btnCancel);
        btnUploadLogo = findViewById(R.id.btnUploadLogo);
        cvUploadedLogoPreview = findViewById(R.id.cvUploadedLogoPreview);
        ivUploadedLogoPreview = findViewById(R.id.ivUploadedLogoPreview);

        btnUploadLogo.setOnClickListener(v -> pickMedia.launch("image/*"));

        // Setup Category Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, categories);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Date Pickers
        etOperationCommenced.setOnClickListener(v -> showDatePicker(etOperationCommenced));
        etFranchiseCommenced.setOnClickListener(v -> showDatePicker(etFranchiseCommenced));

        btnSubmitBrand.setOnClickListener(v -> {
            if (validateForm()) {
                submitBrand();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void uploadLogoToServer(Uri uri) {
        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitBrand.setEnabled(false);
        btnSubmitBrand.setText("Uploading Logo...");

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.uploadImage(body).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                btnSubmitBrand.setEnabled(true);
                btnSubmitBrand.setText("Upload Brand");
                if (response.isSuccessful() && response.body() != null) {
                    selectedLogoUrl = response.body().getUrl();
                    Toast.makeText(UploadBrandActivity.this, "Logo ready!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UploadBrandActivity.this, "Logo upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                btnSubmitBrand.setEnabled(true);
                btnSubmitBrand.setText("Upload Brand");
                Toast.makeText(UploadBrandActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitBrand() {
        String name = etBrandName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String investment = "₹" + etMinInvestment.getText().toString() + " - " + etMaxInvestment.getText().toString();
        
        String logoUrl = (selectedLogoUrl == null || selectedLogoUrl.isEmpty()) 
            ? "https://logo.clearbit.com/" + name.toLowerCase().replace(" ", "") + ".com" 
            : selectedLogoUrl; 

        String ownerEmail = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("userEmail", "");
        String ownerMobile = getSharedPreferences("FranchiseConnect", MODE_PRIVATE).getString("userMobile", "");
        Brand newBrand = new Brand(name, category, investment, logoUrl, ownerEmail, ownerMobile);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.createBrand(newBrand).enqueue(new Callback<Brand>() {
            @Override
            public void onResponse(Call<Brand> call, Response<Brand> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadBrandActivity.this, name + " added to " + category, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(UploadBrandActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Brand> call, Throwable t) {
                Toast.makeText(UploadBrandActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(final EditText dateField) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> dateField.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth),
                year, month, day);
        datePickerDialog.show();
    }

    private boolean validateForm() {
        if (etBrandName.getText().toString().trim().isEmpty()) {
            etBrandName.setError("Required");
            return false;
        }
        if (etMinInvestment.getText().toString().isEmpty() || etMaxInvestment.getText().toString().isEmpty()) {
            Toast.makeText(this, "Investment range is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = new File(getCacheDir(), "temp_logo.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }
}
