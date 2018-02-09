var merge = require('webpack-merge')
var prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"docker-compose"',
  GATEWAY_URI: '"http://192.168.1.113:3002"'
})
