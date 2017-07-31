const proto = require('google-protobuf');
const express = require('express');
const msgs = require('./public/js/proto/Gym_pb.js');
const google_auth = require('google-auth-library');
const body_parser = require('body-parser');
const fs = require('fs');
const app = express();
const port = 8081;
const client_id = '41352784373-92ucj15fdse277kre1458uorhd0vlacl.apps.googleusercontent.com';

app.use(body_parser.urlencoded({extended: false}));

let auth = new google_auth;
let client = new auth.OAuth2(client_id, '', '');

app.use('/', express.static(__dirname + '/public'));

app.use('/gyms', function(request, response) {
  if (request.method === 'POST') {
    token = request.body.idtoken;
    if (token === undefined) {
      response.status(401).send("missing token");
    }
    else {
      client.verifyIdToken(token, client_id, function(e, login) {
        if (e) {
          console.log('invalid token ' + e);
          response.status(401).send('invalid token ' + e);
        }
        else {
          let payload = login.getPayload();
          let userid = payload['sub'];

          // look up the gym(s) corresponding to the userid
          response.status(204).send("Unimplemented");
        }
      });
    }
  }
  else if (request.method === 'GET') {
    let gyms = fakeGyms();

    let writer = new proto.BinaryWriter();
    gyms.serializeBinaryToWriter(writer);
    let data = writer.getResultBase64String();

    response.status(200).send(data);
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

function fakeGyms() {
  let ascend, climb_north;

  {
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
    p5.setX(0);
    p5.setY(5);

    let p6 = new msgs.Point2D();
    p6.setX(2);
    p6.setY(2);

    let p7 = new msgs.Point2D();
    p7.setX(2);
    p7.setY(1);

    let p8 = new msgs.Point2D();
    p8.setX(1);
    p8.setY(2);

    let polygon = new msgs.Polygon();
    polygon.setColor('#ff00ff');
    polygon.setPointsList([p0, p4, p5]);

    let route0 = new msgs.Route();
    route0.setName('Millenium Falcon');
    route0.setPosition(p7);
    route0.setGrade(9);
    route0.setColor('#E53935');

    let route1 = new msgs.Route();
    route1.setName('La Dura Dura');
    route1.setPosition(p8);
    route1.setGrade(16);
    route1.setColor('#5E35B1');

    let route2 = new msgs.Route();
    route2.setName('Burden of Dreams');
    route2.setPosition(p6);
    route2.setGrade(17);
    route2.setColor('#43A047');

    let wall = new msgs.Wall();
    wall.setName('The Dawn Wall');
    wall.setPolygon(polygon);
    wall.setRoutesList([route0, route1, route2]);

    let floor_polygon = new msgs.Polygon();
    floor_polygon.setColor('#ff0f0f');
    floor_polygon.setPointsList([p0, p1, p2, p3]);

    let floor = new msgs.Floor();
    floor.setWallsList([wall]);
    floor.setWidth(20);
    floor.setHeight(10);
    floor.setPolygon(floor_polygon);

    ascend = new msgs.Gym();
    ascend.setFloorsList([floor]);
    ascend.setName('Ascend PGH');
    ascend.setUuid('34e71c83-2d3e-424b-9fe9-57cd5fd1fbbb');
    ascend.setLargeIconUrl(
        'https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/Ascend-Mobile-Logo.png');

  }

  {
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
    p4.setY(10);

    let p5 = new msgs.Point2D();
    p5.setX(10);
    p5.setY(5);

    let p6 = new msgs.Point2D();
    p6.setX(8);
    p6.setY(8);

    let polygon = new msgs.Polygon();
    polygon.setColor('#fff00f');
    polygon.setPointsList([p0, p4, p5]);

    let route0 = new msgs.Route();
    route0.setName('Action Complete');
    route0.setPosition(p6);
    route0.setGrade(16);
    route0.setColor('#FDD835');

    let wall = new msgs.Wall();
    wall.setName('Wave');
    wall.setPolygon(polygon);
    wall.setRoutesList([route0]);

    let floor_polygon = new msgs.Polygon();
    floor_polygon.setColor('#ff0f0f');
    floor_polygon.setPointsList([p0, p1, p2, p3]);

    let floor = new msgs.Floor();
    floor.setWallsList([wall]);
    floor.setWidth(20);
    floor.setHeight(20);
    floor.setPolygon(floor_polygon);

    climb_north = new msgs.Gym();
    climb_north.setFloorsList([floor]);
    climb_north.setName('Climb North');
    climb_north.setUuid('b3d62be5-2d33-491b-8540-4e3cf77f64cd');
    climb_north.setLargeIconUrl(
        'https://pbs.twimg.com/profile_images/543849641700118528/TIfCknj8_400x400.jpeg');

  }

  let gyms = new msgs.Gyms();
  gyms.setGymsList([climb_north, ascend]);

  return gyms;
}
