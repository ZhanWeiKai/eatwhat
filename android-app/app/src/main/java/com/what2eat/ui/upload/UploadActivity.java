package com.what2eat.ui.upload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.Dish;
import com.what2eat.data.model.DishCategory;
import com.what2eat.utils.RetrofitClient;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 上传菜品Activity
 */
public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 1002;

    private FrameLayout imageContainer;
    private ImageView imagePreview;
    private LinearLayout uploadPlaceholder;
    private EditText etDishName;
    private EditText etDishDescription;
    private EditText etDishPrice;
    private Spinner spinnerCategory;
    private Button btnSubmit;

    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private ProgressDialog progressDialog;

    private ApiService apiService;

    // 菜品分类列表
    private List<String> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        initViews();
        setupListeners();

        apiService = RetrofitClient.getApiService(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("上传中...");
        progressDialog.setCancelable(false);

        // 从服务器加载分类
        loadCategoriesFromServer();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        imageContainer = findViewById(R.id.imageContainer);
        imagePreview = findViewById(R.id.imagePreview);
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder);
        etDishName = findViewById(R.id.etDishName);
        etDishDescription = findViewById(R.id.etDishDescription);
        etDishPrice = findViewById(R.id.etDishPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    /**
     * 初始化分类下拉列表
     */
    private void initCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        if (!categories.isEmpty()) {
            spinnerCategory.setSelection(0);
        }
    }

    /**
     * 从服务器加载分类
     */
    private void loadCategoriesFromServer() {
        progressDialog.setMessage("加载分类中...");
        progressDialog.show();

        apiService.getAllCategories().enqueue(new Callback<ApiResponse<List<DishCategory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DishCategory>>> call,
                                   Response<ApiResponse<List<DishCategory>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    List<DishCategory> categoryList = response.body().getData();
                    if (categoryList != null && !categoryList.isEmpty()) {
                        categories.clear();
                        for (DishCategory category : categoryList) {
                            categories.add(category.getName());
                        }
                        initCategorySpinner();
                    } else {
                        Toast.makeText(UploadActivity.this, "暂无分类数据，请先创建分类", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载分类失败";
                    Toast.makeText(UploadActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DishCategory>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(UploadActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 点击选择图片
        imageContainer.setOnClickListener(v -> {
            checkPermissionAndPickImage();
        });

        // 提交按钮
        btnSubmit.setOnClickListener(v -> {
            submitDish();
        });
    }

    /**
     * 检查权限并选择图片
     */
    private void checkPermissionAndPickImage() {
        // Android 13 (API 33)及以上使用 READ_MEDIA_IMAGES
        // Android 12 (API 32)及以下使用 READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_PERMISSION_STORAGE);
            } else {
                pickImage();
            }
        } else {
            // Android 12及以下
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION_STORAGE);
            } else {
                pickImage();
            }
        }
    }

    /**
     * 打开图片选择器
     */
    private void pickImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(this, "打开图库失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imagePreview.setImageURI(selectedImageUri);
                    imagePreview.setVisibility(View.VISIBLE);
                    uploadPlaceholder.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 提交菜品
     */
    private void submitDish() {
        // 验证分类是否已加载
        if (categories.isEmpty()) {
            Toast.makeText(this, "分类数据未加载，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证输入
        String name = etDishName.getText().toString().trim();
        String description = etDishDescription.getText().toString().trim();
        String priceStr = etDishPrice.getText().toString().trim();
        String category = (String) spinnerCategory.getSelectedItem();

        if (category == null) {
            Toast.makeText(this, "请选择菜品分类", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证必填项
        if (selectedImageUri == null && TextUtils.isEmpty(uploadedImageUrl)) {
            Toast.makeText(this, "请选择菜品图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入菜品名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "请输入菜品价格", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证价格格式
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(this, "价格必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "价格格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        // 先上传图片，再提交菜品
        if (selectedImageUri != null && TextUtils.isEmpty(uploadedImageUrl)) {
            uploadImageAndSubmit(name, description, price, category);
        } else {
            submitDishToServer(name, description, uploadedImageUrl, price, category);
        }
    }

    /**
     * 上传图片并提交菜品
     */
    private void uploadImageAndSubmit(String name, String description, BigDecimal price, String category) {
        progressDialog.setMessage("上传图片中...");
        progressDialog.show();

        try {
            // 获取图片文件路径
            String filePath = getFilePathFromUri(selectedImageUri);
            if (filePath == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                progressDialog.dismiss();
                Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建请求体
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    file
            );
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // 上传图片
            apiService.uploadImage(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Map<String, String>> apiResponse = response.body();
                        if (apiResponse.getCode() == 200) {
                            Map<String, String> data = apiResponse.getData();
                            if (data != null && data.containsKey("url")) {
                                uploadedImageUrl = data.get("url");
                                // 图片上传成功，提交菜品
                                submitDishToServer(name, description, uploadedImageUrl, price, category);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(UploadActivity.this, "上传图片失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(UploadActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(UploadActivity.this, "上传图片失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "上传图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 提交菜品到服务器
     */
    private void submitDishToServer(String name, String description, String imageUrl, BigDecimal price, String category) {
        progressDialog.setMessage("提交菜品中...");

        try {
            // 创建菜品对象
            Dish dish = new Dish();
            dish.setName(name);
            dish.setDescription(description);
            dish.setImageUrl(imageUrl);
            dish.setPrice(price);
            dish.setCategory(category);

            // 获取token并提交
            String token = RetrofitClient.getToken(this);
            if (token == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            apiService.createDish("Bearer " + token, dish).enqueue(new Callback<ApiResponse<Dish>>() {
                @Override
                public void onResponse(Call<ApiResponse<Dish>> call, Response<ApiResponse<Dish>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Dish> apiResponse = response.body();
                        if (apiResponse.getCode() == 200) {
                            Toast.makeText(UploadActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UploadActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UploadActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Dish>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从Uri获取文件路径
     */
    private String getFilePathFromUri(Uri uri) {
        try {
            if (uri == null) return null;

            if ("file".equals(uri.getScheme())) {
                return uri.getPath();
            }

            String[] projection = {MediaStore.Images.Media.DATA};
            android.database.Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 防止内存泄漏
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
