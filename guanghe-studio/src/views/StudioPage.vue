<template>
  <div class="studio-page" :class="pageClass">
    <template v-if="page === 'banner'">
      <section class="workspace two-col">
        <div class="left-col">
          <div class="workflow">
            <div class="step" v-for="(item, idx) in bannerSteps" :key="item">
              <div class="dot" :class="{ active: idx === 0 }">{{ idx + 1 }}</div>
              <span>{{ item }}</span>
              <i class="line" v-if="idx < bannerSteps.length - 1"></i>
            </div>
          </div>

          <div class="toolbar-row">
            <button class="ghost-btn"><el-icon><FullScreen /></el-icon>适配屏幕</button>
            <div class="mini-toolbar banner-toolbar">
              <div class="edit-actions">
                <button class="tool-label"><el-icon><RefreshLeft /></el-icon>撤销</button>
                <button class="tool-label"><el-icon><RefreshRight /></el-icon>恢复</button>
                <button class="tool-label"><el-icon><Delete /></el-icon>清空</button>
              </div>
              <div class="zoom-actions">
                <button class="icon-btn"><el-icon><Minus /></el-icon></button>
                <button class="text-btn">100%</button>
                <button class="icon-btn"><el-icon><Plus /></el-icon></button>
                <button class="icon-btn"><el-icon><FullScreen /></el-icon></button>
              </div>
            </div>
          </div>

          <div class="canvas banner-canvas">
            <div class="canvas-empty">
              <el-icon :size="54"><Picture /></el-icon>
              <h3>拖拽图片到画布，或从右侧上传素材</h3>
              <p>支持 JPG / PNG / WebP，建议宽度 >= 1200px</p>
              <p class="canvas-recommend">建议使用高清素材，获得更佳效果</p>
            </div>
          </div>

          <div class="template-strip">
            <div class="strip-head">
              <h4>热门模板</h4>
              <button class="link-btn"><el-icon><Refresh /></el-icon>换一换</button>
            </div>
            <div class="chips">
              <span v-for="tab in bannerTabs" :key="tab" class="chip" :class="{ active: tab === '全部' }">{{ tab }}</span>
            </div>
            <div class="template-row">
              <article v-for="tpl in bannerTemplates" :key="tpl.title" class="template-card">
                <img :src="tpl.image" :alt="tpl.title" />
                <div class="template-title">{{ tpl.title }}</div>
              </article>
              <button class="next-btn"><el-icon><ArrowRight /></el-icon></button>
            </div>
            <div class="footer-note">建议尺寸：1200x300px、1920x600px、1920x1080px 等主流尺寸</div>
          </div>
        </div>

        <aside class="right-col">
          <div class="panel">
            <div class="panel-title">画布尺寸</div>
            <div class="size-row">
              <button class="size-chip active">1200×300（横幅）</button>
              <button class="size-chip">1920×600（通栏）</button>
              <button class="size-chip">1920×1080（大屏）</button>
            </div>
            <div class="custom-size-title">自定义尺寸</div>
            <div class="custom-size">
              <label><span>宽度</span><span class="unit-input"><input value="1200" /><em>px</em></span></label>
              <label><span>高度</span><span class="unit-input"><input value="300" /><em>px</em></span></label>
            </div>
          </div>
          <div class="panel">
            <div class="panel-title">上传素材</div>
            <div class="upload-grid">
              <div class="upload-card primary"><el-icon><Upload /></el-icon><strong>上传产品图（必传）</strong><span>支持 JPG / PNG，最多 10 张</span></div>
              <div class="upload-card"><el-icon><Upload /></el-icon><strong>上传背景图（可选）</strong><span>支持 JPG / PNG，最多 5 张</span></div>
              <div class="upload-card"><el-icon><Camera /></el-icon><strong>上传 LOGO（可选）</strong><span>支持 PNG，透明背景更佳</span></div>
            </div>
          </div>
          <div class="panel">
            <div class="panel-title">Banner 类型</div>
            <div class="tile-grid">
              <button class="tile active"><b>促销活动</b><span>打折促销、限时优惠</span></button>
              <button class="tile"><b>新品上市</b><span>新品发布、产品推荐</span></button>
              <button class="tile"><b>品牌宣传</b><span>品牌故事、品牌形象</span></button>
              <button class="tile"><b>节日季节</b><span>节日活动、季节主题</span></button>
              <button class="tile"><b>信息通知</b><span>公告通知、店铺信息</span></button>
              <button class="tile"><b>店铺装修</b><span>店铺头图、页面装饰</span></button>
            </div>
          </div>
          <div class="panel">
            <div class="panel-title">核心目的</div>
            <div class="check-grid">
              <label v-for="item in bannerGoals" :key="item" class="check-item"><input type="checkbox" :checked="item === '提升销量 / 促销转化'" />{{ item }}</label>
            </div>
          </div>
          <div class="panel">
            <div class="panel-title">关键信息</div>
            <div class="field-list banner-key-fields">
              <div class="field"><span>主标题</span><input placeholder="输入主标题，如：SUMMER SALE" /></div>
              <div class="field"><span>副标题</span><input placeholder="输入副标题，如：UP TO 50% OFF" /></div>
              <div class="field"><span>按钮文案</span><input placeholder="输入按钮文案，如：SHOP NOW" /></div>
            </div>
          </div>
          <button class="generate-btn">生成 Banner（下一步）</button>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'retouch'">
      <section class="workspace two-col">
        <div class="left-col">
          <div class="promo-banner">
            <div>
              <h3>光合AI Studio 2.0 全新上线</h3>
              <p>更专业的AI设计体验，更高效的工作流程</p>
              <button class="ghost-mini">立即体验</button>
            </div>
          </div>
          <div class="workflow compact">
            <div class="step" v-for="(item, idx) in designSteps" :key="item">
              <div class="dot" :class="{ active: idx === 2 }">{{ idx + 1 }}</div>
              <span :class="{ active: idx === 2 }">{{ item }}</span>
              <i class="line" v-if="idx < designSteps.length - 1"></i>
            </div>
          </div>
          <div class="toolbar-row">
            <button class="ghost-btn"><el-icon><ScaleToOriginal /></el-icon>对比</button>
            <div class="mini-toolbar">
              <button class="icon-btn"><el-icon><RefreshLeft /></el-icon></button>
              <button class="icon-btn"><el-icon><RefreshRight /></el-icon></button>
              <button class="icon-btn"><el-icon><Delete /></el-icon></button>
              <button class="text-btn">100%</button>
              <button class="icon-btn"><el-icon><Plus /></el-icon></button>
              <button class="icon-btn"><el-icon><FullScreen /></el-icon></button>
            </div>
          </div>
          <div class="compare-canvas">
            <div class="compare-side">
              <img src="/images/chair-white-bg.png" alt="" />
              <span class="compare-tag">修前</span>
            </div>
            <div class="compare-side">
              <img src="/images/chair-white-bg.png" alt="" />
              <span class="compare-tag">修后</span>
            </div>
            <button class="compare-toggle"><el-icon><ArrowLeft /></el-icon><el-icon><ArrowRight /></el-icon></button>
          </div>
          <div class="footer-hint">提示：在左侧上传需要精修的图片，选择右侧精修功能，或在AI助手中输入您的需求。</div>
        </div>
        <aside class="right-col">
          <div class="panel">
            <div class="panel-title">创作配置</div>
            <div class="collapse-row">上传图片 <span>展开</span></div>
          </div>
          <div class="panel">
            <div class="panel-title">精修工具</div>
            <div class="tile-grid four">
              <button class="tile active"><b>智能优化</b><span>一键提升画质</span></button>
              <button class="tile"><b>去瑕疵</b><span>去除划痕、污渍等</span></button>
              <button class="tile"><b>纹理增强</b><span>增强材质纹理细节</span></button>
              <button class="tile"><b>色彩调整</b><span>调整亮度、对比度等</span></button>
              <button class="tile"><b>光影优化</b><span>优化光影层次</span></button>
              <button class="tile"><b>背景处理</b><span>纯色背景 / 自定义</span></button>
              <button class="tile"><b>锐化增强</b><span>提升清晰度</span></button>
              <button class="tile"><b>降噪处理</b><span>减少图片噪点</span></button>
            </div>
          </div>
          <div class="panel ai-panel">
            <div class="panel-title">AI 助手</div>
            <div class="chat">
              <div class="bubble bot">您好！我是光合AI助手，有什么可以帮您？</div>
              <div class="bubble user">帮我去掉椅子上的划痕和污渍</div>
              <div class="bubble bot">已为您智能修复划痕和污渍，增强了整体质感。</div>
            </div>
            <div class="chat-box">
              <textarea placeholder="请输入您的需求，描述越详细，效果越好..."></textarea>
              <div class="chat-foot"><span>0/2000</span><button class="send-btn">发送（-2积分）</button></div>
            </div>
          </div>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'white-bg' || page === 'background' || page === 'ai-model' || page === 'hero-image' || page === 'detail-page' || page === 'size-mark'">
      <section class="workspace two-col">
        <div class="left-col">
          <div class="promo-strip">
            <div class="promo-card" v-for="card in promoCards" :key="card.title">
              <div class="promo-copy">
                <h3>{{ card.title }}</h3>
                <p>{{ card.desc }}</p>
                <button class="ghost-mini">{{ card.btn }}</button>
              </div>
              <img :src="card.image" alt="" />
            </div>
          </div>
          <div class="workflow compact">
            <div class="step" v-for="(item, idx) in commonSteps" :key="item">
              <div class="dot" :class="{ active: idx === activeStepIndex }">{{ idx + 1 }}</div>
              <span :class="{ active: idx === activeStepIndex }">{{ item }}</span>
              <i class="line" v-if="idx < commonSteps.length - 1"></i>
            </div>
          </div>
          <div class="toolbar-row">
            <button class="ghost-btn"><el-icon><FullScreen /></el-icon>适配屏幕</button>
            <div class="mini-toolbar">
              <button class="icon-btn"><el-icon><RefreshLeft /></el-icon></button>
              <button class="icon-btn"><el-icon><RefreshRight /></el-icon></button>
              <button class="icon-btn"><el-icon><Delete /></el-icon></button>
              <button class="text-btn">100%</button>
              <button class="icon-btn"><el-icon><Plus /></el-icon></button>
              <button class="icon-btn"><el-icon><FullScreen /></el-icon></button>
            </div>
          </div>
          <div class="canvas big-canvas">
            <div class="canvas-empty">
              <el-icon :size="54"><Picture /></el-icon>
              <h3 v-if="page === 'white-bg'">拖拽图片到画布，或从右侧上传</h3>
              <h3 v-else-if="page === 'background'">拖拽图片到画布，或从右侧上传（参考图 + 上传图）</h3>
              <h3 v-else-if="page === 'ai-model'">拖拽图片到画布，或从右侧配置生成</h3>
              <h3 v-else-if="page === 'hero-image'">拖拽图片到画布，或点击右侧上传</h3>
              <h3 v-else-if="page === 'detail-page'">拖拽图片到画布，或从右侧上传素材</h3>
              <h3 v-else>拖拽图片到画布，或点击右侧上传</h3>
              <p>{{ canvasHint }}</p>
            </div>
          </div>
          <div class="footer-hint" v-if="page === 'white-bg' || page === 'background'">上传优质素材下载即可获得积分奖励，积分可用于生成图片或升级套餐 <a href="#">去上传</a></div>
          <div class="footer-hint" v-else-if="page === 'hero-image' || page === 'detail-page'">建议上传高质量的产品图片，以获得更好的生成效果。</div>
        </div>
        <aside class="right-col">
          <div class="panel">
            <div class="panel-head">
              <div class="panel-title">{{ pageRightTitle }}</div>
              <span class="collapse-link">收起 <el-icon><ArrowUp /></el-icon></span>
            </div>
            <div class="upload-zone">
              <el-icon :size="28"><Upload /></el-icon>
              <div>点击或拖拽图片到此处上传</div>
              <small>支持 JPG / PNG / WebP</small>
            </div>
          </div>
          <div class="panel" v-if="page === 'white-bg'">
            <div class="panel-head">
              <div class="panel-title">白底样式</div>
              <span class="collapse-link">收起 <el-icon><ArrowUp /></el-icon></span>
            </div>
            <div class="style-grid">
              <button class="style-card active"><img src="/images/chair-white-bg.png"><span>无阴影</span></button>
              <button class="style-card"><img src="/images/chair-scene-bg.png"><span>自然阴影</span></button>
            </div>
            <div class="helper-line">提示：无阴影白底更适合电商主图，自然阴影更具立体感</div>
          </div>
          <div class="panel" v-if="page === 'white-bg'">
            <div class="panel-head">
              <div class="panel-title">输出尺寸</div>
              <span class="collapse-link">收起 <el-icon><ArrowUp /></el-icon></span>
            </div>
            <div class="field-list">
              <div class="field"><span>尺寸</span><select><option>2000 × 2000 (1:1)</option></select></div>
            </div>
          </div>
          <div class="panel" v-else-if="page === 'background'">
            <div class="panel-head">
              <div class="panel-title">上传参考图</div>
              <span class="collapse-link">收起 <el-icon><ArrowUp /></el-icon></span>
            </div>
            <div class="thumb-row">
              <img v-for="img in bgThumbs" :key="img" :src="img" />
              <button class="add-box">+</button>
            </div>
            <div class="collapse-list">
              <div v-for="item in ['使用平台','场景分类','光线选择','风格选择','输出尺寸']" :key="item" class="collapse-row"><span>{{ item }}</span><span>展开</span></div>
            </div>
          </div>
          <div class="panel" v-else-if="page === 'ai-model'">
            <div class="panel-head">
              <div class="panel-title">配置选项</div>
              <span class="collapse-link">收起 <el-icon><ArrowUp /></el-icon></span>
            </div>
            <div class="collapse-list">
              <div class="collapse-row" v-for="item in ['上传商品图','性别选择','年龄选择','发型选择','人种选择','姿势选择','服装选择','场景风格','输出设置']" :key="item"><span>{{ item }}</span><span>展开</span></div>
            </div>
          </div>
          <div class="panel" v-else-if="page === 'hero-image'">
            <div class="field-list">
              <div class="field"><span>商品图（必选）</span><button class="upload-mini">上传商品图</button></div>
              <div class="field"><span>参考图（可选）</span><button class="upload-mini">上传参考图</button></div>
            </div>
            <div class="field-list">
              <div class="field"><span>目标平台</span><select><option>亚马逊 Amazon</option></select></div>
              <div class="field"><span>语言</span><select><option>英语（美国）</option></select></div>
            </div>
            <div class="panel-subtitle">主图用途</div>
            <div class="tile-grid four compact">
              <button class="tile active"><b>新品上市</b><span>突出商品吸引关注</span></button>
              <button class="tile"><b>提升转化</b><span>突出核心卖点</span></button>
              <button class="tile"><b>季节节日</b><span>契合主题</span></button>
              <button class="tile"><b>促销活动</b><span>展示折扣信息</span></button>
            </div>
          </div>
          <div class="panel" v-else-if="page === 'detail-page'">
            <div class="field-list">
              <div class="field"><span>选择尺寸</span><select><option>970 宽度（亚马逊 A+ 推荐）</option></select></div>
            </div>
            <div class="panel-subtitle">平台与语言</div>
            <div class="platform-row">
              <button class="size-chip active">亚马逊 Amazon</button>
              <button class="size-chip">Shopee</button>
              <button class="size-chip">Lazada</button>
              <button class="size-chip">速卖通</button>
            </div>
            <div class="panel-subtitle">核心卖点</div>
            <div class="tag-cloud">
              <span v-for="tag in detailTags" :key="tag">{{ tag }}</span>
            </div>
          </div>
          <div class="panel" v-else>
            <div class="field-list">
              <div class="field"><span>尺寸</span><select><option>2000 × 2000 (1:1)</option></select></div>
            </div>
          </div>
          <div class="panel ai-panel">
            <div class="panel-head">
              <div class="panel-title">AI 助手</div>
              <span class="collapse-link blue">清空对话</span>
            </div>
            <div class="chat">
              <div class="bubble bot">您好！我是光合AI助手，有什么可以帮您？</div>
              <div class="bubble user">帮我去掉背景，保留主体</div>
              <div class="bubble bot">好的，正在为您处理...</div>
            </div>
            <div class="chat-box">
              <textarea placeholder="请输入您的需求，描述越详细，效果越好..."></textarea>
              <div class="chat-foot"><span>0/2000</span><button class="send-btn">发送（-2积分）</button></div>
            </div>
          </div>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'creator-center' || page === 'material-market' || page === 'history' || page === 'projects' || page === 'my-materials' || page === 'favorites' || page === 'points'">
      <section class="library-layout" :class="page">
        <div class="library-main">
          <div v-if="page === 'creator-center'" class="creator-head">
            <div>
              <h2>我的素材</h2>
              <p>管理您上传的素材，查看使用数据与收益</p>
            </div>
            <div class="searchbar">
              <el-icon><Search /></el-icon>
              <input placeholder="搜索我的素材" />
            </div>
          </div>
          <div v-else-if="page === 'history'" class="history-head">
            <div class="switch-group"><button class="switch active">按时间</button><button class="switch">按项目</button></div>
            <div class="searchbar"><el-icon><Search /></el-icon><input placeholder="搜索项目名称 / 商品名称 / 关键词" /></div>
          </div>
          <div v-else-if="page === 'material-market'" class="market-head">
            <div class="filter-row">
              <button class="filter">产品分类 <b>沙发</b></button>
              <button class="filter">空间分类 <b>客厅</b></button>
              <button class="filter">风格标签 <b>奶油风</b></button>
              <button class="filter">平台适配 <b>Amazon</b></button>
              <button class="filter">更多筛选</button>
            </div>
            <div class="searchbar"><el-icon><Search /></el-icon><input placeholder="搜索素材关键词，例如：奶油风 客厅 沙发" /></div>
          </div>
          <div class="card-grid" v-if="page === 'creator-center'">
            <article class="asset-card" v-for="card in creatorCards" :key="card.title">
              <img :src="card.image" />
              <div class="asset-body">
                <div class="asset-title">{{ card.title }}</div>
                <div class="asset-meta">{{ card.meta }}</div>
              </div>
            </article>
          </div>
          <div class="market-grid" v-else-if="page === 'material-market'">
            <article class="market-card" v-for="card in marketCards" :key="card.title">
              <img :src="card.image" />
              <div class="market-body">
                <div class="market-title">{{ card.title }}</div>
                <div class="market-meta">{{ card.tags }}</div>
              </div>
            </article>
          </div>
          <div class="history-list" v-else-if="page === 'history'">
            <div class="day-group" v-for="group in historyGroups" :key="group.label">
              <div class="day-title">{{ group.label }}</div>
              <div class="history-row">
                <article class="history-card" v-for="item in group.items" :key="item.title">
                  <img :src="item.image" />
                  <div class="history-body">
                    <div class="history-type">{{ item.type }}</div>
                    <div class="history-title">{{ item.title }}</div>
                    <div class="history-meta">{{ item.time }}</div>
                  </div>
                </article>
                <div class="history-card add-card"><el-icon><Plus /></el-icon><span>查看全部</span></div>
              </div>
            </div>
          </div>
          <div class="simple-grid" v-else>
            <div class="simple-card" v-for="img in simpleImages" :key="img">
              <img :src="img" />
              <div class="simple-copy">
                <h3>{{ pageTitles[page] }}</h3>
                <p>围绕参考规范构建的统一视觉层级</p>
              </div>
            </div>
          </div>
        </div>

        <aside class="library-aside">
          <div class="panel" v-if="page === 'creator-center'">
            <h3>我的素材数据</h3>
            <div class="stat-grid">
              <div class="stat"><span>素材总数</span><b>156</b></div>
              <div class="stat"><span>上架中</span><b>128</b></div>
              <div class="stat"><span>总下载量</span><b>12,856</b></div>
              <div class="stat"><span>获得积分</span><b>3,560</b></div>
            </div>
            <div class="mini-chart"></div>
          </div>
          <div class="panel" v-else-if="page === 'history'">
            <h3>筛选条件</h3>
            <div class="collapse-list">
              <div class="collapse-row" v-for="item in ['时间范围','任务状态','功能类型','生成数量']" :key="item"><span>{{ item }}</span><span>展开</span></div>
            </div>
            <button class="generate-btn">应用筛选</button>
            <button class="ghost-wide">保存为常用筛选</button>
          </div>
          <div class="panel" v-else-if="page === 'material-market'">
            <div class="preview-card">
              <img src="/images/cream-livingroom.png" />
            </div>
            <div class="preview-meta">
              <h3>奶油风客厅沙发场景</h3>
              <div class="buyer">光合设计 · 5 积分下载</div>
            </div>
            <button class="generate-btn">5 积分下载</button>
          </div>
          <div class="panel" v-else-if="page === 'points'">
            <h3>我的积分</h3>
            <div class="points-large">2,856</div>
            <button class="generate-btn">去充值</button>
          </div>
        </aside>
      </section>
    </template>

    <template v-else>
      <section class="fallback">
        <div class="fallback-card">
          <h2>{{ route.meta?.title }}</h2>
          <p>{{ route.meta?.subtitle }}</p>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const page = computed(() => route.meta?.page || 'white-bg')
const pageClass = computed(() => `page-${page.value}`)
const activeStepIndex = computed(() => {
  const map = { 'white-bg': 0, background: 1, retouch: 2, 'ai-model': 3, 'hero-image': 2, 'detail-page': 2, banner: 0, 'size-mark': 2 }
  return map[page.value] ?? 0
})

const commonSteps = ['白底图', '生成背景', '产品精修', 'AI模特', '主图', '详情图 / A+', '尺寸标记', 'Banner']
const designSteps = ['白底图', '生成背景', '产品精修', 'AI模特', '主图 / A+', '详情图', '尺寸标记', 'Banner']
const bannerSteps = ['选择模板', '编辑内容', '样式设置', '生成导出']
const bannerTabs = ['全部', '促销活动', '节日季节', '新品上市', '品牌宣传', '信息通知', '店铺装修', '其他']
const bannerGoals = ['提升销量 / 促销转化', '新品推广', '品牌宣传 / 提升认知', '活动宣传 / 引流', '清仓 / 库存处理', '节日营销', '店铺形象展示', '其他']
const promoCards = [
  { title: '光合AI Studio 2.0 全新上线', desc: '更聪明的AI，更专业的设计体验', btn: '查看详情', image: '/images/chair-scene-bg.png' },
  { title: '冬季新品模板更新', desc: '海量高质量场景模板上线', btn: '立即体验', image: '/images/cream-livingroom.png' },
  { title: '邀请好友得积分', desc: '每成功邀请一位好友', btn: '立即邀请', image: '/images/banner-invite.png' }
]
const bannerTemplates = [
  { title: '夏季促销', image: '/images/banner-furniture-sale.png' },
  { title: '大促活动', image: '/images/banner-launch.png' },
  { title: '新品上市', image: '/images/banner-kitchen.png' },
  { title: '季节上新', image: '/images/banner-winter.png' },
  { title: '包邮活动', image: '/images/banner-invite.png' }
]
const bgThumbs = ['/images/chair-white-bg.png', '/images/chair-scene-bg.png', '/images/chair-white-bg.png']
const detailTags = ['高品质材料', '耐用结实', '多功能使用', '易于安装', '防刮耐磨', '安全环保', '时尚设计', '性价比高', '舒适体验', '节省空间']
const creatorCards = [
  { title: '奶油风客厅沙发', image: '/images/cream-livingroom.png', meta: '156 观看 · 128 上架' },
  { title: '原木风餐厅场景', image: '/images/kitchen-dining.png', meta: '128 观看 · 96 上架' },
  { title: '北欧风办公室', image: '/images/nordic-office.png', meta: '98 观看 · 64 上架' },
  { title: '日式卧室场景', image: '/images/japanese-room.png', meta: '145 观看 · 82 上架' }
]
const marketCards = [
  { title: '奶油风客厅沙发场景', image: '/images/cream-livingroom.png', tags: '奶油风 · 客厅 · 沙发' },
  { title: '北欧风客厅沙发场景', image: '/images/industrial-livingroom.png', tags: '北欧风 · 客厅 · 沙发' },
  { title: '日式卧室场景', image: '/images/japanese-room.png', tags: '日式 · 卧室 · 家居' },
  { title: '现代办公场景', image: '/images/nordic-office.png', tags: '现代 · 办公 · 家具' }
]
const historyGroups = [
  {
    label: '今天 2024-06-01',
    items: [
      { type: '主图设计', title: '北欧布艺沙发 A', time: '14:30', image: '/images/cream-livingroom.png' },
      { type: '白底生成背景', title: '单人沙发换背景', time: '14:25', image: '/images/chair-scene-bg.png' },
      { type: 'AI 白底图', title: '北欧沙发白底图', time: '14:20', image: '/images/chair-white-bg.png' },
      { type: '详情图 / A+', title: '北欧沙发详情方案', time: '14:10', image: '/images/banner-kitchen.png' }
    ]
  },
  {
    label: '昨天 2024-05-31',
    items: [
      { type: '主图设计', title: '餐桌套餐主图方案', time: '16:20', image: '/images/kitchen-dining.png' },
      { type: 'AI 模特', title: '餐桌场景 AI 模特图', time: '15:40', image: '/images/clothing-display.png' },
      { type: 'Banner 设计', title: '餐桌 Banner', time: '15:20', image: '/images/banner-furniture-sale.png' },
      { type: '尺寸标记', title: '餐桌尺寸图', time: '15:10', image: '/images/banner-launch.png' }
    ]
  }
]
const simpleImages = ['/images/cream-livingroom.png', '/images/kitchen-dining.png', '/images/nordic-office.png', '/images/japanese-room.png']
const pageTitles = {
  projects: '我的项目',
  favorites: '我的收藏',
  'my-materials': '我的素材',
  points: '我的积分',
  'recycle-bin': '回收站',
  'help-center': '帮助中心',
  'message-center': '消息中心',
  feedback: '意见反馈',
  'customer-support': '客服支持',
  'keyboard-shortcuts': '快捷键帮助'
}
const canvasHint = computed(() => {
  if (page.value === 'white-bg') return '支持 JPG / PNG / WebP，最大 20MB'
  if (page.value === 'background') return '支持 JPG / PNG / WebP，最多 10 张（参考图 + 上传图）'
  if (page.value === 'ai-model') return '生成的模特效果将呈现在画布中'
  if (page.value === 'hero-image') return '支持 JPG / PNG，建议尺寸 >= 2000px'
  if (page.value === 'detail-page') return '支持多页设计，突出卖点，提升转化率'
  if (page.value === 'size-mark') return '支持 JPG / PNG / WebP 格式，最大 20MB'
  return '支持 JPG / PNG / WebP'
})
const pageRightTitle = computed(() => {
  if (page.value === 'white-bg') return 'AI 配置'
  if (page.value === 'background') return '创作配置'
  if (page.value === 'ai-model') return '创作配置'
  if (page.value === 'hero-image') return '上传素材'
  if (page.value === 'detail-page') return '页面配置'
  if (page.value === 'size-mark') return '配置面板'
  return '配置面板'
})
</script>

<style scoped lang="scss">
.studio-page {
  height: 100%;
  overflow: auto;
  background: #f7f9fc;
}

.workspace,
.library-layout {
  min-height: calc(100vh - var(--gh-topbar-height));
}

.two-col {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  padding: 16px;
}

.left-col,
.library-main {
  min-width: 0;
}

.right-col,
.library-aside {
  min-width: 0;
}

.workflow {
  display: flex;
  align-items: center;
  gap: 0;
  background: #fff;
  border: 1px solid var(--gh-border-light);
  border-radius: 14px;
  padding: 10px 14px;
  margin-bottom: 14px;
}

.workflow.compact {
  margin-top: 4px;
}

.step {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8edf5;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  flex: 0 0 auto;
}

.dot.active {
  background: var(--gh-primary);
  color: #fff;
}

.step span {
  margin-left: 8px;
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

.step span.active {
  color: var(--gh-primary);
  font-weight: 600;
}

.line {
  flex: 1;
  height: 2px;
  background: #edf1f7;
  margin: 0 10px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 10px;
}

.ghost-btn,
.ghost-mini,
.ghost-wide,
.filter,
.switch,
.size-chip,
.text-btn,
.icon-btn,
.next-btn,
.link-btn,
.generate-btn,
.upload-mini,
.send-btn,
.tile,
.chip,
.add-box,
.template-card,
.style-card,
.compare-toggle {
  border: 1px solid var(--gh-border);
  background: #fff;
}

.ghost-btn {
  height: 36px;
  border-radius: 10px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #334155;
}

.mini-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.banner-toolbar {
  flex: 1;
  justify-content: flex-end;
  gap: 18px;
}

.edit-actions,
.zoom-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tool-label {
  height: 32px;
  border: 0;
  background: transparent;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 6px;
  font-size: 12px;
  cursor: pointer;
}

.tool-label:hover {
  color: var(--gh-primary);
}

.icon-btn,
.next-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #334155;
}

.text-btn {
  min-width: 58px;
  height: 32px;
  border-radius: 8px;
  font-weight: 600;
  color: #1e3a8a;
}

.canvas {
  background: #fff;
  border: 1px solid #e6ebf3;
  border-radius: 14px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.4);
}

.banner-canvas,
.big-canvas {
  min-height: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.canvas-empty {
  text-align: center;
  color: #667085;
}

.canvas-empty h3 {
  margin-top: 16px;
  font-size: 16px;
  color: #1f2a44;
}

.canvas-empty p {
  margin-top: 10px;
  font-size: 14px;
}

.canvas-recommend {
  color: #7c89a5;
  margin-top: 6px !important;
}

.template-strip {
  margin-top: 16px;
}

.strip-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.strip-head h4 {
  font-size: 14px;
}

.link-btn {
  border: 0;
  background: transparent;
  color: var(--gh-primary);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 10px 0 14px;
}

.chip {
  padding: 6px 12px;
  border-radius: 8px;
  color: #475467;
  font-size: 13px;
}

.chip.active {
  color: var(--gh-primary);
  border-color: rgba(37, 99, 255, 0.4);
  background: rgba(37, 99, 255, 0.05);
}

.template-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr)) 42px;
  gap: 12px;
  align-items: center;
}

.template-card {
  border-radius: 8px;
  overflow: hidden;
}

.template-card img,
.asset-card img,
.market-card img,
.history-card img,
.simple-card img,
.promo-card img,
.compare-side img,
.style-card img,
.preview-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.template-card img {
  aspect-ratio: 1.65 / 1;
}

.template-title {
  font-size: 12px;
  color: #334155;
  margin-top: 8px;
}

.footer-note,
.footer-hint {
  margin-top: 16px;
  font-size: 13px;
  color: #8a94a8;
}

.right-col > .panel,
.library-aside > .panel {
  background: #fff;
  border: 1px solid #e6ebf3;
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 14px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #101828;
  margin-bottom: 12px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.panel-head .panel-title {
  margin-bottom: 0;
}

.collapse-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #475467;
  font-size: 12px;
  white-space: nowrap;
}

.collapse-link.blue {
  color: var(--gh-primary);
}

.size-row,
.platform-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.size-chip {
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  color: #475467;
}

.size-chip.active {
  color: var(--gh-primary);
  border-color: rgba(37, 99, 255, 0.45);
  background: rgba(37, 99, 255, 0.06);
}

.custom-size,
.field-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.custom-size-title {
  margin-top: 12px;
  font-size: 13px;
  color: #1f2937;
  font-weight: 500;
}

.custom-size label,
.field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  color: #475467;
}

.custom-size input,
.field input,
.field select,
.searchbar input {
  width: 130px;
  height: 34px;
  border: 1px solid var(--gh-border);
  border-radius: 8px;
  padding: 0 10px;
  outline: none;
  background: #fff;
}

.unit-input {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.unit-input input {
  padding-right: 34px;
}

.unit-input em {
  position: absolute;
  right: 10px;
  font-style: normal;
  color: #64748b;
  font-size: 12px;
}

.page-banner .custom-size {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.page-banner .custom-size label {
  justify-content: flex-start;
  gap: 8px;
}

.page-banner .custom-size input {
  width: 108px;
}

.page-banner .banner-key-fields {
  gap: 12px;
}

.page-banner .banner-key-fields .field {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr);
  justify-content: stretch;
}

.page-banner .banner-key-fields input {
  width: 100%;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.upload-card {
  min-height: 110px;
  border: 1px solid #dce3f0;
  border-radius: 12px;
  padding: 12px;
  background: #fbfdff;
  display: grid;
  place-items: center;
  text-align: center;
  gap: 6px;
  font-size: 12px;
  color: #667085;
}

.upload-card.primary {
  border-color: rgba(37, 99, 255, 0.38);
}

.tile-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.tile-grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.tile-grid.compact {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.tile {
  min-height: 64px;
  border-radius: 10px;
  padding: 10px;
  text-align: left;
}

.tile b,
.tile span {
  display: block;
}

.tile b {
  font-size: 13px;
  color: #1f2937;
}

.tile span {
  margin-top: 4px;
  font-size: 11px;
  color: #98a2b3;
  line-height: 1.35;
}

.tile.active {
  border-color: rgba(37, 99, 255, 0.45);
  background: rgba(37, 99, 255, 0.04);
}

.check-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  font-size: 13px;
  color: #475467;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.generate-btn {
  width: 100%;
  height: 40px;
  border-radius: 10px;
  background: var(--gh-primary);
  color: #fff;
  border: 0;
  margin-top: 8px;
  font-weight: 600;
}

.ai-panel .chat {
  display: grid;
  gap: 8px;
  margin-bottom: 10px;
}

.bubble {
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
}

.bubble.bot {
  background: #f2f5fa;
  color: #1f2937;
}

.bubble.user {
  justify-self: end;
  background: rgba(37, 99, 255, 0.12);
  color: #1d4ed8;
}

.chat-box {
  border: 1px solid rgba(37, 99, 255, 0.35);
  border-radius: 12px;
  padding: 10px;
}

.chat-box textarea {
  width: 100%;
  height: 100px;
  border: 0;
  outline: none;
  resize: none;
  font: inherit;
}

.chat-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #94a3b8;
}

.send-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  color: var(--gh-primary);
}

.promo-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.promo-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 120px;
  border: 1px solid #e6ebf3;
  border-radius: 16px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #f6f9ff 0%, #ffffff 100%);
}

.promo-banner h3 {
  font-size: 18px;
  font-weight: 600;
  color: #101828;
  margin-bottom: 6px;
}

.promo-banner p {
  font-size: 13px;
  color: #667085;
  margin-bottom: 10px;
}

.promo-card {
  min-height: 120px;
  background: linear-gradient(135deg, #f5f8ff, #ffffff);
  border: 1px solid #e6ebf3;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.promo-card img {
  width: 120px;
  height: 90px;
  object-fit: cover;
}

.ghost-mini {
  height: 28px;
  border-radius: 8px;
  padding: 0 10px;
  color: var(--gh-primary);
}

.promo-copy h3,
.creator-head h2 {
  font-size: 18px;
  margin-bottom: 6px;
}

.promo-copy p,
.creator-head p,
.history-head p {
  color: #667085;
  font-size: 13px;
}

.compare-canvas {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 530px;
  border: 1px solid #e6ebf3;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.compare-side {
  position: relative;
}

.compare-tag {
  position: absolute;
  left: 10px;
  top: 10px;
  background: rgba(255, 255, 255, 0.92);
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  color: #4b5563;
}

.compare-toggle {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.96);
}

.footer-hint a {
  color: var(--gh-primary);
  text-decoration: none;
  margin-left: 6px;
}

.collapse-list {
  display: grid;
  gap: 8px;
}

.collapse-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 42px;
  border-bottom: 1px solid #eef2f7;
  color: #334155;
  font-size: 13px;
}

.style-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.style-card {
  border-radius: 12px;
  padding: 8px;
  text-align: center;
}

.style-card.active {
  border-color: rgba(37, 99, 255, 0.45);
}

.style-card img {
  width: 100%;
  height: 92px;
  object-fit: contain;
}

.style-card span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #475467;
}

.helper-line {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f7f9fc;
  color: #98a2b3;
  font-size: 12px;
}

.thumb-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.thumb-row img,
.add-box {
  width: 74px;
  height: 56px;
  border-radius: 10px;
  border: 1px solid #e6ebf3;
  object-fit: cover;
}

.add-box {
  background: #f8fbff;
  font-size: 22px;
  color: #2563ff;
}

.upload-zone {
  border: 1px dashed #d4dceb;
  border-radius: 12px;
  background: #fbfdff;
  text-align: center;
  padding: 22px 12px;
  color: #667085;
}

.panel-subtitle {
  margin: 10px 0 8px;
  font-size: 13px;
  color: #475467;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-cloud span {
  padding: 6px 10px;
  border: 1px solid #e6ebf3;
  border-radius: 8px;
  font-size: 12px;
  color: #475467;
}

.market-layout .library-main,
.creator-center .library-main,
.history .library-main {
  background: #fff;
  border: 1px solid #e6ebf3;
  border-radius: 16px;
  padding: 16px;
}

.creator-head,
.history-head,
.market-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.searchbar {
  flex: 1;
  max-width: 420px;
  height: 40px;
  border: 1px solid #dce3f0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  background: #fff;
}

.searchbar input {
  width: 100%;
  border: 0;
  outline: none;
  background: transparent;
}

.filter-row,
.switch-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter,
.switch,
.ghost-wide {
  height: 38px;
  border-radius: 10px;
  padding: 0 12px;
  color: #344054;
}

.switch.active,
.filter.active {
  border-color: rgba(37, 99, 255, 0.4);
  color: var(--gh-primary);
  background: rgba(37, 99, 255, 0.04);
}

.card-grid,
.market-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.asset-card,
.market-card,
.history-card,
.simple-card {
  border: 1px solid #e6ebf3;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.asset-card img,
.market-card img {
  height: 180px;
}

.asset-body,
.market-body,
.history-body,
.simple-copy,
.preview-meta {
  padding: 12px;
}

.asset-title,
.market-title,
.history-title {
  font-size: 14px;
  font-weight: 600;
  color: #101828;
}

.asset-meta,
.market-meta,
.history-meta,
.buyer {
  margin-top: 4px;
  font-size: 12px;
  color: #667085;
}

.history-list {
  display: grid;
  gap: 20px;
}

.day-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.history-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.history-card img {
  height: 130px;
}

.history-card.add-card {
  display: grid;
  place-items: center;
  color: var(--gh-primary);
  background: #f8fbff;
}

.simple-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.simple-card img {
  height: 210px;
}

.simple-copy h3 {
  font-size: 15px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.stat {
  border: 1px solid #e6ebf3;
  border-radius: 12px;
  padding: 12px;
}

.stat span {
  display: block;
  font-size: 12px;
  color: #667085;
}

.stat b,
.points-large {
  display: block;
  margin-top: 6px;
  font-size: 24px;
  color: #101828;
}

.mini-chart,
.preview-card {
  border: 1px solid #e6ebf3;
  border-radius: 12px;
  margin-top: 12px;
  min-height: 180px;
  background: linear-gradient(180deg, #f8fbff, #ffffff);
}

.preview-card img {
  height: 170px;
}

.preview-meta h3 {
  font-size: 16px;
}

.ghost-wide {
  width: 100%;
  margin-top: 10px;
  background: #fff;
}

.fallback {
  padding: 24px;
}

.fallback-card {
  max-width: 720px;
  padding: 24px;
  border: 1px solid #e6ebf3;
  border-radius: 16px;
  background: #fff;
}

@media (max-width: 1400px) {
  .two-col {
    grid-template-columns: minmax(0, 1fr) 360px;
  }

  .template-row,
  .card-grid,
  .market-grid,
  .history-row,
  .simple-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .upload-grid,
  .tile-grid.four {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
