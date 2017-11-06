import Vue from 'vue'
import Vuex from 'vuex'
import api from './stores/api'
import oauthToken from './stores/oauth_token'

Vue.use(Vuex)
const store = new Vuex.Store({
  modules: {
    api,
    oauthToken
  }
})
export default store
