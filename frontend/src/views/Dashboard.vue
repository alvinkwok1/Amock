<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { systemApi, requestsApi } from '@/api'
import { useRulesStore, useGroupsStore } from '@/stores'
import { Card, CardHeader, CardTitle, CardContent, Badge } from '@/components/ui'
import { Activity, FileText, FolderTree, Clock } from 'lucide-vue-next'
import type { HealthStatus, RequestStats } from '@/api'

const rulesStore = useRulesStore()
const groupsStore = useGroupsStore()

const health = ref<HealthStatus | null>(null)
const stats = ref<RequestStats | null>(null)
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      rulesStore.fetchRules(),
      groupsStore.fetchGroups(),
      systemApi.health().then((data) => health.value = data),
      requestsApi.stats().then((data) => stats.value = data)
    ])
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-6">仪表盘</h1>

    <!-- Status cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">服务状态</CardTitle>
          <Activity class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">
            <Badge :variant="health?.status === 'UP' ? 'default' : 'destructive'">
              {{ health?.status || '未知' }}
            </Badge>
          </div>
          <p class="text-xs text-muted-foreground mt-2">
            运行时间: {{ health?.uptime ? Math.floor(health.uptime / 60) : 0 }} 分钟
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">活跃规则</CardTitle>
          <FileText class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ rulesStore.total }}</div>
          <p class="text-xs text-muted-foreground mt-2">
            缓存数: {{ health?.cacheSize || 0 }}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">分组数量</CardTitle>
          <FolderTree class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ groupsStore.groups.length }}</div>
          <p class="text-xs text-muted-foreground mt-2">
            规则分类管理
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">请求总数</CardTitle>
          <Clock class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats?.totalRequests || 0 }}</div>
          <p class="text-xs text-muted-foreground mt-2">
            成功率: {{ stats?.successRate ? (stats.successRate * 100).toFixed(1) : 0 }}%
          </p>
        </CardContent>
      </Card>
    </div>

    <!-- Request stats -->
    <Card class="mb-6">
      <CardHeader>
        <CardTitle>请求统计</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4" v-if="stats">
          <div>
            <p class="text-sm text-muted-foreground">平均响应时间</p>
            <p class="text-lg font-semibold">{{ stats.avgResponseTime?.toFixed(2) || 0 }} ms</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">按方法分布</p>
            <div class="flex gap-2 mt-1">
              <Badge v-for="(count, method) in stats.requestsByMethod" :key="method" variant="secondary">
                {{ method }}: {{ count }}
              </Badge>
            </div>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">按状态码分布</p>
            <div class="flex gap-2 mt-1">
              <Badge v-for="(count, status) in stats.requestsByStatus" :key="status" variant="outline">
                {{ status }}: {{ count }}
              </Badge>
            </div>
          </div>
        </div>
        <p v-else class="text-muted-foreground">暂无统计数据</p>
      </CardContent>
    </Card>

    <!-- Recent rules -->
    <Card>
      <CardHeader>
        <CardTitle>最近规则</CardTitle>
      </CardHeader>
      <CardContent>
        <div v-if="rulesStore.rules.length" class="space-y-2">
          <div
            v-for="rule in rulesStore.rules.slice(0, 5)"
            :key="rule.id"
            class="flex items-center justify-between p-2 rounded-md border"
          >
            <div>
              <p class="font-medium">{{ rule.name }}</p>
              <p class="text-sm text-muted-foreground">{{ rule.path }} - {{ rule.method }}</p>
            </div>
            <Badge :variant="rule.enabled ? 'default' : 'secondary'">
              {{ rule.enabled ? '启用' : '禁用' }}
            </Badge>
          </div>
        </div>
        <p v-else class="text-muted-foreground">暂无规则</p>
      </CardContent>
    </Card>
  </div>
</template>