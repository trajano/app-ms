import Vue from 'vue'
import VueRouter from 'vue-router'
import base64url from 'base64-url'
import store from './store'

Vue.use(VueRouter)

function load(component) {
  // '@' is aliased to src/components
  return () => import(`@/${component}.vue`)
}

const router = new VueRouter({
  /*
   * NOTE! VueRouter "history" mode DOESN'T works for Cordova builds,
   * it is only to be used only for websites.
   *
   * If you decide to go with "history" mode, please also open /config/index.js
   * and set "build.publicPath" to something other than an empty string.
   * Example: '/' instead of current ''
   *
   * If switching back to default "hash" mode, don't forget to set the
   * build publicPath back to '' so Cordova builds work again.
   */

  mode: navigator.userAgent.includes('Cordova') ? 'hash' : 'history',
  scrollBehavior: () => ({ y: 0 }),

  routes: [
    {
      path: '/cb',
      beforeEnter: (to, from, next) => {
        console.log(router.mode)
        console.log(window.location.hash.substring(1))
        console.log(to)
        const r = new URLSearchParams(window.location.hash.substring(1))
        store.dispatch('oauthToken/setTokenData', {
          clientCredentials: 'b2lkY19pZDphcHBfc2VjcmV0',
          accessToken: r.get('access_token'),
          refreshToken: r.get('refresh_token'),
          expiresIn: r.get('expires_in')
        })
        const requestedPath = base64url.decode(r.get('state'))
        next(requestedPath)
      },
      meta: {
        permit_all: true
      }
    },

    {
      path: '/signin',
      component: load('SignIn'),
      meta: {
        permit_all: true
      }
    },

    { path: '/2', component: load('Hello') },
    { path: '/', component: load('Hello') },

    // Always leave this last one
    { path: '*', component: load('Error404') } // Not found
  ]
})

router.beforeEach((to, from, next) => {
  if (!to.meta.permit_all && !store.getters.loggedIn) {
    store.commit('oauthToken/requestedPath', to.fullPath)
    next('/signin')
  } else {
    next()
  }
})

export default router
