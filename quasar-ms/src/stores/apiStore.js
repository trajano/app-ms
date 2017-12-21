/**
 * Provides secure API calls.  This has no state, but it provides getters.
 */
const apiStore = {
  getters: {
    loggedIn: (state, getters) => {
      return getters['oauthToken/accessToken'] != null
    },
    secureGet: (state, getters) => async uri => {
      const headers = new Headers()
      headers.set('Accept', 'application/json')
      headers.set(
        'Authorization',
        'Bearer ' + getters['oauthToken/accessToken']
      )
      let response = await fetch(process.env.GATEWAY_URI + uri, { headers })
      return response.json()
    },
    securePost: (state, getters) => async (uri, jsonData) => {
      const headers = new Headers()
      headers.set('Accept', 'application/json')
      headers.set('Content-Type', 'application/json')
      headers.set(
        'Authorization',
        'Bearer ' + getters['oauthToken/accessToken']
      )
      let response = await fetch(process.env.GATEWAY_URI + uri, {
        method: 'POST',
        headers,
        body: JSON.stringify(jsonData)
      })
      return response.json()
    }
  }
}
export default apiStore
