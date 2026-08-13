-- ============================================================
-- 光合AI Studio 创作配置预置数据 V1.0
-- 为前台9个工作台 + 通用配置预置所有配置项
-- 前台通过 /business/creationConfig/public/group/{group} 接口拉取
-- ============================================================

-- 先清理旧数据（按分组删除预置数据）
DELETE FROM `gh_creation_config` WHERE `config_group` IN ('common','white_bg','bg_generation','background','retouch','ai_model','main_image','detail_img','banner','size_mark','batch_process');

-- ============================================================
-- 1. 通用配置 common
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('common','languages','语言列表','[{"label":"中文（简体）","value":"zh-CN"},{"label":"英语（美国）","value":"en-US"},{"label":"英语（英国）","value":"en-GB"},{"label":"日语","value":"ja-JP"},{"label":"韩语","value":"ko-KR"},{"label":"德语","value":"de-DE"},{"label":"法语","value":"fr-FR"},{"label":"西班牙语","value":"es-ES"}]',1,'0','前台多语言选择'),
('common','max_generate_count','单次最大生成数量','10',2,'0','限制单次生图最大数量'),
('common','default_generate_count','默认生成数量','3',3,'0','默认生图数量'),
('common','supported_formats','支持的图片格式','["JPG","PNG","WebP"]',4,'0','上传支持的格式'),
('common','max_upload_size','单张最大上传(MB)','20',5,'0','单张图片大小限制'),
('common','max_product_images','产品图最大数量','10',6,'0','产品图上传上限'),
('common','max_ref_images','参考图最大数量','10',7,'0','参考图上传上限');

-- ============================================================
-- 2. AI白底图 white_bg
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('white_bg','shadow_styles','白底样式（阴影类型）','[{"label":"无阴影","value":"none"},{"label":"自然阴影","value":"natural"},{"label":"柔和阴影","value":"soft"},{"label":"硬阴影","value":"hard"},{"label":"倒影","value":"reflection"}]',1,'0','白底图阴影样式选择'),
('white_bg','size_presets','输出尺寸预设','[{"label":"800×800","value":"800x800","w":800,"h":800},{"label":"1000×1000","value":"1000x1000","w":1000,"h":1000},{"label":"1500×1500","value":"1500x1500","w":1500,"h":1500},{"label":"2000×2000","value":"2000x2000","w":2000,"h":2000},{"label":"自定义","value":"custom","w":0,"h":0}]',2,'0','白底图输出尺寸预设'),
('white_bg','size_min','尺寸最小值','64',3,'0','自定义尺寸最小值'),
('white_bg','size_max','尺寸最大值','4096',4,'0','自定义尺寸最大值');

-- ============================================================
-- 3. 白底图生成背景 bg_generation
--    与 AI白底图 同形式：每项一行简单值，后台通用编辑器自动渲染为友好表单。
--    scene_list/light_options/style_presets/platform_options 为 {label,value} 对象数组，
--    其中 value = 提示词库(gh_prompt_library) 的 prompt_key（opt_*.change_bg.*），
--    工作台选中标签后按 value 反查提示词库拼接 prompt_text 发给 AI。
--    size_options 的 value 为尺寸字串，作为出图参数，不进提示词；max_count 为数字。
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('bg_generation','platform_options','使用平台','[{"label":"淘宝/天猫","value":"opt_platform.change_bg.taobao"},{"label":"京东","value":"opt_platform.change_bg.jd"},{"label":"拼多多","value":"opt_platform.change_bg.pdd"},{"label":"抖音","value":"opt_platform.change_bg.douyin"},{"label":"小红书","value":"opt_platform.change_bg.xhs"},{"label":"亚马逊","value":"opt_platform.change_bg.amazon"}]',1,'0','白底生成背景-目标平台（value=提示词库key）'),
('bg_generation','scene_list','场景列表','[{"label":"居家","value":"opt_scene.change_bg.home"},{"label":"户外","value":"opt_scene.change_bg.outdoor"},{"label":"工作室","value":"opt_scene.change_bg.studio"},{"label":"节日主题","value":"opt_scene.change_bg.festival"},{"label":"极简","value":"opt_scene.change_bg.minimal"},{"label":"自然","value":"opt_scene.change_bg.nature"},{"label":"都市","value":"opt_scene.change_bg.urban"}]',2,'0','白底生成背景-场景（value=提示词库key）'),
('bg_generation','light_options','光线选项','[{"label":"自然光","value":"opt_light.change_bg.natural"},{"label":"柔光","value":"opt_light.change_bg.soft"},{"label":"硬光","value":"opt_light.change_bg.hard"},{"label":"逆光","value":"opt_light.change_bg.backlight"},{"label":"暖光","value":"opt_light.change_bg.warm"},{"label":"冷光","value":"opt_light.change_bg.cool"}]',3,'0','白底生成背景-光线（value=提示词库key）'),
('bg_generation','style_presets','风格预设','[{"label":"简约","value":"opt_style.change_bg.minimal"},{"label":"轻奢","value":"opt_style.change_bg.luxury"},{"label":"活力","value":"opt_style.change_bg.vibrant"},{"label":"复古","value":"opt_style.change_bg.retro"},{"label":"科技感","value":"opt_style.change_bg.tech"},{"label":"ins风","value":"opt_style.change_bg.ins"}]',4,'0','白底生成背景-风格（value=提示词库key）'),
('bg_generation','size_options','输出尺寸','[{"label":"1:1（800×800）","value":"800:800"},{"label":"3:4（800×1067）","value":"800:1067"},{"label":"4:3（1067×800）","value":"1067:800"},{"label":"自定义","value":"custom"}]',5,'0','白底生成背景-输出尺寸（value=尺寸字串，出图参数）'),
('bg_generation','max_count','生图数量上限','4',6,'0','白底生成背景-单次生成数量上限');

-- ============================================================
-- 4. 产品精修 retouch
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('retouch','tools','精修工具','[{"label":"一键修复","value":"one-click-repair"},{"label":"智能优化","value":"smart-optimize"},{"label":"瑕疵去除","value":"defect-remove"},{"label":"纹理增强","value":"texture-enhance"},{"label":"色彩调整","value":"color-adjust"},{"label":"光线优化","value":"light-optimize"},{"label":"锐化增强","value":"sharpen"},{"label":"降噪处理","value":"denoise"},{"label":"畸变校正","value":"distortion-correct"}]',1,'0','精修工具列表'),
('retouch','quality_options','画质选项','[{"label":"标准","value":"standard"},{"label":"高清","value":"hd"},{"label":"超清","value":"ultra"}]',2,'0','输出画质选择'),
('retouch','format_options','输出格式','[{"label":"PNG","value":"PNG"},{"label":"JPG","value":"JPG"},{"label":"WebP","value":"WebP"}]',3,'0','输出格式选择'),
('retouch','default_intensity','默认精修强度','50',4,'0','默认精修强度(10-100)'),
('retouch','max_generate_count','生成数量上限','5',5,'0','精修生成数量上限');

-- ============================================================
-- 5. AI模特 ai_model
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('ai_model','gender_options','性别选择','[{"label":"男","value":"male"},{"label":"女","value":"female"},{"label":"不限","value":"any"}]',1,'0','模特性别'),
('ai_model','age_ranges','年龄选择','[{"label":"18-25","value":"18-25"},{"label":"26-35","value":"26-35"},{"label":"36-45","value":"36-45"},{"label":"46-55","value":"46-55"},{"label":"55+","value":"55+"}]',2,'0','年龄段选择'),
('ai_model','hairstyles','发型选择','[{"label":"长直发","value":"long_straight"},{"label":"短直发","value":"short_straight"},{"label":"长卷发","value":"long_curly"},{"label":"短卷发","value":"short_curly"},{"label":"马尾","value":"ponytail"},{"label":"盘发","value":"bun"}]',3,'0','发型选项'),
('ai_model','ethnicities','人种选择','[{"label":"亚洲","value":"asian"},{"label":"欧美","value":"caucasian"},{"label":"非裔","value":"african"},{"label":"拉美","value":"latino"},{"label":"中东","value":"middle_east"}]',4,'0','人种选项'),
('ai_model','poses','姿势选择','[{"label":"正面站立","value":"front_stand"},{"label":"侧面站立","value":"side_stand"},{"label":"坐姿","value":"sitting"},{"label":"行走","value":"walking"},{"label":"倚靠","value":"leaning"},{"label":"动态","value":"dynamic"}]',5,'0','姿势选项'),
('ai_model','clothing_options','服装选择','[{"label":"商务正装","value":"business"},{"label":"休闲","value":"casual"},{"label":"运动","value":"sport"},{"label":"时尚","value":"fashion"},{"label":"家居","value":"homewear"},{"label":"泳装","value":"swimwear"}]',6,'0','服装风格'),
('ai_model','scene_options','场景风格','[{"label":"纯色背景","value":"solid"},{"label":"工作室","value":"studio"},{"label":"户外","value":"outdoor"},{"label":"室内","value":"indoor"},{"label":"街拍","value":"street"}]',7,'0','模特场景'),
('ai_model','output_sizes','输出尺寸','[{"label":"2000×2000","value":"2000x2000"},{"label":"1500×1500","value":"1500x1500"},{"label":"1000×1000","value":"1000x1000"}]',8,'0','输出尺寸'),
('ai_model','max_generate_count','生成数量上限','20',9,'0','单次最大生成数');

-- ============================================================
-- 6. 主图设计 main_image
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('main_image','platform_options','目标平台','[{"label":"Amazon","value":"amazon"},{"label":"eBay","value":"ebay"},{"label":"淘宝","value":"taobao"},{"label":"Shopee","value":"shopee"},{"label":"Shopify","value":"shopify"}]',1,'0','主图目标平台'),
('main_image','size_options','画布尺寸','[{"label":"2000×2000","value":"2000x2000","w":2000,"h":2000},{"label":"1500×1500","value":"1500x1500","w":1500,"h":1500},{"label":"1000×1000","value":"1000x1000","w":1000,"h":1000},{"label":"800×800","value":"800x800","w":800,"h":800}]',2,'0','主图画布尺寸'),
('main_image','purposes','主图用途','[{"label":"主图","value":"main"},{"label":"附图","value":"sub"},{"label":"变体图","value":"variant"},{"label":"场景图","value":"scene"}]',3,'0','主图用途选择'),
('main_image','selling_points','核心卖点','[{"label":"高品质材料","value":"quality_material"},{"label":"耐用结实","value":"durable"},{"label":"多功能使用","value":"multi_function"},{"label":"易于安装","value":"easy_install"},{"label":"防刮耐磨","value":"scratch_resistant"},{"label":"安全环保","value":"eco_friendly"},{"label":"时尚设计","value":"fashion"},{"label":"性价比高","value":"cost_effective"}]',4,'0','核心卖点选项'),
('main_image','max_selling_count','最大卖点选择数','3',5,'0','最多选择卖点数量'),
('main_image','max_generate_count','生成数量上限','5',6,'0','主图生成数量上限');

-- ============================================================
-- 7. 详情图/A+ detail_img
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('detail_img','page_sizes','页面尺寸','[{"label":"970宽","value":"970","w":970},{"label":"750宽","value":"750","w":750},{"label":"800宽","value":"800","w":800},{"label":"1200宽","value":"1200","w":1200}]',1,'0','详情页宽度'),
('detail_img','platform_options','目标平台','[{"label":"Amazon A+","value":"amazon_aplus"},{"label":"淘宝详情","value":"taobao_detail"},{"label":"京东详情","value":"jd_detail"},{"label":"Shopee","value":"shopee"}]',2,'0','详情图平台'),
('detail_img','selling_points','核心卖点','[{"label":"高品质材料","value":"quality_material"},{"label":"耐用结实","value":"durable"},{"label":"多功能使用","value":"multi_function"},{"label":"易于安装","value":"easy_install"},{"label":"防刮耐磨","value":"scratch_resistant"},{"label":"安全环保","value":"eco_friendly"},{"label":"时尚设计","value":"fashion"},{"label":"性价比高","value":"cost_effective"},{"label":"智能设计","value":"smart"},{"label":"抗菌防霉","value":"antibacterial"}]',3,'0','详情图卖点'),
('detail_img','content_structure','内容结构模块','[{"label":"产品概览","value":"overview"},{"label":"卖点详解","value":"selling_points"},{"label":"规格参数","value":"specs"},{"label":"使用场景","value":"scenes"},{"label":"对比图","value":"comparison"},{"label":"FAQ","value":"faq"},{"label":"品牌故事","value":"brand"}]',4,'0','详情页内容模块'),
('detail_img','max_selling_count','建议卖点数量','5',5,'0','建议选择卖点数量'),
('detail_img','max_generate_count','生成数量上限','5',6,'0','详情图生成数量上限');

-- ============================================================
-- 8. Banner设计 banner
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('banner','size_presets','画布尺寸预设','[{"label":"1200×300（横幅）","value":"1200x300","w":1200,"h":300},{"label":"1920×600（通栏）","value":"1920x600","w":1920,"h":600},{"label":"1920×1080（大屏）","value":"1920x1080","w":1920,"h":1080},{"label":"自定义","value":"custom","w":0,"h":0}]',1,'0','Banner画布尺寸'),
('banner','banner_types','Banner类型','[{"key":"promo","name":"促销活动","desc":"打折促销、限时优惠"},{"key":"new","name":"新品上市","desc":"新品发布、产品推荐"},{"key":"brand","name":"品牌宣传","desc":"品牌故事、品牌形象"},{"key":"season","name":"节日季节","desc":"节日活动、季节主题"},{"key":"notice","name":"信息通知","desc":"公告通知、店铺信息"},{"key":"decorate","name":"店铺装修","desc":"店铺头图、页面装饰"}]',2,'0','Banner类型选择'),
('banner','purposes','核心目的','[{"key":"sales","label":"提升销量/促销转化"},{"key":"newProduct","label":"新品推广"},{"key":"branding","label":"品牌宣传/提升认知"},{"key":"traffic","label":"活动宣传/引流"},{"key":"clearance","label":"清仓/库存处理"},{"key":"festival","label":"节日营销"},{"key":"shopImage","label":"店铺形象展示"},{"key":"other","label":"其他"}]',3,'0','Banner核心目的(可多选)'),
('banner','max_generate_count','生成数量上限','10',4,'0','Banner生成数量上限'),
('banner','title_max_length','主标题最大长度','30',5,'0','主标题字数限制'),
('banner','subtitle_max_length','副标题最大长度','50',6,'0','副标题字数限制');

-- ============================================================
-- 9. 尺寸标记 size_mark
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('size_mark','line_styles','线条样式','[{"label":"实线","value":"solid"},{"label":"虚线1","value":"dashed1"},{"label":"虚线2","value":"dashed2"}]',1,'0','尺寸线样式'),
('size_mark','ratio_options','输出比例','[{"label":"1:1","value":"1:1"},{"label":"4:3","value":"4:3"},{"label":"3:4","value":"3:4"},{"label":"16:9","value":"16:9"},{"label":"9:16","value":"9:16"},{"label":"自定义","value":"custom"}]',2,'0','输出比例选择'),
('size_mark','templates','模板选择','[{"label":"标准尺寸图","value":"standard"},{"label":"多尺寸对比","value":"compare"},{"label":"场景尺寸图","value":"scene"},{"label":"更多模板","value":"more"}]',3,'0','尺寸标记模板'),
('size_mark','unit_options','单位选择','[{"label":"cm","value":"cm"},{"label":"in","value":"in"}]',4,'0','尺寸单位'),
('size_mark','max_product_images','产品图最大数量','10',5,'0','尺寸标记产品图上限');

-- ============================================================
-- 10. 批量生成 batch_process
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`) VALUES
('batch_process','selling_points','核心卖点','[{"label":"高品质材料","value":"quality_material"},{"label":"耐用结实","value":"durable"},{"label":"多功能使用","value":"multi_function"},{"label":"易于安装","value":"easy_install"},{"label":"防刮耐磨","value":"scratch_resistant"},{"label":"安全环保","value":"eco_friendly"},{"label":"时尚设计","value":"fashion"},{"label":"性价比高","value":"cost_effective"},{"label":"智能设计","value":"smart"},{"label":"抗菌防霉","value":"antibacterial"},{"label":"防潮防水","value":"waterproof"},{"label":"静音减震","value":"quiet"}]',1,'0','批量生成卖点选项'),
('batch_process','format_options','图片格式','[{"label":"JPG","value":"JPG"},{"label":"PNG","value":"PNG"},{"label":"WebP","value":"WebP"}]',2,'0','输出格式'),
('batch_process','quality_options','画质选项','[{"label":"标准","value":"standard"},{"label":"高清","value":"hd"},{"label":"超清","value":"ultra"}]',3,'0','输出画质'),
('batch_process','size_options','尺寸设置','[{"label":"2000×2000","value":"2000x2000","w":2000,"h":2000},{"label":"1500×1500","value":"1500x1500","w":1500,"h":1500},{"label":"1000×1000","value":"1000x1000","w":1000,"h":1000},{"label":"自定义","value":"custom","w":0,"h":0}]',4,'0','输出尺寸'),
('batch_process','max_product_images','产品图最大数量','50',5,'0','批量产品图上限'),
('batch_process','max_ref_images','参考图最大数量','20',6,'0','批量参考图上限'),
('batch_process','max_generate_count','单产品最大生成数','99',7,'0','每个产品最多生成数');

-- ============================================================
-- 11. 预置标签数据
-- ============================================================
DELETE FROM `gh_tag` WHERE `tag_type` IN ('material','scene','style','product','platform','function','quality') AND `create_by` = 'system';

INSERT IGNORE INTO `gh_tag` (`tag_name`,`tag_type`,`sort`,`status`,`create_by`,`create_time`,`remark`) VALUES
('实木','material',1,'0','system',NOW(),'材质标签'),
('布艺','material',2,'0','system',NOW(),'材质标签'),
('皮革','material',3,'0','system',NOW(),'材质标签'),
('金属','material',4,'0','system',NOW(),'材质标签'),
('玻璃','material',5,'0','system',NOW(),'材质标签'),
('石材','material',6,'0','system',NOW(),'材质标签'),
('客厅','scene',1,'0','system',NOW(),'场景标签'),
('卧室','scene',2,'0','system',NOW(),'场景标签'),
('餐厅','scene',3,'0','system',NOW(),'场景标签'),
('厨房','scene',4,'0','system',NOW(),'场景标签'),
('书房','scene',5,'0','system',NOW(),'场景标签'),
('户外','scene',6,'0','system',NOW(),'场景标签'),
('现代简约','style',1,'0','system',NOW(),'风格标签'),
('北欧风','style',2,'0','system',NOW(),'风格标签'),
('日式','style',3,'0','system',NOW(),'风格标签'),
('工业风','style',4,'0','system',NOW(),'风格标签'),
('轻奢','style',5,'0','system',NOW(),'风格标签'),
('中式古典','style',6,'0','system',NOW(),'风格标签'),
('沙发','product',1,'0','system',NOW(),'产品标签'),
('床','product',2,'0','system',NOW(),'产品标签'),
('餐桌','product',3,'0','system',NOW(),'产品标签'),
('椅子','product',4,'0','system',NOW(),'产品标签'),
('电视柜','product',5,'0','system',NOW(),'产品标签'),
('茶几','product',6,'0','system',NOW(),'产品标签'),
('灯具','product',7,'0','system',NOW(),'产品标签'),
('Amazon','platform',1,'0','system',NOW(),'平台标签'),
('eBay','platform',2,'0','system',NOW(),'平台标签'),
('淘宝','platform',3,'0','system',NOW(),'平台标签'),
('Shopee','platform',4,'0','system',NOW(),'平台标签'),
('Shopify','platform',5,'0','system',NOW(),'平台标签'),
('Wayfair','platform',6,'0','system',NOW(),'平台标签'),
('白底图','function',1,'0','system',NOW(),'功能标签'),
('换背景','function',2,'0','system',NOW(),'功能标签'),
('产品精修','function',3,'0','system',NOW(),'功能标签'),
('AI模特','function',4,'0','system',NOW(),'功能标签'),
('主图','function',5,'0','system',NOW(),'功能标签'),
('详情图','function',6,'0','system',NOW(),'功能标签'),
('Banner','function',7,'0','system',NOW(),'功能标签'),
('尺寸标记','function',8,'0','system',NOW(),'功能标签'),
('批量生成','function',9,'0','system',NOW(),'功能标签'),
('标准','quality',1,'0','system',NOW(),'质量标签'),
('高清','quality',2,'0','system',NOW(),'质量标签'),
('超清','quality',3,'0','system',NOW(),'质量标签');