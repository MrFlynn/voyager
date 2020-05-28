<template>
  <div class="container">
    <div class="columns search-container">
      <div class="column"></div>
      <div class="column is-half">
        <div class="field has-addons">
          <div class="control is-expanded">
            <input
              v-model="searchQuery"
              v-on:keyup.enter="search"
              class="input"
              type="text"
              placeholder="Search..."
            />
          </div>
          <div class="control">
            <a v-on:click="search" class="button is-dark">Go!</a>
          </div>
        </div>
      </div>
      <div class="column"></div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
.search-container
  padding-top: 20em;
</style>

<script>
import router from "@/router/index.js";

export default {
  name: "Home",
  data() {
    return {
      searchQuery: null
    };
  },
  methods: {
    search: function() {
      if (this.searchQuery != null) {
        router.push({
          name: "search",
          query: { query: encodeURI(this.searchQuery) }
        });
      }
    }
  }
};
</script>
