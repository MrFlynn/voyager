import Vue from "vue";
import VueRouter from "vue-router";
import Home from "@/views/Home.vue";
import Error from "@/views/Error.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home
  },
  {
    path: "/search",
    name: "Search",
    component: () => import("@/views/Search.vue"),
    props: route => ({
      searchQuery: route.query.query,
      after: route.query.after
    })
  },
  {
    path: "/error",
    name: "Error",
    component: Error,
    props: {
      genericError: true
    }
  },
  {
    path: "*",
    name: "404",
    component: Error,
    props: {
      genericError: false
    }
  }
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
