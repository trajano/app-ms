
const headers = new Headers()
headers.set('Accept', 'application/json')
headers.set('Authorization', 'Basic YXBwX2lkOmFwcF9zZWNyZXQ=')
const body = new URLSearchParams()
body.append('j_username', this.username)
body.append('j_password', this.password)
let response = await fetch('http://192.168.1.42:3001/v1/authn', {
  method: 'POST',
  headers,
  body
})
if (response.status === 200) {
  let data = await response.json()
  this.$store.dispatch('oauthToken/setTokenData', {
    clientCredentials: 'YXBwX2lkOmFwcF9zZWNyZXQ=',
    accessToken: data.access_token,
    refreshToken: data.refresh_token,
    expiresIn: data.expires_in
  })
  this.$router.push(this.$store.getters['oauthToken/requestedPath'])
} else {
  this.message = 'Invalid username or password'
  this.username = ''
  this.password = ''
}


const state = base64url.encode(this.$store.getters['oauthToken/requestedPath'])

const acceptJson = new Headers()
acceptJson.set('Accept', 'application/json')
acceptJson.set('Authorization', 'Basic b2lkY19pZDphcHBfc2VjcmV0')
let response = await fetch(
  'http://localhost:3001/oidc/auth-info/google?state=' + state,
  { headers: acceptJson }
)
let data = await response.json()
this.oidcUri = data.uri
window.location.href = this.oidcUri
