import { defineStore } from 'pinia'
import { ref } from 'vue'
import { groupsApi } from '@/api'
import type { RuleGroup, CreateGroupRequest, UpdateGroupRequest } from '@/api'

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref<RuleGroup[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchGroups() {
    loading.value = true
    error.value = null
    try {
      groups.value = await groupsApi.list()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取分组列表失败'
    } finally {
      loading.value = false
    }
  }

  async function createGroup(data: CreateGroupRequest) {
    loading.value = true
    error.value = null
    try {
      const group = await groupsApi.create(data)
      groups.value.push(group)
      return group
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '创建分组失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateGroup(id: string, data: UpdateGroupRequest) {
    loading.value = true
    error.value = null
    try {
      const group = await groupsApi.update(id, data)
      const index = groups.value.findIndex(g => g.id === id)
      if (index !== -1) {
        groups.value[index] = group
      }
      return group
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '更新分组失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteGroup(id: string) {
    loading.value = true
    error.value = null
    try {
      await groupsApi.delete(id)
      const index = groups.value.findIndex(g => g.id === id)
      if (index !== -1) {
        groups.value.splice(index, 1)
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '删除分组失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  function reset() {
    groups.value = []
    error.value = null
  }

  return {
    groups,
    loading,
    error,
    fetchGroups,
    createGroup,
    updateGroup,
    deleteGroup,
    reset
  }
})