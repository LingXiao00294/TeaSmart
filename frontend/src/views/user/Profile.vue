<template>
  <AppShell>
  <div class="profile">
    <AppHeader title="我 · 的" back-to="/" />

    <div class="profile__hero rise">
      <div class="seal seal--lg profile__avatar">{{ user?.username?.[0]?.toUpperCase() || '客' }}</div>
      <div class="profile__name font-heading">{{ user?.username || '茶客' }}</div>
      <div class="profile__role">
        <span class="profile__role-tag" :class="{ 'profile__role-tag--admin': user?.role === 'ADMIN' }">
          {{ user?.role === 'ADMIN' ? '掌柜' : '茶客' }}
        </span>
      </div>
    </div>

    <section class="profile__card rise" style="animation-delay: 0.05s">
      <div class="profile__row">
        <span class="profile__label font-heading">手机号码</span>
        <span class="profile__value">{{ user?.phone || '尚未留下联络' }}</span>
      </div>
      <hr class="gold-line-flat profile__sep" />
    </section>

    <section class="profile__actions rise" style="animation-delay: 0.1s">
      <el-button
        v-if="user?.role === 'ADMIN'"
        type="primary"
        size="large"
        class="profile__btn"
        @click="$router.push('/admin')"
      >
        进入 · 掌柜后台
      </el-button>
      <el-button type="danger" plain size="large" class="profile__btn" @click="handleLogout">退出 · 茶席</el-button>
    </section>

    <div class="profile__foot">
      <hr class="gold-line" />
      <p class="profile__foot-txt font-heading">茶小智 TeaSmart</p>
    </div>
  </div>
  </AppShell>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

async function handleLogout() {
  await ElMessageBox.confirm('确定退出茶席？', '提示')
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile {
  min-height: 100%;
  padding: 0 16px 24px;
}
.profile__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 -16px;
  padding: 24px 16px 30px;
  background:
    radial-gradient(circle at 50% 0%, rgba(110, 139, 90, 0.12) 0, transparent 60%),
    var(--tea-paper-2);
  border-bottom: 1px solid var(--tea-line);
}
.profile__avatar {
  transform: rotate(-5deg);
  margin-bottom: 14px;
}
.profile__name {
  font-size: 22px;
  font-weight: 700;
  color: var(--tea-ink-text);
  letter-spacing: 2px;
}
.profile__role {
  margin-top: 8px;
}
.profile__role-tag {
  display: inline-block;
  font-family: var(--font-heading);
  font-size: 12px;
  letter-spacing: 2px;
  padding: 3px 12px;
  border-radius: 20px;
  background: var(--tea-paper);
  border: 1px solid var(--tea-gold);
  color: var(--tea-amber);
}
.profile__role-tag--admin {
  background: var(--tea-ink);
  border-color: var(--tea-ink);
  color: #f7e9c6;
}

.profile__card {
  margin: 16px 0;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 6px 16px;
}
.profile__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
}
.profile__label {
  font-size: 14px;
  color: var(--tea-text-2);
  letter-spacing: 2px;
}
.profile__value {
  font-size: 14px;
  color: var(--tea-ink-text);
}
.profile__sep {
  margin: 0;
}

.profile__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0 0;
}
.profile__btn {
  width: 100%;
  border-radius: var(--radius);
  height: 46px;
  font-family: var(--font-heading);
  letter-spacing: 3px;
  text-indent: 3px;
}
.profile__btn + .profile__btn {
  margin-left: 0;
}

.profile__foot {
  text-align: center;
  margin: 36px 0 0;
}
.profile__foot-txt {
  margin-top: 12px;
  font-size: 12px;
  color: var(--tea-text-3);
  letter-spacing: 4px;
}
</style>
