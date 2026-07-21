// ★★★ App.tsx —— 这是你写的"源代码"（含 JSX + TypeScript）★★★
//
// 关注两点，编译后回来对比：
//   1. 下面的 <div> <button> 这些 JSX 标签，编译后会变成 React.createElement(...)
//   2. count: number 这个类型标注，编译后会被"擦除"（TS → JS）
//
// 浏览器 / JS 引擎【永远看不到】这个文件，它们只看到编译后的 App.js

// 从 React 库里拿到 useState（状态钩子）
const { useState } = React;

// 一个计数器组件
function App() {
  // ★ TypeScript 类型标注 ": number"，编译后会消失
  const [count, setCount] = useState<number>(0);

  // ★ 下面 return 里的全是 JSX，不是 JS 语法！
  //   编译后每个 <标签> 都会变成 React.createElement(...)
  return (
    <div style={{ fontFamily: "sans-serif", textAlign: "center", padding: 40 }}>
      <h1>Hello React 👋</h1>
      <p>你点了 {count} 次</p>
      <button
        onClick={() => setCount(count + 1)}
        style={{ fontSize: 18, padding: "8px 20px", cursor: "pointer" }}
      >
        点我 +1
      </button>
    </div>
  );
}

// 把 App 组件渲染到 index.html 里 id="root" 的 div 上
const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
