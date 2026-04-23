<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { LayoutDashboard, FileText, FolderTree, History } from 'lucide-vue-next'
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Sidebar -->
    <aside class="fixed inset-y-0 left-0 z-50 w-64 border-r bg-card">
      <div class="flex h-14 items-center border-b px-4">
        <h1 class="text-lg font-semibold">Amock</h1>
      </div>
      <nav class="flex flex-col gap-1 p-4">
        <RouterLink
          to="/"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent hover:text-accent-foreground"
          active-class="bg-accent text-accent-foreground"
        >
          <LayoutDashboard class="h-4 w-4" />
          仪表盘
        </RouterLink>
        <RouterLink
          to="/rules"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent hover:text-accent-foreground"
          active-class="bg-accent text-accent-foreground"
        >
          <FileText class="h-4 w-4" />
          规则管理
        </RouterLink>
        <RouterLink
          to="/groups"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent hover:text-accent-foreground"
          active-class="bg-accent text-accent-foreground"
        >
          <FolderTree class="h-4 w-4" />
          分组管理
        </RouterLink>
        <RouterLink
          to="/requests"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent hover:text-accent-foreground"
          active-class="bg-accent text-accent-foreground"
        >
          <History class="h-4 w-4" />
          请求记录
        </RouterLink>
      </nav>
    </aside>

    <!-- Main content -->
    <main class="pl-64">
      <RouterView />
    </main>
  </div>
</template>