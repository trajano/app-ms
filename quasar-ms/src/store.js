import Vue from 'vue'
import Vuex from 'vuex'
import api from './stores/apiStore'
import oauthToken from './stores/oauthTokenStore'

Vue.use(Vuex)
const store = new Vuex.Store({
  modules: {
    api,
    oauthToken
  }
})
export default store
