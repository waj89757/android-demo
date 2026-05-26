// ====================================================
// RN Bundle v2.0.0  (模拟热更新下发的新版本)
// 变更：新增"直播间"功能，修复购物车 Bug
// ====================================================

var BUNDLE_VERSION = "2.0.0";
var BUNDLE_CONTENT = {
  title: "首页 🎉 已更新",
  subtitle: "发现新版本，已通过热更新自动升级",
  features: ["基础购物车（Bug已修复）", "商品列表", "用户中心", "直播间（新功能）", "优惠券中心（新功能）"],
  bgColor: "#E8F5E9",
  textColor: "#1B5E20",
  notice: "这是热更新下发的 bundle v2，无需重新安装 App"
};

function renderApp(containerId) {
  var container = document.getElementById(containerId);
  if (!container) return;

  container.style.background = BUNDLE_CONTENT.bgColor;
  container.innerHTML =
    '<div style="padding:20px;font-family:sans-serif">' +
    '<div style="background:#1B5E20;color:white;padding:12px;border-radius:8px;margin-bottom:12px">' +
    '  <div style="font-size:11px;opacity:0.8">Bundle 版本</div>' +
    '  <div style="font-size:22px;font-weight:bold">v' + BUNDLE_VERSION + ' 🆕</div>' +
    '</div>' +
    '<div style="font-size:18px;font-weight:bold;color:' + BUNDLE_CONTENT.textColor + ';margin-bottom:8px">' +
    BUNDLE_CONTENT.title + '</div>' +
    '<div style="color:#555;margin-bottom:12px">' + BUNDLE_CONTENT.subtitle + '</div>' +
    '<div style="background:#fff;border-radius:8px;padding:12px;margin-bottom:12px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:6px">功能列表：</div>' +
    BUNDLE_CONTENT.features.map(function(f) {
      var isNew = f.indexOf("新功能") >= 0;
      var isFix = f.indexOf("Bug已修复") >= 0;
      return '<div style="padding:4px 0;font-size:14px">' +
        (isNew ? '🆕 ' : isFix ? '🔧 ' : '✓ ') + f + '</div>';
    }).join('') +
    '</div>' +
    '<div style="background:#C8E6C9;border-radius:6px;padding:10px;font-size:12px;color:#333">' +
    '⚡ ' + BUNDLE_CONTENT.notice + '</div>' +
    '</div>';
}

if (window.BundleBridge) {
  window.BundleBridge.onBundleLoaded(BUNDLE_VERSION);
}
