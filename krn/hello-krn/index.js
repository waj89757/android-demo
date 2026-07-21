/**
 * ★★★ RN 入口文件 ★★★
 *
 * AppRegistry.registerComponent 是 RN 的入口注册机制：
 *   第一个参数 'HelloKRN' = 组件名，必须和 Android 侧的
 *   ReactRootView.startReactApplication(manager, "HelloKRN", ...) 一致
 *
 * 类比：
 *   KRN 里的 componentName=KleaoBriefProfile 就是这个注册名
 *   Android 调 bundleId/componentName URL 打开页面，就是通过这个名字找到组件
 */
import { AppRegistry } from 'react-native';
import HelloScreen from './src/HelloScreen';

AppRegistry.registerComponent('HelloKRN', () => HelloScreen);
