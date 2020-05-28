<template>
  <div id="app">
    <router-view />
    <Footer />
  </div>
</template>

<style lang="sass">
@import "bulma"

.footer
  position: absolute
  width: 100%
  bottom: 0
  overflow: hidden
</style>

<script>
import Footer from "@/components/Footer.vue";

export default {
  name: "App",
  components: {
    Footer
  }
};
</script>
