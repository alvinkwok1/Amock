<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as monaco from 'monaco-editor'

interface Props {
  modelValue: string
  language?: string
  height?: string
  readOnly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  language: 'json',
  height: '300px',
  readOnly: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'change': [value: string]
  'ready': [editor: monaco.editor.IStandaloneCodeEditor]
}>()

const containerRef = ref<HTMLDivElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

onMounted(() => {
  if (!containerRef.value) return

  editor = monaco.editor.create(containerRef.value, {
    value: props.modelValue,
    language: props.language,
    readOnly: props.readOnly,
    theme: 'vs',
    minimap: { enabled: false },
    automaticLayout: true,
    fontSize: 14,
    lineNumbers: 'on',
    roundedSelection: false,
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    folding: true,
    tabSize: 2,
    formatOnPaste: true,
    formatOnType: true
  })

  editor.onDidChangeModelContent(() => {
    const value = editor?.getValue() || ''
    emit('update:modelValue', value)
    emit('change', value)
  })

  emit('ready', editor)
})

onUnmounted(() => {
  if (editor) {
    editor.dispose()
  }
})

watch(() => props.modelValue, (newValue) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue)
  }
})

watch(() => props.readOnly, (newReadOnly) => {
  if (editor) {
    editor.updateOptions({ readOnly: newReadOnly })
  }
})

function format() {
  if (editor) {
    editor.getAction('editor.action.formatDocument')?.run()
  }
}

defineExpose({ format })
</script>

<template>
  <div ref="containerRef" :style="{ height: height, border: '1px solid var(--border)', borderRadius: 'var(--radius)' }" />
</template>