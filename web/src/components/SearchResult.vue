<template>
  <div class="box">
    <div class="media-content">
      <div class="content">
        <p>
          {{ this.score.toFixed(2) }}
          <a :href="this.url"
            ><strong>{{ this.title }}</strong></a
          >
          <br />
        </p>
        <p v-html="description">{{ description }}</p>
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
    },
    score: {
      type: Number,
      default: 0
    }
  }
};
</script>
