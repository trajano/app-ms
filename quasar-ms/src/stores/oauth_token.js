/**
 * OAuth token vuex store.  This is namespaced so it won't be directly accessed by application specific code.
 */

function calculateExpiresIn(expiresInSeconds) {
  return (expiresInSeconds - 60) * 1000
  // return 15 * 1000
}

const oauthTokenStore = {
  namespaced: true,
  state: {
    accessToken: null,
    refreshToken: null,
    clientCredentials: null,
    refreshTimeout: null,
    requestedPath: null
  },
  mutations: {
    setTokenData(
      state,
      { clientCredentials, accessToken, refreshToken, refreshTimeout }
    ) {
      state.clientCredentials = clientCredentials
      state.accessToken = accessToken
      state.refreshToken = refreshToken
      state.refreshTimeout = refreshTimeout
    },
    clearTokenData(state) {
      state.clientCredentials = null
      state.accessToken = null
      state.refreshToken = null
      state.refreshTimeout = null
    },
    requestedPath(state, requestedPath) {
      state.requestedPath = requestedPath
    }
  },
  getters: {
    accessToken: state => {
      return state.accessToken
    },
    requestedPath: state => {
      return state.requestedPath != null ? state.requestedPath : '/'
    }
  },
  actions: {
    setTokenData(
      { dispatch, commit },
      { clientCredentials, accessToken, refreshToken, expiresIn }
    ) {
      const refreshTimeout = setTimeout(() => {
        dispatch('refreshToken')
      }, calculateExpiresIn(expiresIn))
      commit('setTokenData', {
        clientCredentials,
        accessToken,
        refreshToken,
        refreshTimeout
      })
    },
    async refreshToken({ dispatch, commit, state }) {
      const headers = new Headers()
      headers.set('Accept', 'application/json')
      headers.set('Authorization', 'Basic ' + state.clientCredentials)

      let body = new URLSearchParams()
      body.append('grant_type', 'refresh_token')
      body.append('refresh_token', state.refreshToken)
      let response = await fetch(process.env.GATEWAY_URI + '/refresh', {
        method: 'POST',
        headers,
        body
      })
      if (response.status === 401 || response.status === 400) {
        commit('clearTokenData')
        return
      }
      let data = await response.json()

      const refreshTimeout = setTimeout(() => {
        dispatch('refreshToken')
      }, calculateExpiresIn(data.expires_in))
      commit('setTokenData', {
        clientCredentials: state.clientCredentials,
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
        refreshTimeout
      })
    },
    signoff({ commit, state }) {
      clearTimeout(state.refreshTimeout)
      commit('clearTokenData')
    }
  }
}
export default oauthTokenStore
