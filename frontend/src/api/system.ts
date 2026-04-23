import api from './client'
import type { HealthStatus, SystemConfig } from './types'

export const systemApi = {
  health(): Promise<HealthStatus> {
    return api.get('/health')
  },

  config(): Promise<SystemConfig> {
    return api.get('/api/config')
  }
}