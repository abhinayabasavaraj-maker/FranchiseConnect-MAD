package com.example.myapplication;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/brands")
    Call<List<Brand>> getBrands();

    @POST("api/auth/register")
    Call<Void> register(@Body User user);

    @POST("api/auth/login")
    Call<UserResponse> login(@Body User user);

    @GET("api/brands/count")
    Call<CountResponse> getBrandCount();

    @GET("api/users/count")
    Call<CountResponse> getUserCount();

    @POST("api/favorites")
    Call<Void> addFavorite(@Body FavoriteRequest favoriteRequest);

    @GET("api/favorites/{userId}")
    Call<List<Brand>> getFavorites(@Path("userId") String userId);

    @DELETE("api/favorites/{userId}/{brandId}")
    Call<Void> removeFavorite(@Path("userId") String userId, @Path("brandId") String brandId);

    @GET("api/users/{id}")
    Call<User> getProfile(@Header("Authorization") String token, @Path("id") String userId);

    @retrofit2.http.PUT("api/users/{id}")
    Call<User> updateProfile(@Header("Authorization") String token, @Path("id") String userId, @Body User user);

    @POST("api/brands")
    Call<Brand> createBrand(@Body Brand brand);

    @retrofit2.http.Multipart
    @POST("api/upload/image")
    Call<UploadResponse> uploadImage(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @POST("api/auth/verify-details")
    Call<UserResponse> verifyDetails(@Body ForgotPasswordRequest request);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body ForgotPasswordRequest request);
}
