package com.what2eat.ui.photo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.Photo;
import com.what2eat.ui.photo.PhotoAdapter;
import com.what2eat.utils.ImageUtils;
import com.what2eat.utils.RetrofitClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 相册Activity
 */
public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "PhotoActivity";

    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // 请求码
    private static final int REQUEST_CAMERA = 2001;
    private static final int REQUEST_GALLERY = 2002;

    // URI相关
    private Uri cameraImageUri;
    private String currentImagePath;

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList;
    private Button btnAddPhoto;
    private Button btnAuthBaidu;
    private ApiService apiService;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        initViews();
        initData();
        setListeners();
        loadPhotos();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPhotos);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAuthBaidu = findViewById(R.id.btnAuthBaidu);

        // 设置RecyclerView（3列网格布局）
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(photoAdapter);

        // 初始化进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
    }

    private void initData() {
        apiService = RetrofitClient.getApiService(this);
    }

    private void setListeners() {
        // 授权百度网盘按钮
        btnAuthBaidu.setOnClickListener(v -> {
            Intent intent = new Intent(this, BaiduOAuthActivity.class);
            startActivity(intent);
        });

        // 添加照片按钮
        btnAddPhoto.setOnClickListener(v -> {
            checkPermissionsAndShowDialog();
        });
    }

    /**
     * 检查权限并显示选择对话框
     */
    private void checkPermissionsAndShowDialog() {
        List<String> permissionsNeeded = new ArrayList<>();

        // 检查相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // 检查存储权限（Android 12及以下）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Android 13及以上需要READ_MEDIA_IMAGES权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            // 请求权限
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            // 权限已授予，显示选择对话框
            showSelectionDialog();
        }
    }

    /**
     * 显示选择对话框
     */
    private void showSelectionDialog() {
        String[] items = {"拍照", "从相册选择"};

        new AlertDialog.Builder(this)
                .setTitle("选择图片来源")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                showSelectionDialog();
            } else {
                Toast.makeText(this, "需要相机和存储权限才能添加照片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 创建临时文件保存照片
        File photoFile = createImageFile();
        if (photoFile != null) {
            cameraImageUri = Uri.fromFile(photoFile);
            currentImagePath = photoFile.getAbsolutePath();

            // Android 7.0及以上需要使用FileProvider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".fileprovider",
                                photoFile));
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            }

            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "创建照片文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建图片文件
     */
    private File createImageFile() {
        try {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = "JPEG_" + timeStamp + "_";

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 打开相册
     */
    private void openGallery() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上使用新的媒体API
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
        }

        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                // 相册选择
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    processAndUploadImage(selectedImage);
                }
            } else if (requestCode == REQUEST_CAMERA) {
                // 拍照
                if (cameraImageUri != null || currentImagePath != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        processAndUploadImage(cameraImageUri);
                    } else {
                        Uri uri = Uri.fromFile(new File(currentImagePath));
                        processAndUploadImage(uri);
                    }
                }
            }
        }
    }

    /**
     * 处理并上传图片
     */
    private void processAndUploadImage(Uri imageUri) {
        progressDialog.setMessage("处理图片...");
        progressDialog.show();

        // 在后台线程处理图片
        new Thread(() -> {
            try {
                // 压缩图片
                String compressedPath = ImageUtils.compressImage(this, imageUri);

                if (compressedPath == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "图片处理失败", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 上传到服务器
                uploadImageToServer(compressedPath);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "处理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 上传图片到服务器
     */
    private void uploadImageToServer(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // 创建MultipartBody.Part
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                imageFile
        );
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        // 调用上传接口
        apiService.uploadImage(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, String>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        String imageUrl = apiResponse.getData().get("url");
                        long fileSize = ImageUtils.getFileSize(imagePath);

                        // 保存照片记录
                        savePhotoRecord(imageUrl, fileSize);

                        // 删除临时文件
                        ImageUtils.deleteTempFile(imagePath);
                    } else {
                        progressDialog.dismiss();
                        String message = apiResponse.getMessage();
                        Toast.makeText(PhotoActivity.this,
                                message != null ? message : "上传失败",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(PhotoActivity.this, "上传失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                progressDialog.dismiss();
                String errorMsg = t.getMessage();
                Toast.makeText(PhotoActivity.this,
                        "上传失败: " + (errorMsg != null ? errorMsg : "网络错误"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 调用API保存照片记录
     */
    private void savePhotoRecord(String imageUrl, long fileSize) {
        String token = RetrofitClient.getToken(this);
        String userId = RetrofitClient.getUserId(this);

        // 验证必要参数
        if (token == null || userId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "登录信息已过期，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "图片URL不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("imageUrl", imageUrl);
        request.put("description", "");
        request.put("fileSize", fileSize);

        apiService.uploadPhoto("Bearer " + token, request).enqueue(new Callback<ApiResponse<Photo>>() {
            @Override
            public void onResponse(Call<ApiResponse<Photo>> call, Response<ApiResponse<Photo>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Photo> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(PhotoActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        loadPhotos(); // 重新加载照片列表
                    } else {
                        String message = apiResponse.getMessage();
                        Toast.makeText(PhotoActivity.this,
                                message != null ? message : "保存失败",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PhotoActivity.this, "保存失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Photo>> call, Throwable t) {
                progressDialog.dismiss();
                String errorMsg = t.getMessage();
                Toast.makeText(PhotoActivity.this,
                        "保存失败: " + (errorMsg != null ? errorMsg : "未知错误"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加载照片列表
     */
    private void loadPhotos() {
        // 检查登录状态
        if (!RetrofitClient.isLoggedIn(this)) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressDialog.setMessage("加载照片...");
        progressDialog.show();

        String token = RetrofitClient.getToken(this);
        if (token == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "登录信息已过期，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getCurrentUserPhotos("Bearer " + token).enqueue(new Callback<ApiResponse<List<Photo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Photo>>> call, Response<ApiResponse<List<Photo>>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Photo>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<Photo> data = apiResponse.getData();
                        if (data != null) {
                            photoList.clear();
                            photoList.addAll(data);
                            photoAdapter.notifyDataSetChanged();
                        }

                        if (photoList.isEmpty()) {
                            Toast.makeText(PhotoActivity.this, "暂无照片，点击下方按钮添加", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = apiResponse.getMessage();
                        Toast.makeText(PhotoActivity.this, message != null ? message : "加载失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PhotoActivity.this, "加载失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Photo>>> call, Throwable t) {
                progressDialog.dismiss();
                String errorMsg = t.getMessage();
                Toast.makeText(PhotoActivity.this,
                        "网络错误: " + (errorMsg != null ? errorMsg : "未知错误"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
