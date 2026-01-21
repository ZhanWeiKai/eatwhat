package com.what2eat.data.api;

import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.Dish;
import com.what2eat.data.model.DishCategory;
import com.what2eat.data.model.FriendDTO;
import com.what2eat.data.model.Push;
import com.what2eat.data.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * API接口
 */
public interface ApiService {

    // ========== 认证接口 ==========

    @POST("auth/register")
    Call<ApiResponse<Map<String, String>>> register(@Body Map<String, String> request);

    @POST("auth/login")
    Call<ApiResponse<Map<String, String>>> login(@Body Map<String, String> request);

    @GET("auth/me")
    Call<ApiResponse<User>> getCurrentUser(@Header("Authorization") String token);

    // ========== 菜品接口 ==========

    @GET("dishes")
    Call<ApiResponse<List<Dish>>> getAllDishes();

    @GET("dishes/category/{category}")
    Call<ApiResponse<List<Dish>>> getDishesByCategory(@Path("category") String category);

    @POST("dishes")
    Call<ApiResponse<Dish>> createDish(@Header("Authorization") String token, @Body Dish dish);

    // ========== 分类接口 ==========

    @GET("categories")
    Call<ApiResponse<List<DishCategory>>> getAllCategories();

    @POST("categories")
    Call<ApiResponse<DishCategory>> createCategory(@Header("Authorization") String token, @Body DishCategory category);

    @PUT("categories/{id}")
    Call<ApiResponse<DishCategory>> updateCategory(@Header("Authorization") String token, @Path("id") String id, @Body DishCategory category);

    @DELETE("categories/{id}")
    Call<ApiResponse<Void>> deleteCategory(@Header("Authorization") String token, @Path("id") String id);

    // ========== 推送接口 ==========

    @GET("push/list")
    Call<ApiResponse<List<Push>>> getAllPushes();

    @POST("push")
    Call<ApiResponse<Push>> createPush(@Header("Authorization") String token, @Body Push push);

    @DELETE("push/{pushId}")
    Call<ApiResponse<Void>> deletePush(@Header("Authorization") String token, @Path("pushId") String pushId);

    // ========== 好友接口 ==========

    @GET("friends")
    Call<ApiResponse<List<Map<String, String>>>> getFriends(@Header("Authorization") String token);

    @POST("friends/add")
    Call<ApiResponse<Map<String, String>>> addFriend(@Header("Authorization") String token, @Query("friendId") String friendId);

    @GET("friendships/list/{userId}")
    Call<ApiResponse<List<FriendDTO>>> getFriendList(@Path("userId") String userId);

    // ========== 文件上传接口 ==========

    @Multipart
    @POST("upload/image")
    Call<ApiResponse<Map<String, String>>> uploadImage(@Part MultipartBody.Part file);
}
