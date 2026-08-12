<template>
  <div class="gh-logo" :class="[`size-${size}`]">
    <span class="mark-wrap">
      <img class="brand-mark" src="/images/brand-logo.png" alt="光合AI" />
    </span>
    <span class="brand-text">光合AI</span>
    <span class="studio" v-if="showStudio">Studio</span>
  </div>
</template>

<script setup>
defineProps({
  size: { type: String, default: 'md' },
  showStudio: { type: Boolean, default: true }
})
</script>

<style lang="scss" scoped>
.gh-logo {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  height: 28px;
}

.mark-wrap {
  width: 28px;
  height: 28px;
  overflow: hidden;
  flex: 0 0 auto;
  background: #fff;
}

.brand-mark {
  width: 28px;
  height: 28px;
  object-fit: cover;
  object-position: center 12%;
  display: block;
}

.brand-text {
  font-size: 18px;
  line-height: 28px;
  font-weight: 500;
  color: #111827;
  white-space: nowrap;
}

.studio {
  display: inline-flex;
  align-items: center;
  height: 18px;
  padding: 0 8px;
  border: 1px solid #7ab1ff;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: #2563ff;
  background: #f5f9ff;
  white-space: nowrap;
}

.size-sm {
  height: 24px;
  .mark-wrap,
  .brand-mark {
    width: 24px;
    height: 24px;
  }
  .brand-text {
    font-size: 16px;
    line-height: 24px;
  }
}

.size-lg {
  height: 34px;
  .mark-wrap,
  .brand-mark {
    width: 34px;
    height: 34px;
  }
  .brand-text {
    font-size: 22px;
    line-height: 34px;
  }
}
</style>
