// ===================================
// 全局状态管理
// ===================================
const appState = {
    currentUser: {
        id: 'user_123',
        name: '张三',
        avatar: '张'
    },
    cart: [], // 购物车数组
    pushes: [] // 推送记录
};

// ===================================
// 页面导航
// ===================================
function navigateTo(page) {
    document.body.style.opacity = '0';
    setTimeout(() => {
        window.location.href = page;
    }, 200);
}

// ===================================
// 微信登录
// ===================================
function wechatLogin() {
    showToast('正在跳转微信授权...');

    setTimeout(() => {
        showToast('登录成功！');
        navigateTo('index.html');
    }, 1500);
}

// ===================================
// 退出登录
// ===================================
function logout() {
    if (confirm('确定要退出登录吗？')) {
        showToast('已退出登录');
        setTimeout(() => {
            navigateTo('login.html');
        }, 1000);
    }
}

// ===================================
// 点菜页面 - 分类切换
// ===================================
function selectCategory(category) {
    // 移除所有active类
    document.querySelectorAll('.category-item').forEach(item => {
        item.classList.remove('active');
    });

    // 添加active类到当前分类
    document.querySelector(`[data-category="${category}"]`).classList.add('active');

    // 更新分类标题
    const categoryNames = {
        'hot': '热菜',
        'cold': '凉菜',
        'staple': '主食',
        'noodle': '面食',
        'soup': '汤品',
        'dessert': '甜点',
        'drink': '饮品'
    };

    document.querySelector('.category-title').textContent = categoryNames[category];

    // TODO: 根据分类加载不同的菜品
    // 这里暂时使用相同的菜品列表
}

// ===================================
// 点菜页面 - 添加到购物车
// ===================================
function addToCart(name, price, image) {
    // 检查是否已在购物车中
    const existingItem = appState.cart.find(item => item.name === name);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        appState.cart.push({
            name: name,
            price: price,
            image: image || 'https://via.placeholder.com/100x100/FF6600/FFFFFF?text=菜品',
            quantity: 1
        });
    }

    // 更新购物车显示
    updateCartDisplay();

    // 显示提示
    showToast(`已添加 ${name}`);
}

// ===================================
// 点菜页面 - 更新购物车显示
// ===================================
function updateCartDisplay() {
    const totalCount = appState.cart.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = appState.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    // 更新底部购物车栏
    const cartCountEl = document.getElementById('cartCount');
    const cartItemCountEl = document.getElementById('cartItemCount');
    const cartTotalEl = document.getElementById('cartTotal');

    if (cartCountEl) cartCountEl.textContent = totalCount;
    if (cartItemCountEl) cartItemCountEl.textContent = totalCount;
    if (cartTotalEl) cartTotalEl.textContent = totalPrice;

    // 更新购物车面板
    const panelItemCountEl = document.getElementById('panelItemCount');
    const panelTotalEl = document.getElementById('panelTotal');

    if (panelItemCountEl) panelItemCountEl.textContent = totalCount;
    if (panelTotalEl) panelTotalEl.textContent = totalPrice;

    // 更新购物车列表
    renderCartItems();
}

// ===================================
// 点菜页面 - 渲染购物车列表（带加减功能）
// ===================================
function renderCartItems() {
    const cartItemsContainer = document.getElementById('cartItems');
    const cartEmpty = document.getElementById('cartEmpty');

    if (!cartItemsContainer) return;

    if (appState.cart.length === 0) {
        cartEmpty.style.display = 'block';
        // 清空列表但保留empty元素
        const items = cartItemsContainer.querySelectorAll('.cart-item');
        items.forEach(item => item.remove());
        return;
    }

    cartEmpty.style.display = 'none';

    // 清空现有列表
    const items = cartItemsContainer.querySelectorAll('.cart-item');
    items.forEach(item => item.remove());

    // 渲染购物车项目
    appState.cart.forEach((item, index) => {
        const cartItem = document.createElement('div');
        cartItem.className = 'cart-item';
        cartItem.style.cssText = `
            display: flex;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid #E8E8E8;
        `;

        cartItem.innerHTML = `
            <img src="${item.image}" style="width: 60px; height: 60px; border-radius: 8px; object-fit: cover; margin-right: 12px;">
            <div style="flex: 1;">
                <div style="font-weight: bold; color: #333; margin-bottom: 4px;">${item.name}</div>
                <div style="color: #FF6600; font-weight: bold;">¥${item.price}</div>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
                <button onclick="decreaseQuantity(${index})" class="qty-btn" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                <span style="min-width: 24px; text-align: center; font-weight: bold;">${item.quantity}</span>
                <button onclick="increaseQuantity(${index})" class="qty-btn">+</button>
                <button onclick="removeFromCart(${index})" style="background: none; border: none; font-size: 18px; cursor: pointer; margin-left: 8px; color: #999;">🗑️</button>
            </div>
        `;

        cartItemsContainer.appendChild(cartItem);
    });
}

// ===================================
// 点菜页面 - 增加数量
// ===================================
function increaseQuantity(index) {
    appState.cart[index].quantity += 1;
    updateCartDisplay();
}

// ===================================
// 点菜页面 - 减少数量
// ===================================
function decreaseQuantity(index) {
    if (appState.cart[index].quantity > 1) {
        appState.cart[index].quantity -= 1;
        updateCartDisplay();
    } else {
        // 数量为1时，询问是否删除
        if (confirm(`确定要移除 ${appState.cart[index].name} 吗？`)) {
            removeFromCart(index);
        }
    }
}

// ===================================
// 点菜页面 - 从购物车移除
// ===================================
function removeFromCart(index) {
    const itemName = appState.cart[index].name;
    appState.cart.splice(index, 1);
    updateCartDisplay();
    showToast(`已移除 ${itemName}`);
}

// ===================================
// 点菜页面 - 切换购物车面板
// ===================================
function toggleCart() {
    const cartPanel = document.getElementById('cartPanel');
    if (cartPanel) {
        cartPanel.classList.toggle('active');
    }
}

// ===================================
// 点菜页面 - 推送菜单
// ===================================
function pushMenu() {
    if (appState.cart.length === 0) {
        showToast('购物车是空的，先添加菜品吧～');
        return;
    }

    const totalPrice = appState.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    // 创建推送记录
    const push = {
        id: Date.now(),
        pusherId: appState.currentUser.id,
        pusherName: appState.currentUser.name,
        pusherAvatar: appState.currentUser.avatar,
        dishes: [...appState.cart],
        totalAmount: totalPrice,
        time: new Date()
    };

    // 保存推送记录
    appState.pushes.push(push);

    // 清空购物车
    appState.cart = [];
    updateCartDisplay();

    // 关闭购物车面板
    const cartPanel = document.getElementById('cartPanel');
    if (cartPanel) {
        cartPanel.classList.remove('active');
    }

    // 显示成功提示
    showToast('推送菜单成功！');

    // 返回首页
    setTimeout(() => {
        navigateTo('index.html');
    }, 1500);
}

// ===================================
// 已推送菜单页面 - 删除推送
// ===================================
function deletePush(button) {
    const card = button.closest('.pushed-card');
    const owner = card.getAttribute('data-owner');

    // 检查权限
    if (owner !== 'self') {
        showToast('只能删除自己的推送');
        return;
    }

    if (confirm('确定要删除这条推送记录吗？')) {
        card.style.transition = 'all 0.3s';
        card.style.transform = 'scale(0.9)';
        card.style.opacity = '0';

        setTimeout(() => {
            card.remove();
            showToast('删除成功');
        }, 300);
    }
}

// ===================================
// 上传菜单页面 - 图片上传
// ===================================
let uploadedImage = null;

function handleImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showToast('请选择图片文件');
        return;
    }

    if (file.size > 2 * 1024 * 1024) {
        showToast('图片大小不能超过2MB');
        return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
        uploadedImage = e.target.result;

        const preview = document.getElementById('imagePreview');
        const placeholder = document.querySelector('.upload-placeholder');

        if (preview && placeholder) {
            preview.src = uploadedImage;
            preview.style.display = 'block';
            placeholder.style.display = 'none';
        }
    };
    reader.readAsDataURL(file);
}

// ===================================
// 上传菜单页面 - 提交菜品
// ===================================
function submitDish() {
    const dishName = document.getElementById('dishName').value.trim();
    const dishDesc = document.getElementById('dishDesc').value.trim();
    const dishPrice = document.getElementById('dishPrice').value.trim();
    const dishCategory = document.getElementById('dishCategory').value;

    // 验证
    if (!uploadedImage) {
        showToast('请上传菜品图片');
        return;
    }

    if (!dishName) {
        showToast('请输入菜品名称');
        return;
    }

    if (!dishPrice) {
        showToast('请输入价格');
        return;
    }

    if (!dishCategory) {
        showToast('请选择分类');
        return;
    }

    // 模拟提交
    setTimeout(() => {
        const modal = document.getElementById('successModal');
        if (modal) {
            modal.classList.add('active');
        }
    }, 500);
}

// ===================================
// Toast 提示
// ===================================
function showToast(message, duration = 2000) {
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(0, 0, 0, 0.8);
        color: #FFFFFF;
        padding: 16px 24px;
        border-radius: 8px;
        font-size: 14px;
        z-index: 10000;
        animation: fadeIn 0.3s ease-out;
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// ===================================
// 初始化
// ===================================
document.addEventListener('DOMContentLoaded', function() {
    document.body.style.transition = 'opacity 0.2s';
    document.body.style.opacity = '1';

    // 点击弹窗外部关闭
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('active');
            }
        });
    });

    console.log('App initialized successfully!');
});

// ===================================
// 我的二维码页面 - 分享二维码
// ===================================
function shareQRCode() {
    if (navigator.share) {
        navigator.share({
            title: '添加我为好友',
            text: '扫描二维码添加我为好友，一起讨论今天吃什么！',
            url: window.location.href
        }).then(() => {
            showToast('分享成功');
        }).catch(() => {
            showToast('分享取消');
        });
    } else {
        // 降级方案：复制链接
        const tempInput = document.createElement('input');
        tempInput.value = window.location.href;
        document.body.appendChild(tempInput);
        tempInput.select();
        document.execCommand('copy');
        document.body.removeChild(tempInput);
        showToast('链接已复制到剪贴板');
    }
}

// ===================================
// 扫一扫页面 - 模拟扫描（演示用）
// ===================================
function simulateScan() {
    showToast('正在扫描...');

    // 模拟扫描延迟
    setTimeout(() => {
        // 显示扫码结果弹窗
        const modal = document.getElementById('scanResultModal');
        if (modal) {
            modal.classList.add('active');
        }
    }, 1500);
}

// ===================================
// 扫一扫页面 - 关闭扫码结果
// ===================================
function closeScanResult() {
    const modal = document.getElementById('scanResultModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

// ===================================
// 扫一扫页面 - 添加好友
// ===================================
function addFriend() {
    showToast('添加好友成功！');

    // 关闭弹窗
    closeScanResult();

    // 返回首页
    setTimeout(() => {
        navigateTo('index.html');
    }, 1000);
}
