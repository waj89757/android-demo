// ============================================================
// router_bundle.js
// 模拟 RN 里的 JS Router：根据 URI 路由渲染不同页面
//
// 支持的路由：
//   myapp://product/detail?id=xxx&name=xxx   → 商品详情页
//   myapp://user/profile?userId=xxx          → 用户主页
//   myapp://order/list                        → 订单列表
// ============================================================

var currentRoute = null;    // 当前路由（从 Native 传入）
var currentParams = {};     // 路由参数

// ─── Bridge 工具 ─────────────────────────────────────────
function callBridge(method) {
  var args = Array.prototype.slice.call(arguments, 1);
  if (window.NativeBridge && typeof window.NativeBridge[method] === 'function') {
    return window.NativeBridge[method].apply(null, args);
  }
}

// ─── 页面渲染函数 ─────────────────────────────────────────

// 页面1：商品详情
function renderProductDetail(params) {
  return '<div style="padding:20px;font-family:sans-serif">' +
    '<div style="background:#FF6F00;color:white;padding:14px;border-radius:10px;margin-bottom:14px">' +
    '  <div style="font-size:11px;opacity:0.8">RN 页面 · 商品详情</div>' +
    '  <div style="font-size:20px;font-weight:bold;margin-top:4px">🛍️ 商品页</div>' +
    '</div>' +
    '<div style="background:#FFF8E1;border-radius:8px;padding:12px;margin-bottom:12px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:6px">从 URI 解析的参数：</div>' +
    '  <div style="font-size:15px"><b>商品 ID：</b>' + (params.id || '无') + '</div>' +
    '  <div style="font-size:15px"><b>商品名：</b>' + decodeURIComponent(params.name || '未命名') + '</div>' +
    '</div>' +
    '<button onclick="onAddToCart()" style="width:100%;padding:12px;background:#FF6F00;color:white;border:none;border-radius:8px;font-size:15px;margin-bottom:8px">加入购物车（Bridge→Native）</button>' +
    '<button onclick="onGoBack()" style="width:100%;padding:12px;background:#546E7A;color:white;border:none;border-radius:8px;font-size:15px">返回 Native（Bridge→Native）</button>' +
    '<div id="js-log" style="margin-top:12px;background:#263238;border-radius:6px;padding:8px;font-size:10px;color:#80CBC4;font-family:monospace;min-height:40px"></div>' +
    '</div>';
}

// 页面2：用户主页
function renderUserProfile(params) {
  return '<div style="padding:20px;font-family:sans-serif">' +
    '<div style="background:#1565C0;color:white;padding:14px;border-radius:10px;margin-bottom:14px">' +
    '  <div style="font-size:11px;opacity:0.8">RN 页面 · 用户主页</div>' +
    '  <div style="font-size:20px;font-weight:bold;margin-top:4px">👤 用户页</div>' +
    '</div>' +
    '<div style="background:#E3F2FD;border-radius:8px;padding:12px;margin-bottom:12px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:6px">从 URI 解析的参数：</div>' +
    '  <div style="font-size:15px"><b>用户 ID：</b>' + (params.userId || '无') + '</div>' +
    '</div>' +
    '<button onclick="onGetUserInfo()" style="width:100%;padding:12px;background:#1565C0;color:white;border:none;border-radius:8px;font-size:15px;margin-bottom:8px">获取用户信息（Bridge→Native）</button>' +
    '<button onclick="onGoBack()" style="width:100%;padding:12px;background:#546E7A;color:white;border:none;border-radius:8px;font-size:15px">返回</button>' +
    '<div id="js-log" style="margin-top:12px;background:#263238;border-radius:6px;padding:8px;font-size:10px;color:#80CBC4;font-family:monospace;min-height:40px"></div>' +
    '</div>';
}

// 页面3：订单列表
function renderOrderList(params) {
  var orders = [
    { id: 'ORD001', item: '蓝牙耳机', price: '¥299', status: '已发货' },
    { id: 'ORD002', item: '机械键盘', price: '¥599', status: '待付款' },
    { id: 'ORD003', item: '显示器',   price: '¥1299',status: '已完成' }
  ];
  var rows = orders.map(function(o) {
    return '<div style="padding:10px;border-bottom:1px solid #eee;display:flex;justify-content:space-between">' +
      '<div><div style="font-size:13px;font-weight:bold">' + o.item + '</div>' +
      '<div style="font-size:11px;color:#888">' + o.id + '</div></div>' +
      '<div style="text-align:right"><div style="font-size:13px;color:#E65100">' + o.price + '</div>' +
      '<div style="font-size:11px;color:#666">' + o.status + '</div></div></div>';
  }).join('');
  return '<div style="padding:20px;font-family:sans-serif">' +
    '<div style="background:#2E7D32;color:white;padding:14px;border-radius:10px;margin-bottom:14px">' +
    '  <div style="font-size:11px;opacity:0.8">RN 页面 · 订单列表</div>' +
    '  <div style="font-size:20px;font-weight:bold;margin-top:4px">📦 我的订单</div>' +
    '</div>' +
    '<div style="background:white;border-radius:8px;overflow:hidden;margin-bottom:12px">' + rows + '</div>' +
    '<button onclick="onGoBack()" style="width:100%;padding:12px;background:#546E7A;color:white;border:none;border-radius:8px;font-size:15px">返回</button>' +
    '<div id="js-log" style="margin-top:12px;background:#263238;border-radius:6px;padding:8px;font-size:10px;color:#80CBC4;font-family:monospace;min-height:40px"></div>' +
    '</div>';
}

// 404 页面
function renderNotFound(route) {
  return '<div style="padding:40px;text-align:center;font-family:sans-serif">' +
    '<div style="font-size:48px">❓</div>' +
    '<div style="font-size:18px;font-weight:bold;margin:12px 0">路由未匹配</div>' +
    '<div style="font-size:13px;color:#888;background:#F5F5F5;padding:10px;border-radius:6px;word-break:break-all">' + route + '</div>' +
    '<button onclick="onGoBack()" style="margin-top:16px;padding:12px 24px;background:#546E7A;color:white;border:none;border-radius:8px;font-size:14px">返回</button>' +
    '</div>';
}

// ─── 核心 Router ─────────────────────────────────────────

function renderApp(containerId) {
  var container = document.getElementById(containerId);
  if (!container) return;

  // 从 Native 注入的 window.ROUTE_CONFIG 取路由信息
  // Native 在 buildHtmlWrapper 里把路由信息注入到 HTML 里
  var routeInfo = window.ROUTE_CONFIG || {};
  currentRoute = routeInfo.route || '';
  currentParams = routeInfo.params || {};

  appendLog('[Router] 路由: ' + currentRoute + ' 参数: ' + JSON.stringify(currentParams));
  callBridge('onPageReady', currentRoute);

  // ★ 核心：根据路由分发到不同页面
  var html;
  if (currentRoute === 'product/detail') {
    html = renderProductDetail(currentParams);
  } else if (currentRoute === 'user/profile') {
    html = renderUserProfile(currentParams);
  } else if (currentRoute === 'order/list') {
    html = renderOrderList(currentParams);
  } else {
    html = renderNotFound(currentRoute);
  }
  container.innerHTML = html;
}

// ─── 按钮回调 ─────────────────────────────────────────────

function onAddToCart() {
  appendLog('[JS→Native] addToCart: id=' + currentParams.id);
  callBridge('addToCart', currentParams.id, currentParams.name || '');
}

function onGetUserInfo() {
  appendLog('[JS→Native] getUserInfo: userId=' + currentParams.userId);
  callBridge('getUserInfo', currentParams.userId || '');
}

function onGoBack() {
  appendLog('[JS→Native] goBack()');
  callBridge('goBack');
}

// Native 回调：getUserInfo 结果
function onUserInfoReceived(info) {
  appendLog('[Native→JS] 用户信息: ' + JSON.stringify(info));
  var el = document.getElementById('js-log');
  if (el) el.innerHTML += '<div style="color:#FFD54F">name: ' + info.name + ', team: ' + info.team + '</div>';
}

function appendLog(msg) {
  var el = document.getElementById('js-log');
  if (el) el.innerHTML += '<div>' + msg + '</div>';
}
