<template>
  <main class="box">
    <h1>页面不存在</h1>
    <p>请检查地址，或回到商城首页。</p>
    <RouterLink to="/">返回首页</RouterLink>
  </main>
</template>
<style scoped>
.box { min-height: 70vh; display: grid; place-items: center; text-align: center; }
a { color: var(--accent); font-weight: 700; }
</style>
