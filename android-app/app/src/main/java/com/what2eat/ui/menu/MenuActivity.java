package com.what2eat.ui.menu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.Dish;
import com.what2eat.data.model.DishCategory;
import com.what2eat.data.model.Push;
import com.what2eat.ui.category.CategoryManageActivity;
import com.what2eat.ui.menu.adapter.CartAdapter;
import com.what2eat.ui.menu.adapter.CategoryAdapter;
import com.what2eat.ui.menu.adapter.DishAdapter;
import com.what2eat.ui.push.PushListActivity;
import com.what2eat.utils.RetrofitClient;
import com.what2eat.utils.ShoppingCartManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 点菜页面Activity
 */
public class MenuActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnCategoryManage;
    private RecyclerView rvCategories;
    private RecyclerView rvDishes;
    private TextView tvCartCount;
    private TextView tvCartTotal;
    private TextView tvCartBadge;
    private Button btnViewCart;

    private CategoryAdapter categoryAdapter;
    private DishAdapter dishAdapter;
    private CartAdapter cartAdapter;

    private List<String> categories;
    private List<Dish> dishes;
    private Map<String, Integer> categoryPositionMap; // 分类到位置的映射
    private ShoppingCartManager cartManager;

    private ProgressDialog progressDialog;
    private BottomSheetDialog cartBottomSheet;

    private int currentSelectedPosition = 0; // 当前选中的分类位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        initCategories();
        initAdapters();
        initCartBottomSheet();
        setListeners();
        loadAllDishes();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCategoryManage = findViewById(R.id.btnCategoryManage);
        rvCategories = findViewById(R.id.rvCategories);
        rvDishes = findViewById(R.id.rvDishes);
        tvCartCount = findViewById(R.id.tvCartCount);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnViewCart = findViewById(R.id.btnViewCart);

        cartManager = ShoppingCartManager.getInstance();
        dishes = new ArrayList<>();
        categoryPositionMap = new HashMap<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);

        // 设置RecyclerView布局
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvDishes.setLayoutManager(new LinearLayoutManager(this));

        // 添加滚动监听，实现左右联动
        rvDishes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 获取当前可见的第一个item位置
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvDishes.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                    // 根据位置找到对应的分类
                    String category = findCategoryByPosition(firstVisiblePosition);
                    if (category != null) {
                        int categoryIndex = categories.indexOf(category);
                        if (categoryIndex != -1 && categoryIndex != currentSelectedPosition) {
                            currentSelectedPosition = categoryIndex;
                            categoryAdapter.setSelectedPosition(categoryIndex);
                            rvCategories.smoothScrollToPosition(categoryIndex);
                        }
                    }
                }
            }
        });
    }

    /**
     * 初始化分类数据
     */
    private void initCategories() {
        categories = new ArrayList<>();
        loadCategoriesFromServer();
    }

    /**
     * 从服务器加载分类
     */
    private void loadCategoriesFromServer() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAllCategories().enqueue(new Callback<ApiResponse<List<DishCategory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DishCategory>>> call,
                                   Response<ApiResponse<List<DishCategory>>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    List<DishCategory> categoryList = response.body().getData();
                    if (categoryList != null && !categoryList.isEmpty()) {
                        categories.clear();
                        for (DishCategory category : categoryList) {
                            categories.add(category.getName());
                        }
                        // 通知分类适配器更新
                        if (categoryAdapter != null) {
                            categoryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // 服务器返回空列表
                        Toast.makeText(MenuActivity.this, "暂无分类数据", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载分类失败";
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DishCategory>>> call, Throwable t) {
                Toast.makeText(MenuActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化适配器
     */
    private void initAdapters() {
        // 分类适配器
        categoryAdapter = new CategoryAdapter(this, categories, (category, position) -> {
            // 分类点击事件 - 滚动到对应分类位置
            currentSelectedPosition = position;
            scrollToCategory(category);
        });
        rvCategories.setAdapter(categoryAdapter);

        // 菜品适配器
        dishAdapter = new DishAdapter(this, dish -> {
            // 添加到购物车
            cartManager.addItem(dish);
            updateCartDisplay();
            Toast.makeText(MenuActivity.this, "已添加: " + dish.getName(), Toast.LENGTH_SHORT).show();
        });
        rvDishes.setAdapter(dishAdapter);
    }

    /**
     * 初始化购物车底部面板
     */
    private void initCartBottomSheet() {
        cartBottomSheet = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_cart_bottom_sheet, null);
        cartBottomSheet.setContentView(sheetView);

        TextView tvClose = sheetView.findViewById(R.id.tvClose);
        RecyclerView rvCartItems = sheetView.findViewById(R.id.rvCartItems);
        TextView tvSheetTotal = sheetView.findViewById(R.id.tvSheetTotal);
        Button btnPushMenu = sheetView.findViewById(R.id.btnPushMenu);

        // 购物车列表
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onIncreaseClick(ShoppingCartManager.CartItem item) {
                cartManager.updateQuantity(item.getDishId(), item.getQuantity() + 1);
                refreshCart();
                updateCartDisplay();
            }

            @Override
            public void onDecreaseClick(ShoppingCartManager.CartItem item) {
                if (item.getQuantity() > 1) {
                    cartManager.updateQuantity(item.getDishId(), item.getQuantity() - 1);
                    refreshCart();
                    updateCartDisplay();
                } else {
                    // 数量为1时，询问是否删除
                    cartManager.removeItem(item.getDishId());
                    refreshCart();
                    updateCartDisplay();
                }
            }

            @Override
            public void onRemoveClick(ShoppingCartManager.CartItem item) {
                cartManager.removeItem(item.getDishId());
                refreshCart();
                updateCartDisplay();
            }
        });
        rvCartItems.setAdapter(cartAdapter);

        // 关闭按钮
        tvClose.setOnClickListener(v -> cartBottomSheet.dismiss());

        // 推送菜单按钮
        btnPushMenu.setOnClickListener(v -> {
            if (cartManager.isEmpty()) {
                Toast.makeText(MenuActivity.this, "购物车为空", Toast.LENGTH_SHORT).show();
                return;
            }
            cartBottomSheet.dismiss();
            pushMenu();
        });

        // 更新合计
        updateCartDisplay();
    }

    /**
     * 刷新购物车
     */
    private void refreshCart() {
        cartAdapter.setCartItems(cartManager.getItems());

        // 更新合计金额
        TextView tvSheetTotal = cartBottomSheet.findViewById(R.id.tvSheetTotal);
        if (tvSheetTotal != null) {
            tvSheetTotal.setText("¥" + cartManager.getTotalAmount());
        }

        // 如果购物车为空，关闭面板
        if (cartManager.isEmpty()) {
            cartBottomSheet.dismiss();
        }
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 分类管理按钮
        btnCategoryManage.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CategoryManageActivity.class);
            startActivity(intent);
        });

        // 查看购物车按钮
        btnViewCart.setOnClickListener(v -> {
            if (cartManager.isEmpty()) {
                Toast.makeText(MenuActivity.this, "购物车为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 显示购物车面板
            refreshCart();
            cartBottomSheet.show();
        });
    }

    /**
     * 加载所有菜品
     */
    private void loadAllDishes() {
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAllDishes().enqueue(new Callback<ApiResponse<List<Dish>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Dish>>> call,
                                   Response<ApiResponse<List<Dish>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    dishes = response.body().getData();
                    if (dishes == null) {
                        dishes = new ArrayList<>();
                    }
                    // 显示所有菜品，按分类显示
                    dishAdapter.setDishes(dishes);

                    // 默认选中第一个分类
                    if (!categories.isEmpty()) {
                        currentSelectedPosition = 0;
                        categoryAdapter.setSelectedPosition(0);
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Dish>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 根据分类加载菜品
     */
    private void loadDishesByCategory(String category) {
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getDishesByCategory(category).enqueue(new Callback<ApiResponse<List<Dish>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Dish>>> call,
                                   Response<ApiResponse<List<Dish>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    List<Dish> categoryDishes = response.body().getData();
                    if (categoryDishes == null) {
                        categoryDishes = new ArrayList<>();
                    }
                    dishAdapter.setDishes(categoryDishes);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Dish>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 更新购物车显示
     */
    private void updateCartDisplay() {
        int count = cartManager.getTotalCount();
        BigDecimal total = cartManager.getTotalAmount();

        tvCartCount.setText("共" + count + "件");
        tvCartTotal.setText("¥" + total);

        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    /**
     * 根据菜品位置找到对应的分类
     */
    private String findCategoryByPosition(int position) {
        if (dishes == null || dishes.isEmpty() || position < 0 || position >= dishes.size()) {
            return null;
        }

        // 直接获取该位置的菜品
        Dish dish = dishes.get(position);
        if (dish != null && dish.getCategory() != null) {
            return dish.getCategory();
        }

        // 如果当前位置的菜品没有分类，向前查找最近的一个有分类的菜品
        for (int i = position - 1; i >= 0; i--) {
            Dish d = dishes.get(i);
            if (d != null && d.getCategory() != null) {
                return d.getCategory();
            }
        }

        return null;
    }

    /**
     * 根据分类滚动到对应位置
     */
    private void scrollToCategory(String category) {
        if (dishes == null || dishes.isEmpty()) {
            return;
        }

        // 找到该分类第一个菜品的位置
        for (int i = 0; i < dishes.size(); i++) {
            Dish dish = dishes.get(i);
            if (dish != null && category.equals(dish.getCategory())) {
                // 滚动到该位置
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvDishes.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(i, 0);
                }
                break;
            }
        }
    }

    /**
     * 推送菜单
     */
    private void pushMenu() {
        progressDialog.setMessage("推送中...");
        progressDialog.show();

        // 构建推送数据
        String userId = RetrofitClient.getUserId(this);
        String nickname = getSharedPreferences("What2Eat", MODE_PRIVATE)
                .getString("nickname", "用户");

        // 构建菜品列表
        List<Push.DishItem> dishList = new ArrayList<>();
        for (ShoppingCartManager.CartItem item : cartManager.getItems()) {
            Push.DishItem dishItem = new Push.DishItem();
            dishItem.setDishId(item.getDishId());
            dishItem.setName(item.getName());
            dishItem.setPrice(item.getPrice());
            dishItem.setQuantity(item.getQuantity());
            dishItem.setImageUrl(item.getImageUrl());
            dishList.add(dishItem);
        }

        // 构建推送对象
        Push push = new Push();
        push.setDishes(dishList);
        push.setTotalAmount(cartManager.getTotalAmount());

        // 调用推送接口
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.createPush(RetrofitClient.getToken(this), push).enqueue(new Callback<ApiResponse<Push>>() {
            @Override
            public void onResponse(Call<ApiResponse<Push>> call,
                                   Response<ApiResponse<Push>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    // 清空购物车
                    cartManager.clear();
                    updateCartDisplay();

                    Toast.makeText(MenuActivity.this, "推送成功", Toast.LENGTH_SHORT).show();

                    // 跳转到推送列表
                    Intent intent = new Intent(MenuActivity.this, PushListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "推送失败";
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Push>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MenuActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
