import api from './api'

describe('API', () => {
  it('is well defined', () => {
    expect(api.getters.loggedIn({}, {}))
    expect(api.getters.secureGet)
    expect(api.getters.securePost)
  })
})
