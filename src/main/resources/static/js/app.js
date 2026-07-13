const API_BASE = '/api';
let currentUser = null;
let ws = null;

// === API Helpers (axios) ===

async function apiPost(path, body) {
    const res = await axios.post(API_BASE + path, body);
    return res.data;
}

async function apiGet(path) {
    const res = await axios.get(API_BASE + path);
    return res.data;
}

// === User State — 仅 token 存 sessionStorage，用户数据全从 Redis 获取 ===

function saveUser(user) {
    // 只保存 token，不存用户数据
    currentUser = user;
    if (user.token) {
        sessionStorage.setItem('lottery_token', user.token);
    }
}

/** 简版：仅检查是否存在 token，用于快速判断是否已登录 */
function hasToken() {
    return !!sessionStorage.getItem('lottery_token');
}

function clearUser() {
    var token = sessionStorage.getItem('lottery_token');
    if (token) {
        apiPost('/logout', { token: token }).catch(function () {});
    }
    currentUser = null;
    sessionStorage.removeItem('lottery_token');
}

/** 从 Redis 加载当前用户的完整信息 */
function loadSessionUser() {
    var token = sessionStorage.getItem('lottery_token');
    if (!token) return Promise.reject('no token');

    return apiGet('/session/info?token=' + encodeURIComponent(token)).then(function (res) {
        if (res.code === 200) {
            res.data.token = token;
            currentUser = res.data;
            return res.data;
        } else {
            // 会话过期
            clearUser();
            window.location.href = 'login.html';
            throw new Error('session expired');
        }
    }).catch(function (err) {
        if (err && err.message === 'session expired') throw err;
        // 网络错误或 Redis 不可用 → 跳登录
        clearUser();
        window.location.href = 'login.html';
        throw err;
    });
}

// === WebSocket ===

function connectWebSocket(userId) {
    if (ws && ws.readyState === WebSocket.OPEN) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = protocol + '//' + window.location.host + '/ws/notification';

    ws = new WebSocket(wsUrl);

    ws.onopen = function () {
        ws.send(JSON.stringify({ type: 'auth', userId: userId }));
    };

    ws.onmessage = function (event) {
        try {
            const msg = JSON.parse(event.data);
            if (msg.type === 'auth_ok') {
                console.log('WebSocket authenticated');
            } else if (msg.type === 'winning') {
                showWinningNotification(msg.notifications);
            } else if (msg.type === 'draw_result') {
                showDrawNotification(msg.data);
            }
        } catch (e) {
            console.error('WebSocket message error', e);
        }
    };

    ws.onclose = function () {
        // 3秒后重连
        setTimeout(function () {
            if (currentUser) connectWebSocket(currentUser.userId);
        }, 3000);
    };

    ws.onerror = function (e) {
        console.error('WebSocket error', e);
    };
}

function disconnectWebSocket() {
    if (ws) {
        ws.close();
        ws = null;
    }
}

// === Notifications ===

function showWinningNotification(notifications) {
    const panel = document.getElementById('notificationPanel');
    const list = document.getElementById('notificationList');
    if (!panel || !list) return;

    list.innerHTML = '';
    notifications.forEach(function (n) {
        var div = document.createElement('div');
        div.className = 'notification-item';
        div.innerHTML = '<strong>' + n.prizeLevel + '</strong> | 期号: ' + n.drawNo +
            ' | 奖金: ¥' + (n.prizeAmount || 0).toLocaleString();
        list.appendChild(div);
    });
    panel.classList.add('show');
}

function showDrawNotification(data) {
    var existing = document.getElementById('drawToast');
    if (existing) existing.remove();

    var toast = document.createElement('div');
    toast.id = 'drawToast';
    toast.style.cssText = 'position:fixed;top:16px;right:16px;background:#fff;border:1px solid #e0e0e0;' +
        'border-radius:6px;padding:16px;z-index:9999;box-shadow:0 2px 12px rgba(0,0,0,0.12);max-width:320px;';
    toast.innerHTML = '<div style="font-weight:600;margin-bottom:8px;">开奖通知</div>' +
        '<div>期号: ' + data.drawNo + '</div>' +
        '<div>号码: ' + (data.numbers || []).join(', ') + '</div>' +
        '<button onclick="this.parentElement.remove()" style="margin-top:8px;padding:4px 12px;border:1px solid #ccc;border-radius:4px;background:#fff;cursor:pointer;">关闭</button>';
    document.body.appendChild(toast);

    setTimeout(function () { if (toast.parentElement) toast.remove(); }, 15000);
}

// === DOM helpers ===

function showError(msg) {
    var el = document.getElementById('errorMsg');
    if (el) {
        el.textContent = msg;
        el.classList.remove('hidden');
    }
}

function showSuccess(msg) {
    var el = document.getElementById('successMsg');
    if (el) {
        el.textContent = msg;
        el.classList.remove('hidden');
        setTimeout(function () { el.classList.add('hidden'); }, 3000);
    }
}

function hideMessages() {
    var err = document.getElementById('errorMsg');
    var suc = document.getElementById('successMsg');
    if (err) err.classList.add('hidden');
    if (suc) suc.classList.add('hidden');
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    return timeStr.replace('T', ' ').substring(0, 19);
}

function getStatusText(status) {
    switch (status) {
        case 0: return '未开奖';
        case 1: return '未中奖';
        case 2: return '特等奖';
        case 3: return '一等奖';
        default: return '未知';
    }
}

function getStatusTag(status) {
    var text = getStatusText(status);
    if (status === 2 || status === 3) {
        return '<span class="tag tag-winner">' + text + '</span>';
    }
    return '<span class="tag">' + text + '</span>';
}

// === Page initialization — 所有页面统一入口 ===
// 每个页面在 body onload 中调用 initPage()
// 页面初始化完成后会调用 onPageReady()（由各页面自行定义）

function initPage() {
    // 1. 检查 sessionStorage 中是否有 token
    if (!hasToken()) {
        window.location.href = 'login.html';
        return;
    }

    // 2. 从 Redis 获取用户数据
    loadSessionUser().then(function (user) {
        // 渲染用户信息
        renderUserInfo(user);

        // WebSocket 连接
        connectWebSocket(user.userId);

        // 高亮当前导航
        var currentPage = window.location.pathname.split('/').pop();
        var navLinks = document.querySelectorAll('.nav a');
        navLinks.forEach(function (a) {
            if (a.getAttribute('href') === currentPage) {
                a.classList.add('active');
            }
        });

        // 未读通知
        if (user.unreadNotifications && user.unreadNotifications.length > 0) {
            showWinningNotification(user.unreadNotifications);
        }

        // 调用页面自定义初始化（由各页面定义，无需则可省略）
        if (typeof onPageReady === 'function') {
            onPageReady();
        }
    }).catch(function () {
        // loadSessionUser 内部已处理跳转
    });
}

function renderUserInfo(user) {
    var userInfo = document.getElementById('userInfo');
    if (userInfo) {
        userInfo.innerHTML = '用户: ' + user.username +
            ' | 余额: ¥<span id="balanceDisplay">' + (user.balance || 0).toFixed(2) + '</span>' +
            ' | <a href="login.html" onclick="clearUser();disconnectWebSocket();return true;" style="color:#555;">退出</a>';
    }
}

// 刷新余额
function refreshBalance() {
    if (!currentUser) return;
    apiGet('/user/info?userId=' + currentUser.userId).then(function (res) {
        if (res.code === 200) {
            // 同时更新 Redis 中的 balance
            currentUser.balance = res.data.balance;
            var el = document.getElementById('balanceDisplay');
            if (el) el.textContent = (res.data.balance || 0).toFixed(2);
        }
    });
}

// === Draw helpers ===

function getCurrentDrawNo() {
    return apiGet('/current-draw');
}