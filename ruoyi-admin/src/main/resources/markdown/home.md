# 若依管理系统 API 文档

## 简介

若依管理系统是一套全部开源的快速开发平台，毫无保留给个人及企业免费使用。

- 前端采用Vue、Element UI
- 后端采用Spring Boot、Spring Security、Redis & Jwt
- 权限认证使用Jwt，支持多终端认证系统
- 支持加载动态权限菜单，多方式轻松权限控制

## 技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.2.0 | 容器+MVC框架 |
| Spring Security | 6.2.0 | 认证和授权框架 |
| JWT | 0.11.5 | JWT登录支持 |
| MyBatis | 3.0.3 | ORM框架 |
| Redis | - | 分布式缓存 |
| Knife4j | 4.5.0 | API文档工具 |

## 接口认证

大部分接口需要JWT Token认证，请在请求头中添加：

```
Authorization: Bearer <your_token>
```

获取Token方式：
1. 调用登录接口 `/login` 获取token
2. 在Swagger UI右上角点击"Authorize"按钮
3. 输入 `Bearer <token>` 格式的认证信息

## 模块说明

| 分组 | 说明 |
| --- | --- |
| 系统管理 | 用户、角色、菜单、部门、岗位、字典等管理 |
| 系统监控 | 在线用户、操作日志、登录日志、服务监控、缓存监控 |
| 业务模块 | 积分套餐、支付记录、AI合同分析等业务功能 |
| 用户接口 | 客户端用户登录、注册等接口 |
| 公共接口 | 文件上传、下载等公共功能 |
| 工具接口 | 代码生成器、电商爬虫等工具 |
| 定时任务 | 定时任务管理、日志查询 |
| 代码生成 | 代码生成器接口 |

## 官方文档

- [若依文档](http://doc.ruoyi.vip)
- [Knife4j文档](https://doc.xiaominfo.com)
