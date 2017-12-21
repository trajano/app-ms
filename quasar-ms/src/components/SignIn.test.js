import { mount } from 'vue-test-utils'
import SignIn from './SignIn'

describe('SignIn', () => {
  it('renders with no problem', () => {
    const wrapper = mount(SignIn, { mocks: { $q: {} } })

    expect(wrapper.html())
  })
})
