<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useGroupsStore, useRulesStore } from '@/stores'
import {
  Button, Input, Card, CardContent,
  Table, TableHeader, TableHead, TableBody, TableRow, TableCell,
  Dialog, Badge
} from '@/components/ui'
import { Plus, Pencil, Trash2, FolderTree } from 'lucide-vue-next'
import type { CreateGroupRequest, RuleGroup, MockRule } from '@/api'

const groupsStore = useGroupsStore()
const rulesStore = useRulesStore()

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingGroup = ref<RuleGroup | null>(null)

const form = ref<CreateGroupRequest>({
  name: '',
  description: ''
})

onMounted(async () => {
  await Promise.all([
    groupsStore.fetchGroups(),
    rulesStore.fetchRules()
  ])
})

function getRulesCount(groupId: string): number {
  return rulesStore.rules.filter((r: MockRule) => r.groupId === groupId).length
}

function openEditDialog(group: RuleGroup) {
  editingGroup.value = group
  form.value = {
    name: group.name,
    description: group.description
  }
  showEditDialog.value = true
}

async function handleCreate() {
  try {
    await groupsStore.createGroup(form.value)
    showCreateDialog.value = false
    resetForm()
  } catch (e) {
    console.error('Create failed:', e)
  }
}

async function handleUpdate() {
  if (!editingGroup.value) return
  try {
    await groupsStore.updateGroup(editingGroup.value.id, form.value)
    showEditDialog.value = false
    editingGroup.value = null
    resetForm()
  } catch (e) {
    console.error('Update failed:', e)
  }
}

async function handleDelete(id: string) {
  const rulesCount = getRulesCount(id)
  if (rulesCount > 0) {
    alert(`该分组下有 ${rulesCount} 个规则，请先移除规则再删除分组`)
    return
  }
  if (confirm('确定要删除此分组吗？')) {
    try {
      await groupsStore.deleteGroup(id)
    } catch (e) {
      console.error('Delete failed:', e)
    }
  }
}

function resetForm() {
  form.value = {
    name: '',
    description: ''
  }
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">分组管理</h1>
      <Button @click="showCreateDialog = true">
        <Plus class="h-4 w-4 mr-1" />
        新建分组
      </Button>
    </div>

    <Card>
      <CardContent class="pt-4">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>分组名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>规则数量</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead class="w-[100px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="groupsStore.loading">
              <TableCell colspan="5" class="text-center text-muted-foreground">
                加载中...
              </TableCell>
            </TableRow>
            <TableRow v-if="!groupsStore.loading && groupsStore.groups.length === 0">
              <TableCell colspan="5" class="text-center text-muted-foreground">
                <div class="flex flex-col items-center py-8">
                  <FolderTree class="h-12 w-12 text-muted-foreground mb-2" />
                  <p>暂无分组</p>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-for="group in groupsStore.groups" :key="group.id">
              <TableCell class="font-medium">{{ group.name }}</TableCell>
              <TableCell>{{ group.description || '-' }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ getRulesCount(group.id) }}</Badge>
              </TableCell>
              <TableCell class="text-sm text-muted-foreground">
                {{ new Date(group.createdAt).toLocaleDateString() }}
              </TableCell>
              <TableCell>
                <div class="flex gap-1">
                  <Button variant="ghost" size="icon" @click="openEditDialog(group)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon" @click="handleDelete(group.id)">
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <!-- Create dialog -->
    <Dialog v-model:open="showCreateDialog">
      <template #trigger />
      <template #title>新建分组</template>
      <template #description>创建一个新的规则分组</template>
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium">分组名称</label>
          <Input v-model="form.name" placeholder="输入分组名称" class="mt-1" />
        </div>
        <div>
          <label class="text-sm font-medium">描述</label>
          <Input v-model="form.description" placeholder="输入分组描述" class="mt-1" />
        </div>
        <div class="flex justify-end gap-2 pt-4">
          <Button variant="outline" @click="showCreateDialog = false">取消</Button>
          <Button @click="handleCreate">创建</Button>
        </div>
      </div>
    </Dialog>

    <!-- Edit dialog -->
    <Dialog v-model:open="showEditDialog">
      <template #trigger />
      <template #title>编辑分组</template>
      <template #description>修改分组信息</template>
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium">分组名称</label>
          <Input v-model="form.name" placeholder="输入分组名称" class="mt-1" />
        </div>
        <div>
          <label class="text-sm font-medium">描述</label>
          <Input v-model="form.description" placeholder="输入分组描述" class="mt-1" />
        </div>
        <div class="flex justify-end gap-2 pt-4">
          <Button variant="outline" @click="showEditDialog = false">取消</Button>
          <Button @click="handleUpdate">保存</Button>
        </div>
      </div>
    </Dialog>
  </div>
</template>