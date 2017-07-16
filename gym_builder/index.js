const proto = require('google-protobuf');
const express = require('express');
const msgs = require('./public/js/proto/Gym_pb.js');
const google_auth = require('google-auth-library');
const body_parser = require('body-parser');
const fs = require('fs');
const app = express();
const port = 3000;
const client_id = '41352784373-92ucj15fdse277kre1458uorhd0vlacl.apps.googleusercontent.com';

app.use(body_parser.urlencoded({extended: false}));

let auth = new google_auth;
let client = new auth.OAuth2(client_id, '', '');

app.use('/', express.static(__dirname + '/public'));

app.use('/gyms', function(request, response) {
  if (request.method === 'POST') {
    token = request.body.idtoken;
    client.verifyIdToken(token, client_id, function(e, login) {
      if (e) {
        console.log('invalid token ' + e);
        response.status(401).send('invalid token ' + e);
      }
      else {
        let payload = login.getPayload();
        let userid = payload['sub'];

        // look up the gym(s) corresponding to the userid
        let gyms = fakeGyms();

        let writer = new proto.BinaryWriter();
        gyms.serializeBinaryToWriter(writer);
        let data = writer.getResultBase64String();

        response.status(200).send(data);
      }
    });
  }
  else {
    response.status(405).send('Method not allowed');
  }
});

app.listen(port, (err) => {
  if (err) {
    return console.log('Error', err);
  }
});

function fakeGyms () {
  let p0 = new msgs.Point2D();
  p0.setX(0);
  p0.setY(0);

  let p1 = new msgs.Point2D();
  p1.setX(0);
  p1.setY(10);

  let p2 = new msgs.Point2D();
  p2.setX(10);
  p2.setY(10);

  let p3 = new msgs.Point2D();
  p3.setX(10);
  p3.setY(0);

  let p4 = new msgs.Point2D();
  p4.setX(5);
  p4.setY(0);

  let p5 = new msgs.Point2D();
  p5.setX(5);
  p5.setY(0);

  let polygon = new msgs.Polygon();
  polygon.setColor('#ff00ff');
  polygon.setPointsList([p0, p4, p5]);

  let route0 = new msgs.Route();
  route0.setName('Lappnor Project');
  route0.setPosition(p0);
  route0.setGrade(17);

  let wall = new msgs.Wall();
  wall.setName('The Dawn Wall');
  wall.setPolygon(polygon);
  wall.setRoutesList([route0]);

  let floor_polygon = new msgs.Polygon();
  floor_polygon.setColor('#ff00ff');
  floor_polygon.setPointsList([p0, p1, p2, p3]);

  let floor = new msgs.Floor();
  floor.setWallsList([wall]);
  floor.setWidth(100);
  floor.setHeight(100);
  floor.setPolygon(floor_polygon);

  let gym = new msgs.Gym();
  gym.setFloorsList([floor]);
  gym.setName('Ascend PGH');
  gym.setLargeIconUrl(
      'https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/header-images/02-Header-Visiting-Ascend.jpg');

  let gyms = new msgs.Gyms();
  gyms.setGymsList([gym]);

  return gyms;
}
