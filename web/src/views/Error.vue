<template>
  <div class="container">
    <div class="columns">
      <div class="column"></div>
      <div class="column is-one-third">
        <div class="notification is-danger">
          <button v-on:click="$router.push('/')" class="delete"></button>
          <p v-if="genericError">We're sorry, but something has gone wrong.</p>
          <p v-else>This page does not exist.</p>
          <br />
          Click the <strong>x</strong> to return home.
        </div>
      </div>
      <div class="column"></div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
.notification
  margin-top: 30vh
</style>

<script>
export default {
  name: "Error",
  props: {
    genericError: {
      type: Boolean,
      default: true
    }
  }
};
</script>
