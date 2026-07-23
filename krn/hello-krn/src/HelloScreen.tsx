/**
 * ★★★ HelloScreen：第一个 KRN 风格页面 ★★★
 *
 * 功能：
 *   1. 展示用户名 + Follow 状态（模拟 BriefProfile 核心结构）
 *   2. 点击 Follow/Unfollow 按钮 → 切换状态（演示 useState）
 *   3. 点击 "Call Native Toast" → 通过 invoke 调用 Android Toast（演示 Bridge）
 *   4. 点击 "Get Device Info" → invoke 获取 Native 返回的设备信息（演示 Promise）
 *
 * 对照 BriefProfile 的结构：
 *   useState  → 对应 BriefProfile 里的 nickname/hasFollowed 等 state
 *   useEffect → 对应 fetchBriefProfileData
 *   invoke    → 对应 yoda.invoke('live.xxx', params)
 *   View/Text/TouchableOpacity → 和 KRN 完全相同（都是标准 RN 组件）
 */

import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
} from 'react-native';
import yoda from './yoda';
import logger from './logger';

// ─── 类型定义 ──────────────────────────────────────────────────────────────

interface DeviceInfo {
  model: string;
  os: string;
  appVersion: string;
}

// ─── 组件 ──────────────────────────────────────────────────────────────────

const HelloScreen: React.FC = () => {
  // ★ State：对照 BriefProfile 的 nickname/hasFollowed 等
  const [nickname] = useState<string>('王安杰 (KRN Demo)');
  const [hasFollowed, setHasFollowed] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [deviceInfo, setDeviceInfo] = useState<DeviceInfo | null>(null);
  const [log, setLog] = useState<string[]>([]);
  // ★ 埋点：数据加载完成标志（对照 KRN 里的 hasLoadedData）
  const [hasLoadedData, setHasLoadedData] = useState<boolean>(false);

  // ★ 埋点：构建页面通用参数（对照 getPageLogParams）
  //   useCallback 保证引用稳定，不会每次渲染都重新创建，
  //   这样 useEffect 的 deps 数组比较时不会无限触发
  // ★ 埋点公参（对照 KRN 里的 getPageLogParams）
  //   useCallback 保证引用稳定，避免 useEffect 无限触发
  const getPageLogParams = useCallback(() => ({
    page: 'HELLO_SCREEN',
    nickname,
    hasFollowed: hasFollowed ? 1 : 0,
  }), [nickname, hasFollowed]);

  // 日志工具：显示在页面上，方便 Android 调试
  const appendLog = useCallback((msg: string) => {
    setLog(prev => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev.slice(0, 9)]);
  }, []);

  // ★ useEffect：页面挂载时模拟"加载用户数据"（对照 fetchBriefProfileData）
  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      setHasLoadedData(true);   // ★ 数据加载完成 → 触发曝光埋点
      appendLog('页面数据加载完成');
    }, 500);
  }, [appendLog]);

  // ★ 页面曝光埋点（对照真实 KRN 代码）
  //   hasLoadedData 变为 true 时触发（确保数据已加载再上报，和你给的代码一致）
  useEffect(() => {
    if (!hasLoadedData) return;

    const params = getPageLogParams();
    logger.sendShow({
      action: 'HELLO_SCREEN_SHOW',
      params,
    });
    appendLog('📊 曝光埋点已上报 → HELLO_SCREEN_SHOW');
  }, [hasLoadedData, getPageLogParams, appendLog]);

  // ─── 事件处理 ──────────────────────────────────────────────────────────

  // ★ Follow 按钮：切换状态 + 发点击埋点
  const handleFollowPress = useCallback(() => {
    const next = !hasFollowed;
    setHasFollowed(next);
    logger.sendClick({
      action: 'HELLO_SCREEN_FOLLOW_BTN',
      params: { ...getPageLogParams(), toFollowed: next ? 1 : 0 },
    });
    appendLog(`Follow 状态切换 → ${next ? 'Following' : 'Unfollowed'}`);
    appendLog('📊 点击埋点已上报 → HELLO_SCREEN_FOLLOW_BTN');
  }, [hasFollowed, appendLog, getPageLogParams]);

  // ★ Bridge 演示1：调 Native 弹 Toast
  //   KRN 对应：yoda.invoke('live.showToast', { msg: '...' })
  //   本 Demo：yoda.invoke('YodaBridge.showToast', { msg: '...' })
  const handleNativeToast = useCallback(async () => {
    appendLog('调用 Native Toast...');
    try {
      await yoda.invoke('YodaBridge.showToast', {
        msg: `Hello from RN! hasFollowed=${hasFollowed}`,
      });
      appendLog('✅ Native Toast 调用成功');
    } catch (e) {
      appendLog(`❌ Bridge 调用失败: ${String(e)}`);
    }
  }, [hasFollowed, appendLog]);

  // ★ Bridge 演示2：调 Native 获取设备信息（有返回值的 invoke）
  //   KRN 对应：yoda.invoke('device.getInfo', {})
  //   Native 返回 JSON → Promise resolve → React state 更新
  const handleGetDeviceInfo = useCallback(async () => {
    appendLog('获取设备信息...');
    try {
      const result = (await yoda.invoke('YodaBridge.getDeviceInfo', {})) as DeviceInfo;
      setDeviceInfo(result);
      appendLog(`✅ 设备信息: ${result.model} / ${result.os}`);
    } catch (e) {
      appendLog(`❌ 获取失败: ${String(e)}`);
    }
  }, [appendLog]);

  // ─── 渲染 ──────────────────────────────────────────────────────────────

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* 顶部：Header（对照 BriefProfile topWrapper）*/}
      <View style={styles.header}>
        <View style={styles.avatarPlaceholder}>
          <Text style={styles.avatarText}>👤</Text>
        </View>
        <Text style={styles.nickname}>{nickname}</Text>
        <Text style={styles.subtitle}>Hello KRN Demo Page</Text>
      </View>

      {/* Loading 指示器（对照 BriefProfile loading 状态）*/}
      {loading && (
        <View style={styles.loadingRow}>
          <ActivityIndicator size="small" color="#8d5cff" />
          <Text style={styles.loadingText}>加载中...</Text>
        </View>
      )}

      {/* Follow 按钮（对照 BriefProfile handleFollowPress）*/}
      <TouchableOpacity
        style={[styles.button, hasFollowed ? styles.buttonFollowing : styles.buttonFollow]}
        onPress={handleFollowPress}
        activeOpacity={0.8}>
        <Text style={styles.buttonText}>
          {hasFollowed ? '✓ Following' : '+ Follow'}
        </Text>
      </TouchableOpacity>

      {/* 分割线 */}
      <View style={styles.divider} />

      {/* Bridge 演示区 */}
      <Text style={styles.sectionTitle}>Bridge 调用演示</Text>
      <Text style={styles.sectionDesc}>
        对照 KRN：yoda.invoke('YodaBridge.xxx', params){'\n'}
        下面两个按钮直接调 Android Native 方法
      </Text>

      {/* Bridge 按钮1：Toast */}
      <TouchableOpacity
        style={[styles.button, styles.buttonBridge]}
        onPress={handleNativeToast}
        activeOpacity={0.8}>
        <Text style={styles.buttonText}>📣 Call Native Toast</Text>
      </TouchableOpacity>

      {/* Bridge 按钮2：获取设备信息 */}
      <TouchableOpacity
        style={[styles.button, styles.buttonBridge]}
        onPress={handleGetDeviceInfo}
        activeOpacity={0.8}>
        <Text style={styles.buttonText}>📱 Get Device Info</Text>
      </TouchableOpacity>

      {/* 设备信息展示 */}
      {deviceInfo && (
        <View style={styles.infoCard}>
          <Text style={styles.infoTitle}>Native 返回的设备信息：</Text>
          <Text style={styles.infoText}>型号：{deviceInfo.model}</Text>
          <Text style={styles.infoText}>系统：{deviceInfo.os}</Text>
          <Text style={styles.infoText}>App 版本：{deviceInfo.appVersion}</Text>
        </View>
      )}

      {/* ★★★ Native Banner 控制区 ★★★ */}
      <View style={styles.divider} />
      <Text style={styles.sectionTitle}>🎯 控制 Native Banner（跨 Activity）</Text>
      <Text style={styles.sectionDesc}>
        以下操作会实时改变 MainActivity 顶部的蓝色 Banner{'\n'}
        原理：JS → Bridge → NativeBannerModule → ActivityHolder → View
      </Text>

      {/* 高度控制 */}
      <View style={styles.bannerRow}>
        <TouchableOpacity
          style={[styles.bannerBtn, { backgroundColor: '#1565C0' }]}
          onPress={async () => {
            appendLog('设置 Banner 高度 → 160dp...');
            try {
              await yoda.invoke('NativeBanner.setHeight', { height: 160 });
              appendLog('✅ Banner 高度 → 160dp');
            } catch (e) { appendLog(`❌ ${String(e)}`); }
          }}>
          <Text style={styles.bannerBtnText}>📏 放大 Banner</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.bannerBtn, { backgroundColor: '#37474F' }]}
          onPress={async () => {
            appendLog('设置 Banner 高度 → 40dp...');
            try {
              await yoda.invoke('NativeBanner.setHeight', { height: 40 });
              appendLog('✅ Banner 高度 → 40dp');
            } catch (e) { appendLog(`❌ ${String(e)}`); }
          }}>
          <Text style={styles.bannerBtnText}>📏 缩小 Banner</Text>
        </TouchableOpacity>
      </View>

      {/* 颜色控制 */}
      <View style={styles.bannerRow}>
        {[
          { color: '#E53935', label: '🔴 红' },
          { color: '#2E7D32', label: '🟢 绿' },
          { color: '#F57F17', label: '🟡 黄' },
          { color: '#6A1B9A', label: '🟣 紫' },
        ].map(({ color, label }) => (
          <TouchableOpacity
            key={color}
            style={[styles.colorBtn, { backgroundColor: color }]}
            onPress={async () => {
              appendLog(`设置 Banner 颜色 → ${color}...`);
              try {
                await yoda.invoke('NativeBanner.setColor', { color });
                appendLog(`✅ Banner 颜色 → ${color}`);
              } catch (e) { appendLog(`❌ ${String(e)}`); }
            }}>
            <Text style={styles.bannerBtnText}>{label}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* 标题控制 */}
      <TouchableOpacity
        style={[styles.button, { backgroundColor: '#00695C' }]}
        onPress={async () => {
          appendLog('修改 Banner 标题...');
          try {
            await yoda.invoke('NativeBanner.setTitle', {
              title: '🚀 由 RN 控制',
              subtitle: `hasFollowed=${hasFollowed}`,
            });
            appendLog('✅ Banner 标题已修改');
          } catch (e) { appendLog(`❌ ${String(e)}`); }
        }}>
        <Text style={styles.buttonText}>✏️ 修改 Banner 标题</Text>
      </TouchableOpacity>

      {/* 重置 */}
      <TouchableOpacity
        style={[styles.button, { backgroundColor: '#78909C' }]}
        onPress={async () => {
          appendLog('重置 Banner...');
          try {
            await yoda.invoke('NativeBanner.reset', {});
            appendLog('✅ Banner 已重置');
          } catch (e) { appendLog(`❌ ${String(e)}`); }
        }}>
        <Text style={styles.buttonText}>↩️ 重置 Banner</Text>
      </TouchableOpacity>

      {/* 日志区：显示 invoke 调用链路 */}
      <View style={styles.divider} />
      <Text style={styles.sectionTitle}>调用日志</Text>
      {log.map((entry, i) => (
        <Text key={i} style={styles.logEntry}>
          {entry}
        </Text>
      ))}

      <View style={styles.bottomPadding} />
    </ScrollView>
  );
};

// ─── 样式（对照 KRN 里的 StyleSheet.create，原理和 convertStyles(SrcStyles) 一样）─

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  header: {
    alignItems: 'center',
    paddingTop: 60,
    paddingBottom: 24,
    backgroundColor: '#16213e',
  },
  avatarPlaceholder: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#2d2d5e',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  avatarText: {
    fontSize: 36,
  },
  nickname: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 13,
    color: '#8888aa',
  },
  loadingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    gap: 8,
  },
  loadingText: {
    color: '#8888aa',
    fontSize: 13,
    marginLeft: 8,
  },
  button: {
    marginHorizontal: 20,
    marginVertical: 8,
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonFollow: {
    backgroundColor: '#8d5cff',
  },
  buttonFollowing: {
    backgroundColor: '#2d2d5e',
    borderWidth: 1,
    borderColor: '#8d5cff',
  },
  buttonBridge: {
    backgroundColor: '#0f3460',
    borderWidth: 1,
    borderColor: '#4488ff',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '600',
  },
  divider: {
    height: 1,
    backgroundColor: '#2d2d5e',
    marginHorizontal: 20,
    marginVertical: 16,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#ffffff',
    marginHorizontal: 20,
    marginBottom: 6,
  },
  sectionDesc: {
    fontSize: 12,
    color: '#6666aa',
    marginHorizontal: 20,
    marginBottom: 12,
    lineHeight: 18,
  },
  infoCard: {
    marginHorizontal: 20,
    marginTop: 8,
    padding: 14,
    backgroundColor: '#0d2137',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#4488ff',
  },
  infoTitle: {
    fontSize: 13,
    color: '#4488ff',
    fontWeight: 'bold',
    marginBottom: 8,
  },
  infoText: {
    fontSize: 13,
    color: '#ccccee',
    marginBottom: 4,
  },
  logEntry: {
    fontSize: 11,
    color: '#6688aa',
    marginHorizontal: 20,
    marginVertical: 2,
    fontFamily: 'monospace',
  },
  bottomPadding: {
    height: 40,
  },
  // ★ Native Banner 控制区样式
  bannerRow: {
    flexDirection: 'row',
    marginHorizontal: 20,
    marginBottom: 8,
    gap: 8,
  },
  bannerBtn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
  },
  colorBtn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
  },
  bannerBtnText: {
    color: '#ffffff',
    fontSize: 13,
    fontWeight: '600',
  },
});

export default HelloScreen;
