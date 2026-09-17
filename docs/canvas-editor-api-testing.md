# Canvas Editor API 测试指南

**更新时间**: 2026-09-07
**Base URL**: `http://localhost:8080`

---

## 🔧 测试准备

### 1. 启动服务

```bash
# 启动后端服务
cd ruoyi-admin
mvn spring-boot:run

# 或使用IDE启动 Application.java
```

### 2. 获取认证Token

```bash
# 登录获取token
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 响应示例
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

保存token到环境变量：
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📡 API测试用例

### 1. 图片扩图

```bash
curl -X POST http://localhost:8080/ai/image/extend \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "ratio": "16:9",
    "width": 1920,
    "height": 1080
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "url": "https://...",
    "width": 1920,
    "height": 1080,
    "ratio": "16:9"
  }
}
```

---

### 2. 多角度生成

```bash
curl -X POST http://localhost:8080/ai/image/multi-angle \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "count": 4,
    "type": "rotate"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "images": [
      {"url": "https://...", "angle": "角度 1", "type": "rotate"},
      {"url": "https://...", "angle": "角度 2", "type": "rotate"},
      {"url": "https://...", "angle": "角度 3", "type": "rotate"},
      {"url": "https://...", "angle": "角度 4", "type": "rotate"}
    ]
  }
}
```

---

### 3. 修改文字

```bash
curl -X POST http://localhost:8080/ai/image/edit-text \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "originalText": "原文字",
    "newText": "新文字",
    "font": "Arial"
  }'
```

---

### 4. 局部重绘

```bash
curl -X POST http://localhost:8080/ai/image/partial-redraw \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "description": "把红色部分改成蓝色",
    "mask": {
      "x": 0.1,
      "y": 0.1,
      "width": 0.3,
      "height": 0.3
    }
  }'
```

---

### 5. 图层检测

```bash
curl -X POST http://localhost:8080/ai/image/detect-layers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg"
  }'
```

---

### 6. 下载图层

```bash
curl -X POST http://localhost:8080/ai/image/download-layers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "layers": ["layer_1", "layer_2"],
    "format": "png"
  }'
```

---

### 7. OCR识别

```bash
curl -X POST http://localhost:8080/ai/image/ocr \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg"
  }'
```

---

### 8. 编辑历史

```bash
curl -X GET "http://localhost:8080/ai/image/IMG_123456/history" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 9. 撤销编辑

```bash
curl -X POST http://localhost:8080/ai/image/revert \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageId": "IMG_123456",
    "version": 1
  }'
```

---

## 🧪 Postman集合

### 导入Postman集合

1. 打开Postman
2. 点击Import
3. 粘贴以下JSON

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
          "raw": "{\n  \"imageUrl\": \"https://example.com/product.jpg\",\n  \"ratio\": \"16:9\"\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:8080/ai/image/extend",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["ai", "image", "extend"]
        }
      }
    },
    {
      "name": "多角度生成",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"imageUrl\": \"https://example.com/product.jpg\",\n  \"count\": 4,\n  \"type\": \"rotate\"\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:8080/ai/image/multi-angle",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["ai", "image", "multi-angle"]
        }
      }
    }
  ]
}
```

---

## 📊 测试检查清单

### 功能测试

- [ ] 图片扩图 - 正常比例
- [ ] 图片扩图 - 自定义尺寸
- [ ] 多角度生成 - rotate类型
- [ ] 多角度生成 - multi-view类型
- [ ] 修改文字 - 正常场景
- [ ] 局部重绘 - 正常场景
- [ ] 图层检测 - 正常场景
- [ ] 下载图层 - PNG格式
- [ ] 下载图层 - PSD格式
- [ ] OCR识别 - 正常场景
- [ ] 编辑历史 - 有历史记录
- [ ] 撤销编辑 - 正常场景

### 异常测试

- [ ] 图片URL为空
- [ ] 图片格式不支持
- [ ] 图片大小超限
- [ ] 参数缺失
- [ ] 参数类型错误
- [ ] 未授权访问
- [ ] Token过期

### 性能测试

- [ ] 并发请求测试
- [ ] 大图片处理测试
- [ ] 超时测试
- [ ] 内存占用测试

---

## 🐛 常见问题

### Q1: 401 Unauthorized

**原因**: Token未提供或过期

**解决**: 重新登录获取token

### Q2: 500 Internal Server Error

**原因**: 服务器内部错误

**解决**: 查看日志排查问题

### Q3: 图片URL无法访问

**原因**: URL格式错误或图片不存在

**解决**: 检查URL格式，确保图片可访问

---

## 📝 测试报告模板

```markdown
### 测试用例: 图片扩图

**测试时间**: 2026-09-07
**测试人员**: xxx
**测试结果**: ✅ 通过 / ❌ 失败

**请求**:
```json
{
  "imageUrl": "https://example.com/product.jpg",
  "ratio": "16:9"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "url": "https://...",
    "width": 1920,
    "height": 1080
  }
}
```

**备注**: 
- 扩图功能正常
- 返回URL可访问
- 尺寸正确
```

---

**最后更新**: 2026-09-07
