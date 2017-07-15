const express = require('express');
const msgs = require('./public/js/proto/Gym_pb.js');
const fs = require('fs');
const app = express();
const port = 3000;

app.use("/", express.static(__dirname + '/public'));

app.use('/gyms', function(request, response) {
  response.send("hello world");
});

app.listen(port, (err) => {
  if (err) {
    return console.log('Error', err);
  }
});
