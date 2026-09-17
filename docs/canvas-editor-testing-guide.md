# Canvas Editor 测试指南

**更新时间**: 2026-09-07
**适用范围**: Canvas Editor 后端API和业务逻辑测试

---

## 📋 测试概览

### 测试范围

- ✅ 单元测试 (Unit Tests)
- ⏳ 集成测试 (Integration Tests)
- ⏳ 端到端测试 (E2E Tests)

### 测试环境配置

#### 1. 配置文件

在 `application.yml` 或 `application-test.yml` 中添加Canvas Editor配置：

```yaml
# Vertex AI 配置
vertex:
  ai:
    project-id: your-project-id
    location: global
    model: gemini-3-pro-image
    read-timeout: 600
    credentials-path: /path/to/credentials.json

    # 代理配置（如果需要）
    proxy:
      host: your-proxy-host
      port: 8080

# Canvas Editor 配置
canvas-editor:
  max-image-size: 10485760  # 10MB
  compression-quality: 0.8
  max-retries: 3
```

#### 2. 环境变量（可选）

```bash
# Google Vertex AI
export VERTEX_AI_PROJECT_ID=your-project-id
export VERTEX_AI_CREDENTIALS_PATH=/path/to/credentials.json

# 代理配置
export HTTPS_PROXY_HOST=your-proxy-host
export HTTPS_PROXY_PORT=8080
```

---

## 🧪 单元测试

### 运行测试

```bash
# 运行所有CanvasEditorService测试
mvn test -Dtest=CanvasEditorServiceTest

# 运行单个测试方法
mvn test -Dtest=CanvasEditorServiceTest#testSaveEditHistory

# 生成测试覆盖率报告
mvn test -Dtest=CanvasEditorServiceTest jacoco:report
```

### 测试用例清单

#### 编辑历史测试 ✅

- [x] `testSaveEditHistory` - 保存编辑历史
- [x] `testGetNextVersion` - 获取下一个版本号（有历史）
- [x] `testGetNextVersionWhenNoHistory` - 获取下一个版本号（无历史）
- [x] `testGetEditHistory` - 查询编辑历史

#### 撤销编辑测试 ✅

- [x] `testRevertImageSuccess` - 成功撤销到指定版本
- [x] `testRevertImageNotFound` - 撤销不存在的版本

#### 图片处理测试 ✅

- [x] `testDetectMimeType` - MIME类型检测
- [x] `testValidateImageBytes` - 图片验证
- [x] `testCompressImageNullInput` - 压缩空输入
- [x] `testCompressImageEmptyInput` - 压缩空数组
- [x] `testCompressImageSmallImage` - 小图片不压缩

#### 提示词构建测试 ✅

- [x] `testBuildExtendPrompt` - 构建扩图提示词（带尺寸）
- [x] `testBuildExtendPromptWithoutDimensions` - 构建扩图提示词（不带尺寸）
- [x] `testBuildMultiAnglePromptRotate` - 构建旋转角度提示词
- [x] `testBuildMultiAnglePromptMultiView` - 构建多视角提示词
- [x] `testBuildMultiAnglePrompt360` - 构建360度提示词
- [x] `testGetAngleDescriptionsRotate` - 获取旋转角度描述
- [x] `testGetAngleDescriptionsMultiView` - 获取多视角描述
- [x] `testGetAngleDescriptions360` - 获取360度描述
- [x] `testBuildEditTextPrompt` - 构建修改文字提示词（带字体）
- [x] `testBuildEditTextPromptWithoutFont` - 构建修改文字提示词（不带字体）
- [x] `testBuildPartialRedrawPromptWithMask` - 构建局部重绘提示词（带mask）
- [x] `testBuildPartialRedrawPromptWithoutMask` - 构建局部重绘提示词（不带mask）

#### 重试机制测试 ✅

- [x] `testExecuteWithRetrySuccess` - 重试成功
- [x] `testExecuteWithRetryAllFailures` - 所有重试失败
- [x] `testIsTimeoutError` - 超时错误判断
- [x] `testIsRateLimitError` - 限流错误判断
- [x] `testIsServerError` - 服务端错误判断

#### 待实现测试 ⏳

- [ ] `testExtendImage` - 图片扩图集成测试
- [ ] `testGenerateMultiAngle` - 多角度生成集成测试
- [ ] `testEditImageText` - 修改文字集成测试
- [ ] `testPartialRedraw` - 局部重绘集成测试
- [ ] `testDetectLayers` - 图层检测集成测试
- [ ] `testDownloadLayers` - 下载图层集成测试
- [ ] `testRecognizeText` - OCR识别集成测试

---

## 🔗 集成测试

### 使用Postman/Insomnia测试API

#### 1. 导入集合

我们提供了一个Postman集合示例：

```json
{
  "info": {
    "name": "Canvas Editor API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "图片扩图",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\"imageUrl\": \"https://example.com/image.jpg\", \"ratio\": \"16:9\", \"width\": 1920, \"height\": 1080}"
        },
        "url": {
          "raw": "http://localhost:8080/customer/ai/image/extend",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["customer", "ai", "image", "extend"]
        }
      }
    }
  ]
}
```

### 使用curl测试

#### 图片扩图

```bash
curl -X POST http://localhost:8080/customer/ai/image/extend \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/image.jpg",
    "ratio": "16:9",
    "width": 1920,
    "height": 1080
  }'
```

#### 多角度生成

```bash
curl -X POST http://localhost:8080/customer/ai/image/multi-angle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "count": 4,
    "type": "rotate"
  }'
```

#### 修改文字

```bash
curl -X POST http://localhost:8080/customer/ai/image/edit-text \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/text-image.jpg",
    "originalText": "旧文字",
    "newText": "新文字",
    "font": "Arial"
  }'
```

#### 局部重绘

```bash
curl -X POST http://localhost:8080/customer/ai/image/partial-redraw \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/image.jpg",
    "description": "将这部分改为红色",
    "mask": {
      "x": 100,
      "y": 100,
      "width": 200,
      "height": 200
    }
  }'
```

#### OCR识别

```bash
curl -X POST http://localhost:8080/customer/ai/image/ocr \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/text-image.jpg"
  }'
```

#### 图层检测

```bash
curl -X POST http://localhost:8080/customer/ai/image/detect-layers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/complex-image.jpg"
  }'
```

#### 下载图层

```bash
curl -X POST http://localhost:8080/customer/ai/image/download-layers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageUrl": "https://example.com/complex-image.jpg",
    "layers": ["layer_1", "layer_2", "layer_3"],
    "format": "png"
  }'
```

#### 查询编辑历史

```bash
curl -X GET "http://localhost:8080/customer/ai/image/history/test-image-id" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 撤销编辑

```bash
curl -X POST http://localhost:8080/customer/ai/image/revert \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "imageId": "test-image-id",
    "version": 2
  }'
```

---

## 📊 性能测试

### 使用JMeter

#### 测试计划

1. **并发测试**
   - 模拟10个用户同时调用图片扩图API
   - 响应时间应该 < 30s
   - 错误率应该 < 5%

2. **压力测试**
   - 逐步增加并发用户数：10, 50, 100, 200
   - 记录吞吐量和响应时间
   - 找出系统瓶颈

3. **稳定性测试**
   - 持续运行1小时
   - 监控内存和CPU使用率
   - 检查是否有内存泄漏

### 使用Apache Bench

```bash
# 测试图片扩图API
ab -n 100 -c 10 -H "Authorization: Bearer YOUR_TOKEN" \
   -H "Content-Type: application/json" \
   -p test-data/extend.json \
   http://localhost:8080/customer/ai/image/extend
```

---

## 🔍 测试数据

### 测试图片

建议准备以下测试图片：

1. **小图片** (< 1MB) - 测试正常流程
2. **中等图片** (1-5MB) - 测试压缩
3. **大图片** (> 5MB) - 测试压缩和超时
4. **PNG图片** - 测试PNG格式
5. **JPEG图片** - 测试JPEG格式
6. **WebP图片** - 测试WebP格式
7. **Data URI** - 测试base64编码

### 测试数据文件

创建 `src/test/resources/test-data/` 目录，存放测试数据：

```
test-data/
├── extend.json          # 图片扩图测试数据
├── multi-angle.json     # 多角度生成测试数据
├── edit-text.json       # 修改文字测试数据
├── partial-redraw.json  # 局部重绘测试数据
├── ocr.json             # OCR识别测试数据
├── layers.json          # 图层检测测试数据
└── download-layers.json # 下载图层测试数据
```

---

## 📈 测试覆盖率目标

| 模块 | 行覆盖率 | 分支覆盖率 | 状态 |
|------|---------|-----------|------|
| CanvasEditorService | 80% | 70% | ⏳ |
| CanvasEditorController | 90% | 80% | ⏳ |

### 生成覆盖率报告

```bash
mvn clean test jacoco:report
# 报告生成在: target/site/jacoco/index.html
```

---

## 🐛 常见问题

### 1. Google认证失败

**问题**: `Vertex AI 认证失败`

**解决**:
- 检查 `credentials-path` 是否正确
- 检查凭证文件是否有效
- 检查项目ID是否正确
- 检查网络代理配置

### 2. 图片下载超时

**问题**: `下载图片失败: timeout`

**解决**:
- 检查图片URL是否可访问
- 增加超时时间配置
- 检查网络连接

### 3. Vertex AI返回429

**问题**: `HTTP 429 Too Many Requests`

**解决**:
- 系统会自动重试（3次）
- 检查配额是否用完
- 联系管理员增加配额

### 4. 数据库连接失败

**问题**: `保存编辑历史失败`

**解决**:
- 检查数据库配置
- 表是否已创建（会自动创建）
- 检查用户权限

---

## 📝 测试检查清单

### 单元测试

- [ ] 所有业务逻辑方法都有对应的单元测试
- [ ] 测试覆盖率 > 80%
- [ ] 所有边界情况都已测试
- [ ] Mock对象使用正确

### 集成测试

- [ ] 所有API端点都可访问
- [ ] 请求参数验证正确
- [ ] 响应格式符合规范
- [ ] 错误处理正确

### 功能测试

- [ ] 图片扩图功能正常
- [ ] 多角度生成功能正常
- [ ] 修改文字功能正常
- [ ] 局部重绘功能正常
- [ ] OCR识别功能正常
- [ ] 图层检测功能正常
- [ ] 下载图层功能正常
- [ ] 编辑历史保存正常
- [ ] 撤销编辑功能正常

### 性能测试

- [ ] 平均响应时间 < 10s
- [ ] 95%请求响应时间 < 30s
- [ ] 并发100用户时无错误
- [ ] 内存使用稳定

---

## 🚀 自动化测试

### GitHub Actions配置

```yaml
name: Canvas Editor Tests

on:
  push:
    paths:
      - 'ruoyi-admin/src/main/java/com/ruoyi/web/service/CanvasEditorService.java'
      - 'ruoyi-admin/src/test/java/com/ruoyi/web/service/CanvasEditorServiceTest.java'
  pull_request:
    paths:
      - 'ruoyi-admin/src/main/java/com/ruoyi/web/service/CanvasEditorService.java'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run unit tests
        run: mvn test -Dtest=CanvasEditorServiceTest
      - name: Generate coverage report
        run: mvn test jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

---

## 📚 相关文档

- [后端完成度文档](./canvas-editor-backend-completion.md)
- [项目总结](./canvas-editor-final-summary.md)
- [API规范](./canvas-editor-api-spec.md)
- [开发计划](./canvas-editor-backend-development.md)

---

**最后更新**: 2026-09-07
**版本**: v1.0.0
