import api from './client'
import type { RequestLog, PaginatedResponse, RequestStats } from './types'

export interface RequestListParams {
  page?: number
  pageSize?: number
  url?: string
  method?: string
  startTime?: string
  endTime?: string
}

export const requestsApi = {
  list(params: RequestListParams = {}): Promise<PaginatedResponse<RequestLog>> {
    return api.get('/api/requests', { params })
  },

  get(id: string): Promise<RequestLog> {
    return api.get(`/api/requests/${id}`)
  },

  clear(): Promise<void> {
    return api.delete('/api/requests')
  },

  stats(): Promise<RequestStats> {
    return api.get('/api/requests/stats')
  },

  replay(id: string): Promise<void> {
    return api.post(`/api/requests/${id}/replay`)
  }
}

// SSE stream for real-time requests
export function createRequestStream(onMessage: (data: RequestLog) => void): EventSource {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const eventSource = new EventSource(`${baseUrl}/api/requests/stream`)

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data) as RequestLog
      onMessage(data)
    } catch (e) {
      console.error('Failed to parse SSE data:', e)
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE error:', error)
  }

  return eventSource
}