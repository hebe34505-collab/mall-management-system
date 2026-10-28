<template>
  <main class="box">
    <h1>没有访问权限</h1>
    <p>当前账号不能打开这个页面。</p>
    <RouterLink to="/">返回商城</RouterLink>
  </main>
</template>
<style scoped>
.box { min-height: 70vh; display: grid; place-items: center; text-align: center; }
a { color: var(--accent); font-weight: 700; }
</style>
