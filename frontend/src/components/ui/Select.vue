<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  SelectRoot,
  SelectTrigger,
  SelectValue,
  SelectPortal,
  SelectContent,
  SelectViewport,
  SelectItem,
  SelectItemText,
  SelectItemIndicator,
  SelectScrollUpButton,
  SelectScrollDownButton
} from 'radix-vue'
import { ChevronDown, Check } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

interface Props {
  options: { value: string; label: string }[]
  placeholder?: string
  modelValue?: string
  disabled?: boolean
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择...'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const open = ref(false)
const selectedValue = computed(() => props.modelValue)
</script>

<template>
  <SelectRoot
    :open="open"
    :model-value="selectedValue"
    :disabled="disabled"
    @update:model-value="(v) => emit('update:modelValue', v)"
    @update:open="(v) => open = v"
  >
    <SelectTrigger
      :class="cn(
        'flex h-9 w-full items-center justify-between whitespace-nowrap rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50 [&>span]:line-clamp-1',
        props.class
      )"
    >
      <SelectValue :placeholder="placeholder" />
      <ChevronDown class="h-4 w-4 opacity-50" />
    </SelectTrigger>
    <SelectPortal>
      <SelectContent
        :class="cn(
          'relative z-50 max-h-96 min-w-[8rem] overflow-hidden rounded-md border bg-popover text-popover-foreground shadow-md data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2'
        )"
        position="popper"
        :side-offset="4"
      >
        <SelectScrollUpButton class="flex cursor-default items-center justify-center py-1">
          <ChevronDown class="h-4 w-4 rotate-180" />
        </SelectScrollUpButton>
        <SelectViewport class="p-1 h-[var(--radix-select-trigger-height)] w-full var(--radix-select-trigger-width)]">
          <SelectItem
            v-for="option in options"
            :key="option.value"
            :value="option.value"
            :class="cn(
              'relative flex w-full cursor-default select-none items-center rounded-sm py-1.5 pl-2 pr-8 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50'
            )"
          >
            <span class="absolute right-2 flex h-3.5 w-3.5 items-center justify-center">
              <SelectItemIndicator>
                <Check class="h-4 w-4" />
              </SelectItemIndicator>
            </span>
            <SelectItemText>{{ option.label }}</SelectItemText>
          </SelectItem>
        </SelectViewport>
        <SelectScrollDownButton class="flex cursor-default items-center justify-center py-1">
          <ChevronDown class="h-4 w-4" />
        </SelectScrollDownButton>
      </SelectContent>
    </SelectPortal>
  </SelectRoot>
</template>