package com.what2eat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.what2eat.BuildConfig;
import com.what2eat.data.api.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit客户端工具类
 */
public class RetrofitClient {

    private static ApiService apiService;
    private static SharedPreferences preferences;

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // 添加日志拦截器（Debug模式）
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logging);
            }

            // 添加Token拦截器
            httpClient.addInterceptor(chain -> {
                String token = getToken(context);
                if (token != null) {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                }
                return chain.proceed(chain.request());
            });

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    /**
     * 保存Token
     */
    public static void saveToken(Context context, String token) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        preferences.edit().putString("token", token).apply();
    }

    /**
     * 获取Token
     */
    public static String getToken(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        return preferences.getString("token", null);
    }

    /**
     * 清除Token
     */
    public static void clearToken(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        preferences.edit().remove("token").apply();
    }

    /**
     * 保存用户信息
     */
    public static void saveUserInfo(Context context, String userId, String username, String nickname) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        preferences.edit()
                .putString("userId", userId)
                .putString("username", username)
                .putString("nickname", nickname)
                .apply();
    }

    /**
     * 保存用户头像
     */
    public static void saveUserAvatar(Context context, String avatar) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        preferences.edit().putString("avatar", avatar).apply();
    }

    /**
     * 获取用户ID
     */
    public static String getUserId(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        return preferences.getString("userId", null);
    }

    /**
     * 获取用户昵称
     */
    public static String getNickname(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        return preferences.getString("nickname", null);
    }

    /**
     * 获取用户头像
     */
    public static String getAvatar(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        return preferences.getString("avatar", null);
    }

    /**
     * 获取用户名
     */
    public static String getUsername(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        }
        return preferences.getString("username", null);
    }

    /**
     * 检查是否已登录
     */
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}
