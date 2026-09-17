# Canvas Editor 后端API开发进度

**开始时间**: 2026-09-07
**状态**: 🟡 API框架已完成，业务逻辑待实现

## ✅ 已完成

### 1. API框架 (100%)

- ✅ Controller层: `CanvasEditorController.java`
  - 9个RESTful端点
  - 统一的请求/响应格式
  - 完整的参数验证
  - 错误处理

- ✅ Service层: `CanvasEditorService.java`
  - 9个服务方法
  - TODO标记待实现
  - 返回模拟数据

### 2. API端点清单

| # | API | 方法 | 路径 | Controller | Service | 业务逻辑 |
|---|-----|------|------|-----------|---------|---------|
| 1 | 图片扩图 | POST | `/ai/image/extend` | ✅ | ✅ | ⏳ 待实现 |
| 2 | 多角度生成 | POST | `/ai/image/multi-angle` | ✅ | ✅ | ⏳ 待实现 |
| 3 | 修改文字 | POST | `/ai/image/edit-text` | ✅ | ✅ | ⏳ 待实现 |
| 4 | 局部重绘 | POST | `/ai/image/partial-redraw` | ✅ | ✅ | ⏳ 待实现 |
| 5 | 图层检测 | POST | `/ai/image/detect-layers` | ✅ | ✅ | ⏳ 待实现 |
| 6 | 下载图层 | POST | `/ai/image/download-layers` | ✅ | ✅ | ⏳ 待实现 |
| 7 | OCR识别 | POST | `/ai/image/ocr` | ✅ | ✅ | ⏳ 待实现 |
| 8 | 编辑历史 | GET | `/ai/image/{id}/history` | ✅ | ✅ | ⏳ 待实现 |
| 9 | 撤销编辑 | POST | `/ai/image/revert` | ✅ | ✅ | ⏳ 待实现 |

## ⏳ 待完成

### 3. 业务逻辑实现 (0%)

每个API的业务逻辑需要：

#### 3.1 图片扩图 (extendImage)
**难度**: ⭐⭐⭐
**预计时间**: 4-6小时

**实现方案**:
1. 下载原图
2. 计算新画布尺寸（根据ratio/width/height）
3. 使用Vertex AI gemini-3-pro-image进行外绘（outpainting）
4. 返回扩展后的图片URL

**技术要点**:
- 画布扩展算法
- Vertex AI图片生成API
- 图片上传存储

---

#### 3.2 多角度生成 (generateMultiAngle)
**难度**: ⭐⭐⭐⭐
**预计时间**: 6-8小时

**实现方案**:
1. 下载原图
2. 根据type生成对应角度的提示词
3. 并行调用Vertex AI生成多张图片
4. 返回图片URL列表

**技术要点**:
- 角度计算
- 并行请求处理
- 结果去重和筛选

---

#### 3.3 修改文字 (editImageText)
**难度**: ⭐⭐⭐⭐⭐
**预计时间**: 8-12小时

**实现方案**:
1. 调用OCR API识别文字位置
2. 使用Vertex AI inpainting替换文字
3. 保持字体风格一致
4. 返回修改后的图片

**技术要点**:
- OCR文字识别
- Inpainting算法
- 字体样式匹配

---

#### 3.4 局部重绘 (partialRedraw)
**难度**: ⭐⭐⭐⭐
**预计时间**: 6-8小时

**实现方案**:
1. 解析mask区域坐标
2. 构建Inpainting提示词
3. 调用Vertex AI进行局部重绘
4. 返回重绘后的图片

**技术要点**:
- Mask区域处理
- Inpainting算法
- 上下文保持

---

#### 3.5 图层检测 (detectLayers)
**难度**: ⭐⭐⭐⭐⭐
**预计时间**: 8-12小时

**实现方案**:
1. 使用计算机视觉算法分析图片
2. 或调用第三方图层分离服务
3. 返回每个图层的边界和类型

**技术要点**:
- 图像分割算法
- 图层边界检测
- 图层分类

---

#### 3.6 下载图层 (downloadLayers)
**难度**: ⭐⭐⭐
**预计时间**: 4-6小时

**实现方案**:
1. 根据图层ID分离图片
2. 打包成ZIP文件
3. 返回下载链接

**技术要点**:
- 图片裁剪
- ZIP打包
- 文件存储

---

#### 3.7 OCR识别 (recognizeText)
**难度**: ⭐⭐⭐
**预计时间**: 3-4小时

**实现方案**:
1. 调用Vertex AI或第三方OCR服务
2. 返回识别到的文字和位置

**技术要点**:
- OCR集成
- 文字定位
- 置信度计算

---

#### 3.8 编辑历史 (getEditHistory)
**难度**: ⭐⭐
**预计时间**: 2-3小时

**实现方案**:
1. 创建canvas_edit_history数据库表
2. 记录每次编辑操作
3. 返回历史记录列表

**技术要点**:
- 数据库设计
- 历史记录查询

---

#### 3.9 撤销编辑 (revertImage)
**难度**: ⭐⭐⭐
**预计时间**: 3-4小时

**实现方案**:
1. 从历史记录中获取指定版本
2. 恢复图片到该版本
3. 返回恢复后的图片URL

**技术要点**:
- 版本管理
- 图片快照

## 📊 总体估算

| 阶段 | 预计时间 | 难度 |
|------|---------|------|
| API框架 | ✅ 已完成 | - |
| 业务逻辑实现 | 44-63小时 | ⭐⭐⭐~⭐⭐⭐⭐⭐ |
| 测试 | 8-12小时 | - |
| **总计** | **52-75小时** | - |

**预计工期**: 约7-10个工作日

## 🎯 优先级建议

### P0 (必须实现)
1. ✅ API框架（已完成）
2. ⏳ 图片扩图（最常用）
3. ⏳ 多角度生成（最常用）
4. ⏳ 下载图层（已实现基础功能）
5. ⏳ 删除/下载/图层调整（已实现）

### P1 (重要)
6. ⏳ OCR识别（支持改文字功能）
7. ⏳ 局部重绘
8. ⏳ 编辑历史
9. ⏳ 撤销编辑

### P2 (增强)
10. ⏳ 图层检测（技术难度较高）
11. ⏳ 修改文字（依赖OCR）

## 🔧 技术依赖

### 已实现
- ✅ Vertex AI集成（参考CustomerAiImageService）
- ✅ 图片下载
- ✅ 图片上传
- ✅ 数据库访问（JdbcTemplate）

### 待实现
- ⏳ 图像处理库（ImageIO已可用）
- ⏳ Inpainting算法
- ⏳ OCR集成
- ⏳ 图层分割算法

## 📝 数据库设计

### canvas_edit_history表

```sql
CREATE TABLE `canvas_edit_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_id` varchar(100) NOT NULL COMMENT '图片ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `operation` varchar(50) NOT NULL COMMENT '操作类型',
  `params` json DEFAULT NULL COMMENT '操作参数',
  `result_url` varchar(500) DEFAULT NULL COMMENT '结果URL',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_image_id` (`image_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 🚀 下一步

1. **立即可做**:
   - 实现图片扩图功能
   - 实现多角度生成功能
   - 实现下载图层功能

2. **短期目标**（本周）:
   - 完成P0功能（扩图、多角度）
   - 完成基础测试

3. **中期目标**（下周）:
   - 完成P1功能
   - 完成全部测试

4. **长期目标**:
   - 完成P2功能
   - 性能优化
   - 文档完善

---

**最后更新**: 2026-09-07
**负责人**: Claude Code
**状态**: 🟡 API框架完成，业务逻辑待实现
