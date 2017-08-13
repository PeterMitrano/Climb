const proto = require('google-protobuf');
const msgs = require('./public/js/proto/Gym_pb.js');
const AWS = require('aws-sdk');

if (process.env.DEBUG) {
  console.log("USING DEBUG");
  AWS.config.update({
    region: 'region',
    endpoint: 'http://localhost:8000',
    accessKeyId: 'accessKeyId',
    secretAccessKey: 'secretAccessKey',
  });
}
else {
  AWS.config.update({
    region: 'us-east-1',
  });
}

let dynamo_client = new AWS.DynamoDB.DocumentClient();

const table_name = 'gyms';
const user_id_key = 'user_id_key';

let gyms = fakeGyms();
let promises = gyms.getGymsList().map(function(gym) {
  let writer = new proto.BinaryWriter();
  gym.serializeBinaryToWriter(writer);
  let gym_string = writer.getResultBase64String();
  let put_params = {
    TableName: table_name,
    Item: {
      'gym': gym_string,
    },
  };
  put_params.Item[user_id_key] = '' + Math.floor(Math.random() * 1000);

  // insert into db
  return dynamo_client.put(put_params).promise();
});

Promise.all(promises).then(function() {
  let scan_params = {
    TableName: table_name,
    ProjectionExpression: '#id',
    ExpressionAttributeNames: {
      '#id': user_id_key,
    },
  };
  return dynamo_client.scan(scan_params).promise();
}).then(function(results) {
  console.log(results);
}).catch(console.log.bind(console));

function fakeGyms() {
  let gyms = new msgs.Gyms();
  let ascend, climb_north;

  {
    let p0 = new msgs.Point2D();
    p0.setX(0);
    p0.setY(0);

    let p1 = new msgs.Point2D();
    p1.setX(0);
    p1.setY(10);

    let p2 = new msgs.Point2D();
    p2.setX(22);
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

    let p9 = new msgs.Point2D();
    p9.setX(0.5);
    p9.setY(2.5);

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

    let route3 = new msgs.Route();
    route3.setName('');
    route3.setPosition(p9);
    route3.setGrade(5);
    route3.setColor('#3F51B5');

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

    let polygon2 = new msgs.Polygon();
    polygon2.setColor('#f00f0f');
    polygon2.setPointsList([p0, p1, p4]);

    let wall2 = new msgs.Wall();
    wall2.setName('The Other Wall');
    wall2.setPolygon(polygon2);
    wall2.setRoutesList([route3]);

    let floor_polygon2 = new msgs.Polygon();
    floor_polygon2.setColor('#f0ff0f');
    floor_polygon2.setPointsList([p0, p1, p3]);

    let floor2 = new msgs.Floor();
    floor2.setWallsList([wall2]);
    floor2.setWidth(20);
    floor2.setHeight(10);
    floor2.setPolygon(floor_polygon2);

    ascend = new msgs.Gym();
    ascend.setFloorsList([floor, floor2]);
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

    let l = [ascend, climb_north];
    for (let i = 0; i < 100; ++i) {
      g = new msgs.Gym();
      g.setFloorsList([floor]);
      g.setName('fake_gym_' + i);
      g.setUuid(uuidv4());
      g.setLargeIconUrl('http://via.placeholder.com/400x400');
      l.push(g);
    }
    gyms.setGymsList(l);
  }

  return gyms;
}

function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}
