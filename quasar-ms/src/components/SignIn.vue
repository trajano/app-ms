<template>
  <div class='error-page window-height window-width bg-light column items-center no-wrap'>
    <div class='error-code bg-primary flex items-center content-center justify-center'>
      401
    </div>
    <div>
      <div class='error-card shadow-4 bg-white column items-center justify-center no-wrap'>
        <p class='text-center group' v-if='message'>
          {{ message }}
        </p>
        <form class='group' v-on:submit.prevent="noop">
          <q-input v-model='username' float-label='User Name' />
          <q-input v-model='password' type='password' float-label='Password' />
          <q-btn
            color='primary'
            push
            @click='signIn'
          >
            Sign In
          </q-btn>
          <q-btn
            color='primary'
            push
            @click='signInWithGoogle'
            icon='fa-google'
          >
            Sign In With Google
          </q-btn>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { QBtn, QIcon, QInput, Loading } from 'quasar'
import base64url from 'base64-url'

export default {
  components: {
    QBtn,
    QIcon,
    QInput
  },
  data() {
    return {
      username: '',
      password: '',
      message: null,
      nonce: null
    }
  },
  async beforeRouteEnter(to, from, next) {
    const headers = new Headers()
    headers.set('Accept', 'application/json')
    headers.set('Authorization', 'Basic YXBwX2lkOmFwcF9zZWNyZXQ=')
    let response = await fetch(process.env.GATEWAY_URI + '/v1/authn/nonce', {
      method: 'GET',
      headers
    })
    if (response.status === 200) {
      let data = await response.json()
      next(vm => vm.setNonce(data.nonce))
    }
  },
  methods: {
    async signIn() {
      Loading.show()
      const headers = new Headers()
      headers.set('Accept', 'application/json')
      headers.set('Authorization', 'Basic YXBwX2lkOmFwcF9zZWNyZXQ=')
      const body = new URLSearchParams()
      body.append('j_username', this.username)
      body.append('j_password', this.password)
      body.append('nonce', this.nonce)
      let response = await fetch(process.env.GATEWAY_URI + '/v1/authn', {
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
      Loading.hide()
    },
    async signInWithGoogle() {
      Loading.show()
      const state = base64url.encode(
        this.$store.getters['oauthToken/requestedPath']
      )

      const acceptJson = new Headers()
      acceptJson.set('Accept', 'application/json')
      acceptJson.set('Authorization', 'Basic b2lkY19pZDphcHBfc2VjcmV0')
      let response = await fetch(
        process.env.GATEWAY_URI + '/oidc/auth-info/google?state=' + state,
        { headers: acceptJson }
      )
      let data = await response.json()
      this.oidcUri = data.uri
      window.location.href = this.oidcUri
    },
    setNonce(nonce) {
      this.nonce = nonce
    },
    noop() {}
  }
}
</script>

<style lang='stylus'>
.error-page {
  .error-code {
    height: 50vh;
    width: 100%;
    padding-top: 15vh;

    @media (orientation: landscape) {
      font-size: 30vw;
    }

    @media (orientation: portrait) {
      font-size: 30vh;
    }

    color: rgba(255, 255, 255, 0.2);
    overflow: hidden;
  }

  .error-card {
    border-radius: 2px;
    margin-top: -50px;
    width: 80vw;
    max-width: 600px;
    padding: 25px;

    > i {
      font-size: 5rem;
    }
  }
}
</style>
