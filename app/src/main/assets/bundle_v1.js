// ============================================================
// RN Bundle v1.0.0
// 包含：UI 渲染 + Bridge 调用（获取设备信息、Toast、埋点上报）
// ============================================================

var BUNDLE_VERSION = "1.0.0";

// ★ Bridge 工具函数：安全调用 Native，不存在时降级
function callBridge(method) {
  var args = Array.prototype.slice.call(arguments, 1);
  var hasBridge = !!(window.NativeBridge && typeof window.NativeBridge[method] === 'function');
  appendLog('[callBridge] ' + method + ' | NativeBridge存在: ' + hasBridge);
  if (hasBridge) {
    return window.NativeBridge[method].apply(null, args);
  } else {
    appendLog('[callBridge] ⚠️ 方法不存在: ' + method);
    return null;
  }
}

// ★ 埋点上报（调用 Native Bridge）
function trackEvent(eventName, params) {
  var paramsStr = JSON.stringify(params || {});
  callBridge('trackEvent', eventName, paramsStr);
  appendLog('[埋点] ' + eventName + ' ' + paramsStr);
}

// ★ 工具：往日志区追加日志
function appendLog(msg) {
  var logEl = document.getElementById('js-log');
  if (logEl) {
    logEl.innerHTML += '<div>' + msg + '</div>';
    logEl.scrollTop = logEl.scrollHeight;
  }
}

// ★ 主渲染函数
function renderApp(containerId) {
  var container = document.getElementById(containerId);
  if (!container) return;

  container.innerHTML =
    '<div style="font-family:sans-serif;padding:16px;background:#E3F2FD;min-height:100%">' +

    // 版本标题
    '<div style="background:#0D47A1;color:white;padding:12px;border-radius:8px;margin-bottom:12px">' +
    '  <div style="font-size:11px;opacity:0.8">当前 Bundle</div>' +
    '  <div style="font-size:20px;font-weight:bold">v' + BUNDLE_VERSION + '</div>' +
    '</div>' +

    // ★ Bridge 功能区：3个按钮分别调不同 Bridge
    '<div style="background:white;border-radius:8px;padding:12px;margin-bottom:10px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:8px">Bridge 调用演示：</div>' +

    '  <button onclick="onGetDeviceInfo()" ' +
    '    style="width:100%;padding:10px;background:#1565C0;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    📱 获取设备信息（Bridge → Native）' +
    '  </button>' +

    '  <button onclick="onShowToast()" ' +
    '    style="width:100%;padding:10px;background:#2E7D32;color:white;border:none;border-radius:6px;margin-bottom:8px;font-size:13px">' +
    '    🔔 弹出 Toast（Bridge → Native）' +
    '  </button>' +

    '  <button onclick="onTrackEvent()" ' +
    '    style="width:100%;padding:10px;background:#6A1B9A;color:white;border:none;border-radius:6px;font-size:13px">' +
    '    📊 上报埋点（Bridge → Native）' +
    '  </button>' +
    '</div>' +

    // 设备信息展示区（Native 回调后填入）
    '<div id="device-info" style="background:white;border-radius:8px;padding:12px;margin-bottom:10px;min-height:50px">' +
    '  <div style="font-size:12px;color:#888">设备信息（点上方按钮获取）：</div>' +
    '</div>' +

    // JS 日志区
    '<div style="background:#263238;border-radius:8px;padding:10px">' +
    '  <div style="font-size:11px;color:#B2EBF2;margin-bottom:4px">JS 日志：</div>' +
    '  <div id="js-log" style="font-size:10px;color:#80CBC4;max-height:80px;overflow-y:auto;font-family:monospace"></div>' +
    '</div>' +

    '</div>';

  // 页面加载完成，通知 Native + 上报埋点
  callBridge('onBundleLoaded', BUNDLE_VERSION);
  trackEvent('page_view', { page: 'home', bundle_version: BUNDLE_VERSION });
}

// ★ 按钮点击处理：调 Bridge 获取设备信息
// Native 执行后通过 evaluateJavascript 回调 onDeviceInfoReceived()
function onGetDeviceInfo() {
  appendLog('[JS→Native] 调用 getDeviceInfo()');
  callBridge('getDeviceInfo');
}

// ★ Native 回调：收到设备信息（由 Native evaluateJavascript 调用）
function onDeviceInfoReceived(info) {
  appendLog('[Native→JS] 收到设备信息回调');
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

function onShowToast() {
  appendLog('[JS→Native] 调用 showToast()');
  callBridge('showToast', 'Hello from Bundle v' + BUNDLE_VERSION + '!');
  trackEvent('button_click', { button: 'toast', bundle_version: BUNDLE_VERSION });
}

function onTrackEvent() {
  trackEvent('manual_track', { source: 'bundle_v1', ts: Date.now() });
}
