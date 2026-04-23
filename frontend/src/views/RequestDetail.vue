<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRequestsStore, useRulesStore } from '@/stores'
import { Button, Card, CardContent, Badge, Tabs, ScrollArea } from '@/components/ui'
import { ArrowLeft, RefreshCw, ExternalLink, Copy, Clock } from 'lucide-vue-next'
import type { RequestLog, MockRule } from '@/api'

const router = useRouter()
const requestsStore = useRequestsStore()
const rulesStore = useRulesStore()

const requestId = router.currentRoute.value.params.id as string
const request = ref<RequestLog | null>(null)

const tabsData = [
  { value: 'request', label: '请求信息' },
  { value: 'response', label: '响应信息' }
]

const activeTab = ref('request')

onMounted(async () => {
  await requestsStore.fetchRequest(requestId)
  request.value = requestsStore.currentRequest
})

const matchedRule = computed(() => {
  if (!request.value?.mockRuleId) return null
  return rulesStore.rules.find((r: MockRule) => r.id === request.value!.mockRuleId)
})

function formatJson(str: string): string {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
  alert('已复制')
}

async function handleReplay() {
  if (!request.value) return
  try {
    await requestsStore.replayRequest(request.value.id)
    alert('回放成功')
  } catch (e) {
    alert('回放失败')
  }
}

function openRule() {
  if (matchedRule.value) {
    router.push(`/rules/${matchedRule.value.id}`)
  }
}

function formatHeaders(headers: Record<string, string>): string {
  return Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n')
}

function getStatusBadgeVariant(status: number): 'default' | 'secondary' | 'destructive' | 'outline' {
  if (status >= 200 && status < 300) return 'default'
  if (status >= 400 && status < 500) return 'secondary'
  if (status >= 500) return 'destructive'
  return 'outline'
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center gap-4 mb-6">
      <Button variant="ghost" size="icon" @click="router.back()">
        <ArrowLeft class="h-4 w-4" />
      </Button>
      <h1 class="text-2xl font-bold">请求详情</h1>
      <div class="flex-1" />
      <Button variant="outline" v-if="matchedRule" @click="openRule">
        <ExternalLink class="h-4 w-4 mr-1" />
        查看规则
      </Button>
      <Button @click="handleReplay">
        <RefreshCw class="h-4 w-4 mr-1" />
        回放
      </Button>
    </div>

    <div v-if="request">
      <!-- Basic info -->
      <Card class="mb-4">
        <CardContent class="pt-4">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p class="text-sm text-muted-foreground">方法</p>
              <Badge variant="outline">{{ request.method }}</Badge>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">路径</p>
              <code class="text-sm bg-muted px-1 rounded">{{ request.path }}</code>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">状态码</p>
              <Badge :variant="getStatusBadgeVariant(request.responseStatus)">
                {{ request.responseStatus }}
              </Badge>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">耗时</p>
              <div class="flex items-center gap-1">
                <Clock class="h-4 w-4" />
                {{ request.duration }} ms
              </div>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">客户端IP</p>
              <p class="text-sm">{{ request.clientIp }}</p>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">时间</p>
              <p class="text-sm">{{ new Date(request.createdAt).toLocaleString() }}</p>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">Query String</p>
              <code class="text-sm bg-muted px-1 rounded">{{ request.queryString || '-' }}</code>
            </div>
            <div>
              <p class="text-sm text-muted-foreground">匹配规则</p>
              <p class="text-sm">{{ matchedRule?.name || '未匹配' }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Tabs -->
      <Card>
        <CardContent class="pt-4">
          <Tabs
            :tabs="tabsData"
            :default-value="activeTab"
            v-model:model-value="activeTab"
          >
            <template #request>
              <div class="space-y-4">
                <!-- Request headers -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="font-medium">请求头</h3>
                    <Button variant="ghost" size="sm" @click="copyToClipboard(formatHeaders(request.headers))">
                      <Copy class="h-4 w-4" />
                    </Button>
                  </div>
                  <ScrollArea class="h-[150px] rounded-md border p-2">
                    <pre class="text-sm">{{ formatHeaders(request.headers) }}</pre>
                  </ScrollArea>
                </div>

                <!-- Request body -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="font-medium">请求体</h3>
                    <Button variant="ghost" size="sm" @click="copyToClipboard(request.body)">
                      <Copy class="h-4 w-4" />
                    </Button>
                  </div>
                  <ScrollArea class="h-[200px] rounded-md border p-2">
                    <pre class="text-sm">{{ formatJson(request.body) || '(空)' }}</pre>
                  </ScrollArea>
                </div>
              </div>
            </template>

            <template #response>
              <div class="space-y-4">
                <!-- Response headers -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="font-medium">响应头</h3>
                    <Button variant="ghost" size="sm" @click="copyToClipboard(formatHeaders(request.responseHeaders))">
                      <Copy class="h-4 w-4" />
                    </Button>
                  </div>
                  <ScrollArea class="h-[150px] rounded-md border p-2">
                    <pre class="text-sm">{{ formatHeaders(request.responseHeaders) }}</pre>
                  </ScrollArea>
                </div>

                <!-- Response body -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="font-medium">响应体</h3>
                    <Button variant="ghost" size="sm" @click="copyToClipboard(request.responseBody)">
                      <Copy class="h-4 w-4" />
                    </Button>
                  </div>
                  <ScrollArea class="h-[300px] rounded-md border p-2">
                    <pre class="text-sm">{{ formatJson(request.responseBody) || '(空)' }}</pre>
                  </ScrollArea>
                </div>
              </div>
            </template>
          </Tabs>
        </CardContent>
      </Card>
    </div>

    <div v-else-if="requestsStore.loading" class="text-center py-8">
      <p class="text-muted-foreground">加载中...</p>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-muted-foreground">请求不存在</p>
    </div>
  </div>
</template>