<template>
  <ContentWrap>
    <div class="entry-hero">
      <div class="entry-hero__eyebrow">案件中心</div>
      <div class="entry-hero__title">工作台入口</div>
      <div class="entry-hero__desc">
        当前已经拆分成客服工作台和法务工作台两个入口。你后面可以按登录权限分别挂菜单，这里先保留成兼容入口。
      </div>
    </div>

    <div class="entry-grid">
      <button class="entry-card" @click="router.push('/court-case/service-workbench')">
        <div class="entry-card__title">客服工作台</div>
        <div class="entry-card__desc">处理提醒、当日未还、追客和客服侧待办案件。</div>
      </button>
      <button class="entry-card" @click="router.push('/court-case/legal-workbench')">
        <div class="entry-card__title">法务工作台</div>
        <div class="entry-card__desc">处理法务承接、诉讼推进和归档案件。</div>
      </button>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
defineOptions({ name: 'CourtCaseWorkbenchEntry' })

const router = useRouter()
</script>

<style scoped>
.entry-hero__eyebrow {
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.entry-hero__title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.entry-hero__desc {
  max-width: 720px;
  margin-top: 12px;
  line-height: 1.8;
  color: var(--el-text-color-secondary);
}

.entry-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 24px;
}

.entry-card {
  padding: 24px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 18px;
  text-align: left;
  background:
    radial-gradient(circle at top right, rgb(64 158 255 / 0.14), transparent 35%),
    linear-gradient(180deg, var(--el-bg-color) 0%, var(--el-fill-color-extra-light) 100%);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.entry-card:hover {
  transform: translateY(-2px);
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 10px 24px rgb(31 35 41 / 0.08);
}

.entry-card__title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.entry-card__desc {
  margin-top: 10px;
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

@media (max-width: 900px) {
  .entry-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
