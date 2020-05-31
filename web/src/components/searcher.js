import router from "@/router/index.js";

const searcher = (query, after) => {
  if (query != null) {
    if (after === 0) {
      after = undefined;
    }

    router.push({
      name: "search",
      query: { query, after }
    });
  }
};

export default searcher;
