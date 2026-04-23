<script setup lang="ts">
import {
  DropdownMenuRoot,
  DropdownMenuTrigger,
  DropdownMenuPortal,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuLabel
} from 'radix-vue'
import { cn } from '@/lib/utils'

interface Props {
  class?: string
  contentClass?: string
}

const props = defineProps<Props>()

interface MenuItem {
  label: string
  action?: () => void
  disabled?: boolean
  danger?: boolean
}

const items = defineModel<MenuItem[]>('items', { default: [] })
</script>

<template>
  <DropdownMenuRoot>
    <DropdownMenuTrigger as-child>
      <slot name="trigger" />
    </DropdownMenuTrigger>
    <DropdownMenuPortal>
      <DropdownMenuContent
        :class="cn(
          'z-50 min-w-[8rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2',
          contentClass
        )"
        :side-offset="5"
        align="start"
      >
        <slot />
        <template v-if="items.length">
          <DropdownMenuLabel v-if="$slots.label">
            <slot name="label" />
          </DropdownMenuLabel>
          <DropdownMenuSeparator v-if="$slots.label" class="-mx-1 my-1 h-px bg-muted" />
          <DropdownMenuItem
            v-for="item in items"
            :key="item.label"
            :disabled="item.disabled"
            :class="cn(
              'relative flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none transition-colors focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
              item.danger && 'text-destructive focus:text-destructive'
            )"
            @select="item.action"
          >
            {{ item.label }}
          </DropdownMenuItem>
        </template>
      </DropdownMenuContent>
    </DropdownMenuPortal>
  </DropdownMenuRoot>
</template>