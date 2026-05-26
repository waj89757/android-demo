# React vs 纯 JS+HTML 对比 Demo

## 文件说明

| 文件 | 说明 |
|------|------|
| `01_vanilla_js.html` | 纯 JS + HTML 实现的餐厅点单系统 |
| `02_react.html` | React 实现的同一个系统 |

两个文件功能完全一样，代码量相近，但维护数据-UI同步的方式截然不同。

---

## 如何运行（无需安装任何东西）

直接用浏览器打开 HTML 文件即可：

```bash
# 方式一：命令行打开
open react/01_vanilla_js.html
open react/02_react.html

# 方式二：在 Finder 里双击 .html 文件

# 注意：02_react.html 需要联网（从 CDN 加载 React 库）
# 01_vanilla_js.html 无需联网
```

---

## 核心区别对照

### 纯 JS：数据变了，手动更新每个地方

```javascript
function updateAllUI() {
    // 统计栏：3个 getElementById
    document.getElementById('stat-count').textContent = totalCount
    document.getElementById('stat-total').textContent = '¥' + totalPrice
    document.getElementById('stat-kinds').textContent = kinds

    // 结算栏：2个 getElementById
    document.getElementById('checkout-count').textContent = totalCount
    document.getElementById('checkout-total').textContent = '¥' + totalPrice

    // 购物车列表：重建整个 DOM
    cartEl.innerHTML = ''
    // ...

    // 菜单按钮：循环更新
    MENU.forEach(item => {
        document.getElementById('btn-' + item.id).textContent = ...
    })

    // 漏掉任何一个 → Bug
}
```

### React：只改数据，UI 自动同步

```jsx
// 一行改数据
setCart(prev => ({ ...prev, [id]: prev[id] + 1 }))

// UI 里直接用数据变量，React 保证自动同步
<div>{totalCount}</div>   // 统计栏
<div>{totalPrice}</div>   // 结算栏
// 不需要 getElementById，不可能漏掉
```

---

## 亲自体验的建议

1. 打开两个文件，功能操作一模一样
2. 看代码时对比：
   - 纯 JS 的 `updateAllUI()` 函数：要更新多少个地方
   - React 的 `setCart()`：只改数据，全部搞定
3. 想象如果再加一个"显示屏"（比如顶部 Header 也要显示总价）：
   - 纯 JS：要在 `updateAllUI()` 里再加一行 `getElementById`
   - React：在 JSX 里加 `{totalPrice}` 就完了，完全不用改其他地方
