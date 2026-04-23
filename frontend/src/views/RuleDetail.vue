<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useRulesStore, useGroupsStore } from '@/stores'
import {
  Button, Input, Select, Card, CardContent,
  Switch, Label, Tabs, Textarea
} from '@/components/ui'
import { Save, ArrowLeft, Copy } from 'lucide-vue-next'
import type { MockRule, UpdateRuleRequest, ConditionRule, CreateRuleRequest } from '@/api'

const responseTemplates = [
  { name: '成功响应', body: JSON.stringify({ code: 200, message: 'success', data: {} }, null, 2) },
  { name: '错误响应', body: JSON.stringify({ code: 500, message: 'error', data: null }, null, 2) },
  { name: '分页响应', body: JSON.stringify({ code: 200, message: 'success', data: { list: [], total: 0, page: 1, pageSize: 10 } }, null, 2) },
  { name: '空对象', body: '{}' }
]

const router = useRouter()
const route = useRoute()
const rulesStore = useRulesStore()
const groupsStore = useGroupsStore()

const ruleId = route.params.id as string
const isNew = computed(() => ruleId === 'new')

const form = ref<CreateRuleRequest>({
  name: '',
  description: '',
  path: '',
  pathMatchType: 'exact',
  method: 'GET',
  statusCode: 200,
  headers: {},
  body: '{}',
  delayMs: 0,
  enabled: true,
  groupId: undefined,
  conditions: undefined
})

const headersJson = ref('{}')
const conditionsJson = ref('')

const methodOptions = [
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'DELETE', label: 'DELETE' },
  { value: 'PATCH', label: 'PATCH' },
  { value: 'HEAD', label: 'HEAD' },
  { value: 'OPTIONS', label: 'OPTIONS' }
]

const pathMatchOptions = [
  { value: 'exact', label: '精确匹配' },
  { value: 'prefix', label: '前缀匹配' },
  { value: 'regex', label: '正则匹配' }
]

const groupOptions = computed(() => [
  { value: '', label: '无分组' },
  ...groupsStore.groups.map(g => ({ value: g.id, label: g.name }))
])

const tabsData = [
  { value: 'basic', label: '基本信息' },
  { value: 'response', label: '响应配置' },
  { value: 'conditions', label: '匹配条件' },
  { value: 'headers', label: '响应头' }
]

const activeTab = ref('basic')

onMounted(async () => {
  await groupsStore.fetchGroups()
  if (!isNew.value) {
    await rulesStore.fetchRule(ruleId)
    if (rulesStore.currentRule) {
      initForm(rulesStore.currentRule)
    }
  }
})

function initForm(rule: MockRule) {
  form.value = {
    name: rule.name,
    description: rule.description,
    path: rule.path,
    pathMatchType: rule.pathMatchType,
    method: rule.method,
    statusCode: rule.statusCode,
    headers: rule.headers,
    body: rule.body,
    delayMs: rule.delayMs,
    enabled: rule.enabled,
    groupId: rule.groupId || undefined,
    conditions: rule.conditions || undefined
  }
  headersJson.value = JSON.stringify(rule.headers || {}, null, 2)
  conditionsJson.value = rule.conditions ? JSON.stringify(rule.conditions, null, 2) : ''
}

async function handleSave() {
  try {
    try {
      form.value.headers = JSON.parse(headersJson.value)
    } catch {
      alert('响应头JSON格式错误')
      return
    }

    if (conditionsJson.value) {
      try {
        form.value.conditions = JSON.parse(conditionsJson.value) as ConditionRule
      } catch {
        alert('匹配条件JSON格式错误')
        return
      }
    } else {
      form.value.conditions = undefined
    }

    if (isNew.value) {
      const rule = await rulesStore.createRule(form.value)
      router.replace(`/rules/${rule.id}`)
    } else {
      const updateData: UpdateRuleRequest = {
        name: form.value.name,
        description: form.value.description,
        path: form.value.path,
        pathMatchType: form.value.pathMatchType,
        method: form.value.method,
        statusCode: form.value.statusCode,
        headers: form.value.headers,
        body: form.value.body,
        delayMs: form.value.delayMs,
        enabled: form.value.enabled,
        groupId: form.value.groupId,
        conditions: form.value.conditions
      }
      await rulesStore.updateRule(ruleId, updateData)
    }
    alert('保存成功')
  } catch (e) {
    console.error('Save failed:', e)
    alert('保存失败')
  }
}

function applyTemplate(template: { name: string; body: string }) {
  form.value.body = template.body
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center gap-4 mb-6">
      <Button variant="ghost" size="icon" @click="router.back()">
        <ArrowLeft class="h-4 w-4" />
      </Button>
      <h1 class="text-2xl font-bold">
        {{ isNew ? '新建规则' : '编辑规则' }}
      </h1>
      <div class="flex-1" />
      <Button variant="outline" v-if="!isNew" @click="rulesStore.copyRule(ruleId)">
        <Copy class="h-4 w-4 mr-1" />
        复制
      </Button>
      <Button @click="handleSave">
        <Save class="h-4 w-4 mr-1" />
        保存
      </Button>
    </div>

    <Card>
      <CardContent class="pt-4">
        <Tabs
          :tabs="tabsData"
          :default-value="activeTab"
          v-model:model-value="activeTab"
        >
          <template #basic>
            <div class="space-y-4">
              <div>
                <Label>规则名称</Label>
                <Input v-model="form.name" class="mt-1" />
              </div>
              <div>
                <Label>描述</Label>
                <Textarea v-model="form.description" class="mt-1" />
              </div>
              <div>
                <Label>URL路径</Label>
                <Input v-model="form.path" placeholder="/api/example" class="mt-1" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <Label>匹配类型</Label>
                  <Select v-model="form.pathMatchType" :options="pathMatchOptions" class="mt-1" />
                </div>
                <div>
                  <Label>HTTP方法</Label>
                  <Select v-model="form.method" :options="methodOptions" class="mt-1" />
                </div>
              </div>
              <div>
                <Label>分组</Label>
                <Select v-model="form.groupId" :options="groupOptions" class="mt-1" />
              </div>
              <div class="flex items-center gap-2">
                <Switch v-model:checked="form.enabled" />
                <Label>启用规则</Label>
              </div>
            </div>
          </template>

          <template #response>
            <div class="space-y-4">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <Label>状态码</Label>
                  <Input v-model="form.statusCode" type="number" class="mt-1" />
                </div>
                <div>
                  <Label>延迟(ms)</Label>
                  <Input v-model="form.delayMs" type="number" class="mt-1" />
                </div>
              </div>
              <div>
                <Label>响应体模板</Label>
                <div class="flex gap-2 mt-1">
                  <Button
                    v-for="template in responseTemplates"
                    :key="template.name"
                    variant="outline"
                    size="sm"
                    @click="applyTemplate(template)"
                  >
                    {{ template.name }}
                  </Button>
                </div>
              </div>
              <div>
                <Label>响应体 (JSON)</Label>
                <div class="mt-1 rounded-md border overflow-hidden">
                  <Textarea
                    v-model="form.body"
                    class="min-h-[300px] font-mono text-sm"
                    placeholder="输入JSON格式的响应体..."
                  />
                </div>
              </div>
            </div>
          </template>

          <template #conditions>
            <div class="space-y-4">
              <Label>匹配条件规则 (JSON)</Label>
              <p class="text-sm text-muted-foreground">
                格式: { "type": "and" | "or", "rules": [...] }
              </p>
              <Textarea
                v-model="conditionsJson"
                class="min-h-[200px] font-mono text-sm"
                placeholder='{"type": "and", "rules": [{"field": "header.X-Token", "op": "eq", "value": "xxx"}]}'
              />
              <div class="text-sm text-muted-foreground">
                <p>支持操作符:</p>
                <ul class="list-disc list-inside">
                  <li>eq - 等于</li>
                  <li>neq - 不等于</li>
                  <li>exists - 存在</li>
                  <li>contains - 包含</li>
                  <li>regex - 正则匹配</li>
                </ul>
              </div>
            </div>
          </template>

          <template #headers>
            <div class="space-y-4">
              <Label>响应头 (JSON)</Label>
              <Textarea
                v-model="headersJson"
                class="min-h-[200px] font-mono text-sm"
                placeholder='{"Content-Type": "application/json", "X-Custom-Header": "value"}'
              />
            </div>
          </template>
        </Tabs>
      </CardContent>
    </Card>
  </div>
</template>