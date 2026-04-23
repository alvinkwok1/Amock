import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { requestsApi, createRequestStream } from '@/api'
import type { RequestLog, RequestStats, RequestListParams } from '@/api'

export const useRequestsStore = defineStore('requests', () => {
  const requests = ref<RequestLog[]>([])
  const currentRequest = ref<RequestLog | null>(null)
  const stats = ref<RequestStats | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const streaming = ref(false)

  let eventSource: EventSource | null = null

  const params = ref<RequestListParams>({
    page: 1,
    pageSize: 20
  })

  const hasMore = computed(() => requests.value.length < total.value)

  async function fetchRequests(newParams?: RequestListParams) {
    loading.value = true
    error.value = null
    try {
      if (newParams) {
        params.value = { ...params.value, ...newParams }
      }
      const response = await requestsApi.list(params.value)
      requests.value = response.data
      total.value = response.total
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取请求记录失败'
    } finally {
      loading.value = false
    }
  }

  async function fetchRequest(id: string) {
    loading.value = true
    error.value = null
    try {
      currentRequest.value = await requestsApi.get(id)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取请求详情失败'
    } finally {
      loading.value = false
    }
  }

  async function fetchStats() {
    try {
      stats.value = await requestsApi.stats()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取请求统计失败'
    }
  }

  async function clearRequests() {
    loading.value = true
    error.value = null
    try {
      await requestsApi.clear()
      requests.value = []
      total.value = 0
      stats.value = null
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '清空请求记录失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function replayRequest(id: string) {
    try {
      await requestsApi.replay(id)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '回放请求失败'
      throw e
    }
  }

  function startStream() {
    if (eventSource) return

    streaming.value = true
    eventSource = createRequestStream((data) => {
      requests.value.unshift(data)
      total.value++
    })
  }

  function stopStream() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    streaming.value = false
  }

  function reset() {
    requests.value = []
    currentRequest.value = null
    stats.value = null
    total.value = 0
    params.value = { page: 1, pageSize: 20 }
    error.value = null
    stopStream()
  }

  return {
    requests,
    currentRequest,
    stats,
    total,
    loading,
    error,
    streaming,
    params,
    hasMore,
    fetchRequests,
    fetchRequest,
    fetchStats,
    clearRequests,
    replayRequest,
    startStream,
    stopStream,
    reset
  }
})