-- 光合AI Studio 前台 UI 选项库初始化数据
-- 将前台9个创作功能组件的硬编码配置项迁移到 gh_prompt_library 表
-- category 以 opt_ 前缀区分 UI 选项与已有 AI 提示词数据
DELETE FROM `gh_prompt_library` WHERE `category` LIKE 'opt_%';
-- 1. 语言 opt_language
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_language','opt_language.zh-CN','中文（简体）','{"text":"中文"}','white_bg,main_image,dimension,banner,batch',1,'0'),
('opt_language','opt_language.en-US','英语（美国）','{"text":"英文"}','white_bg,main_image,dimension,banner,batch',2,'0'),
('opt_language','opt_language.en-GB','英语（英国）','{"text":"英文"}','white_bg,main_image,dimension,banner,batch',3,'0'),
('opt_language','opt_language.ja-JP','日语','{"text":"日文"}','white_bg,main_image,dimension,banner,batch',4,'0'),
('opt_language','opt_language.ko-KR','韩语','{"text":"韩文"}','white_bg,main_image,dimension,banner,batch',5,'0'),
('opt_language','opt_language.de-DE','德语','{"text":"德文"}','white_bg,main_image,dimension,banner,batch',6,'0'),
('opt_language','opt_language.fr-FR','法语','{"text":"法文"}','white_bg,main_image,dimension,banner,batch',7,'0'),
('opt_language','opt_language.es-ES','西班牙语','{"text":"西班牙文"}','white_bg,main_image,dimension,banner,batch',8,'0');
-- 2. 阴影 opt_shadow
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_shadow','opt_shadow.white_bg.no-shadow','无阴影','{"image":"/images/chair-white-bg.png","prompt":"生成干净无阴影的白底图"}','white_bg','1',1,'0'),
('opt_shadow','opt_shadow.white_bg.natural-shadow','自然阴影','{"image":"/images/chair-scene-bg.png","prompt":"生成带自然投影的白底图，阴影自然柔和"}','white_bg','0',2,'0'),
('opt_shadow','opt_shadow.white_bg.soft-shadow','柔和阴影','{"image":"/images/chair-white-bg.png","prompt":"生成带柔和渐变阴影的白底图，阴影过渡平滑"}','white_bg','0',3,'0'),
('opt_shadow','opt_shadow.white_bg.hard-shadow','硬阴影','{"image":"/images/chair-scene-bg.png","prompt":"生成带硬朗阴影的白底图，阴影边缘清晰对比强烈"}','white_bg','0',4,'0');
-- 3. 尺寸 opt_size
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_size','opt_size.white_bg.800x800','800 × 800','{}','white_bg','1',1,'0'),
('opt_size','opt_size.white_bg.1000x1000','1000 × 1000','{}','white_bg','0',2,'0'),
('opt_size','opt_size.white_bg.1500x1500','1500 × 1500','{}','white_bg','0',3,'0'),
('opt_size','opt_size.white_bg.2000x2000','2000 × 2000','{}','white_bg','0',4,'0'),
('opt_size','opt_size.white_bg.custom','自定义','{}','white_bg','0',5,'0'),
('opt_size','opt_size.change_bg.800:800','1:1（800×800）','{}','change_bg','1',1,'0'),
('opt_size','opt_size.change_bg.800:1067','3:4（800×1067）','{}','change_bg','0',2,'0'),
('opt_size','opt_size.change_bg.1067:800','4:3（1067×800）','{}','change_bg','0',3,'0'),
('opt_size','opt_size.change_bg.custom','自定义','{}','change_bg','0',4,'0'),
('opt_size','opt_size.main_image.1:1','1:1','{}','main_image','1',1,'0'),
('opt_size','opt_size.main_image.3:4','3:4','{}','main_image','0',2,'0'),
('opt_size','opt_size.main_image.4:3','4:3','{}','main_image','0',3,'0'),
('opt_size','opt_size.main_image.16:9','16:9','{}','main_image','0',4,'0'),
('opt_size','opt_size.main_image.9:16','9:16','{}','main_image','0',5,'0'),
('opt_size','opt_size.main_image.2:3','2:3','{}','main_image','0',6,'0'),
('opt_size','opt_size.retouch.1:1','1:1','{"w":20,"h":20}','retouch','1',1,'0'),
('opt_size','opt_size.retouch.4:3','4:3','{"w":24,"h":18}','retouch','0',2,'0'),
('opt_size','opt_size.retouch.3:4','3:4','{"w":18,"h":24}','retouch','0',3,'0'),
('opt_size','opt_size.retouch.16:9','16:9','{"w":28,"h":16}','retouch','0',4,'0'),
('opt_size','opt_size.retouch.9:16','9:16','{"w":16,"h":28}','retouch','0',5,'0'),
('opt_size','opt_size.retouch.3:2','3:2','{"w":24,"h":16}','retouch','0',6,'0'),
('opt_size','opt_size.retouch.2:3','2:3','{"w":16,"h":24}','retouch','0',7,'0'),
('opt_size','opt_size.retouch.custom','自定义','{"w":18,"h":18}','retouch','0',8,'0'),
('opt_size','opt_size.banner.1200x300','1200×300（横幅）','{"w":1200,"h":300}','banner','1',1,'0'),
('opt_size','opt_size.banner.1920x600','1920×600（通栏）','{"w":1920,"h":600}','banner','0',2,'0'),
('opt_size','opt_size.banner.1920x1080','1920×1080（大屏）','{"w":1920,"h":1080}','banner','0',3,'0'),
('opt_size','opt_size.banner.custom','自定义','{"w":0,"h":0}','banner','0',4,'0'),
('opt_size','opt_size.batch.1600x1600','1600 × 1600（1:1 主图）','{}','batch','1',1,'0'),
('opt_size','opt_size.batch.2000x2000','2000 × 2000（高清 1:1）','{}','batch','0',2,'0'),
('opt_size','opt_size.batch.1200x1800','1200 × 1800（3:2）','{}','batch','0',3,'0'),
('opt_size','opt_size.batch.1800x1200','1800 × 1200（3:2 横版）','{}','batch','0',4,'0'),
('opt_size','opt_size.batch.1200x1200','1200 × 1200（1:1 标准）','{}','batch','0',5,'0'),
('opt_size','opt_size.batch.800x800','800 × 800（小尺寸）','{}','batch','0',6,'0'),
('opt_size','opt_size.batch.custom','自定义','{}','batch','0',7,'0');
-- 4. 平台 opt_platform
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_platform','opt_platform.change_bg.taobao','淘宝/天猫','{}','change_bg',1,'0'),
('opt_platform','opt_platform.change_bg.jd','京东','{}','change_bg',2,'0'),
('opt_platform','opt_platform.change_bg.pdd','拼多多','{}','change_bg',3,'0'),
('opt_platform','opt_platform.change_bg.douyin','抖音','{}','change_bg',4,'0'),
('opt_platform','opt_platform.change_bg.xhs','小红书','{}','change_bg',5,'0'),
('opt_platform','opt_platform.change_bg.amazon','亚马逊','{}','change_bg',6,'0'),
('opt_platform','opt_platform.main_image.amazon','亚马逊','{}','main_image',1,'0'),
('opt_platform','opt_platform.main_image.shopee','Shopee','{}','main_image',2,'0'),
('opt_platform','opt_platform.main_image.lazada','Lazada','{}','main_image',3,'0'),
('opt_platform','opt_platform.main_image.aliexpress','速卖通','{}','main_image',4,'0'),
('opt_platform','opt_platform.main_image.taobao','淘宝','{}','main_image',5,'0'),
('opt_platform','opt_platform.main_image.jd','京东','{}','main_image',6,'0'),
('opt_platform','opt_platform.main_image.independent','独立站','{}','main_image',7,'0'),
('opt_platform','opt_platform.main_image.other','其他','{}','main_image',8,'0');
-- 5. 场景 opt_scene
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_scene','opt_scene.change_bg.home','居家','{}','change_bg',1,'0'),
('opt_scene','opt_scene.change_bg.outdoor','户外','{}','change_bg',2,'0'),
('opt_scene','opt_scene.change_bg.studio','工作室','{}','change_bg',3,'0'),
('opt_scene','opt_scene.change_bg.festival','节日主题','{}','change_bg',4,'0'),
('opt_scene','opt_scene.change_bg.minimal','极简','{}','change_bg',5,'0'),
('opt_scene','opt_scene.change_bg.nature','自然','{}','change_bg',6,'0'),
('opt_scene','opt_scene.change_bg.urban','都市','{}','change_bg',7,'0'),
('opt_scene','opt_scene.ai_model.indoor-white','室内白底','{}','ai_model',1,'0'),
('opt_scene','opt_scene.ai_model.indoor','室内场景','{}','ai_model',2,'0'),
('opt_scene','opt_scene.ai_model.outdoor-nature','户外自然','{}','ai_model',3,'0'),
('opt_scene','opt_scene.ai_model.street','街头','{}','ai_model',4,'0'),
('opt_scene','opt_scene.ai_model.mall','商场','{}','ai_model',5,'0'),
('opt_scene','opt_scene.ai_model.pure-bg','纯色背景','{}','ai_model',6,'0');
-- 6. 光线 opt_light
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_light','opt_light.change_bg.natural','自然光','{}','change_bg',1,'0'),
('opt_light','opt_light.change_bg.soft','柔光','{}','change_bg',2,'0'),
('opt_light','opt_light.change_bg.hard','硬光','{}','change_bg',3,'0'),
('opt_light','opt_light.change_bg.backlight','逆光','{}','change_bg',4,'0'),
('opt_light','opt_light.change_bg.warm','暖光','{}','change_bg',5,'0'),
('opt_light','opt_light.change_bg.cool','冷光','{}','change_bg',6,'0');
-- 7. 风格 opt_style
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_style','opt_style.change_bg.minimal','简约','{}','change_bg',1,'0'),
('opt_style','opt_style.change_bg.luxury','轻奢','{}','change_bg',2,'0'),
('opt_style','opt_style.change_bg.vibrant','活力','{}','change_bg',3,'0'),
('opt_style','opt_style.change_bg.retro','复古','{}','change_bg',4,'0'),
('opt_style','opt_style.change_bg.tech','科技感','{}','change_bg',5,'0'),
('opt_style','opt_style.change_bg.ins','ins风','{}','change_bg',6,'0');
-- 8. 卖点 opt_selling
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_selling','opt_selling.main_image.高品质材料','高品质材料','{}','main_image',1,'0'),
('opt_selling','opt_selling.main_image.耐用性强','耐用性强','{}','main_image',2,'0'),
('opt_selling','opt_selling.main_image.舒适体验','舒适体验','{}','main_image',3,'0'),
('opt_selling','opt_selling.main_image.易于安装','易于安装','{}','main_image',4,'0'),
('opt_selling','opt_selling.main_image.多功能','多功能','{}','main_image',5,'0'),
('opt_selling','opt_selling.main_image.大容量收纳','大容量收纳','{}','main_image',6,'0'),
('opt_selling','opt_selling.main_image.环保健康','环保健康','{}','main_image',7,'0'),
('opt_selling','opt_selling.main_image.节省空间','节省空间','{}','main_image',8,'0'),
('opt_selling','opt_selling.main_image.防水防污','防水防污','{}','main_image',9,'0'),
('opt_selling','opt_selling.main_image.安全可靠','安全可靠','{}','main_image',10,'0'),
('opt_selling','opt_selling.main_image.轻便便携','轻便便携','{}','main_image',11,'0'),
('opt_selling','opt_selling.main_image.设计感强','设计感强','{}','main_image',12,'0'),
('opt_selling','opt_selling.batch.高品质材料','高品质材料','{}','batch',1,'0'),
('opt_selling','opt_selling.batch.耐用结实','耐用结实','{}','batch',2,'0'),
('opt_selling','opt_selling.batch.多功能使用','多功能使用','{}','batch',3,'0'),
('opt_selling','opt_selling.batch.易于安装','易于安装','{}','batch',4,'0'),
('opt_selling','opt_selling.batch.防刮耐磨','防刮耐磨','{}','batch',5,'0'),
('opt_selling','opt_selling.batch.安全环保','安全环保','{}','batch',6,'0'),
('opt_selling','opt_selling.batch.时尚设计','时尚设计','{}','batch',7,'0'),
('opt_selling','opt_selling.batch.性价比高','性价比高','{}','batch',8,'0'),
('opt_selling','opt_selling.batch.智能设计','智能设计','{}','batch',9,'0'),
('opt_selling','opt_selling.batch.抗菌防霉','抗菌防霉','{}','batch',10,'0'),
('opt_selling','opt_selling.batch.防潮防水','防潮防水','{}','batch',11,'0'),
('opt_selling','opt_selling.batch.静音减震','静音减震','{}','batch',12,'0');
-- 9. 用途 opt_purpose
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_purpose','opt_purpose.main_image.新品上市','新品上市','{}','main_image',1,'0'),
('opt_purpose','opt_purpose.main_image.提升转化','提升转化','{}','main_image',2,'0'),
('opt_purpose','opt_purpose.main_image.季节节日','季节/节日','{}','main_image',3,'0'),
('opt_purpose','opt_purpose.main_image.促销活动','促销活动','{}','main_image',4,'0'),
('opt_purpose','opt_purpose.main_image.品牌宣传','品牌宣传','{}','main_image',5,'0'),
('opt_purpose','opt_purpose.main_image.其他用途','其他用途','{}','main_image',6,'0'),
('opt_purpose','opt_purpose.banner.sales','提升销量/促销转化','{}','banner',1,'0'),
('opt_purpose','opt_purpose.banner.newProduct','新品推广','{}','banner',2,'0'),
('opt_purpose','opt_purpose.banner.branding','品牌宣传/提升认知','{}','banner',3,'0'),
('opt_purpose','opt_purpose.banner.traffic','活动宣传/引流','{}','banner',4,'0'),
('opt_purpose','opt_purpose.banner.clearance','清仓/库存处理','{}','banner',5,'0'),
('opt_purpose','opt_purpose.banner.festival','节日营销','{}','banner',6,'0'),
('opt_purpose','opt_purpose.banner.shopImage','店铺形象展示','{}','banner',7,'0'),
('opt_purpose','opt_purpose.banner.other','其他','{}','banner',8,'0');
-- 10. 性别 opt_gender
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_gender','opt_gender.ai_model.male','男','{}','ai_model','0',1,'0'),
('opt_gender','opt_gender.ai_model.female','女','{}','ai_model','1',2,'0'),
('opt_gender','opt_gender.ai_model.any','不限','{}','ai_model','0',3,'0');
-- 11. 年龄 opt_age
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_age','opt_age.ai_model.青年18-25','青年(18-25)','{}','ai_model',1,'0'),
('opt_age','opt_age.ai_model.轻熟26-35','轻熟(26-35)','{}','ai_model',2,'0'),
('opt_age','opt_age.ai_model.成熟36-45','成熟(36-45)','{}','ai_model',3,'0'),
('opt_age','opt_age.ai_model.中年46','中年(46+)','{}','ai_model',4,'0');
-- 11. 发型 opt_hairstyle
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_hairstyle','opt_hairstyle.ai_model.短发','短发','{}','ai_model',1,'0'),
('opt_hairstyle','opt_hairstyle.ai_model.长发','长发','{}','ai_model',2,'0'),
('opt_hairstyle','opt_hairstyle.ai_model.卷发','卷发','{}','ai_model',3,'0'),
('opt_hairstyle','opt_hairstyle.ai_model.直发','直发','{}','ai_model',4,'0'),
('opt_hairstyle','opt_hairstyle.ai_model.马尾','马尾','{}','ai_model',5,'0'),
('opt_hairstyle','opt_hairstyle.ai_model.丸子头','丸子头','{}','ai_model',6,'0');
-- 12. 人种 opt_ethnicity
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_ethnicity','opt_ethnicity.ai_model.asian','亚洲','{"color":"#F5DEB3"}','ai_model',1,'0'),
('opt_ethnicity','opt_ethnicity.ai_model.western','欧美','{"color":"#FFDAB9"}','ai_model',2,'0'),
('opt_ethnicity','opt_ethnicity.ai_model.african','非洲','{"color":"#8B6914"}','ai_model',3,'0'),
('opt_ethnicity','opt_ethnicity.ai_model.middleeast','中东','{"color":"#D2B48C"}','ai_model',4,'0'),
('opt_ethnicity','opt_ethnicity.ai_model.latin','拉丁','{"color":"#DEB887"}','ai_model',5,'0');
-- 13. 姿势 opt_pose
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_pose','opt_pose.ai_model.站立','站立','{}','ai_model',1,'0'),
('opt_pose','opt_pose.ai_model.坐姿','坐姿','{}','ai_model',2,'0'),
('opt_pose','opt_pose.ai_model.行走','行走','{}','ai_model',3,'0'),
('opt_pose','opt_pose.ai_model.半身','半身','{}','ai_model',4,'0'),
('opt_pose','opt_pose.ai_model.全身','全身','{}','ai_model',5,'0'),
('opt_pose','opt_pose.ai_model.回眸','回眸','{}','ai_model',6,'0'),
('opt_pose','opt_pose.ai_model.侧面','侧面','{}','ai_model',7,'0'),
('opt_pose','opt_pose.ai_model.正面','正面','{}','ai_model',8,'0');
-- 14. 服装 opt_clothing
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_clothing','opt_clothing.ai_model.商务','商务','{}','ai_model',1,'0'),
('opt_clothing','opt_clothing.ai_model.休闲','休闲','{}','ai_model',2,'0'),
('opt_clothing','opt_clothing.ai_model.运动','运动','{}','ai_model',3,'0'),
('opt_clothing','opt_clothing.ai_model.正装','正装','{}','ai_model',4,'0'),
('opt_clothing','opt_clothing.ai_model.街拍','街拍','{}','ai_model',5,'0');
-- 15. 精修工具 opt_tool
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_tool','opt_tool.retouch.one-click-repair','一键修复','{"desc":"智能一键修复"}','retouch','0',1,'0'),
('opt_tool','opt_tool.retouch.smart-optimize','智能优化','{"desc":"一键提升画质"}','retouch','1',2,'0'),
('opt_tool','opt_tool.retouch.defect-remove','去瑕疵','{"desc":"去除划痕、污渍"}','retouch','0',3,'0'),
('opt_tool','opt_tool.retouch.texture-enhance','纹理增强','{"desc":"增强材质细节"}','retouch','0',4,'0'),
('opt_tool','opt_tool.retouch.color-adjust','色彩调整','{"desc":"亮度、对比度"}','retouch','0',5,'0'),
('opt_tool','opt_tool.retouch.light-optimize','光影优化','{"desc":"优化光影层次"}','retouch','0',6,'0'),
('opt_tool','opt_tool.retouch.bg-process','背景处理','{"desc":"纯色 / 自定义"}','retouch','0',7,'0'),
('opt_tool','opt_tool.retouch.sharpen','锐化增强','{"desc":"提升清晰度"}','retouch','0',8,'0'),
('opt_tool','opt_tool.retouch.denoise','降噪处理','{"desc":"减少噪点"}','retouch','0',9,'0');
-- 16. 比例 opt_ratio（先清理旧 dimension scope 数据，防止 pk 冲突）
DELETE FROM `gh_prompt_library` WHERE `category` = 'opt_ratio' AND `scope` = 'dimension';
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_ratio','opt_ratio.dimension.1:1','1:1','{}','dimension','0',1,'0'),
('opt_ratio','opt_ratio.dimension.4:3','4:3','{}','dimension','0',2,'0'),
('opt_ratio','opt_ratio.dimension.3:4','3:4','{}','dimension','0',3,'0'),
('opt_ratio','opt_ratio.dimension.16:9','16:9','{}','dimension','0',4,'0'),
('opt_ratio','opt_ratio.dimension.9:16','9:16','{}','dimension','0',5,'0'),
('opt_ratio','opt_ratio.dimension.custom','自定义','{}','dimension','0',6,'0');
-- 17. Banner类型 opt_banner_type
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_banner_type','opt_banner_type.banner.promo','促销活动','{"desc":"打折促销、限时优惠"}','banner','1',1,'0'),
('opt_banner_type','opt_banner_type.banner.new','新品上市','{"desc":"新品发布、产品推荐"}','banner','0',2,'0'),
('opt_banner_type','opt_banner_type.banner.brand','品牌宣传','{"desc":"品牌故事、品牌形象"}','banner','0',3,'0'),
('opt_banner_type','opt_banner_type.banner.season','节日季节','{"desc":"节日活动、季节主题"}','banner','0',4,'0'),
('opt_banner_type','opt_banner_type.banner.notice','信息通知','{"desc":"公告通知、店铺信息"}','banner','0',5,'0'),
('opt_banner_type','opt_banner_type.banner.decorate','店铺装修','{"desc":"店铺头图、页面装饰"}','banner','0',6,'0');
-- 18. Banner模板 opt_template
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_template','opt_template.banner.夏季促销','夏季促销','{"tag":"促销活动","title":"SUMMER SALE","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #2563FF, #1E40AF)"}','banner',1,'0'),
('opt_template','opt_template.banner.大促活动','大促活动','{"tag":"促销活动","title":"MEGA SALE","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #EF4444, #B91C1C)"}','banner',2,'0'),
('opt_template','opt_template.banner.新品上市','新品上市','{"tag":"新品上市","title":"NEW ARRIVAL","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #1F2937, #111827)"}','banner',3,'0'),
('opt_template','opt_template.banner.季节上新','季节上新','{"tag":"节日季节","title":"AUTUMN COLLECTION","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #F59E0B, #D97706)"}','banner',4,'0'),
('opt_template','opt_template.banner.包邮活动','包邮活动','{"tag":"促销活动","title":"FREE SHIPPING","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #22C55E, #15803D)"}','banner',5,'0'),
('opt_template','opt_template.banner.品牌宣传','品牌宣传','{"tag":"品牌宣传","title":"BRAND STORY","subtitle":"","bg":"linear-gradient(135deg, #8B5CF6, #6D28D9)"}','banner',6,'0'),
('opt_template','opt_template.banner.限时抢购','限时抢购','{"tag":"促销活动","title":"FLASH SALE","subtitle":"img-placeholder","bg":"linear-gradient(135deg, #F97316, #EA580C)"}','banner',7,'0');
-- 19. 详情页 opt_page
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_page','opt_page.detail.封面图','封面图','{"desc":"吸引注意力","bgClass":"furniture-icon-sofa"}','detail',1,'0'),
('opt_page','opt_page.detail.核心卖点','核心卖点','{"desc":"解决用户需求","bgClass":"furniture-icon-bed"}','detail',2,'0'),
('opt_page','opt_page.detail.功能展示','功能展示','{"desc":"产品功能亮点","bgClass":"furniture-icon-chair"}','detail',3,'0'),
('opt_page','opt_page.detail.细节展示','细节展示','{"desc":"产品细节","bgClass":"furniture-icon-lamp"}','detail',4,'0'),
('opt_page','opt_page.detail.场景应用','场景应用','{"desc":"使用场景","bgClass":"furniture-icon-table"}','detail',5,'0'),
('opt_page','opt_page.detail.尺寸参数','尺寸参数','{"desc":"规格参数","bgClass":"furniture-icon-cabinet"}','detail',6,'0'),
('opt_page','opt_page.detail.售后保障','售后保障','{"desc":"售后服务","bgClass":"furniture-icon-decor"}','detail',7,'0');
-- 20. 质量 opt_quality
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_quality','opt_quality.batch.standard','标准','{}','batch','0',1,'0'),
('opt_quality','opt_quality.batch.hd','高清','{}','batch','1',2,'0'),
('opt_quality','opt_quality.batch.ultra','超清','{}','batch','0',3,'0');
-- 21. 输出格式 opt_format
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_format','opt_format.retouch.PNG','PNG','{}','retouch','1',1,'0'),
('opt_format','opt_format.retouch.JPG','JPG','{}','retouch','0',2,'0'),
('opt_format','opt_format.retouch.WebP','WebP','{}','retouch','0',3,'0');
-- 22. 尺寸标记-线条样式 opt_line_style
DELETE FROM `gh_prompt_library` WHERE `category` = 'opt_line_style';
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_line_style','opt_line_style.dimension.solid','实线','{"prompt":"使用实线标注尺寸线"}','dimension','1',1,'0'),
('opt_line_style','opt_line_style.dimension.dashed1','虚线1','{"prompt":"使用短虚线标注尺寸线"}','dimension','0',2,'0'),
('opt_line_style','opt_line_style.dimension.dashed2','虚线2','{"prompt":"使用长虚线标注尺寸线"}','dimension','0',3,'0');
-- 23. 尺寸标记-输出比例 opt_ratio 已在第16节统一插入（dimension scope），此处无需重复
-- 24. 尺寸标记-单位选择 opt_unit
DELETE FROM `gh_prompt_library` WHERE `category` = 'opt_unit';
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_unit','opt_unit.dimension.cm','cm','{"prompt":"统一使用cm单位"}','dimension','1',1,'0'),
('opt_unit','opt_unit.dimension.in','in','{"prompt":"统一使用inch单位"}','dimension','0',2,'0');
-- 25. 尺寸标记-尺寸模板 opt_size_template
DELETE FROM `gh_prompt_library` WHERE `category` = 'opt_size_template';
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`is_default`,`sort`,`status`) VALUES
('opt_size_template','opt_size_template.dimension.standard','标准尺寸图','{"prompt":"生成标准尺寸说明图，标注长宽高"}','dimension','1',1,'0'),
('opt_size_template','opt_size_template.dimension.compare','多尺寸对比','{"prompt":"生成多尺寸对比图，展示不同规格差异"}','dimension','0',2,'0'),
('opt_size_template','opt_size_template.dimension.scene','场景尺寸图','{"prompt":"生成场景尺寸图，展示产品与场景的空间关系"}','dimension','0',3,'0'),
('opt_size_template','opt_size_template.dimension.more','更多模板','{}','dimension','0',4,'0');