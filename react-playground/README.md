# React Playground —— 亲手体验 JSX/TSX 编译

这个目录用来**亲眼看到** `.tsx` 里的 `<div>` 是怎么被 Babel 翻译成纯 JS 的。

## 目录结构

```
react-playground/
├── src/App.tsx          ← 你写的源码（含 JSX + TS 类型）
├── dist/App.js          ← Babel 编译后的产物（浏览器能跑的纯 JS）★编译后生成
├── index.html           ← 浏览器入口（引入 dist/App.js + React CDN）
├── package.json         ← 依赖 + 编译命令
└── babel.config.json    ← Babel 翻译规则
```

## 体验步骤

### 第 1 步：安装 Babel 工具

```bash
cd react-playground
npm install
```

### 第 2 步：编译 tsx → js（核心！）

```bash
npm run build
```

这条命令会把 `src/App.tsx` 编译成 `dist/App.js`。

### 第 3 步：★对比编译前后★（最重要的学习点）

打开两个文件对照看：

**编译前 `src/App.tsx`：**
```tsx
const [count, setCount] = useState<number>(0);   // 有 <number> 类型
return (
  <div style={{ ... }}>          // 有 <div> JSX 标签
    <p>你点了 {count} 次</p>
  </div>
);
```

**编译后 `dist/App.js`：**
```js
const [count, setCount] = useState(0);   // ← <number> 类型被擦除了！
return React.createElement("div", { style: {...} },   // ← <div> 变成 createElement！
  React.createElement("p", null, "你点了 ", count, " 次")
);
```

你会亲眼看到：
- **JSX 的 `<div>` → `React.createElement("div", ...)`**（Babel 的 preset-react 干的）
- **TS 的 `<number>` 类型 → 消失**（Babel 的 preset-typescript 干的）

### 第 4 步：浏览器运行

直接双击 `index.html`，或用命令：

```bash
open index.html
```

你会看到一个计数器页面，点按钮数字 +1。这就是 `dist/App.js`（纯 JS）在浏览器里跑起来的效果。

## 关键结论

> `App.tsx` 里的 `<div>` **不是 JS 语法**，浏览器根本不认识。
> 是 **Babel 在运行前**把它翻译成了 `React.createElement("div", ...)`（纯 JS）。
> 浏览器和 React 库**只见过编译后的纯 JS**，从没见过 `<div>` 标签。
>
> **编译时工具（Babel）** 和 **运行时库（React）** 是两拨人干活。
