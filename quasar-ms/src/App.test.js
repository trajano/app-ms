import { mount } from 'vue-test-utils'
import App from './App'

describe('App', () => {
  it('renders with no problem', () => {
    const wrapper = mount(App)

    expect(wrapper.html())
  })
})
