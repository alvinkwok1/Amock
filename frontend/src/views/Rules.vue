<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRulesStore, useGroupsStore } from '@/stores'
import {
  Button, Input, Select, Badge, Card, CardContent,
  Table, TableHeader, TableHead, TableBody, TableRow, TableCell,
  Dialog, DropdownMenu, Switch
} from '@/components/ui'
import { Plus, Search, Download, Upload, MoreHorizontal, Copy, Pencil, Trash2, RefreshCw } from 'lucide-vue-next'
import type { MockRule, CreateRuleRequest } from '@/api'

const router = useRouter()
const rulesStore = useRulesStore()
const groupsStore = useGroupsStore()

const searchQuery = ref('')
const filterEnabled = ref<string | undefined>(undefined)
const filterMethod = ref<string | undefined>(undefined)
const filterGroup = ref<string | undefined>(undefined)

const showCreateDialog = ref(false)
const showImportDialog = ref(false)
const importJson = ref('')

const newRule = ref<CreateRuleRequest>({
  name: '',
  path: '',
  pathMatchType: 'exact',
  method: 'GET',
  statusCode: 200,
  headers: {},
  body: '',
  delayMs: 0,
  enabled: true
})

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

const enabledOptions = [
  { value: '', label: '全部' },
  { value: 'true', label: '已启用' },
  { value: 'false', label: '已禁用' }
]

const groupOptions = computed(() => [
  { value: '', label: '全部' },
  ...groupsStore.groups.map(g => ({ value: g.id, label: g.name }))
])

onMounted(async () => {
  await Promise.all([
    rulesStore.fetchRules(),
    groupsStore.fetchGroups()
  ])
})

async function handleSearch() {
  await rulesStore.fetchRules({
    page: 1,
    enabled: filterEnabled.value === 'true' ? true : filterEnabled.value === 'false' ? false : undefined,
    method: filterMethod.value,
    groupId: filterGroup.value
  })
}

function openRuleDetail(rule: MockRule) {
  router.push(`/rules/${rule.id}`)
}

async function handleCreate() {
  try {
    const rule = await rulesStore.createRule(newRule.value)
    showCreateDialog.value = false
    resetNewRule()
    router.push(`/rules/${rule.id}`)
  } catch (e) {
    console.error('Create failed:', e)
  }
}

async function handleCopy(id: string) {
  try {
    const rule = await rulesStore.copyRule(id)
    router.push(`/rules/${rule.id}`)
  } catch (e) {
    console.error('Copy failed:', e)
  }
}

async function handleDelete(id: string) {
  if (confirm('确定要删除此规则吗？')) {
    try {
      await rulesStore.deleteRule(id)
    } catch (e) {
      console.error('Delete failed:', e)
    }
  }
}

async function handleExport() {
  try {
    const rules = await rulesStore.exportRules()
    const json = JSON.stringify(rules, null, 2)
    const blob = new Blob([json], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `amock-rules-${new Date().toISOString().split('T')[0]}.json`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Export failed:', e)
  }
}

async function handleImport() {
  try {
    const rules = JSON.parse(importJson.value) as CreateRuleRequest[]
    await rulesStore.importRules(rules)
    showImportDialog.value = false
    importJson.value = ''
  } catch (e) {
    console.error('Import failed:', e)
    alert('导入失败，请检查JSON格式')
  }
}

async function toggleEnabled(rule: MockRule) {
  try {
    await rulesStore.updateRule(rule.id, { enabled: !rule.enabled })
  } catch (e) {
    console.error('Toggle failed:', e)
  }
}

function resetNewRule() {
  newRule.value = {
    name: '',
    path: '',
    pathMatchType: 'exact',
    method: 'GET',
    statusCode: 200,
    headers: {},
    body: '',
    delayMs: 0,
    enabled: true
  }
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">规则管理</h1>
      <div class="flex gap-2">
        <Button variant="outline" size="sm" @click="showImportDialog = true">
          <Upload class="h-4 w-4 mr-1" />
          导入
        </Button>
        <Button variant="outline" size="sm" @click="handleExport">
          <Download class="h-4 w-4 mr-1" />
          导出
        </Button>
        <Button @click="showCreateDialog = true">
          <Plus class="h-4 w-4 mr-1" />
          新建规则
        </Button>
      </div>
    </div>

    <!-- Filters -->
    <Card class="mb-4">
      <CardContent class="pt-4">
        <div class="flex flex-wrap gap-4">
          <div class="flex-1 min-w-[200px]">
            <Input
              v-model="searchQuery"
              placeholder="搜索规则名称或路径..."
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <Search class="h-4 w-4" />
              </template>
            </Input>
          </div>
          <Select
            v-model="filterEnabled"
            :options="enabledOptions"
            placeholder="启用状态"
          />
          <Select
            v-model="filterMethod"
            :options="methodOptions"
            placeholder="HTTP方法"
          />
          <Select
            v-model="filterGroup"
            :options="groupOptions"
            placeholder="分组"
          />
          <Button variant="outline" size="sm" @click="handleSearch">
            <RefreshCw class="h-4 w-4 mr-1" />
            刷新
          </Button>
        </div>
      </CardContent>
    </Card>

    <!-- Rules table -->
    <Card>
      <CardContent class="pt-4">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>名称</TableHead>
              <TableHead>路径</TableHead>
              <TableHead>方法</TableHead>
              <TableHead>状态码</TableHead>
              <TableHead>分组</TableHead>
              <TableHead>状态</TableHead>
              <TableHead class="w-[100px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="rulesStore.loading">
              <TableCell colspan="7" class="text-center text-muted-foreground">
                加载中...
              </TableCell>
            </TableRow>
            <TableRow v-if="!rulesStore.loading && rulesStore.rules.length === 0">
              <TableCell colspan="7" class="text-center text-muted-foreground">
                暂无规则
              </TableCell>
            </TableRow>
            <TableRow
              v-for="rule in rulesStore.rules"
              :key="rule.id"
              class="cursor-pointer"
              @click="openRuleDetail(rule)"
            >
              <TableCell class="font-medium">{{ rule.name }}</TableCell>
              <TableCell>
                <code class="text-xs bg-muted px-1 rounded">{{ rule.path }}</code>
              </TableCell>
              <TableCell>
                <Badge variant="outline">{{ rule.method }}</Badge>
              </TableCell>
              <TableCell>{{ rule.statusCode }}</TableCell>
              <TableCell>
                <span v-if="rule.groupId" class="text-sm">
                  {{ groupsStore.groups.find(g => g.id === rule.groupId)?.name || '-' }}
                </span>
                <span v-else class="text-muted-foreground">-</span>
              </TableCell>
              <TableCell>
                <Switch
                  :checked="rule.enabled"
                  @update:checked="() => toggleEnabled(rule)"
                  @click.stop
                />
              </TableCell>
              <TableCell>
                <DropdownMenu>
                  <template #trigger>
                    <Button variant="ghost" size="icon" @click.stop>
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </template>
                  <DropdownMenuItem @select="() => handleCopy(rule.id)">
                    <Copy class="h-4 w-4 mr-2" />
                    复制
                  </DropdownMenuItem>
                  <DropdownMenuItem @select="() => openRuleDetail(rule)">
                    <Pencil class="h-4 w-4 mr-2" />
                    编辑
                  </DropdownMenuItem>
                  <DropdownMenuItem class="text-destructive" danger @select="() => handleDelete(rule.id)">
                    <Trash2 class="h-4 w-4 mr-2" />
                    删除
                  </DropdownMenuItem>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <!-- Create dialog -->
    <Dialog v-model:open="showCreateDialog">
      <template #trigger />
      <template #title>新建规则</template>
      <template #description>创建一个新的Mock规则</template>
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium">规则名称</label>
          <Input v-model="newRule.name" placeholder="输入规则名称" class="mt-1" />
        </div>
        <div>
          <label class="text-sm font-medium">路径</label>
          <Input v-model="newRule.path" placeholder="/api/example" class="mt-1" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm font-medium">匹配类型</label>
            <Select v-model="newRule.pathMatchType" :options="pathMatchOptions" class="mt-1" />
          </div>
          <div>
            <label class="text-sm font-medium">HTTP方法</label>
            <Select v-model="newRule.method" :options="methodOptions" class="mt-1" />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm font-medium">状态码</label>
            <Input v-model="newRule.statusCode" type="number" placeholder="200" class="mt-1" />
          </div>
          <div>
            <label class="text-sm font-medium">延迟(ms)</label>
            <Input v-model="newRule.delayMs" type="number" placeholder="0" class="mt-1" />
          </div>
        </div>
        <div class="flex justify-end gap-2 pt-4">
          <Button variant="outline" @click="showCreateDialog = false">取消</Button>
          <Button @click="handleCreate">创建</Button>
        </div>
      </div>
    </Dialog>

    <!-- Import dialog -->
    <Dialog v-model:open="showImportDialog">
      <template #trigger />
      <template #title>导入规则</template>
      <template #description>粘贴JSON格式的规则数据</template>
      <div class="space-y-4">
        <textarea
          v-model="importJson"
          class="flex min-h-[200px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          placeholder="粘贴JSON数据..."
        />
        <div class="flex justify-end gap-2">
          <Button variant="outline" @click="showImportDialog = false">取消</Button>
          <Button @click="handleImport">导入</Button>
        </div>
      </div>
    </Dialog>
  </div>
</template>