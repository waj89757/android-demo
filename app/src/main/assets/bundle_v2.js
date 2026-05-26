// ============================================================
// RN Bundle v2.0.0（热更新版本）
// 新增：openPage Bridge（跳转 Native 页面）、用户信息获取
// 修复：Toast 调用时增加防抖
// ============================================================

var BUNDLE_VERSION = "2.0.0";
var _lastToastTime = 0;  // 修复：Toast 防抖

function callBridge(method) {
  var args = Array.prototype.slice.call(arguments, 1);
  if (window.NativeBridge && typeof window.NativeBridge[method] === 'function') {
    return window.NativeBridge[method].apply(null, args);
  }
  return null;
}

function trackEvent(eventName, params) {
  var paramsStr = JSON.stringify(params || {});
  callBridge('trackEvent', eventName, paramsStr);
  appendLog('[埋点] ' + eventName + ' ' + paramsStr);
}

function appendLog(msg) {
  var logEl = document.getElementById('js-log');
  if (logEl) {
    logEl.innerHTML += '<div>' + msg + '</div>';
    logEl.scrollTop = logEl.scrollHeight;
  }
}

function renderApp(containerId) {
  var container = document.getElementById(containerId);
  if (!container) return;

  container.innerHTML =
    '<div style="font-family:sans-serif;padding:16px;background:#E8F5E9;min-height:100%">' +

    '<div style="background:#1B5E20;color:white;padding:12px;border-radius:8px;margin-bottom:12px">' +
    '  <div style="font-size:11px;opacity:0.8">热更新 Bundle 🆕</div>' +
    '  <div style="font-size:20px;font-weight:bold">v' + BUNDLE_VERSION + '</div>' +
    '  <div style="font-size:11px;margin-top:4px;opacity:0.8">新增 openPage + getUserInfo | 修复 Toast 防抖</div>' +
    '</div>' +

    // ★ v2 新增第4个按钮：openPage（跳转 Native 页面）
    // ★ v2 新增第5个按钮：getUserInfo
    '<div style="background:white;border-radius:8px;padding:12px;margin-bottom:10px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:8px">Bridge 调用（v2 新增了 2 个）：</div>' +

    '  <button onclick="onGetDeviceInfo()" ' +
    '    style="width:100%;padding:10px;background:#1565C0;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    📱 获取设备信息' +
    '  </button>' +

    '  <button onclick="onShowToast()" ' +
    '    style="width:100%;padding:10px;background:#2E7D32;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    🔔 弹出 Toast（已修复防抖）' +
    '  </button>' +

    '  <button onclick="onTrackEvent()" ' +
    '    style="width:100%;padding:10px;background:#6A1B9A;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    📊 上报埋点' +
    '  </button>' +

    '  <button onclick="onOpenPage()" ' +
    '    style="width:100%;padding:10px;background:#E65100;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    🚀 跳转 Native 页面（v2 新增）' +
    '  </button>' +

    '  <button onclick="onGetUserInfo()" ' +
    '    style="width:100%;padding:10px;background:#37474F;color:white;border:none;border-radius:6px;font-size:13px">' +
    '    👤 获取用户信息（v2 新增）' +
    '  </button>' +
    '</div>' +

    '<div id="device-info" style="background:white;border-radius:8px;padding:12px;margin-bottom:10px;min-height:50px">' +
    '  <div style="font-size:12px;color:#888">设备/用户信息（点按钮获取）：</div>' +
    '</div>' +

    '<div style="background:#263238;border-radius:8px;padding:10px">' +
    '  <div style="font-size:11px;color:#B2EBF2;margin-bottom:4px">JS 日志：</div>' +
    '  <div id="js-log" style="font-size:10px;color:#80CBC4;max-height:80px;overflow-y:auto;font-family:monospace"></div>' +
    '</div>' +

    '</div>';

  callBridge('onBundleLoaded', BUNDLE_VERSION);
  trackEvent('page_view', { page: 'home', bundle_version: BUNDLE_VERSION });
}

function onGetDeviceInfo() {
  appendLog('[JS→Native] 调用 getDeviceInfo()');
  callBridge('getDeviceInfo');
}

function onDeviceInfoReceived(info) {
  appendLog('[Native→JS] 收到设备信息');
  // info 已经是 JSON 对象，直接用
  var el = document.getElementById('device-info');
  if (el) {
    el.innerHTML =
      '<div style="font-size:12px;color:#888;margin-bottom:6px">设备信息：</div>' +
      Object.keys(info).map(function(k) {
        return '<div style="font-size:13px;padding:2px 0"><b>' + k + '</b>: ' + info[k] + '</div>';
      }).join('');
  }
}

// ★ 修复：Toast 防抖，1秒内不重复弹
function onShowToast() {
  var now = Date.now();
  if (now - _lastToastTime < 1000) {
    appendLog('[JS] Toast 防抖拦截（1秒内重复点击）');
    return;
  }
  _lastToastTime = now;
  appendLog('[JS→Native] 调用 showToast()（防抖版）');
  callBridge('showToast', 'Hello from Bundle v' + BUNDLE_VERSION + '!');
  trackEvent('button_click', { button: 'toast', bundle_version: BUNDLE_VERSION });
}

function onTrackEvent() {
  trackEvent('manual_track', { source: 'bundle_v2', ts: Date.now() });
}

// ★ v2 新增：跳转 Native 页面
function onOpenPage() {
  appendLog('[JS→Native] 调用 openPage("MainActivity")');
  callBridge('openPage', 'MainActivity');
  trackEvent('navigate', { target: 'MainActivity', from: 'bundle_v2' });
}

// ★ v2 新增：获取用户信息
function onGetUserInfo() {
  appendLog('[JS→Native] 调用 getUserInfo()');
  callBridge('getUserInfo');
}

// Native 回调：收到用户信息
function onUserInfoReceived(info) {
  appendLog('[Native→JS] 收到用户信息回调');
  // info 已经是 JSON 对象，直接用
  var el = document.getElementById('device-info');
  if (el) {
    el.innerHTML =
      '<div style="font-size:12px;color:#888;margin-bottom:6px">👤 用户信息：</div>' +
      Object.keys(info).map(function(k) {
        return '<div style="font-size:13px;padding:2px 0"><b>' + k + '</b>: ' + info[k] + '</div>';
      }).join('');
  }
}
