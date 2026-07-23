import {
  H1, H2, Row, Stack, Text,
  useHostTheme,
} from 'codeflicker/canvas';

export default function AndroidProfileFigma() {
  const { tokens } = useHostTheme();

  const phoneBg    = tokens.bg.chrome;
  const cardBg     = tokens.bg.elevated;
  const borderClr  = tokens.stroke.tertiary;
  const accentClr  = tokens.accent.primary;
  const textPri    = tokens.text.primary;
  const textSec    = tokens.text.secondary;
  const textTer    = tokens.text.tertiary;
  const fillBg     = tokens.fill.tertiary;

  const phoneShell: React.CSSProperties = {
    width: 360,
    minHeight: 720,
    background: phoneBg,
    borderRadius: 32,
    border: `2px solid ${borderClr}`,
    overflow: 'hidden',
    fontFamily: 'sans-serif',
    margin: '0 auto',
  };

  const statusBar: React.CSSProperties = {
    height: 28,
    background: cardBg,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 20px',
  };

  const navbar: React.CSSProperties = {
    height: 52,
    background: cardBg,
    display: 'flex',
    alignItems: 'center',
    padding: '0 16px',
    gap: 12,
    borderBottom: `1px solid ${borderClr}`,
  };

  const scrollBody: React.CSSProperties = {
    overflowY: 'auto',
    padding: '0 0 24px 0',
    background: phoneBg,
  };

  const banner: React.CSSProperties = {
    height: 100,
    background: fillBg,
    position: 'relative',
  };

  const avatarWrap: React.CSSProperties = {
    position: 'absolute',
    bottom: -36,
    left: 20,
    width: 72,
    height: 72,
    borderRadius: '50%',
    border: `3px solid ${phoneBg}`,
    background: accentClr,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  };

  const editBtn: React.CSSProperties = {
    position: 'absolute',
    bottom: -28,
    right: 16,
    padding: '6px 14px',
    borderRadius: 20,
    border: `1.5px solid ${accentClr}`,
    color: accentClr,
    fontSize: 12,
    fontWeight: 600,
    background: 'transparent',
    cursor: 'pointer',
  };

  const section: React.CSSProperties = {
    margin: '12px 12px 0',
    background: cardBg,
    borderRadius: 12,
    overflow: 'hidden',
    border: `1px solid ${borderClr}`,
  };

  const rowBase: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    padding: '13px 16px',
    gap: 10,
  };

  const rowStyle: React.CSSProperties = { ...rowBase, borderBottom: `1px solid ${borderClr}` };
  const rowLastStyle: React.CSSProperties = { ...rowBase };

  const statDivider: React.CSSProperties = {
    width: 1,
    background: borderClr,
    alignSelf: 'stretch',
    margin: '10px 0',
  };

  const infoRows = [
    ['手机号', '138****8888'],
    ['邮箱', 'wanganjie@example.com'],
    ['所在地区', '北京市'],
  ];

  const settingRows = ['消息通知', '隐私设置', '退出登录'];

  return (
    <Stack gap={0} style={{ padding: 0 }}>
      <div style={{ display: 'flex', gap: 40, alignItems: 'flex-start' }}>

        {/* ── 左栏：说明 ── */}
        <div style={{ flex: 1, minWidth: 260 }}>
          <H1>作业：用户个人资料页</H1>
          <Text tone="secondary" size="small">Android UI 布局练习 · Figma 设计稿</Text>

          <div style={{ marginTop: 24 }}>
            <H2>页面结构（从上到下）</H2>
            <Stack gap={8} style={{ marginTop: 10 }}>
              {([
                ['状态栏', '系统级，固定 28dp，不需要自己写'],
                ['Toolbar 导航栏', '返回箭头 + 标题 + 更多按钮，高 52dp'],
                ['Banner 背景图', '纯色占位，高 100dp'],
                ['圆形头像', '72dp，叠在 Banner 底部，ConstraintLayout 负偏移'],
                ['编辑按钮', '描边圆角按钮，绝对定位在右侧'],
                ['用户名 + 简介', '两行文字，TextAppearance 控制字号'],
                ['统计数字行', '关注 / 粉丝 / 获赞，LinearLayout 横向等宽三格'],
                ['个人信息列表', '手机 / 邮箱 / 地区，带右箭头 ›'],
                ['设置列表', '通知 / 隐私 / 退出登录'],
              ] as [string, string][]).map(([title, desc]) => (
                <div key={title} style={{ display: 'flex', gap: 8, alignItems: 'flex-start' }}>
                  <div style={{ width: 6, height: 6, borderRadius: '50%', background: accentClr, marginTop: 6, flexShrink: 0 }} />
                  <div>
                    <Text size="small" style={{ fontWeight: 600, color: textPri }}>{title}</Text>
                    <Text size="small" tone="secondary">{desc}</Text>
                  </div>
                </div>
              ))}
            </Stack>
          </div>

          <div style={{ marginTop: 24 }}>
            <H2>需要掌握的布局知识</H2>
            <Stack gap={6} style={{ marginTop: 10 }}>
              {[
                'ConstraintLayout — 头像叠 Banner（bias + 负 margin）',
                'LinearLayout 横向 — 统计三格 weight="1" 等宽',
                'CardView — 圆角卡片容器',
                'Toolbar — 顶部导航栏，setSupportActionBar',
                'ImageView + ShapeAppearance — 圆形裁剪头像',
                'dp vs sp — 间距用 dp，字号用 sp',
                'include / merge — 可复用的列表行布局',
              ].map((item) => (
                <Row key={item} gap={8} style={{ alignItems: 'flex-start' }}>
                  <div style={{ width: 4, height: 4, borderRadius: '50%', background: textTer, marginTop: 8, flexShrink: 0 }} />
                  <Text size="small">{item}</Text>
                </Row>
              ))}
            </Stack>
          </div>
        </div>

        {/* ── 右栏：手机预览 ── */}
        <div style={{ flexShrink: 0 }}>
          <Text size="small" tone="secondary" style={{ display: 'block', textAlign: 'center', marginBottom: 8 }}>
            UI 效果预览（360dp 手机尺寸）
          </Text>

          <div style={phoneShell}>

            {/* 状态栏 */}
            <div style={statusBar}>
              <span style={{ fontSize: 11, color: textSec }}>9:41</span>
              <span style={{ fontSize: 11, color: textSec }}>100%</span>
            </div>

            {/* 导航栏 */}
            <div style={navbar}>
              <span style={{ fontSize: 18, color: textPri }}>←</span>
              <span style={{ flex: 1, fontWeight: 600, fontSize: 15, color: textPri }}>个人资料</span>
              <span style={{ fontSize: 18, color: textPri }}>⋯</span>
            </div>

            {/* 内容区 */}
            <div style={scrollBody}>

              {/* Banner + 头像 + 编辑按钮 */}
              <div style={{ position: 'relative', marginBottom: 44 }}>
                <div style={banner} />
                <div style={avatarWrap}>
                  <span style={{ fontSize: 28, color: 'white', fontWeight: 700 }}>王</span>
                </div>
                <button style={editBtn}>编辑资料</button>
              </div>

              {/* 用户名 + 简介 */}
              <div style={{ padding: '0 16px' }}>
                <div style={{ fontSize: 18, fontWeight: 700, color: textPri }}>王安杰</div>
                <div style={{ fontSize: 13, color: textSec, marginTop: 4 }}>技术负责人 · 海外增长团队</div>
                <div style={{ fontSize: 12, color: textTer, marginTop: 6 }}>学习 Android · 热爱技术 · 持续成长</div>
              </div>

              {/* 统计数字 */}
              <div style={{ ...section, display: 'flex', alignItems: 'stretch', marginTop: 14 }}>
                {(['128', '3.2K', '1.8W'] as const).map((val, i) => (
                  <>
                    {i > 0 && <div key={`d${i}`} style={statDivider} />}
                    <div key={val} style={{ flex: 1, textAlign: 'center', padding: '14px 0' }}>
                      <div style={{ fontSize: 20, fontWeight: 700, color: textPri, lineHeight: 1.2 }}>{val}</div>
                      <div style={{ fontSize: 11, color: textSec, marginTop: 2 }}>
                        {['关注', '粉丝', '获赞'][i]}
                      </div>
                    </div>
                  </>
                ))}
              </div>

              {/* 个人信息 */}
              <div style={{ ...section, marginTop: 12 }}>
                <div style={{ padding: '10px 16px 6px', fontSize: 11, color: textTer, letterSpacing: 0.5 }}>个人信息</div>
                {infoRows.map(([label, val], i) => (
                  <div key={label} style={i === infoRows.length - 1 ? rowLastStyle : rowStyle}>
                    <span style={{ flex: 1, fontSize: 14, color: textPri }}>{label}</span>
                    <span style={{ fontSize: 13, color: textSec }}>{val}</span>
                    <span style={{ fontSize: 14, color: textTer }}>›</span>
                  </div>
                ))}
              </div>

              {/* 设置 */}
              <div style={{ ...section, marginTop: 12 }}>
                <div style={{ padding: '10px 16px 6px', fontSize: 11, color: textTer, letterSpacing: 0.5 }}>设置</div>
                {settingRows.map((label, i) => (
                  <div key={label} style={i === settingRows.length - 1 ? rowLastStyle : rowStyle}>
                    <span style={{ flex: 1, fontSize: 14, color: label === '退出登录' ? '#e05252' : textPri }}>{label}</span>
                    {label !== '退出登录' && <span style={{ fontSize: 14, color: textTer }}>›</span>}
                  </div>
                ))}
              </div>

            </div>
          </div>
        </div>

      </div>
    </Stack>
  );
}
