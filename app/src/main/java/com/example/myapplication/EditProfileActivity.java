package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etEditFirstName, etEditMiddleName, etEditLastName, etEditMobile, etEditCity;
    private Spinner spEditState, spEditGender, spEditQualification;
    private Button btnSaveProfile, btnCancelEdit;
    private ImageView btnBack;

    private User currentUser;

    private final String[] indianStates = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
            "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
            "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim",
            "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Jammu & Kashmir", "Ladakh"
    };

    private final String[] qualifications = {
            "SSLC", "PUC", "Diploma", "B.Sc", "B.Com", "B.E/B.Tech", "BCA", "BBA", "M.Sc", "M.Com", "M.E/M.Tech", "MCA", "MBA", "PhD"
    };

    private final String[] genders = {"Male", "Female", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Bind Views
        etEditFirstName = findViewById(R.id.etEditFirstName);
        etEditMiddleName = findViewById(R.id.etEditMiddleName);
        etEditLastName = findViewById(R.id.etEditLastName);
        etEditMobile = findViewById(R.id.etEditMobile);
        etEditCity = findViewById(R.id.etEditCity);
        spEditState = findViewById(R.id.spEditState);
        spEditGender = findViewById(R.id.spEditGender);
        spEditQualification = findViewById(R.id.spEditQualification);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnBack = findViewById(R.id.btnBack);

        // Setup Spinners
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, indianStates);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditState.setAdapter(stateAdapter);

        ArrayAdapter<String> qualAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, qualifications);
        qualAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditQualification.setAdapter(qualAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditGender.setAdapter(genderAdapter);

        // Setup Click Listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnCancelEdit.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        // Fetch current profile data
        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        SharedPreferences prefs = getSharedPreferences("FranchiseConnect", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String token = prefs.getString("token", null);

        if (userId == null || token == null) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProfile("Bearer " + token, userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    prefillFields(currentUser);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prefillFields(User user) {
        if (user.getFirstName() != null) etEditFirstName.setText(user.getFirstName());
        if (user.getMiddleName() != null) etEditMiddleName.setText(user.getMiddleName());
        if (user.getLastName() != null) etEditLastName.setText(user.getLastName());
        if (user.getMobile() != null) etEditMobile.setText(user.getMobile());
        if (user.getCity() != null) etEditCity.setText(user.getCity());

        // Preselect state spinner
        if (user.getState() != null) {
            spEditState.setSelection(getSpinnerIndex(spEditState, user.getState()));
        }

        // Preselect gender spinner
        if (user.getGender() != null) {
            spEditGender.setSelection(getSpinnerIndex(spEditGender, user.getGender()));
        }

        // Preselect qualification spinner
        if (user.getQualification() != null) {
            spEditQualification.setSelection(getSpinnerIndex(spEditQualification, user.getQualification()));
        }
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return 0;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void saveProfileChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "Profile data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etEditFirstName.getText().toString().trim();
        String middleName = etEditMiddleName.getText().toString().trim();
        String lastName = etEditLastName.getText().toString().trim();
        String mobile = etEditMobile.getText().toString().trim();
        String city = etEditCity.getText().toString().trim();
        String state = spEditState.getSelectedItem().toString();
        String gender = spEditGender.getSelectedItem().toString();
        String qualification = spEditQualification.getSelectedItem().toString();

        if (TextUtils.isEmpty(firstName)) {
            etEditFirstName.setError("First Name is required");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            etEditLastName.setError("Last Name is required");
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            etEditMobile.setError("Mobile number is required");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            etEditCity.setError("City is required");
            return;
        }

        // Update fields on the current user object
        currentUser.setFirstName(firstName);
        currentUser.setMiddleName(middleName);
        currentUser.setLastName(lastName);
        currentUser.setMobile(mobile);
        currentUser.setCity(city);
        currentUser.setState(state);
        currentUser.setGender(gender);
        currentUser.setQualification(qualification);

        SharedPreferences prefs = getSharedPreferences("FranchiseConnect", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String token = prefs.getString("token", null);

        if (userId == null || token == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving Changes...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateProfile("Bearer " + token, userId, currentUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Changes");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Update local shared preference cache for uploader/inquiry email compositions
                    String fullName = response.body().getFirstName();
                    if (response.body().getLastName() != null && !response.body().getLastName().isEmpty()) {
                        fullName += " " + response.body().getLastName();
                    }
                    prefs.edit()
                        .putString("userName", fullName)
                        .putString("userEmail", response.body().getEmail())
                        .putString("userMobile", response.body().getMobile())
                        .apply();

                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Update failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Changes");
                Toast.makeText(EditProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
