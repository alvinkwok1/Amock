# Amock 产品方案（V2 - 整改版）

## 一、产品定位

Amock 是一个轻量级的 HTTP 接口 Mock 服务平台，旨在帮助开发人员、测试人员进行接口联调、测试和数据模拟。通过可视化的前端界面，用户可以快速配置 Mock 规则，无需编写代码即可模拟任意 HTTP 接口的响应。

## 二、核心功能模块

### 2.1 Mock 规则管理

| 功能点 | 描述 |
|--------|------|
| 规则配置 | 支持 URL 路径、HTTP 方法（GET/POST/PUT/DELETE/PATCH 等）匹配 |
| 响应配置 | 支持配置响应状态码、响应头、响应体（JSON/XML/Text） |
| 条件匹配 | 支持基于请求参数、请求头、请求体的条件匹配 |
| 延迟模拟 | 支持配置响应延迟，模拟网络延迟场景 |
| 规则优先级 | 支持多条规则的优先级排序和冲突处理 |

### 2.2 请求记录

| 功能点 | 描述 |
|--------|------|
| 请求列表 | 实时展示所有到达 Mock 服务的 HTTP 请求 |
| 请求详情 | 展示完整的请求信息：URL、方法、请求头、请求体 |
| 响应详情 | 展示 Mock 服务返回的响应信息 |
| 请求过滤 | 支持按 URL、方法、时间范围过滤请求记录 |
| 请求统计 | 统计各接口的调用次数、成功率等指标 |

### 2.3 前端管理界面

| 功能点 | 描述 |
|--------|------|
| 规则管理页 | CRUD 操作 Mock 规则，支持批量导入/导出 |
| 请求记录页 | 实时请求列表和详情查看 |
| 仪表盘 | 总览 Mock 服务状态、请求统计、活跃规则数 |

## 三、技术架构

```
┌─────────────────────────────────────────────────────┐
│                    前端 (Vue3 + shadcn/ui)           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐    │
│  │  规则管理   │ │  请求记录   │ │   仪表盘    │    │
│  └─────────────┘ └─────────────┘ └─────────────┘    │
└─────────────────────────────────────────────────────┘
                         │ REST API
┌─────────────────────────────────────────────────────┐
│                    后端 (Java + Spring Boot)        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐    │
│  │ Mock 服务   │ │ 规则引擎   │ │ 请求记录器  │    │
│  └─────────────┘ └─────────────┘ └─────────────┘    │
│  ┌─────────────────────────────────────────────┐   │
│  │              数据持久层 (SQLite/H2)          │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 3.1 后端技术栈
- **框架**: Spring Boot 3.x
- **数据库**: SQLite（轻量级，单文件部署）/ H2
- **Mock 服务**: 基于 Spring MVC 的动态路由
- **API 风格**: RESTful

### 3.2 前端技术栈
- **框架**: Vue 3 + TypeScript
- **UI 组件**: shadcn/ui + Tailwind CSS
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

## 四、数据模型设计

### 4.1 MockRule（Mock 规则）
```
- id: String (UUID)
- name: String (规则名称)
- description: String (规则说明)
- path: String (URL 路径)
- pathMatchType: String (精确匹配exact/前缀匹配prefix/正则匹配regex)
- method: String (HTTP 方法)
- statusCode: Integer (响应状态码，默认 200)
- headers: JSON (响应头)
- body: Text (响应体)
- delayMs: Integer (延迟毫秒数，默认 0)
- conditions: JSON (匹配条件，见下方结构定义)
- priority: Integer (优先级)
- enabled: Boolean (是否启用)
- groupId: String (规则分组 ID)
- createdAt: DateTime
- updatedAt: DateTime
```

### 4.2 条件匹配规则格式
```json
{
  "type": "and" | "or",
  "rules": [
    {"field": "header.X-Token", "op": "eq", "value": "xxx"},
    {"field": "query.userId", "op": "exists"},
    {"field": "body.status", "op": "eq", "value": "active"}
  ]
}
```
支持操作符：`eq`（等于）、`neq`（不等于）、`exists`（存在）、`contains`（包含）、`regex`（正则匹配）

### 4.3 RequestLog（请求记录）
```
- id: String (UUID)
- mockRuleId: String (匹配的规则 ID，可为空)
- method: String
- path: String
- queryString: String
- headers: JSON
- body: Text
- clientIp: String (客户端 IP)
- responseStatus: Integer
- responseHeaders: JSON
- responseBody: Text
- duration: Integer (处理耗时 ms)
- createdAt: DateTime
```

### 4.4 RuleGroup（规则分组）
```
- id: String (UUID)
- name: String (分组名称)
- description: String (分组说明)
- createdAt: DateTime
- updatedAt: DateTime
```

## 五、API 设计

### 5.1 Mock 规则 API
- `GET /api/rules` - 获取规则列表（支持分页、按启用状态/方法/分组过滤）
- `GET /api/rules/{id}` - 获取单个规则详情
- `POST /api/rules` - 创建规则
- `PATCH /api/rules/{id}` - 更新规则（支持部分更新）
- `DELETE /api/rules/{id}` - 删除规则
- `POST /api/rules/{id}/copy` - 复制规则
- `POST /api/rules/import` - 批量导入规则（事务处理）
- `GET /api/rules/export` - 导出规则

### 5.2 规则分组 API
- `GET /api/groups` - 获取分组列表
- `POST /api/groups` - 创建分组
- `PATCH /api/groups/{id}` - 更新分组
- `DELETE /api/groups/{id}` - 删除分组

### 5.3 请求记录 API
- `GET /api/requests` - 获取请求记录列表（支持分页、过滤）
- `GET /api/requests/{id}` - 获取请求详情
- `DELETE /api/requests` - 清空请求记录
- `GET /api/requests/stats` - 请求统计
- `POST /api/requests/{id}/replay` - 请求回放保存为测试用例

### 5.4 系统 API
- `GET /health` - Mock 服务健康检查
- `GET /api/config` - 获取系统配置（采样率、清理策略等）

### 5.5 Mock 服务 API（动态路由）
- `ANY /mock/**` - Mock 服务入口，匹配所有配置的规则

## 六、动态路由实现方案

采用 **Filter + 规则引擎** 方式统一拦截处理：

1. 实现 `MockFilter` 拦截 `/mock/**` 路径的所有请求
2. 规则引擎按优先级顺序匹配：`pathMatchType` → `method` → `conditions`
3. 规则数据使用 **Caffeine 内存缓存**，数据库变更时通过事件通知刷新缓存
4. 规则热更新使用 **读写锁** 保证并发安全

## 七、性能与可靠性设计

### 7.1 请求记录清理策略
- 默认 TTL：7 天
- 配置项：`requestLog.retentionDays`（可调整）
- 定时任务：每日凌晨 3:00 执行清理

### 7.2 规则缓存策略
- 使用 Caffeine 缓存活跃规则
- 缓存刷新：规则 CRUD 操作后立即刷新
- 最大缓存数：1000 条规则

### 7.3 请求记录采样率
- 默认采样率：100%（全量记录）
- 配置项：`requestLog.sampleRate`（0.01~1.0）
- 高并发场景可降低采样率

## 八、前端设计

### 8.1 规则编辑器增强
- JSON 编辑器：集成 **Monaco Editor**，支持语法高亮、校验、自动补全
- 响应体模板：预置常用响应模板（成功/失败/分页等）

### 8.2 请求记录实时展示
- 使用 **SSE (Server-Sent Events)** 实现实时推送
- 端点：`GET /api/requests/stream`
- 支持暂停/恢复实时更新

## 九、新增功能点

| 功能点 | 描述 |
|--------|------|
| 规则分组 | 按项目/模块分组管理规则 |
| 规则复制 | 快速复制已有规则，便于创建相似规则 |
| 请求回放 | 将历史请求保存为测试用例，便于回归测试 |
| 健康检查 | `/health` 端点，返回服务状态、规则数量、缓存状态 |

## 十、开发计划

| 阶段 | 内容 | 预计工时 |
|------|------|----------|
| P0 | 项目初始化、基础架构搭建 | 1 天 |
| P1-1 | 动态路由核心（Filter + 缓存） | 1 天 |
| P1-2 | 条件匹配引擎 | 1 天 |
| P2 | 请求记录功能 + 清理策略 | 1.5 天 |
| P3-1 | 前端框架 + 规则管理页 | 2 天 |
| P3-2 | 规则编辑器（Monaco Editor） + 请求记录实时展示（SSE） | 2 天 |
| P4 | 测试与优化 | 1 天 |

**总工时：约 8.5 天**

## 十一、后续扩展方向

1. **团队协作**: 多用户、工作空间、规则分享
2. **高级匹配**: 正则表达式、JSON Schema 校验
3. **动态响应**: 基于 JS 脚本的动态响应生成
4. **代理模式**: 代理真实服务，选择性 Mock
5. **OpenAPI 集成**: 从 Swagger/OpenAPI 文档自动生成 Mock 规则