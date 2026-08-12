img-ai
├── ruoyi-admin          后端启动与 Web API 入口
│   ├── com.ruoyi.web.controller.system 若依原生系统能力
│   ├── com.ruoyi.web.controller.user 客户端所有的对外api接口
│   └── com.ruoyi.web.controller.module 管理端各个模块功能
├── ruoyi-framework      Spring Security / 配置 / AOP / Web基础设施
├── ruoyi-common         通用常量、工具、异常、基类
├── ruoyi-system         若依原生系统能力：用户、角色、菜单、字典、日志等
├── ruoyi-quartz         定时任务与任务执行
├── ruoyi-generator      代码生成器
├── ruoyi-customer       面向 C 端用户的业务服务
├── ruoyi-module         业务领域模块集合
│   ├── points-package   积分、套餐、扣费、支付记录
│   ├── convenient       AI 图片配置、会话、提示词管理
│   ├── expand           文件管理、套餐明细等扩展能力
│   └── statistics       统计报表
├── ruoyi-tools          外部能力适配层
│   ├── ai               多家 AI 服务接入
│   ├── payment          微信/支付宝支付
│   ├── oss              文件上传/对象存储
│   ├── sms              短信
│   └── external         外部系统/工具型 Mapper
├── ruoyi-ui             Vue2 + ElementUI 管理后台前端
└── logs                 运行日志
