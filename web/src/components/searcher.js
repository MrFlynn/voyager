import router from "@/router/index.js";

const search = query => {
  if (query != null) {
    router.push({
      name: "search",
      query: { query: encodeURI(query) }
    });
  }
};

export default search;
