-- ============================================================
-- 光合AI Studio 提示词选项库 gh_prompt_library
-- 依据《全功能内置提示词系统设计 V1.0》《前端功能与后台提示词映射汇总 V1.0》
-- 沉淀为可配置、可复用、可迭代的提示词库，前端按 category 拉取并以列表选择呈现
-- ============================================================

DROP TABLE IF EXISTS `gh_prompt_library`;
CREATE TABLE `gh_prompt_library` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category`    varchar(30)  NOT NULL                COMMENT '提示词库分类（function/platform/product/material/scene/style/selling/size/quality/negative/camera/option）',
  `prompt_key`  varchar(120) NOT NULL                COMMENT '点分式唯一标识，如 function.white_bg',
  `label`       varchar(100) NOT NULL                COMMENT '前端显示名',
  `prompt_text` text                                 COMMENT '提示词内容（正向约束/负向约束/参数描述）',
  `scope`       varchar(200) DEFAULT NULL            COMMENT '适用功能（逗号分隔，如 white_bg,change_bg；空表示所有功能）',
  `model`       varchar(20)  NOT NULL DEFAULT 'all'  COMMENT '适配模型（gpt/gemini/all）',
  `priority`    int(11)      NOT NULL DEFAULT 100    COMMENT '拼接优先级（数字越小越靠前）',
  `is_default`  char(1)      NOT NULL DEFAULT '0'    COMMENT '是否默认（0否 1是）',
  `sort`        int(11)      NOT NULL DEFAULT 0      COMMENT '排序权重',
  `status`      char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  `version`     varchar(20)  NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
  `create_by`   varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prompt_key` (`prompt_key`),
  KEY `idx_category` (`category`),
  KEY `idx_scope` (`scope`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提示词选项库表';

-- ============================================================
-- 1. 功能提示词库 category=function（对应文档第6节10个功能）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`is_default`,`sort`,`remark`) VALUES
('function','function.white_bg','AI白底图','完整保留产品结构、比例、材质、颜色、视角和部件数量；产品居中；边缘完整；去除原背景、杂物和环境反射；背景为纯白；无透视畸变；边缘干净，无白边、毛边、锯齿和抠图痕迹；商业产品摄影；真实材质；清晰细节。负向：不改变结构，不增加部件，不裁切，不变形，不出现重复产品，不出现灰脏背景、硬阴影、过曝和噪点。','white_bg',10,'1',1,'锁定产品结构，生成标准白底图'),
('function','function.white_bg_to_scene','白底图生成背景','自动识别产品类型、结构、材质和视角；参考图只用于参考场景、风格、布局、空间关系和光影，不改变产品本身；完整保留产品结构、颜色、材质和比例；产品与场景的透视、视角、光向、色温、亮度和饱和度一致；自动生成接触阴影、环境反射和合理遮挡关系；去除白边、抠图痕迹和悬浮感；背景真实整洁，摆件克制，不遮挡主体。负向：不改变产品结构；不新增错误部件；不让背景主体喧宾夺主；不出现明显拼贴感、透视冲突和光影冲突。','change_bg',10,'1',2,'将白底商品自然融合到场景中'),
('function','function.retouch','产品精修','保持产品结构、轮廓、颜色、比例、视角和位置不变；修复脏点、划痕、噪点、模糊、锯齿和局部曝光问题；增强木纹、布纹、皮纹、金属、玻璃、石材和藤编的真实质感；保留自然纹理与使用痕迹；校正偏色；提高细节清晰度但避免过度锐化和磨皮。负向：不塑料化，不改变材质类型，不添加部件，不改变产品颜色，不产生重复纹理、过度反光或油腻高光。','retouch',10,'1',3,'提升材质、清晰度、商业质感'),
('function','function.ai_model','AI模特','人体比例自然，姿势符合人体工学；面部五官对称自然；皮肤细腻但保留真实肌理；双手、手指、脚趾数量与结构正确；头发边缘自然，无粘连和重复发丝；服装结构完整，无穿模和异常褶皱；人物与家具接触关系合理；坐姿时重心、腿部和手臂位置自然；人物不遮挡产品核心结构；人物光影、色温和场景一致。负向：无额外肢体、缺失肢体、扭曲手指、重复人物、塑料皮肤、面部变形、衣物破损、身体穿透家具、悬浮和错误比例。','ai_model',10,'1',4,'生成自然人物与家具搭配图'),
('function','function.main_image','主图设计','商品为绝对视觉中心；根据平台和尺寸自动设置安全区域；商品占比合理，不裁切核心部位；卖点层级清晰；文字与商品保持安全距离；背景简洁，视觉焦点明确；保持品牌色和整体风格统一；输出多方案时保持商品一致，只改变营销表达或构图。负向：不生成乱码、虚假认证、虚假参数、未经确认的价格或折扣；不改变商品结构和颜色。','main_image',10,'1',5,'生成电商主图'),
('function','function.detail_a_plus','详情图/A+','按封面、核心卖点、材质、产品细节、场景应用、尺寸参数、安装说明、品牌故事等模块组织；每页只表达一个核心信息；商品结构、颜色和材质在所有页面保持一致；文案层级清晰；保留安全边距；局部细节图与实际产品对应；根据平台自动匹配页面宽度、语言和信息密度；整套页面视觉统一。负向：不得虚构尺寸、认证、材质、功能和售后承诺；不得生成乱码；不得出现不同页面商品结构不一致。','detail',10,'1',6,'生成详情页与A+页面'),
('function','function.dimension_mark','尺寸标记','保持产品结构、比例和位置不变；尺寸线对应准确部位；箭头方向正确；横线和竖线保持水平垂直；单位统一；文字放在留白区域，避免遮挡主体；样式清晰适合电商阅读。数据规则：用户未输入尺寸时不得猜测或虚构数值。负向：不改变产品比例，不错误连接尺寸线，不混用单位，不遮挡关键结构。','dimension',10,'1',7,'生成尺寸说明图'),
('function','function.banner','Banner设计','根据尺寸自动构图；商品区和文案区明确分离；预留标题、副标题、价格或按钮安全区；商品不变形，不裁切核心部位；Logo位置统一；背景服务于商品，不喧宾夺主；促销层级清晰；保持品牌一致性。负向：不生成乱码；不虚构价格、折扣和活动规则；不遮挡商品，不堆叠过多装饰。','banner',10,'1',8,'生成营销横幅图'),
('function','function.batch','批量生成','同一商品在所有结果中保持结构、颜色、材质、比例和视角逻辑一致；不同方案只改变场景、构图、营销方向或版式；自动检查变形、重复、失败结果和低清结果；每套方案分配版本号；保持品牌、平台和尺寸规则一致。负向：禁止每张图出现不同产品结构；禁止同一套页面风格失控；禁止重复方案和明显低质量结果。','batch',10,'1',9,'批量生成多套方案'),
('function','function.dialog_edit','对话改图/局部修改','只修改用户明确指出的区域、对象或属性；未提及区域保持不变；保持原图尺寸、构图、视角、光线和整体风格；调整大小时必须转化为明确比例或画面占比；调整角度时必须转化为明确度数；修改后重新匹配阴影、透视、色温和环境反射。负向：不额外删除或新增其他物体；不改变非目标区域；不引发全图重绘和结构漂移。','dialog_edit',10,'1',10,'精准局部修改');

-- ============================================================
-- 2. 平台提示词库 category=platform（对应文档第9节）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`,`remark`) VALUES
('platform','platform.taobao','淘宝/天猫','符合淘宝电商主图视觉习惯，商品主体突出，卖点清晰，适合移动端浏览，提升点击吸引力。','main_image,banner,change_bg',20,1,'淘宝主图'),
('platform','platform.jd','京东','符合京东商品图片展示逻辑，信息明确，结构清晰，兼顾品牌感与转化效率。','main_image,banner,change_bg',20,2,'京东'),
('platform','platform.pdd','拼多多','符合拼多多高转化电商视觉，突出价格优势与促销氛围，商品主体清晰。','main_image,banner',20,3,'拼多多'),
('platform','platform.amazon','Amazon','符合Amazon图片规范，画面简洁，主体清晰，信息准确，避免夸张促销元素。','main_image,change_bg,banner',20,4,'Amazon主图/附图'),
('platform','platform.amazon_aplus','Amazon A+','符合Amazon A+版式逻辑，模块清晰，品牌感强，图文层级分明。','detail',20,5,'Amazon A+'),
('platform','platform.taobao_detail','淘宝详情','符合淘宝详情页展示习惯，卖点突出，信息密度适中，适合移动端长图浏览。','detail',20,6,'淘宝详情'),
('platform','platform.shopee','Shopee','符合Shopee电商视觉风格，突出价格敏感型电商氛围和快速转化。','main_image,banner,change_bg',20,7,'Shopee'),
('platform','platform.shopify','Shopify','符合独立站品牌展示逻辑，强调品牌感、质感和页面统一性。','banner,main_image',20,8,'Shopify独立站'),
('platform','platform.wayfair','Wayfair','符合家具独立站/Wayfair风格，注重生活方式展示和家居氛围。','main_image,change_bg,detail',20,9,'Wayfair');

-- ============================================================
-- 3. 产品类别提示词库 category=product（对应文档第7节）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`priority`,`sort`) VALUES
('product','product.sofa','沙发','锁定座位数量、扶手、靠背、坐垫、脚架、模块组合、布纹/皮纹，保持整体结构与对称关系。',30,1),
('product','product.bed','床','锁定床头板、床架、软包结构、灯带、床脚、对称关系，保持床头与床身比例。',30,2),
('product','product.dining_table','餐桌','锁定桌面形状、厚度、桌腿结构、材质、边角和比例，保持桌面水平与桌腿对称。',30,3),
('product','product.chair','餐椅/办公椅','锁定靠背、扶手、坐垫、椅腿、滚轮、人体工学结构，保持各部件连接关系正确。',30,4),
('product','product.tv_stand','电视柜/边柜','锁定柜门数量、抽屉、把手、脚架、格栅和储物结构，保持对称与开合逻辑。',30,5),
('product','product.coffee_table','茶几','锁定桌面、桌腿、储物空间、圆角、木纹方向，保持桌面与桌腿结构稳定。',30,6),
('product','product.lamp','灯具','锁定灯罩、灯杆、底座、发光区域、材质与电线结构，保持灯体比例与发光部位合理。',30,7),
('product','product.outdoor','户外家具','锁定防水材质、藤编、金属框架、坐垫，保持户外光线和植被真实性。',30,8);

-- ============================================================
-- 4. 材质提示词库 category=material（对应文档第8节）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`priority`,`sort`) VALUES
('material','material.wood','实木','木纹方向自然、纹理连续、拼接合理、边角真实、无塑料感。',40,1),
('material','material.fabric','布艺','织物纤维可见、经纬纹理均匀、自然褶皱、柔软但不过度平滑。',40,2),
('material','material.leather','皮革','细腻皮纹、轻微自然光泽、合理折痕、无油腻塑料反光。',40,3),
('material','material.metal','金属','细腻拉丝或喷涂质感、微弱环境反光、边缘清晰。',40,4),
('material','material.glass','玻璃','通透明亮、折射合理、轻微反光、无浑浊与错误穿透。',40,5),
('material','material.stone','石材','纹理自然不重复、哑光或合理抛光、边缘厚度真实。',40,6),
('material','material.rattan','藤编','编织孔洞清晰、结构连续、自然投影、无重复和断裂。',40,7);

-- ============================================================
-- 5. 场景提示词库 category=scene（对应文档6.2）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
('scene','scene.living_room','客厅','指定空间类别为客厅，沙发茶几电视柜组合，居家氛围。','change_bg,main_image,detail,banner',50,1),
('scene','scene.bedroom','卧室','指定空间类别为卧室，床与床头柜组合，安静私密氛围。','change_bg,main_image,detail,banner',50,2),
('scene','scene.dining_room','餐厅','指定空间类别为餐厅，餐桌餐椅餐边柜组合，用餐氛围。','change_bg,main_image,detail,banner',50,3);

-- ============================================================
-- 6. 风格提示词库 category=style（对应文档第4节、6.2）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
('style','style.cream','奶油风','低饱和米白色调、柔和温暖氛围，材质偏布艺与浅木。','change_bg,main_image,detail,banner',60,1),
('style','style.modern_farmhouse','田园农舍','农舍风木质与温馨乡村感，自然材质与暖色调。','change_bg,main_image,detail,banner',60,2),
('style','style.modern_minimal','现代简约','简洁、克制、现代线条，低装饰高留白。','change_bg,main_image,detail,banner',60,3),
('style','style.japandi','Japandi','日式与北欧融合，自然克制，低饱和与原木质感。','change_bg,main_image,detail,banner',60,4),
('style','style.mcm','MCM','世纪中叶现代风，胡桃木与黄铜元素，几何线条。','change_bg,main_image,detail,banner',60,5),
('style','style.coastal','Coastal','滨海度假风，蓝白主色，藤编与浅木，明亮通透。','change_bg,main_image,detail,banner',60,6);

-- ============================================================
-- 7. 卖点提示词库 category=selling（对应文档6.5）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
('selling','selling.premium_material','高品质材料','强调高品质材质与用料考究。','main_image,detail',70,1),
('selling','selling.durable','耐用结实','强调耐用与稳固结构。','main_image,detail',70,2),
('selling','selling.multi_use','多功能使用','强调多场景使用与灵活变换。','main_image,detail',70,3),
('selling','selling.easy_install','安装简单','强调安装便捷与省时省力。','main_image,detail',70,4),
('selling','selling.eco_friendly','环保材料','强调环保属性与安全无害。','main_image,detail',70,5);

-- ============================================================
-- 8. 尺寸/输出库 category=size（对应文档6.1/6.5/9）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
('size','size.square_800','800×800','输出尺寸800×800，正方形构图，安全区居中。','white_bg,main_image,retouch',80,1),
('size','size.square_1024','1024×1024','输出尺寸1024×1024，正方形构图。','white_bg,main_image,retouch,ai_model',80,2),
('size','size.square_2000','2000×2000','输出尺寸2000×2000，正方形高分辨率，构图安全区居中。','white_bg,main_image,change_bg,retouch,ai_model',80,3),
('size','size.amazon_main_2000','Amazon主图2000×2000','符合Amazon主图规范，2000×2000，纯白背景，商品占比85%以上。','main_image',80,4),
('size','size.ratio_3_4','3:4竖版','3:4竖版构图，适合手机端浏览。','main_image,detail,banner',80,5),
('size','size.ratio_4_3','4:3横版','4:3横版构图，适合PC端展示。','main_image,detail,banner',80,6),
('size','size.banner_landscape','Banner横版','横版Banner构图，预留标题/副标题/价格/按钮安全区。','banner',80,7),
('size','size.banner_portrait','Banner竖版','竖版Banner构图，预留标题/副标题/价格/按钮安全区。','banner',80,8);

-- ============================================================
-- 9. 质量约束库 category=quality（对应文档第5节家具通用质量约束）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`priority`,`is_default`,`sort`) VALUES
('quality','quality.furniture_default','家具通用质量约束','保持主体产品结构、尺寸比例、颜色、材质、部件数量和原始设计不变。产品边缘完整，不裁切，不遮挡核心结构。真实商业摄影质感，避免明显3D建模感、塑料感和过度平滑。材质纹理自然：木纹有方向与拼接逻辑，布艺有纤维与自然褶皱，皮革有细腻皮纹，金属和玻璃仅保留合理微弱反光。透视自然，横平竖直，无广角夸张、梯形畸变和比例失衡。画面清晰，暗部有细节，高光不过曝，阴影自然过渡。背景干净，不出现杂色、噪点、乱码、重复纹理或无关物体。除非用户明确要求，否则不得增加、删除、替换产品部件。',90,'1',1);

-- ============================================================
-- 10. 负向约束库 category=negative（对应文档第4节负向约束库）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`priority`,`is_default`,`sort`) VALUES
('negative','negative.general','通用负向约束','变形、重复、错误肢体、缺失肢体、扭曲手指、错误结构、乱码、文字错误、透视畸变、比例失衡、塑料感、过度平滑、悬浮、穿模、硬阴影、过曝、噪点、灰脏背景、重复纹理。',95,'1',1),
('negative','negative.white_bg','白底图负向约束','不改变结构，不增加部件，不裁切，不变形，不出现重复产品，不出现灰脏背景、硬阴影、过曝和噪点。',95,'0',2),
('negative','negative.change_bg','生成背景负向约束','不改变产品结构；不新增错误部件；不让背景主体喧宾夺主；不出现明显拼贴感、透视冲突和光影冲突。',95,'0',3),
('negative','negative.ai_model','AI模特负向约束','无额外肢体、缺失肢体、扭曲手指、重复人物、塑料皮肤、面部变形、衣物破损、身体穿透家具、悬浮和错误比例。',95,'0',4),
('negative','negative.main_image','主图负向约束','不生成乱码、虚假认证、虚假参数、未经确认的价格或折扣；不改变商品结构和颜色。',95,'0',5),
('negative','negative.dialog_edit','局部修改负向约束','不额外删除或新增其他物体；不改变非目标区域；不引发全图重绘和结构漂移。',95,'0',6);

-- ============================================================
-- 11. 镜头/角度/占比标准化 category=camera（对应文档第9节）
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
('camera','camera.distance.wide','远景','镜头距离：远景，展示整体环境与空间关系。','change_bg,main_image,detail,banner,ai_model',100,1),
('camera','camera.distance.mid_wide','中远景','镜头距离：中远景，主体与环境并重。','change_bg,main_image,detail,banner,ai_model',100,2),
('camera','camera.distance.mid','中景','镜头距离：中景，主体突出，环境适度。','change_bg,main_image,detail,banner,ai_model',100,3),
('camera','camera.distance.mid_close','中近景','镜头距离：中近景，主体为主，环境弱化。','main_image,detail,ai_model',100,4),
('camera','camera.distance.close','特写','镜头距离：特写，聚焦局部细节。','main_image,detail,retouch',100,5),
('camera','camera.h.front','正面0°','水平角度：正面0°，对称构图。','white_bg,change_bg,main_image',100,6),
('camera','camera.h.left30','左侧30°','水平角度：左侧30°，轻微侧面。','change_bg,main_image',100,7),
('camera','camera.h.left45','左侧45°','水平角度：左侧45°，立体感强。','change_bg,main_image',100,8),
('camera','camera.h.right30','右侧30°','水平角度：右侧30°，轻微侧面。','change_bg,main_image',100,9),
('camera','camera.h.right45','右侧45°','水平角度：右侧45°，立体感强。','change_bg,main_image',100,10),
('camera','camera.v.eye','平视0°','俯仰角度：平视0°，自然视角。','white_bg,change_bg,main_image',100,11),
('camera','camera.v.slight_down10','轻微俯视10°','俯仰角度：轻微俯视10°，略带俯瞰。','change_bg,main_image',100,12),
('camera','camera.v.down15','俯视15°','俯仰角度：俯视15°，展示台面与布局。','change_bg,main_image,detail',100,13),
('camera','camera.v.down30','俯视30°','俯仰角度：俯视30°，俯瞰构图。','change_bg,detail',100,14),
('camera','camera.v.slight_up10','轻微仰视10°','俯仰角度：轻微仰视10°，强调气势。','main_image,banner',100,15),
('camera','camera.occupation.30','主体占比30%','主体占比30%，环境为主。','change_bg,detail,banner',100,16),
('camera','camera.occupation.40','主体占比40%','主体占比40%，环境并重。','change_bg,detail,banner',100,17),
('camera','camera.occupation.50','主体占比50%','主体占比50%，均衡构图。','change_bg,main_image,detail',100,18),
('camera','camera.occupation.60','主体占比60%','主体占比60%，主体为主。','main_image,detail',100,19),
('camera','camera.occupation.70','主体占比70%','主体占比70%，主体突出。','main_image',100,20),
('camera','camera.occupation.80','主体占比80%','主体占比80%，主体绝对突出。','main_image,white_bg',100,21),
('camera','camera.position.center','居中','主体位置：居中。','white_bg,change_bg,main_image,detail',100,22),
('camera','camera.position.left_third','左侧三分位','主体位置：左侧三分位。','change_bg,main_image,banner',100,23),
('camera','camera.position.right_third','右侧三分位','主体位置：右侧三分位。','change_bg,main_image,banner',100,24),
('camera','camera.position.wall_center','靠墙居中','主体位置：靠墙居中。','change_bg,detail',100,25);

-- ============================================================
-- 12. 功能内选项 category=option（对应文档6.1-6.9各功能前端选项→后台key）
-- scope 标识所属功能
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`sort`) VALUES
-- 6.1 AI白底图：白底样式
('option','white_bg.shadow.none','无阴影','纯白背景，产品底部不出现明显投影，但保持自然接地感。','white_bg',110,1),
('option','white_bg.shadow.natural','自然阴影','生成方向正确、强度克制、边缘柔和的接触阴影，不悬浮。','white_bg',110,2),
-- 6.3 产品精修：精修类型
('option','retouch.cleanup','去瑕疵','去除污点、划痕、噪点、脏污与杂色。','retouch',110,1),
('option','retouch.sharpness','增强清晰度','增强细节但不过度锐化，保留自然质感。','retouch',110,2),
('option','retouch.material','提升材质','强化木纹、布纹、皮纹、金属、玻璃、石材与藤编真实感。','retouch',110,3),
('option','retouch.color_correct','颜色校正','校正曝光与偏色，还原真实色彩。','retouch',110,4),
-- 6.4 AI模特：性别/年龄/发型/人种/姿势/服装
('option','model.gender.female','女','指定女性模特。','ai_model',110,1),
('option','model.gender.male','男','指定男性模特。','ai_model',110,2),
('option','model.age.young_adult','年轻','年轻成人形象。','ai_model',110,3),
('option','model.age.middle','中年','中年成熟形象。','ai_model',110,4),
('option','model.hair.long','长发','长发造型。','ai_model',110,5),
('option','model.hair.short','短发','短发造型。','ai_model',110,6),
('option','model.hair.curly','卷发','卷发造型。','ai_model',110,7),
('option','model.ethnicity.western','欧美','欧美人种特征。','ai_model',110,8),
('option','model.ethnicity.asian','亚洲','亚洲人种特征。','ai_model',110,9),
('option','model.pose.sitting','坐姿','自然坐姿并符合人体工学。','ai_model',110,10),
('option','model.pose.standing','站姿','自然站姿。','ai_model',110,11),
('option','model.outfit.casual_home','休闲家居','符合家居场景的休闲服装风格。','ai_model',110,12),
('option','model.outfit.business','商务','商务正装风格。','ai_model',110,13),
-- 6.5 主图设计：语言/生成数量
('option','lang.zh_cn','中文','生成中文文案与排版。','main_image,detail,banner',110,1),
('option','lang.en_us','英文','生成英文文案与排版。','main_image,detail,banner',110,2),
('option','count.1','1张','单次生成1张。','main_image,white_bg,change_bg,retouch,ai_model',110,3),
('option','count.3','3张','单次生成3张。','main_image,white_bg,change_bg,retouch,ai_model',110,4),
('option','count.4','4张','单次生成4张。','main_image,white_bg,change_bg,retouch,ai_model',110,5),
-- 6.6 详情/A+：页面数量/模块
('option','page_count.3','3页','生成3页详情。','detail',110,1),
('option','page_count.5','5页','生成5页详情。','detail',110,2),
('option','page_count.8','8页','生成8页详情。','detail',110,3),
('option','module.intro','产品介绍','生成产品介绍模块。','detail',110,4),
('option','module.material','材质说明','生成材质说明模块。','detail',110,5),
('option','module.dimension','尺寸参数','生成尺寸参数模块。','detail',110,6),
('option','module.scene_usage','场景应用','生成使用场景模块。','detail',110,7),
('option','module.install','安装说明','生成安装步骤模块。','detail',110,8),
('option','module.brand_story','品牌故事','生成品牌介绍模块。','detail',110,9),
-- 6.7 尺寸标记：单位/样式
('option','unit.cm','cm','统一使用cm单位。','dimension',110,1),
('option','unit.inch','inch','统一使用inch单位。','dimension',110,2),
('option','dimension.style.clean','简洁样式','简洁电商尺寸标记风格。','dimension',110,3),
('option','dimension.style.technical','技术样式','更偏参数说明图风格。','dimension',110,4),
-- 6.8 Banner：用途/尺寸
('option','banner.purpose.promo','促销','强调促销氛围与价格优势。','banner',110,1),
('option','banner.purpose.new_arrival','新品','强调新品展示与首发氛围。','banner',110,2),
('option','banner.purpose.branding','品牌展示','强调品牌形象与质感。','banner',110,3),
-- 6.9 批量生成：任务/套数
('option','batch.task.main_image','主图','批量主图任务。','batch',110,1),
('option','batch.task.detail','详情/A+','批量详情页任务。','batch',110,2),
('option','batch.task.banner','Banner','批量Banner任务。','batch',110,3),
('option','batch.count.3','3套','生成3套方案。','batch',110,4),
('option','batch.count.5','5套','生成5套方案。','batch',110,5);

-- ============================================================
-- 字典：提示词库分类 gh_prompt_library_category（供后台管理端下拉）
-- ============================================================
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_library_category';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'gh_prompt_library_category';
INSERT INTO `sys_dict_type` (`dict_name`,`dict_type`,`status`,`create_by`,`create_time`,`remark`) VALUES
('提示词库分类','gh_prompt_library_category','0','admin',NOW(),'光合AI提示词选项库分类');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`remark`) VALUES
(1,'功能','function','gh_prompt_library_category','','primary','N','0','admin',NOW(),'功能提示词库'),
(2,'平台','platform','gh_prompt_library_category','','success','N','0','admin',NOW(),'平台提示词库'),
(3,'产品类别','product','gh_prompt_library_category','','info','N','0','admin',NOW(),'产品类别提示词库'),
(4,'材质','material','gh_prompt_library_category','','warning','N','0','admin',NOW(),'材质提示词库'),
(5,'场景','scene','gh_prompt_library_category','','primary','N','0','admin',NOW(),'场景提示词库'),
(6,'风格','style','gh_prompt_library_category','','success','N','0','admin',NOW(),'风格提示词库'),
(7,'卖点','selling','gh_prompt_library_category','','info','N','0','admin',NOW(),'卖点提示词库'),
(8,'尺寸/输出','size','gh_prompt_library_category','','warning','N','0','admin',NOW(),'尺寸输出库'),
(9,'质量约束','quality','gh_prompt_library_category','','danger','N','0','admin',NOW(),'质量约束库'),
(10,'负向约束','negative','gh_prompt_library_category','','danger','N','0','admin',NOW(),'负向约束库'),
(11,'镜头/角度/占比','camera','gh_prompt_library_category','','primary','N','0','admin',NOW(),'镜头角度占比标准化'),
(12,'功能内选项','option','gh_prompt_library_category','','info','N','0','admin',NOW(),'各功能前端选项');

-- ============================================================
-- 后台管理菜单：提示词选项库管理
-- ============================================================
-- 菜单ID沿用 RuoYi 业务菜单段（2000-2999 为光合业务），此处使用 2080 段
DELETE FROM `sys_menu` WHERE `menu_id` IN (2080,2081,2082,2083,2084,2085);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
(2080,'提示词选项库',2000,30,'promptLibrary','business/promptLibrary/index',1,0,'C','0','0','gh:promptLibrary:list','dict','admin',NOW(),'光合AI提示词选项库管理');
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
(2081,'提示词选项库查询',2080,1,'','','1','0','F','0','0','gh:promptLibrary:query','#','admin',NOW(),''),
(2082,'提示词选项库新增',2080,2,'','','1','0','F','0','0','gh:promptLibrary:add','#','admin',NOW(),''),
(2083,'提示词选项库修改',2080,3,'','','1','0','F','0','0','gh:promptLibrary:edit','#','admin',NOW(),''),
(2084,'提示词选项库删除',2080,4,'','','1','0','F','0','0','gh:promptLibrary:remove','#','admin',NOW(),''),
(2085,'提示词选项库导出',2080,5,'','','1','0','F','0','0','gh:promptLibrary:export','#','admin',NOW(),'');