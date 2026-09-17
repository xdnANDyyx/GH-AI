# Canvas Editor 部署指南

**版本**: v1.0.0
**更新日期**: 2026-09-07
**适用环境**: 生产环境

---

## 📋 部署前检查清单

### 环境要求

- [x] Java 17+
- [x] MySQL 8.0+
- [x] Maven 3.8+
- [x] Node.js 16+ (前端)
- [x] Google Cloud 账号（Vertex AI）
- [x] 可选的MinIO/OSS存储

### 配置文件检查

- [ ] Vertex AI 配置
- [ ] 数据库配置
- [ ] 文件上传配置
- [ ] 代理配置（如果需要）

---

## 🚀 部署步骤

### 1. 数据库初始化

#### 1.1 创建编辑历史表

```bash
# 登录MySQL
mysql -u root -p ruoyi

# 执行SQL脚本
source sql/canvas_edit_history.sql
```

#### 1.2 验证表创建

```sql
USE ruoyi;
SHOW TABLES LIKE 'canvas_edit_history';
-- 应该返回: canvas_edit_history
```

### 2. 后端配置

#### 2.1 更新application.yml

```yaml
# Vertex AI 配置
vertex:
  ai:
    project-id: YOUR_PROJECT_ID
    location: global
    model: gemini-3-pro-image
    read-timeout: 600
    credentials-path: /path/to/credentials.json

    # 代理配置（如果需要）
    proxy:
      host: YOUR_PROXY_HOST
      port: 3128

# 图片上传配置
customer:
  image-upload:
    type: LOCAL  # 或 OSS / MINIO

# 文件路径配置
ruoyi:
  profile: /data/uploadPath  # 生产环境路径
```

#### 2.2 创建上传目录

```bash
mkdir -p /data/uploadPath/canvas-editor
chmod 755 /data/uploadPath/canvas-editor
chown -R appuser:appgroup /data/uploadPath
```

#### 2.3 验证Vertex AI认证

```bash
# 测试Google认证
gcloud auth application-default login
# 或使用服务账号
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

### 3. 前端配置

#### 3.1 更新API地址

在CanvasEditor.js中确认API地址：

```javascript
const API_PATHS = {
  extend: '/customer/ai/image/extend',
  multiAngle: '/customer/ai/image/multi-angle',
  editText: '/customer/ai/image/edit-text',
  partialRedraw: '/customer/ai/image/partial-redraw',
  detectLayers: '/customer/ai/image/detect-layers',
  downloadLayers: '/customer/ai/image/download-layers',
  ocr: '/customer/ai/image/ocr',
  history: '/customer/ai/image/history',
  revert: '/customer/ai/image/revert'
}
```

#### 3.2 构建前端

```bash
cd guanghe-studio
npm install
npm run build
```

#### 3.3 部署前端文件

```bash
# 复制到Web服务器目录
cp -r dist/* /var/www/guanghe-studio/
# 或配置Nginx反向代理到开发服务器
```

### 4. 启动服务

#### 4.1 后端启动

```bash
# 方式1: 直接运行JAR
java -jar guanghe-server.jar --spring.profiles.active=prod

# 方式2: 使用Systemd服务
sudo systemctl start guanghe-server
sudo systemctl enable guanghe-server
```

#### 4.2 验证启动

```bash
# 检查日志
tail -f logs/ruoyi.log | grep "Canvas Editor"

# 检查数据库表
mysql -u root -p -e "USE ruoyi; SHOW TABLES LIKE 'canvas_edit_history';"

# 测试API
curl http://localhost:8780/customer/ai/image/health
```

---

## 🔧 生产环境配置

### 1. MinIO配置（可选）

#### 1.1 添加MinIO依赖

在`pom.xml`中添加：

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.3</version>
</dependency>
```

#### 1.2 创建MinIO配置类

```java
@Configuration
@ConditionalOnProperty(name = "customer.image-upload.type", havingValue = "MINIO")
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
```

#### 1.3 配置application.yml

```yaml
customer:
  image-upload:
    type: MINIO

minio:
  endpoint: http://localhost:9000
  access-key: YOUR_ACCESS_KEY
  secret-key: YOUR_SECRET_KEY
  bucket-name: guanghe-images
```

### 2. OSS配置（可选）

参考MinIO配置，类似实现阿里云OSS或腾讯云COS。

### 3. Nginx配置

```nginx
server {
    listen 80;
    server_name guanghe.example.com;

    # 前端静态文件
    location / {
        root /var/www/guanghe-studio;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /customer/ {
        proxy_pass http://localhost:8780/customer/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # 文件上传大小限制
        client_max_body_size 50M;

        # 超时设置
        proxy_connect_timeout 600s;
        proxy_send_timeout 600s;
        proxy_read_timeout 600s;
    }

    # 上传文件访问
    location /uploadPath/ {
        alias /data/uploadPath/;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 4. SSL配置

```nginx
server {
    listen 443 ssl http2;
    server_name guanghe.example.com;

    ssl_certificate /etc/letsencrypt/live/guanghe.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/guanghe.example.com/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 其他配置...
}
```

---

## 📊 性能优化

### 1. 数据库优化

#### 1.1 添加索引

```sql
-- 编辑历史表索引
CREATE INDEX idx_image_id ON canvas_edit_history(image_id);
CREATE INDEX idx_user_id ON canvas_edit_history(user_id);
CREATE INDEX idx_create_time ON canvas_edit_history(create_time);
CREATE INDEX idx_operation ON canvas_edit_history(operation);

-- 联合唯一索引
ALTER TABLE canvas_edit_history
ADD UNIQUE KEY uk_image_version (image_id, version);
```

#### 1.2 定期清理

```sql
-- 清理90天前的历史记录
DELETE FROM canvas_edit_history
WHERE create_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

添加定时任务：

```bash
# crontab -e
0 3 * * * mysql -u root -p ruoyi -e "DELETE FROM canvas_edit_history WHERE create_time < DATE_SUB(NOW(), INTERVAL 90 DAY)"
```

### 2. 图片缓存

#### 2.1 添加Redis缓存

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 1
```

#### 2.2 缓存实现

```java
@Cacheable(value = "canvas-editor", key = "#imageId + #ratio")
public Map<String, Object> extendImage(String imageId, String ratio, ...) {
    // ...
}
```

### 3. 异步处理

#### 3.1 添加异步支持

```java
@Async
public CompletableFuture<Map<String, Object>> extendImageAsync(String imageUrl, ...) {
    Map<String, Object> result = extendImage(imageUrl, ...);
    return CompletableFuture.completedFuture(result);
}
```

---

## 🔍 监控和日志

### 1. 日志配置

```yaml
logging:
  level:
    com.ruoyi.web.service.CanvasEditorService: INFO
    com.ruoyi.web.service.ImageUploadService: INFO
  file:
    name: logs/canvas-editor.log
    max-size: 100MB
    max-history: 30
```

### 2. 监控指标

建议监控以下指标：

- **API响应时间**: P50, P95, P99
- **API错误率**: 4xx, 5xx比例
- **图片处理成功率**
- **图片上传延迟**
- **数据库查询时间**
- **内存使用率**
- **CPU使用率**

### 3. 告警配置

```yaml
# Prometheus告警规则
groups:
  - name: canvas_editor
    rules:
      - alert: CanvasEditorHighLatency
        expr: histogram_quantile(0.95, canvas_editor_request_duration_seconds) > 30
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Canvas Editor API响应时间过高"

      - alert: CanvasEditorHighErrorRate
        expr: rate(canvas_editor_errors_total[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Canvas Editor API错误率过高"
```

---

## 🔒 安全配置

### 1. 权限控制

确保只有授权用户可以访问Canvas Editor API：

```java
@PreAuthorize("@ss.hasPermi('canvas:editor:edit')")
@PostMapping("/extend")
public AjaxResult<Map<String, Object>> extendImage(...) {
    // ...
}
```

### 2. 限流配置

```java
@Component
public class CanvasEditorRateLimiter {

    @RateLimiter(name = "canvasEditor", fallbackMethod = "rateLimitFallback")
    public Map<String, Object> extendImage(...) {
        // ...
    }
}
```

### 3. 输入验证

```java
@PostMapping("/extend")
public AjaxResult<Map<String, Object>> extendImage(@Valid @RequestBody ExtendImageRequest request) {
    // 使用JSR-380验证注解
}
```

---

## 🐛 故障排查

### 常见问题

#### 1. Vertex AI认证失败

**症状**: `Vertex AI 认证失败`

**排查步骤**:
```bash
# 1. 检查凭证文件
ls -la /path/to/credentials.json

# 2. 测试认证
gcloud auth application-default print-access-token

# 3. 检查日志
grep "Vertex AI" logs/ruoyi.log
```

#### 2. 图片上传失败

**症状**: `图片上传失败`

**排查步骤**:
```bash
# 1. 检查目录权限
ls -la /data/uploadPath/canvas-editor

# 2. 检查磁盘空间
df -h /data

# 3. 检查日志
grep "图片上传" logs/ruoyi.log
```

#### 3. 数据库连接失败

**症状**: `保存编辑历史失败`

**排查步骤**:
```bash
# 1. 检查MySQL连接
mysql -h localhost -u root -p ruoyi -e "SELECT 1"

# 2. 检查表是否存在
mysql -u root -p ruoyi -e "SHOW TABLES LIKE 'canvas_edit_history'"

# 3. 检查日志
grep "canvas_edit_history" logs/ruoyi.log
```

---

## 📝 更新日志

### v1.0.0 (2026-09-07)

- ✅ Canvas Editor功能上线
- ✅ 9个编辑功能全部实现
- ✅ 图片上传功能
- ✅ 编辑历史存储
- ✅ 重试机制
- ✅ 图片压缩

---

## 📞 技术支持

遇到问题？

1. 查看日志：`logs/ruoyi.log`
2. 检查配置：`application.yml`
3. 查看文档：[Canvas Editor文档中心](../README.md)

---

**最后更新**: 2026-09-07
**维护团队**: Claude Code
