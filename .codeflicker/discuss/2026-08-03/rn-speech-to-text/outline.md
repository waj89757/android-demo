# 讨论：RN 语音转文字功能

> 状态：进行中 | 轮次：R1 | 日期：2026-08-03

## 🔵 当前焦点

- **参考代码（快手电商 speech 包）问题盘点**
- **ASR（语音转文字）方案选型：本地 SDK / 系统 API / 服务端上传**

## ⚪ 待讨论

- [ ] 录音引擎选型（AudioRecord 裸写 / MediaRecorder / SpeechRecognizer）
- [ ] Native → RN 的事件通道设计（DeviceEventEmitter vs Promise）
- [ ] 状态机设计（idle / recording / recognizing / done / error）
- [ ] RN 侧 UI（麦克风按钮 + 声波动画 + 结果展示）
- [ ] 目录拆分（components / services 落地）

## ✅ 已确认

（暂无）

## ❌ 已否决

（暂无）

## 📁 归档

| 问题 | 结论 | 详情 |
|------|------|------|
| 参考代码审查 | 发现 8 类问题，见分析 | 本轮讨论 |
