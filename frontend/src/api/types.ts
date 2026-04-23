// API Types based on spec

export interface MockRule {
  id: string
  name: string
  description: string
  path: string
  pathMatchType: 'exact' | 'prefix' | 'regex'
  method: string
  statusCode: number
  headers: Record<string, string>
  body: string
  delayMs: number
  conditions: ConditionRule | null
  priority: number
  enabled: boolean
  groupId: string | null
  createdAt: string
  updatedAt: string
}

export interface ConditionRule {
  type: 'and' | 'or'
  rules: ConditionItem[]
}

export interface ConditionItem {
  field: string
  op: 'eq' | 'neq' | 'exists' | 'contains' | 'regex'
  value?: string
}

export interface RuleGroup {
  id: string
  name: string
  description: string
  createdAt: string
  updatedAt: string
}

export interface RequestLog {
  id: string
  mockRuleId: string | null
  method: string
  path: string
  queryString: string
  headers: Record<string, string>
  body: string
  clientIp: string
  responseStatus: number
  responseHeaders: Record<string, string>
  responseBody: string
  duration: number
  createdAt: string
}

export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
  hasMore: boolean
}

export interface CreateRuleRequest {
  name: string
  description?: string
  path: string
  pathMatchType: 'exact' | 'prefix' | 'regex'
  method: string
  statusCode?: number
  headers?: Record<string, string>
  body?: string
  delayMs?: number
  conditions?: ConditionRule
  priority?: number
  enabled?: boolean
  groupId?: string
}

export interface UpdateRuleRequest {
  name?: string
  description?: string
  path?: string
  pathMatchType?: 'exact' | 'prefix' | 'regex'
  method?: string
  statusCode?: number
  headers?: Record<string, string>
  body?: string
  delayMs?: number
  conditions?: ConditionRule
  priority?: number
  enabled?: boolean
  groupId?: string
}

export interface CreateGroupRequest {
  name: string
  description?: string
}

export interface UpdateGroupRequest {
  name?: string
  description?: string
}

export interface RequestStats {
  totalRequests: number
  successRate: number
  avgResponseTime: number
  requestsByMethod: Record<string, number>
  requestsByStatus: Record<string, number>
}

export interface HealthStatus {
  status: string
  ruleCount: number
  cacheSize: number
  uptime: number
}

export interface SystemConfig {
  requestLogRetentionDays: number
  requestLogSampleRate: number
  maxRulesCache: number
}