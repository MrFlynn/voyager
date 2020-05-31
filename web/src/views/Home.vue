<template>
  <div>
    <div class="container">
      <div class="columns search-container">
        <div class="column"></div>
        <div class="column is-half">
          <div class="field has-addons">
            <div class="control is-expanded">
              <input
                v-model="searchQuery"
                v-on:keyup.enter="search(searchQuery)"
                class="input"
                type="text"
                placeholder="Search..."
              />
            </div>
            <div class="control">
              <a v-on:click="search(searchQuery)" class="button is-dark">Go!</a>
            </div>
          </div>
        </div>
        <div class="column"></div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<style lang="sass" scoped>
.search-container
  padding-top: 30vh
  margin: 1em

.footer
  position: absolute
  width: 100%
  bottom: 0
  overflow: hidden
</style>

<script>
import searcher from "@/components/searcher.js";

import Footer from "@/components/Footer.vue";

export default {
  name: "Home",
  components: {
    Footer
  },
  data() {
    return {
      searchQuery: null
    };
  },
  methods: {
    search(query) {
      searcher(encodeURI(query));
    }
  }
};
</script>
