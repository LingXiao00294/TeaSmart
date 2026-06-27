<template>
  <AppShell>
  <AppHeader title="我 · 的" back-to="/" />
  <div class="profile">
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
      <button type="button" class="profile__row profile__row--action" @click="openPhoneDialog">
        <span class="profile__label font-heading">手机号码</span>
        <span class="profile__value profile__value--link">
          {{ user?.phone || '尚未留下联络' }}
          <el-icon><ArrowRight /></el-icon>
        </span>
      </button>
      <hr class="gold-line-flat profile__sep" />
      <button type="button" class="profile__row profile__row--action" @click="openPasswordDialog">
        <span class="profile__label font-heading">登录密码</span>
        <span class="profile__value profile__value--link">
          修改密码
          <el-icon><ArrowRight /></el-icon>
        </span>
      </button>
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

  <el-dialog v-model="phoneDialogVisible" title="修改手机号" width="360px" destroy-on-close>
    <el-form :model="phoneForm" label-width="72px">
      <el-form-item label="手机号">
        <el-input v-model="phoneForm.phone" placeholder="选填，便于联络" maxlength="20" clearable />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="phoneDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="phoneSaving" @click="savePhone">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" destroy-on-close>
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="72px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSaving" @click="savePassword">保存</el-button>
    </template>
  </el-dialog>
  </AppShell>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

const phoneDialogVisible = ref(false)
const phoneSaving = ref(false)
const phoneForm = ref({ phone: '' })

const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref()
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度 6-100 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

function openPhoneDialog() {
  phoneForm.value.phone = user.value?.phone || ''
  phoneDialogVisible.value = true
}

function openPasswordDialog() {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordDialogVisible.value = true
}

async function savePhone() {
  phoneSaving.value = true
  try {
    const res = await updateProfile({ phone: phoneForm.value.phone || null })
    userStore.setUserInfo(res.data)
    ElMessage.success('手机号已更新')
    phoneDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '更新失败，请稍后重试')
  } finally {
    phoneSaving.value = false
  }
}

async function savePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  passwordSaving.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    ElMessage.success('密码已修改，请重新登录')
    passwordDialogVisible.value = false
    userStore.logout()
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '修改失败，请稍后重试')
  } finally {
    passwordSaving.value = false
  }
}

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
  width: 100%;
  border: none;
  background: none;
  text-align: left;
  font: inherit;
  color: inherit;
}
.profile__row--action {
  cursor: pointer;
}
.profile__row--action:active {
  opacity: 0.7;
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
.profile__value--link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--tea-text-3);
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
