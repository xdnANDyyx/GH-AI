# Banner 系统前后台联动实现说明

## 实施时间
2026-07-27

## 功能概述
将后台管理系统的 Banner 管理功能与用户前台界面进行联动，管理员在后台编辑的 Banner 会实时展示在用户界面的轮播组件中。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      管理员后台                               │
│  http://101.43.66.196/admin/banner                          │
│                                                              │
│  - 新增/编辑 Banner                                          │
│  - 上传图片、设置标题、链接、描述                              │
│  - 状态管理（已发布/已下架/定时发布）                          │
│  - 排序设置                                                  │
│  - 点击趋势统计                                               │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   │ Spring Boot API (/business/banner/*)
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据库层                                 │
│                                                              │
│  - sys_banner 表                                             │
│  - sys_banner_click_log 表（点击统计）                        │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   │ 前台 API 调用
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│                      用户前台                                 │
│  http://101.43.66.196/whiteBg (及其他工作台页面)             │
│                                                              │
│  - BannerCarousel 组件（轮播展示）                            │
│  - 自动加载已发布的 Banner                                    │
│  - 点击跳转 + 统计                                            │
└─────────────────────────────────────────────────────────────┘
```

## 实现细节

### 1. 后台管理页面（已完成）

**文件**: `/home/beiming/guanghe-studio/src/views/admin/AdminBanner.vue`

**功能**:
- ✅ Banner CRUD 操作
- ✅ 图片上传（拖拽/点击）
- ✅ 状态管理（已发布/已下架/定时发布）
- ✅ 排序字段
- ✅ 点击趋势统计（柱状图，支持 7/30/90 天）
- ✅ 分页展示

**API 接口**:
```javascript
// 增删改查
POST   /business/banner              // 新增
PUT    /business/banner              // 编辑
DELETE /business/banner/{id}         // 删除
GET    /business/banner/list         // 列表
GET    /business/banner/{id}         // 详情

// 状态管理
PUT    /business/banner/status/{id}/{status}

// 统计
GET    /business/banner/clickTrend/{id}?range=7
POST   /business/banner/click/{id}  // 记录点击
```

### 2. 前台轮播组件（已完成）

**文件**: `/home/beiming/guanghe-studio/src/components/BannerCarousel.vue`

**功能**:
- ✅ 从后端 API 动态加载 Banner
- ✅ 只展示状态为"已发布"的 Banner
- ✅ 自动过滤定时发布（检查 publishTime）
- ✅ 按 sort 字段降序排序
- ✅ 支持点击跳转（内部路由/外部链接）
- ✅ 点击统计（调用 recordBannerClick API）
- ✅ 4 种主题色自动轮换（蓝/绿/橙/紫）
- ✅ 鼠标悬停缩放动画

**关键代码**:
```javascript
// 获取已发布的 Banner
async function fetchBanners() {
  const res = await getBannerList({
    pageNum: 1,
    pageSize: 10,
    status: '0' // 只获取已发布
  })
  banners.value = (res.rows || [])
    .filter(item => {
      // 过滤定时发布
      if (item.status === '2' && item.publishTime) {
        return new Date(item.publishTime) <= new Date()
      }
      return item.status === '0'
    })
    .sort((a, b) => (b.sort || 0) - (a.sort || 0))
}

// 点击事件
async function handleBannerClick(item) {
  // 记录点击
  await recordBannerClick(item.id)
  
  // 跳转
  if (item.linkUrl) {
    if (item.linkUrl.startsWith('http')) {
      window.open(item.linkUrl, '_blank')
    } else {
      window.location.href = item.linkUrl
    }
  }
}
```

### 3. 组件集成（已完成）

**文件**: `/home/beiming/guanghe-studio/src/components/StudioLayout.vue`

**修改内容**:
```vue
<script setup>
import BannerCarousel from '@/components/BannerCarousel.vue'
// ...其他导入
</script>

<template>
  <main class="main-area">
    <BannerCarousel />
    <router-view />
  </main>
</template>
```

**集成位置**: 所有用户工作台页面顶部
- AI白底图 (`/whiteBg`)
- 白底图生成背景 (`/whiteToBg`)
- 产品精修 (`/refine`)
- AI模特 (`/aiModel`)
- 主图设计 (`/mainImage`)
- 详情图设计 (`/detailImg`)
- Banner设计 (`/banner`)
- 尺寸标记 (`/size`)
- 批量处理 (`/batchProcess`)

### 4. API 接口文件（已完成）

**文件**: `/home/beiming/guanghe-studio/src/api/operation.js`

**新增接口**:
```javascript
// 记录 Banner 点击（新增）
export function recordBannerClick(id) {
  return request({
    url: `/business/banner/click/${id}`,
    method: 'post'
  })
}
```

## 数据流转示意

### 管理员发布 Banner 流程

```
1. 管理员访问 /admin/banner
   ↓
2. 点击"新增 Banner"
   ↓
3. 填写表单（标题、描述、上传图片、设置跳转链接）
   ↓
4. 设置排序（sort 字段，数值越大越靠前）
   ↓
5. 选择状态：
   - 已发布（立即在前台显示）
   - 定时发布（到达指定时间后显示）
   - 已下架（不在前台显示）
   ↓
6. 点击"确定"，调用 POST /business/banner
   ↓
7. 数据保存到 sys_banner 表
```

### 用户查看 Banner 流程

```
1. 用户访问任意工作台页面（如 /whiteBg）
   ↓
2. BannerCarousel 组件自动挂载
   ↓
3. onMounted 钩子调用 fetchBanners()
   ↓
4. 发送请求 GET /business/banner/list?status=0
   ↓
5. 后端返回已发布的 Banner 列表
   ↓
6. 前端过滤定时发布（检查 publishTime）
   ↓
7. 按 sort 降序排序
   ↓
8. 渲染轮播组件（自动轮播，5秒/张）
```

### 用户点击 Banner 流程

```
1. 用户点击 Banner 卡片
   ↓
2. 触发 handleBannerClick(item)
   ↓
3. 调用 POST /business/banner/click/{id} 记录点击
   ↓
4. 后端更新 sys_banner_click_log 表
   ↓
5. 根据 linkUrl 跳转：
   - 外部链接 → 新窗口打开
   - 内部路由 → 当前窗口跳转
```

## 数据库表设计

### sys_banner 表

```sql
CREATE TABLE `sys_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort` int DEFAULT 0 COMMENT '排序（数值越大越靠前）',
  `status` char(1) DEFAULT '0' COMMENT '状态(0已发布 1已下架 2定时发布)',
  `publish_time` datetime DEFAULT NULL COMMENT '定时发布时间',
  `click_count` int DEFAULT 0 COMMENT '总点击量',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort` DESC)
) ENGINE=InnoDB COMMENT='Banner管理表';
```

### sys_banner_click_log 表

```sql
CREATE TABLE `sys_banner_click_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `banner_id` bigint NOT NULL COMMENT 'Banner ID',
  `click_date` date NOT NULL COMMENT '点击日期',
  `click_count` int DEFAULT 0 COMMENT '当日点击量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_banner_date` (`banner_id`, `click_date`),
  KEY `idx_banner_id` (`banner_id`),
  KEY `idx_click_date` (`click_date`)
) ENGINE=InnoDB COMMENT='Banner点击统计表';
```

**说明**:
- `uk_banner_date` 唯一索引确保每个 Banner 每天只有一条记录
- 后端接口收到点击时，使用 `INSERT ... ON DUPLICATE KEY UPDATE` 累加点击量

## 后端接口实现要点

### 1. 获取前台 Banner 列表

```java
@GetMapping("/list")
public TableDataInfo list(SysBanner query) {
    startPage();
    List<SysBanner> list = bannerService.selectBannerList(query);
    return getDataTable(list);
}
```

**SQL 查询逻辑**:
```sql
SELECT * FROM sys_banner
WHERE status = '0'  -- 只返回已发布
  OR (status = '2' AND publish_time <= NOW())  -- 或定时发布时间已到
ORDER BY sort DESC, create_time DESC
```

### 2. 记录 Banner 点击

```java
@PostMapping("/click/{id}")
public AjaxResult recordClick(@PathVariable Long id) {
    // 1. 更新 sys_banner.click_count += 1
    bannerService.incrementClickCount(id);
    
    // 2. 更新 sys_banner_click_log（按日期累加）
    LocalDate today = LocalDate.now();
    bannerClickLogService.insertOrUpdate(id, today);
    
    return AjaxResult.success();
}
```

**SQL 实现**:
```sql
-- 更新总点击量
UPDATE sys_banner SET click_count = click_count + 1 WHERE id = ?

-- 更新每日点击量（使用 ON DUPLICATE KEY）
INSERT INTO sys_banner_click_log (banner_id, click_date, click_count)
VALUES (?, ?, 1)
ON DUPLICATE KEY UPDATE click_count = click_count + 1
```

### 3. 获取点击趋势

```java
@GetMapping("/clickTrend/{id}")
public AjaxResult getClickTrend(@PathVariable Long id, @RequestParam String range) {
    int days = "7".equals(range) ? 7 : ("30".equals(range) ? 30 : 90);
    LocalDate startDate = LocalDate.now().minusDays(days - 1);
    
    List<BannerClickLog> logs = bannerClickLogService.selectByDateRange(id, startDate, LocalDate.now());
    
    return AjaxResult.success(logs);
}
```

## 前端样式主题

BannerCarousel 支持 4 种渐变主题色：

| 主题 | 背景渐变 | 标题颜色 | 描述颜色 |
|------|---------|---------|---------|
| theme-blue | #EFF6FF → #DBEAFE | #1E40AF | #3B82F6 |
| theme-green | #F0FDF4 → #DCFCE7 | #166534 | #22C55E |
| theme-orange | #FFF7ED → #FED7AA | #9A3412 | #EA580C |
| theme-purple | #FAF5FF → #E9D5FF | #6B21A8 | #9333EA |

主题色根据 `item.id % 4` 自动分配，确保同一 Banner 主题色一致。

## 使用场景示例

### 场景 1：发布新品促销 Banner

**管理员操作**:
1. 访问 `http://101.43.66.196/admin/banner`
2. 点击"新增 Banner"
3. 填写信息：
   - 标题：双11大促 全场5折
   - 描述：限时优惠，先到先得
   - 上传促销海报图片
   - 跳转链接：https://example.com/promo
   - 排序：100（高优先级）
   - 状态：已发布
4. 点击"确定"

**用户看到**:
- 所有工作台页面顶部立即显示新 Banner
- 点击后跳转到促销页面
- 后台记录点击数据

### 场景 2：定时发布功能上线公告

**管理员操作**:
1. 新增 Banner，标题："新功能上线：AI模特生成"
2. 状态：定时发布
3. 发布时间：2026-07-28 00:00:00
4. 保存

**系统行为**:
- 2026-07-28 00:00:00 之前：前台不显示
- 2026-07-28 00:00:00 之后：自动在前台轮播中出现

### 场景 3：下架过期活动

**管理员操作**:
1. 在 Banner 列表中找到已过期的活动 Banner
2. 点击"下架"按钮
3. 状态变为"已下架"

**用户看到**:
- 该 Banner 立即从轮播中消失
- 不影响其他 Banner 的展示

## 测试清单

### 后台管理测试

- [ ] 新增 Banner（上传图片、填写信息、选择状态）
- [ ] 编辑 Banner（修改标题、图片、链接等）
- [ ] 删除 Banner（二次确认）
- [ ] 状态切换（已发布 ↔ 已下架）
- [ ] 定时发布（设置未来时间，确认到时自动发布）
- [ ] 排序功能（sort 字段控制展示顺序）
- [ ] 点击趋势图（7/30/90天数据，柱状图展示）
- [ ] 分页功能（前后翻页、修改每页条数）

### 前台展示测试

- [ ] 访问工作台页面，确认顶部显示 Banner 轮播
- [ ] 轮播自动切换（5秒间隔）
- [ ] 只显示已发布的 Banner（已下架的不显示）
- [ ] 定时发布的 Banner 在时间到达后自动显示
- [ ] 按 sort 字段排序（数值大的靠前）
- [ ] 点击 Banner 正确跳转（内部路由/外部链接）
- [ ] 点击后正确记录统计（后台点击量 +1）
- [ ] 鼠标悬停有缩放动画
- [ ] 4 种主题色正确轮换

### 联动测试

- [ ] 后台新增 Banner → 前台立即显示
- [ ] 后台修改 Banner 标题 → 刷新前台页面后标题更新
- [ ] 后台下架 Banner → 前台立即消失
- [ ] 后台调整排序 → 前台展示顺序改变
- [ ] 前台点击 Banner → 后台点击量增加
- [ ] 后台查看点击趋势 → 数据与前台点击一致

### 异常处理测试

- [ ] 后端接口返回错误 → 前台静默失败，不影响页面
- [ ] Banner 列表为空 → 轮播组件不显示（v-if 控制）
- [ ] 图片 URL 失效 → 显示占位图或隐藏
- [ ] linkUrl 为空 → 点击无响应（不跳转）

## 注意事项

1. **图片 URL**: 确保上传的图片 URL 可以被前台访问（建议使用 OSS/CDN）
2. **定时任务**: 如果使用定时发布，前台每次刷新时会检查 publishTime，无需后端定时任务
3. **点击统计**: 点击记录为异步调用，失败不影响跳转
4. **缓存策略**: Banner 数据可以考虑添加缓存（Redis），减少数据库查询
5. **权限控制**: 后台管理接口需要验证管理员权限
6. **图片规格**: 建议 Banner 图片尺寸 200x120px，保证显示效果

## 后续优化建议

1. **位置管理**: 添加 position 字段（首页/工作流/积分页），不同位置展示不同 Banner
2. **A/B 测试**: 支持多个 Banner 随机展示，对比点击率
3. **用户画像**: 根据用户标签展示个性化 Banner
4. **曝光统计**: 除了点击，还统计 Banner 的曝光次数（PV）
5. **拖拽排序**: 后台管理页面支持拖拽调整 Banner 顺序
6. **批量操作**: 支持批量发布/下架/删除
7. **前台刷新**: 添加定时刷新或 WebSocket 推送，无需用户手动刷新

## 文件清单

### 前端文件（guanghe-studio）

```
guanghe-studio/
├── src/
│   ├── components/
│   │   ├── AdminLayout.vue          # 管理后台布局（已修改）
│   │   ├── StudioLayout.vue         # 用户工作台布局（已修改）
│   │   └── BannerCarousel.vue       # Banner轮播组件（已修改）
│   ├── router/
│   │   └── index.js                 # 路由配置（已修改）
│   ├── api/
│   │   └── operation.js             # 运营中心API（已修改）
│   └── views/
│       └── admin/
│           ├── AdminBanner.vue      # Banner管理页面（新建）
│           └── AdminMaterial.vue    # 素材管理页面（新建）
```

### 后端文件（待实现）

```
ruoyi-business/
├── src/main/java/com/ruoyi/business/
│   ├── controller/
│   │   └── SysBannerController.java         # Banner控制器
│   ├── service/
│   │   ├── ISysBannerService.java           # Banner服务接口
│   │   ├── impl/
│   │   │   └── SysBannerServiceImpl.java    # Banner服务实现
│   │   ├── ISysBannerClickLogService.java   # 点击日志服务接口
│   │   └── impl/
│   │       └── SysBannerClickLogServiceImpl.java
│   ├── mapper/
│   │   ├── SysBannerMapper.java             # Banner Mapper
│   │   └── SysBannerClickLogMapper.java     # 点击日志 Mapper
│   └── domain/
│       ├── SysBanner.java                   # Banner实体
│       └── SysBannerClickLog.java           # 点击日志实体
└── src/main/resources/mapper/business/
    ├── SysBannerMapper.xml                  # Banner SQL映射
    └── SysBannerClickLogMapper.xml          # 点击日志 SQL映射
```

## 部署说明

1. **前端部署**: guanghe-studio Vite dev server 已在端口 3000 运行，所有改动已通过 HMR 生效
2. **数据库迁移**: 执行 SQL 脚本创建 `sys_banner` 和 `sys_banner_click_log` 表
3. **后端部署**: 实现 Spring Boot 接口，重启 ruoyi-admin 服务
4. **验证**: 访问 `http://101.43.66.196/admin/banner` 测试后台管理，访问 `http://101.43.66.196/whiteBg` 测试前台展示

---

**实现状态**: ✅ 前端完成，等待后端接口对接
