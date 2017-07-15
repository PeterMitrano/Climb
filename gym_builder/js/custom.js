const proto = require('google-protobuf');
const msgs = require('./proto/Gym_pb.js');

let gym = new msgs.Gym();
let new_wall;
let canvas, stage;
let background;
let down;
let points = [];
let color = '#ff0000';
let gym_name_input;
let icon_url_input;
let floors_chart;
let floors_chart_height;
let floors;
let floor_number;
let floor_add;

window.onload = function init() {
  floor_number = 0;
  canvas = document.getElementById('map-canvas');
  gym_name_input = document.getElementById('gym-name');
  icon_url_input = document.getElementById('large-icon-url');
  floors = document.getElementById('floors');
  floors_chart = document.getElementById('floors-chart');
  let sidebar = document.getElementById('sidebar');
  let upload_button = document.getElementById('upload-file-button');
  let download_button = document.getElementById('download-file-button');
  let new_wall_fab = document.getElementById('add-wall-fab');
  let new_route_fab = document.getElementById('add-route-fab');

  floors_chart_height = parseInt(window.getComputedStyle(floors_chart).height);

  upload_button.onclick = handleUpload;
  download_button.onclick = handleDownload;

  // set the canvas size dynamically
  canvas.width = window.innerWidth - sidebar.clientWidth - 148;
  canvas.height = window.innerHeight - 130;

  stage = new createjs.Stage('map-canvas');
  canvas.style.backgroundColor = '#d2d2d2';

  stage.autoClear = false;
  stage.enableDOMEvents(true);
  createjs.Touch.enable(stage);
  createjs.Ticker.framerate = 24;
  background = new createjs.Shape();

  stage.addEventListener('stagemousedown', handleMouseDown);
  stage.addEventListener('stagemouseup', handleMouseUp);
  stage.addEventListener('stagemousemove', handleMouseMove);

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

  gym.setFloorsList([floor]);
  gym.setName('Ascend PGH');
  gym.setLargeIconUrl(
      'https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/header-images/02-Header-Visiting-Ascend.jpg');

  stage.addChild(background);
  stage.update();

  drawGym();
};

function drawGym() {
  let g = background.graphics;

  let S = 40;
  let cols = Math.trunc(canvas.width / S);
  let rows = Math.trunc(canvas.height / S);

  // trunacate grid size to match grid
  canvas.width = S * cols;
  canvas.height = S * rows;

  g.beginStroke('black');

  for (let i = 0; i < rows; i++) {
    g.moveTo(0, i * S).lineTo(canvas.width, i * S);
  }

  for (let i = 0; i < cols; i++) {
    g.moveTo(i * S, 0).lineTo(i * S, canvas.height);
  }

  g.moveTo(canvas.width, 0).lineTo(canvas.width, canvas.height);
  g.moveTo(0, canvas.height).lineTo(canvas.width, canvas.height);

  g.endStroke();

  stage.update();
}

function handleMouseMove(event) {
  if (!event.primary) {
    return;
  }
  let current_p = new createjs.Point(stage.mouseX, stage.mouseY);

  if (points.length > 0) {
    let g = background.graphics;
    g.clear();
    g.setStrokeStyle(4, 'round', 'round').
        beginStroke('black').
        moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g.lineTo(p.x, p.y);
    }
    g.lineTo(current_p.x, current_p.y);
  }
  stage.update();

}

function handleMouseClick(event, pt) {
  if (!event.primary) {
    return;
  }
  points.push(pt);
  let PT_SIZE = 4;
  let g = background.graphics;

  if (points.length > 1) {
    g.setStrokeStyle(4, 'round', 'round').
        beginStroke('black').
        moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g.lineTo(p.x, p.y);
      g.beginFill('black');
      g.drawCircle(p.x, p.y, PT_SIZE);
    }
  }
  else {
    g.beginFill('black').drawCircle(pt.x, pt.y, PT_SIZE);
  }

  stage.update();
}

function handleMouseDown(event) {
  if (!event.primary) {
    return;
  }
  down = new createjs.Point(stage.mouseX, stage.mouseY);
}

function handleMouseUp(event) {
  if (!event.primary) {
    return;
  }
  let up = new createjs.Point(stage.mouseX, stage.mouseY);
  let dx = up.x - down.x;
  let dy = up.y - down.y;
  let l2_dist = Math.sqrt(dx * dx + dy * dy);

  if (l2_dist === 0) {
    handleMouseClick(event, up);
  }
}

function handleDownload(event) {
  gym.setName(gym_name_input.value);
  gym.setLargeIconUrl(icon_url_input.value);

  let writer = new proto.BinaryWriter();
  gym.serializeBinaryToWriter(writer);
  let contents = writer.getResultBase64String();

  let element = document.createElement('a');
  element.setAttribute('href', 'data:,' + contents);
  element.setAttribute('download', 'my_gym.map');

  element.style.display = 'none';
  document.body.appendChild(element);

  element.click();

  document.body.removeChild(element);
}

function handleUpload(event) {
}

function showFloor(event) {
  console.log(event);
}

let floor_divs = document.getElementsByClassName('floor-n');
for (let i = 0; i < floor_divs.length; i++) {
  let floor = floor_divs[i];
  floor.onclick = showFloor;
}
