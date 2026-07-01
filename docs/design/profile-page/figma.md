# 用户个人资料页 — 设计稿

## 页面结构（从上到下）

| 区域 | 组件 | 尺寸/备注 |
|---|---|---|
| 状态栏 | 系统级，无需自己写 | 高 28dp |
| 导航栏 Toolbar | 返回箭头 + 标题「个人资料」+ 更多按钮 | 高 52dp |
| Banner 背景 | 纯色填充（可替换为图片） | 高 100dp |
| 圆形头像 | ImageView + ShapeAppearance 圆形裁剪 | 72dp，叠在 Banner 底部偏移 -36dp |
| 编辑资料按钮 | 描边圆角按钮，浮在右侧 | 对齐头像底部 |
| 用户名 + 简介 | 两行文字，TextAppearance 控制字号 | paddingStart 16dp |
| 统计数字行 | 关注 / 粉丝 / 获赞，三格横向等宽 | LinearLayout + weight="1" |
| 个人信息卡片 | 手机号 / 邮箱 / 所在地区，带右箭头 | CardView |
| 设置卡片 | 消息通知 / 隐私设置 / 退出登录 | CardView，退出登录文字红色 |

---

## 布局知识点

- **ConstraintLayout** — 头像叠在 Banner 底部（app:layout_constraintTop_toBottomOf + 负 margin）
- **LinearLayout 横向** — 统计三格 android:layout_weight="1" 等宽分配
- **CardView** — 圆角卡片容器，app:cardCornerRadius="12dp"
- **Toolbar** — setSupportActionBar，setNavigationIcon 设置返回箭头
- **ImageView + ShapeAppearance** — 圆形头像裁剪
- **dp vs sp** — 间距/尺寸用 dp，字号用 sp
- **include / merge** — 可复用的列表行布局（信息行和设置行结构相同）

---

## 颜色规范（Material You 风格）

| 用途 | Token | 说明 |
|---|---|---|
| 主色 | `colorPrimary` | 头像背景、编辑按钮描边 |
| 背景 | `colorSurface` | 页面底色 |
| 卡片背景 | `colorSurfaceVariant` | 列表卡片 |
| 分割线 | `colorOutlineVariant` | 行间分隔线 |
| 危险操作 | `colorError` | 退出登录文字 |

---

## 尺寸规范

```
头像直径：72dp
头像 border：3dp（与背景同色，制造分离感）
Banner 高度：100dp
头像叠出 Banner：36dp（头像半径）
页面左右边距：16dp
卡片圆角：12dp
行高：48dp（信息行）
```

---

## 开发任务拆解

- [ ] Step 1：搭 Toolbar + 静态页面框架
- [ ] Step 2：加 Banner + ConstraintLayout 头像叠加
- [ ] Step 3：加统计数字横向三格
- [ ] Step 4：加个人信息 CardView 列表
- [ ] Step 5：加设置 CardView 列表 + 退出登录红色样式

---

## 设计稿预览

设计稿 Canvas 文件：`.codeflicker/canvas/android-profile-figma.canvas.tsx`
（在 CodeFlicker 中打开可看到实时渲染效果）
