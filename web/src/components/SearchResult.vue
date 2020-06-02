<template>
  <div class="box">
    <div class="media-content">
      <div class="content">
        <p>
          <a :href="this.url"
            ><strong>{{ this.title }}</strong></a
          >
          <br />
          {{ this.description }}
        </p>
      </div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
.box
  margin: 1em
</style>

<script>
export default {
  name: "SearchResult",
  props: {
    title: {
      type: String,
      default: null
    },
    url: {
      type: String,
      default: null
    },
    description: {
      type: String,
      default: null
    }
  }
};
</script>
