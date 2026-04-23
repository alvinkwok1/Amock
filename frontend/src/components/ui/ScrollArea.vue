<script setup lang="ts">
import { ScrollAreaRoot, ScrollAreaViewport, ScrollAreaScrollbar, ScrollAreaCorner } from 'radix-vue'
import { cn } from '@/lib/utils'

interface Props {
  class?: string
  viewportClass?: string
}

const props = defineProps<Props>()
</script>

<template>
  <ScrollAreaRoot :class="cn('relative overflow-hidden', props.class)">
    <ScrollAreaViewport :class="cn('h-full w-full rounded-[inherit]', viewportClass)">
      <slot />
    </ScrollAreaViewport>
    <ScrollAreaScrollbar
      class="flex touch-none select-none transition-colors data-[orientation=horizontal]:h-2.5 data-[orientation=vertical]:w-2.5 data-[orientation=horizontal]:flex-col data-[orientation=horizontal]:border-t data-[orientation=vertical]:border-l data-[orientation=horizontal]:[&>[data-radix-scroll-area-thumb]]:h-full data-[orientation=vertical]:[&>[data-radix-scroll-area-thumb]]:w-full"
      orientation="vertical"
    >
      <div class="relative flex-1 rounded-full bg-border" />
    </ScrollAreaScrollbar>
    <ScrollAreaScrollbar
      class="flex touch-none select-none transition-colors data-[orientation=horizontal]:h-2.5 data-[orientation=vertical]:w-2.5 data-[orientation=horizontal]:flex-col data-[orientation=horizontal]:border-t data-[orientation=vertical]:border-l data-[orientation=horizontal]:[&>[data-radix-scroll-area-thumb]]:h-full data-[orientation=vertical]:[&>[data-radix-scroll-area-thumb]]:w-full"
      orientation="horizontal"
    >
      <div class="relative flex-1 rounded-full bg-border" />
    </ScrollAreaScrollbar>
    <ScrollAreaCorner />
  </ScrollAreaRoot>
</template>