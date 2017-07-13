const express = require('express');
const msgs = require('./js/Gym_pb.js');
const fs = require('fs');
const app = express();
const port = 3000;

app.use("/", express.static(__dirname));

app.listen(port, (err) => {
  if (err) {
    return console.log('Error', err);
  }
})
