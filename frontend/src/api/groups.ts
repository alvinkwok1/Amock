import api from './client'
import type { RuleGroup, CreateGroupRequest, UpdateGroupRequest } from './types'

export const groupsApi = {
  list(): Promise<RuleGroup[]> {
    return api.get('/api/groups')
  },

  create(data: CreateGroupRequest): Promise<RuleGroup> {
    return api.post('/api/groups', data)
  },

  update(id: string, data: UpdateGroupRequest): Promise<RuleGroup> {
    return api.patch(`/api/groups/${id}`, data)
  },

  delete(id: string): Promise<void> {
    return api.delete(`/api/groups/${id}`)
  }
}