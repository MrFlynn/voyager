<template>
  <div id="search>">
    <nav
      class="navbar is-dark"
      role="navigation"
      aria-label="search navigation"
    >
      <div class="field has-addons">
        <div class="control is-expanded">
          <input
            v-model="newQuery"
            v-on:keyup.enter="search(newQuery)"
            class="input"
            type="text"
            placeholder="Search..."
          />
        </div>
        <div class="control">
          <a v-on:click="search(searchQuery)" class="button is-info">Go!</a>
        </div>
      </div>
    </nav>
    <div class="container">
      <div class="columns results-container">
        <div class="column"></div>
        <div class="column is-two-thirds">
          <ul v-for="result in results" :key="result.url">
            <SearchResult
              :title="result.title"
              :url="result.url"
              :description="result.description"
            />
          </ul>
        </div>
        <div class="column"></div>
      </div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
.navbar
  padding: 0.5em
</style>

<script>
import router from "@/router/index.js";
import search from "@/components/searcher.js";

import SearchResult from "@/components/SearchResult.vue";

import axios from "axios";

export default {
  name: "Search",
  components: {
    SearchResult
  },
  data() {
    return {
      results: [],
      newQuery: null
    };
  },
  created() {
    if (this.searchQuery == null) {
      router.replace("/");
    }

    var resultsUrl = `/api/search?query=${this.searchQuery}`;
    if (this.after > 0) {
      resultsUrl += `&after=${this.after}`;
    }

    axios
      .get(resultsUrl)
      .then(r => {
        this.results = r.data;
      })
      .catch(e => {
        console.log(e);
      });
  },
  props: {
    searchQuery: {
      type: String,
      default: null
    },
    after: {
      type: Number,
      default: 0
    }
  },
  methods: {
    search: search
  }
};
</script>
