import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { rulesApi } from '@/api'
import type { MockRule, CreateRuleRequest, UpdateRuleRequest, RuleListParams } from '@/api'

export const useRulesStore = defineStore('rules', () => {
  const rules = ref<MockRule[]>([])
  const currentRule = ref<MockRule | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const params = ref<RuleListParams>({
    page: 1,
    pageSize: 20
  })

  const hasMore = computed(() => {
    return rules.value.length < total.value
  })

  async function fetchRules(newParams?: RuleListParams) {
    loading.value = true
    error.value = null
    try {
      if (newParams) {
        params.value = { ...params.value, ...newParams }
      }
      const response = await rulesApi.list(params.value)
      rules.value = response.data
      total.value = response.total
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取规则列表失败'
    } finally {
      loading.value = false
    }
  }

  async function fetchRule(id: string) {
    loading.value = true
    error.value = null
    try {
      currentRule.value = await rulesApi.get(id)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取规则详情失败'
    } finally {
      loading.value = false
    }
  }

  async function createRule(data: CreateRuleRequest) {
    loading.value = true
    error.value = null
    try {
      const rule = await rulesApi.create(data)
      rules.value.unshift(rule)
      total.value++
      return rule
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '创建规则失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateRule(id: string, data: UpdateRuleRequest) {
    loading.value = true
    error.value = null
    try {
      const rule = await rulesApi.update(id, data)
      const index = rules.value.findIndex(r => r.id === id)
      if (index !== -1) {
        rules.value[index] = rule
      }
      if (currentRule.value?.id === id) {
        currentRule.value = rule
      }
      return rule
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '更新规则失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteRule(id: string) {
    loading.value = true
    error.value = null
    try {
      await rulesApi.delete(id)
      const index = rules.value.findIndex(r => r.id === id)
      if (index !== -1) {
        rules.value.splice(index, 1)
        total.value--
      }
      if (currentRule.value?.id === id) {
        currentRule.value = null
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '删除规则失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function copyRule(id: string) {
    loading.value = true
    error.value = null
    try {
      const rule = await rulesApi.copy(id)
      rules.value.unshift(rule)
      total.value++
      return rule
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '复制规则失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function importRules(data: CreateRuleRequest[]) {
    loading.value = true
    error.value = null
    try {
      const newRules = await rulesApi.import(data)
      rules.value = [...newRules, ...rules.value]
      total.value += newRules.length
      return newRules
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '导入规则失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function exportRules() {
    try {
      return await rulesApi.export(params.value)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '导出规则失败'
      throw e
    }
  }

  function reset() {
    rules.value = []
    currentRule.value = null
    total.value = 0
    params.value = { page: 1, pageSize: 20 }
    error.value = null
  }

  return {
    rules,
    currentRule,
    total,
    loading,
    error,
    params,
    hasMore,
    fetchRules,
    fetchRule,
    createRule,
    updateRule,
    deleteRule,
    copyRule,
    importRules,
    exportRules,
    reset
  }
})