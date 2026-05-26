// ====================================================
// RN Bundle v1.0.0  (模拟 APK 内置的初始版本)
// 真实 RN bundle 里是所有业务 JS 代码打包在一起
// ====================================================

var BUNDLE_VERSION = "1.0.0";
var BUNDLE_CONTENT = {
  title: "首页",
  subtitle: "欢迎使用 Demo App",
  features: ["基础购物车", "商品列表", "用户中心"],
  bgColor: "#E3F2FD",
  textColor: "#0D47A1",
  notice: "这是 APK 内置的 bundle v1，随安装包发布"
};

// 模拟 RN 组件渲染（真实 RN 里这里是 React.render）
function renderApp(containerId) {
  var container = document.getElementById(containerId);
  if (!container) return;

  container.style.background = BUNDLE_CONTENT.bgColor;
  container.innerHTML =
    '<div style="padding:20px;font-family:sans-serif">' +
    '<div style="background:#0D47A1;color:white;padding:12px;border-radius:8px;margin-bottom:12px">' +
    '  <div style="font-size:11px;opacity:0.8">Bundle 版本</div>' +
    '  <div style="font-size:22px;font-weight:bold">v' + BUNDLE_VERSION + '</div>' +
    '</div>' +
    '<div style="font-size:18px;font-weight:bold;color:' + BUNDLE_CONTENT.textColor + ';margin-bottom:8px">' +
    BUNDLE_CONTENT.title + '</div>' +
    '<div style="color:#555;margin-bottom:12px">' + BUNDLE_CONTENT.subtitle + '</div>' +
    '<div style="background:#fff;border-radius:8px;padding:12px;margin-bottom:12px">' +
    '  <div style="font-size:12px;color:#888;margin-bottom:6px">功能列表：</div>' +
    BUNDLE_CONTENT.features.map(function(f) {
      return '<div style="padding:4px 0;font-size:14px">✓ ' + f + '</div>';
    }).join('') +
    '</div>' +
    '<div style="background:#FFF9C4;border-radius:6px;padding:10px;font-size:12px;color:#666">' +
    '📦 ' + BUNDLE_CONTENT.notice + '</div>' +
    '</div>';
}

// 通知 Native 渲染完成（模拟 RN bridge 回调）
if (window.BundleBridge) {
  window.BundleBridge.onBundleLoaded(BUNDLE_VERSION);
}
