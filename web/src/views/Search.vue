<template>
  <div class="about">
    <p>{{ results }}</p>
  </div>
</template>

<script>
import router from "@/router/index.js";

import axios from "axios";

export default {
  name: "Search",
  data() {
    return {
      results: []
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
  }
};
</script>
