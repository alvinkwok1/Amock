<script setup lang="ts">
import {
  TabsRoot,
  TabsList,
  TabsTrigger,
  TabsContent
} from 'radix-vue'
import { cn } from '@/lib/utils'

interface Tab {
  value: string
  label: string
}

interface Props {
  defaultValue?: string
  modelValue?: string
  tabs?: Tab[]
  class?: string
  tabsClass?: string
  contentClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  tabs: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <TabsRoot
    :default-value="defaultValue"
    :model-value="modelValue"
    @update:model-value="(v: string) => emit('update:modelValue', v)"
    :class="cn('w-full', props.class)"
  >
    <TabsList
      :class="cn(
        'inline-flex h-9 items-center justify-center rounded-lg bg-muted p-1 text-muted-foreground',
        tabsClass
      )"
    >
      <TabsTrigger
        v-for="tab in tabs"
        :key="tab.value"
        :value="tab.value"
        :class="cn(
          'inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 data-[state=active]:bg-background data-[state=active]:text-foreground data-[state=active]:shadow'
        )"
      >
        {{ tab.label }}
      </TabsTrigger>
    </TabsList>
    <TabsContent
      v-for="tab in tabs"
      :key="tab.value"
      :value="tab.value"
      :class="cn(
        'mt-2 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        contentClass
      )"
    >
      <slot :name="tab.value" />
    </TabsContent>
  </TabsRoot>
</template>