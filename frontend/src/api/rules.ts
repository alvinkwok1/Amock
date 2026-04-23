import api from './client'
import type { MockRule, PaginatedResponse, CreateRuleRequest, UpdateRuleRequest } from './types'

export interface RuleListParams {
  page?: number
  pageSize?: number
  enabled?: boolean
  method?: string
  groupId?: string
}

export const rulesApi = {
  list(params: RuleListParams = {}): Promise<PaginatedResponse<MockRule>> {
    return api.get('/api/rules', { params })
  },

  get(id: string): Promise<MockRule> {
    return api.get(`/api/rules/${id}`)
  },

  create(data: CreateRuleRequest): Promise<MockRule> {
    return api.post('/api/rules', data)
  },

  update(id: string, data: UpdateRuleRequest): Promise<MockRule> {
    return api.patch(`/api/rules/${id}`, data)
  },

  delete(id: string): Promise<void> {
    return api.delete(`/api/rules/${id}`)
  },

  copy(id: string): Promise<MockRule> {
    return api.post(`/api/rules/${id}/copy`)
  },

  import(rules: CreateRuleRequest[]): Promise<MockRule[]> {
    return api.post('/api/rules/import', rules)
  },

  export(params: RuleListParams = {}): Promise<MockRule[]> {
    return api.get('/api/rules/export', { params })
  }
}