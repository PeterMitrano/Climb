const express = require('express');
const proto = require('google-protobuf');
const msgs = require('./js/Gym_pb.js');
const app = express();
const port = 3000;

app.use("/", express.static(__dirname));

app.get('/gyms', (request, response) => {
  response.send('gyms');
})

app.listen(port, (err) => {
  if (err) {
    return console.log('Error', err);
  }
})
