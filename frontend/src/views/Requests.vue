<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRequestsStore, useRulesStore } from '@/stores'
import {
  Button, Input, Select, Badge, Card, CardHeader, CardTitle, CardContent,
  Table, TableHeader, TableHead, TableBody, TableRow, TableCell,
  DropdownMenu
} from '@/components/ui'
import { Search, Play, Pause, Trash2, MoreHorizontal, ExternalLink, RefreshCw } from 'lucide-vue-next'
import type { RequestLog, MockRule } from '@/api'

const router = useRouter()
const requestsStore = useRequestsStore()
const rulesStore = useRulesStore()

const filterUrl = ref('')
const filterMethod = ref<string | undefined>(undefined)
const dateRange = ref<'today' | 'week' | 'month' | 'all'>('all')

const methodOptions = [
  { value: '', label: '全部' },
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'DELETE', label: 'DELETE' },
  { value: 'PATCH', label: 'PATCH' }
]

const dateRangeOptions = [
  { value: 'all', label: '全部' },
  { value: 'today', label: '今天' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' }
]

onMounted(async () => {
  await Promise.all([
    requestsStore.fetchRequests(),
    requestsStore.fetchStats(),
    rulesStore.fetchRules()
  ])
})

onUnmounted(() => {
  requestsStore.stopStream()
})

function getDateRangeFilter() {
  const now = new Date()
  switch (dateRange.value) {
    case 'today':
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      return { startTime: today.toISOString() }
    case 'week':
      const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      return { startTime: weekAgo.toISOString() }
    case 'month':
      const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      return { startTime: monthAgo.toISOString() }
    default:
      return {}
  }
}

async function handleSearch() {
  await requestsStore.fetchRequests({
    page: 1,
    url: filterUrl.value || undefined,
    method: filterMethod.value || undefined,
    ...getDateRangeFilter()
  })
}

function toggleStream() {
  if (requestsStore.streaming) {
    requestsStore.stopStream()
  } else {
    requestsStore.startStream()
  }
}

function openRequestDetail(request: RequestLog) {
  router.push(`/requests/${request.id}`)
}

function getRuleName(ruleId: string | null): string {
  if (!ruleId) return '未匹配'
  const rule = rulesStore.rules.find((r: MockRule) => r.id === ruleId)
  return rule?.name || '未知规则'
}

function getStatusBadge(status: number): 'default' | 'secondary' | 'destructive' | 'outline' {
  if (status >= 200 && status < 300) return 'default'
  if (status >= 400 && status < 500) return 'secondary'
  if (status >= 500) return 'destructive'
  return 'outline'
}

async function handleClear() {
  if (confirm('确定要清空所有请求记录吗？')) {
    await requestsStore.clearRequests()
  }
}

async function handleReplay(id: string) {
  try {
    await requestsStore.replayRequest(id)
    alert('回放成功')
  } catch (e) {
    alert('回放失败')
  }
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(2)}s`
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">请求记录</h1>
      <div class="flex gap-2">
        <Button
          :variant="requestsStore.streaming ? 'default' : 'outline'"
          size="sm"
          @click="toggleStream"
        >
          <Play v-if="!requestsStore.streaming" class="h-4 w-4 mr-1" />
          <Pause v-if="requestsStore.streaming" class="h-4 w-4 mr-1" />
          {{ requestsStore.streaming ? '暂停' : '实时' }}
        </Button>
        <Button variant="outline" size="sm" @click="handleClear">
          <Trash2 class="h-4 w-4 mr-1" />
          清空
        </Button>
      </div>
    </div>

    <!-- Filters -->
    <Card class="mb-4">
      <CardContent class="pt-4">
        <div class="flex flex-wrap gap-4">
          <div class="flex-1 min-w-[200px]">
            <Input
              v-model="filterUrl"
              placeholder="搜索URL..."
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <Search class="h-4 w-4" />
              </template>
            </Input>
          </div>
          <Select
            v-model="filterMethod"
            :options="methodOptions"
            placeholder="HTTP方法"
          />
          <Select
            v-model="dateRange"
            :options="dateRangeOptions"
            placeholder="时间范围"
          />
          <Button variant="outline" size="sm" @click="handleSearch">
            <RefreshCw class="h-4 w-4 mr-1" />
            刷新
          </Button>
        </div>
      </CardContent>
    </Card>

    <!-- Stats -->
    <Card class="mb-4">
      <CardHeader>
        <CardTitle class="text-sm">请求统计</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="flex gap-6" v-if="requestsStore.stats">
          <div>
            <p class="text-sm text-muted-foreground">总数</p>
            <p class="text-lg font-semibold">{{ requestsStore.stats.totalRequests }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">成功率</p>
            <p class="text-lg font-semibold">{{ (requestsStore.stats.successRate * 100).toFixed(1) }}%</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">平均响应时间</p>
            <p class="text-lg font-semibold">{{ requestsStore.stats.avgResponseTime?.toFixed(2) || 0 }} ms</p>
          </div>
        </div>
        <p v-else class="text-muted-foreground">暂无统计数据</p>
      </CardContent>
    </Card>

    <!-- Requests table -->
    <Card>
      <CardContent class="pt-4">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>时间</TableHead>
              <TableHead>方法</TableHead>
              <TableHead>路径</TableHead>
              <TableHead>状态码</TableHead>
              <TableHead>耗时</TableHead>
              <TableHead>匹配规则</TableHead>
              <TableHead>客户端IP</TableHead>
              <TableHead class="w-[80px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="requestsStore.loading">
              <TableCell colspan="8" class="text-center text-muted-foreground">
                加载中...
              </TableCell>
            </TableRow>
            <TableRow v-if="!requestsStore.loading && requestsStore.requests.length === 0">
              <TableCell colspan="8" class="text-center text-muted-foreground">
                暂无请求记录
              </TableCell>
            </TableRow>
            <TableRow
              v-for="request in requestsStore.requests"
              :key="request.id"
              class="cursor-pointer"
              @click="openRequestDetail(request)"
            >
              <TableCell class="text-sm text-muted-foreground">
                {{ new Date(request.createdAt).toLocaleTimeString() }}
              </TableCell>
              <TableCell>
                <Badge variant="outline">{{ request.method }}</Badge>
              </TableCell>
              <TableCell>
                <code class="text-xs bg-muted px-1 rounded">{{ request.path }}</code>
              </TableCell>
              <TableCell>
                <Badge :variant="getStatusBadge(request.responseStatus)">
                  {{ request.responseStatus }}
                </Badge>
              </TableCell>
              <TableCell>
                <span class="text-sm">{{ formatDuration(request.duration) }}</span>
              </TableCell>
              <TableCell>
                <span class="text-sm">{{ getRuleName(request.mockRuleId) }}</span>
              </TableCell>
              <TableCell>
                <span class="text-sm text-muted-foreground">{{ request.clientIp }}</span>
              </TableCell>
              <TableCell>
                <DropdownMenu>
                  <template #trigger>
                    <Button variant="ghost" size="icon" @click.stop>
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </template>
                  <DropdownMenuItem @select="() => openRequestDetail(request)">
                    <ExternalLink class="h-4 w-4 mr-2" />
                    详情
                  </DropdownMenuItem>
                  <DropdownMenuItem @select="() => handleReplay(request.id)">
                    <RefreshCw class="h-4 w-4 mr-2" />
                    回放
                  </DropdownMenuItem>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>