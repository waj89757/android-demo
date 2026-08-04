/**
 * ★★★ VoiceButton — 麦克风按钮（纯 UI 组件）★★★
 *
 * 设计原则：这是一个「哑组件」（dumb component）
 *   - 不 import NativeModules
 *   - 不 import speechService
 *   - 不 import logger
 *   - 所有数据从 props 来，所有行为通过 props 回调传出去
 *
 * 好处：
 *   1. 换掉底层语音 SDK 时，这个文件一行不用改
 *   2. 可以单独在 Storybook / 测试里渲染，不用启动整个页面
 *   3. 其他页面想加语音按钮，直接 import 复用
 *
 * 对照 Android：这相当于一个自定义 View，只管 onDraw，不管业务
 */

import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  ActivityIndicator,
} from 'react-native';

// ─── Props ─────────────────────────────────────────────────────────────────

export type VoiceButtonStatus = 'idle' | 'listening' | 'recognizing';

interface VoiceButtonProps {
  /** 当前状态，决定按钮外观 */
  status: VoiceButtonStatus;
  /** 音量值（来自 Native 的 VOLUME 事件），范围约 -2 ~ 10 */
  volume: number;
  /** 点击回调 */
  onPress: () => void;
}

// ─── 常量 ──────────────────────────────────────────────────────────────────

/** 声波条的最小/最大高度（dp） */
const WAVE_MIN_HEIGHT = 6;
const WAVE_MAX_HEIGHT = 32;

/** Native 传来的音量上限，用于归一化 */
const VOLUME_MAX = 10;

// ─── 组件 ──────────────────────────────────────────────────────────────────

const VoiceButton: React.FC<VoiceButtonProps> = ({ status, volume, onPress }) => {
  // 三条声波各自的动画值（用不同系数错开，看起来更自然）
  const wave1 = useRef(new Animated.Value(WAVE_MIN_HEIGHT)).current;
  const wave2 = useRef(new Animated.Value(WAVE_MIN_HEIGHT)).current;
  const wave3 = useRef(new Animated.Value(WAVE_MIN_HEIGHT)).current;

  /**
   * ★ volume 变化时驱动声波动画
   *
   * useEffect 依赖 [volume, status]：
   *   Native 每 200ms 发一次 VOLUME 事件 → setState → volume 变化
   *   → 这个 effect 重新执行 → Animated.timing 到新高度
   */
  useEffect(() => {
    if (status !== 'listening') {
      // 不在录音状态，声波收回最小高度
      Animated.parallel([
        Animated.timing(wave1, { toValue: WAVE_MIN_HEIGHT, duration: 150, useNativeDriver: false }),
        Animated.timing(wave2, { toValue: WAVE_MIN_HEIGHT, duration: 150, useNativeDriver: false }),
        Animated.timing(wave3, { toValue: WAVE_MIN_HEIGHT, duration: 150, useNativeDriver: false }),
      ]).start();
      return;
    }

    // 音量归一化到 0~1（Native 可能传负值，用 Math.max 兜底）
    const normalized = Math.min(Math.max(volume, 0) / VOLUME_MAX, 1);
    const base = WAVE_MIN_HEIGHT + normalized * (WAVE_MAX_HEIGHT - WAVE_MIN_HEIGHT);

    // 三条波用不同系数，产生高低错落的效果
    Animated.parallel([
      Animated.timing(wave1, {
        toValue: base * 0.6,
        duration: 180,
        useNativeDriver: false,
      }),
      Animated.timing(wave2, {
        toValue: base,
        duration: 180,
        useNativeDriver: false,
      }),
      Animated.timing(wave3, {
        toValue: base * 0.75,
        duration: 180,
        useNativeDriver: false,
      }),
    ]).start();
  }, [volume, status, wave1, wave2, wave3]);

  // ─── 根据状态决定外观 ────────────────────────────────────────────────────

  const isListening = status === 'listening';
  const isRecognizing = status === 'recognizing';

  const buttonStyle = [
    styles.button,
    isListening && styles.buttonListening,
    isRecognizing && styles.buttonRecognizing,
  ];

  const label = isListening
    ? '正在聆听，点击结束'
    : isRecognizing
    ? '识别中...'
    : '点击开始说话';

  return (
    <View style={styles.container}>
      <TouchableOpacity
        style={buttonStyle}
        onPress={onPress}
        // 识别中禁止点击（此时 Native 正在等系统返回结果）
        disabled={isRecognizing}
        activeOpacity={0.8}>
        {isRecognizing ? (
          // 识别中：转圈
          <ActivityIndicator size="small" color="#ffffff" />
        ) : isListening ? (
          // 录音中：声波动画
          <View style={styles.waveRow}>
            <Animated.View style={[styles.waveBar, { height: wave1 }]} />
            <Animated.View style={[styles.waveBar, { height: wave2 }]} />
            <Animated.View style={[styles.waveBar, { height: wave3 }]} />
          </View>
        ) : (
          // 空闲：麦克风图标
          <Text style={styles.micIcon}>🎤</Text>
        )}
      </TouchableOpacity>

      <Text style={[styles.label, isListening && styles.labelListening]}>{label}</Text>
    </View>
  );
};

// ─── 样式 ──────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    marginVertical: 12,
  },
  button: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#0f3460',
    borderWidth: 2,
    borderColor: '#4488ff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonListening: {
    backgroundColor: '#c62828',
    borderColor: '#ff5252',
  },
  buttonRecognizing: {
    backgroundColor: '#6A1B9A',
    borderColor: '#ba68c8',
  },
  micIcon: {
    fontSize: 30,
  },
  waveRow: {
    flexDirection: 'row',
    alignItems: 'center',
    height: WAVE_MAX_HEIGHT,
    gap: 5,
  },
  waveBar: {
    width: 5,
    borderRadius: 3,
    backgroundColor: '#ffffff',
  },
  label: {
    marginTop: 10,
    fontSize: 12,
    color: '#8888aa',
  },
  labelListening: {
    color: '#ff8a80',
    fontWeight: '600',
  },
});

export default VoiceButton;
