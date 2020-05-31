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
          <div class="field has-addons page-control">
            <p class="control">
              <button
                v-on:click="changePage(-numResults)"
                class="button is-dark"
              >
                <span>Previous</span>
              </button>
            </p>
            <p class="control">
              <button
                v-on:click="changePage(numResults)"
                class="button is-dark"
              >
                <span>Next</span>
              </button>
            </p>
          </div>
        </div>
        <div class="column"></div>
      </div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
.navbar
  padding: 0.5em

.page-control
  padding-top: 2em
  display: flex
  justify-content: center
  align-items: center
</style>

<script>
import router from "@/router/index.js";
import searcher from "@/components/searcher.js";

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
      newQuery: null,
      numResults: 10
    };
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.getResults();
    });
  },
  beforeRouteUpdate(to, from, next) {
    this.results = [];
    next(vm => {
      vm.getResults();
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
  watch: {
    // eslint-disable-next-line
    "$route": "getResults" // Honestly I have no idea why this is needed.
  },
  methods: {
    search(query) {
      searcher(encodeURI(query));
    },
    getResults() {
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
    changePage(offset) {
      if (
        !(this.results.length < this.numResults) &&
        parseInt(this.after) + parseInt(offset) >= 0
      ) {
        searcher(this.searchQuery, parseInt(this.after) + parseInt(offset));
      }
    }
  }
};
</script>
