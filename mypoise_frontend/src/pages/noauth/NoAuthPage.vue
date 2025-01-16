<template>
  <div class="not-found">
    <a-result
      status="404"
      title="404"
      sub-title="抱歉，页面没有找到"
    >
    <!--  插槽    -->
      <template #extra>
        <a-button type="primary" href="/">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup>
// 无需额外的脚本，所有功能已经通过 Ant Design Vue 的 <a-result> 组件实现
</script>

<style scoped>
.not-found {
  padding: 50px;
}
</style>
