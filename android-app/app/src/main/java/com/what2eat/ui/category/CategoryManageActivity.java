package com.what2eat.ui.category;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.DishCategory;
import com.what2eat.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 分类管理Activity
 */
public class CategoryManageActivity extends AppCompatActivity {

    private Button btnBack;
    private Button btnAdd;
    private RecyclerView rvCategories;

    private CategoryManageAdapter adapter;
    private List<DishCategory> categories;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manage);

        initViews();
        initAdapter();
        setListeners();
        loadCategories();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);
        rvCategories = findViewById(R.id.rvCategories);

        categories = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        adapter = new CategoryManageAdapter(categories, new CategoryManageAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(DishCategory category) {
                showEditDialog(category);
            }

            @Override
            public void onDeleteClick(DishCategory category) {
                showDeleteConfirmDialog(category);
            }
        });
        rvCategories.setAdapter(adapter);
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    /**
     * 加载分类列表
     */
    private void loadCategories() {
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAllCategories().enqueue(new Callback<ApiResponse<List<DishCategory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DishCategory>>> call,
                                   Response<ApiResponse<List<DishCategory>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    List<DishCategory> data = response.body().getData();
                    if (data != null) {
                        categories.clear();
                        categories.addAll(data);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    Toast.makeText(CategoryManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DishCategory>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CategoryManageActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示添加对话框
     */
    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_input, null);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);

        new MaterialAlertDialogBuilder(this)
                .setTitle("添加分类")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入分类名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createCategory(name);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示编辑对话框
     */
    private void showEditDialog(DishCategory category) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_input, null);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        etName.setText(category.getName());

        new MaterialAlertDialogBuilder(this)
                .setTitle("编辑分类")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入分类名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateCategory(category, name);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(DishCategory category) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除分类")
                .setMessage("确定要删除分类 \"" + category.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteCategory(category);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 创建分类
     */
    private void createCategory(String name) {
        progressDialog.setMessage("创建中...");
        progressDialog.show();

        DishCategory category = new DishCategory();
        category.setName(name);

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.createCategory(RetrofitClient.getToken(this), category).enqueue(new Callback<ApiResponse<DishCategory>>() {
            @Override
            public void onResponse(Call<ApiResponse<DishCategory>> call,
                                   Response<ApiResponse<DishCategory>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CategoryManageActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "创建失败";
                    Toast.makeText(CategoryManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DishCategory>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CategoryManageActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 更新分类
     */
    private void updateCategory(DishCategory oldCategory, String newName) {
        progressDialog.setMessage("更新中...");
        progressDialog.show();

        DishCategory category = new DishCategory();
        category.setName(newName);

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateCategory(RetrofitClient.getToken(this), oldCategory.getCategoryId(), category).enqueue(new Callback<ApiResponse<DishCategory>>() {
            @Override
            public void onResponse(Call<ApiResponse<DishCategory>> call,
                                   Response<ApiResponse<DishCategory>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CategoryManageActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "更新失败";
                    Toast.makeText(CategoryManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DishCategory>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CategoryManageActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 删除分类
     */
    private void deleteCategory(DishCategory category) {
        progressDialog.setMessage("删除中...");
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.deleteCategory(RetrofitClient.getToken(this), category.getCategoryId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CategoryManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "删除失败";
                    Toast.makeText(CategoryManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CategoryManageActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
