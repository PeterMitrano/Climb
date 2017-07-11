proto = require("google-protobuf");
msgs = require("./Gym_pb.js");

let gym = new msgs.Gym();
let new_wall;
let canvas, stage;
let background;
let down;
let points = [];
let color = "#ff0000"
let gym_name_input;
let icon_url_input;

window.onload = function init() {
  canvas = document.getElementById('map-canvas');
  let sidebar = document.getElementById('sidebar');
  let upload_button = document.getElementById('upload-file-button');
  let download_button = document.getElementById('download-file-button');
  gym_name_input = document.getElementById('gym-name');
  icon_url_input = document.getElementById('large-icon-url');
  let new_wall_fab = document.getElementById('add-wall-fab');
  let new_route_fab = document.getElementById('add-route-fab');

  upload_button.onclick = handleUpload;
  download_button.onclick = handleDownload;

  // set the canvas size dynamically
  canvas.width = window.innerWidth - sidebar.clientWidth - 148;
  canvas.height = window.innerHeight - 130;

  stage = new createjs.Stage("map-canvas");
  canvas.style.backgroundColor = "#d2d2d2";

  stage.autoClear = false;
  stage.enableDOMEvents(true);
  createjs.Touch.enable(stage);
  createjs.Ticker.framerate = 24;
  background = new createjs.Shape();

  stage.addEventListener("stagemousedown", handleMouseDown);
  stage.addEventListener("stagemouseup", handleMouseUp);
  stage.addEventListener("stagemousemove", handleMouseMove);

  p0 = new msgs.Point2D();
  p0.setX(0);
  p0.setY(0);

  p1 = new msgs.Point2D();
  p1.setX(0);
  p1.setY(10);

  p2 = new msgs.Point2D();
  p2.setX(10);
  p2.setY(10);

  p3 = new msgs.Point2D();
  p3.setX(10);
  p3.setY(0);

  p4 = new msgs.Point2D();
  p4.setX(5);
  p4.setY(0);

  p5 = new msgs.Point2D();
  p5.setX(5);
  p5.setY(0);

  polygon = new msgs.Polygon();
  polygon.setColorCode("#ff00ff");
  polygon.setPointsList([p0, p4, p5]);

  route0 = new msgs.Route();
  route0.setName("Lappnor Project");
  route0.setPosition(p0);
  route0.setGrade(17);

  wall = new msgs.Wall();
  wall.setName("The Dawn Wall");
  wall.setPolygon(polygon);
  wall.setRoutesList([route0]);

  floor_polygon = new msgs.Polygon();
  floor_polygon.setColorCode("#ff00ff");
  floor_polygon.setPointsList([p0, p1, p2, p3]);

  floor = new msgs.Floor();
  floor.setWallsList([wall]);
  floor.setWidth(100);
  floor.setHeight(100);
  floor.setPolygon(floor_polygon);

  gym.setFloorsList([floor]);
  gym.setName("Ascend PGH");
  gym.setLargeIconUrl("https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/header-images/02-Header-Visiting-Ascend.jpg");

  stage.addChild(background);
  stage.update();

  drawGym();
};

function drawGym() {
  let g = background.graphics;

  let S = 40;
  let cols = canvas.width / S;
  let rows = canvas.height / S;

  for (let i = 0; i < rows; i++) {
    g.beginStroke("black").moveTo(0, i*S).lineTo(canvas.width, i*S);
  }

  for (let i = 0; i < cols; i++) {
    g.beginStroke("black").moveTo(i*S, 0).lineTo(i*S, canvas.height);
  }

  g.endStroke();

  console.log("drawing gym");

  stage.update();
}

function handleMouseMove(event) {
  if (!event.primary) { return; }
  let current_p = new createjs.Point(stage.mouseX, stage.mouseY);

  if (points.length > 0) {
    let g = background.graphics;
    g.clear();
    g.setStrokeStyle(4, 'round', 'round').beginStroke("black").moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g.lineTo(p.x, p.y);
    }
    g.lineTo(current_p.x, current_p.y);
  }
  stage.update();

}

function handleMouseClick(event, pt) {
  if (!event.primary) { return; }
  points.push(pt);
  let PT_SIZE = 4;
  let g = background.graphics;

  if (points.length > 1) {
    g.setStrokeStyle(4, 'round', 'round').beginStroke("black").moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g.lineTo(p.x, p.y);
      g.beginFill("black");
      g.drawCircle(p.x, p.y, PT_SIZE);
    }
  }
  else {
    g.beginFill("black").drawCircle(pt.x, pt.y, PT_SIZE);
  }

  stage.update();
}

function handleMouseDown(event) {
  if (!event.primary) { return; }
  down = new createjs.Point(stage.mouseX, stage.mouseY);
}

function handleMouseUp(event) {
  if (!event.primary) { return; }
  let up = new createjs.Point(stage.mouseX, stage.mouseY);
  let dx = up.x - down.x;
  let dy = up.y - down.y;
  let l2_dist = Math.sqrt(dx * dx + dy * dy);

  if (l2_dist == 0) {
    handleMouseClick(event, up);
  }
}

function handleDownload(event) {
  gym.setName(gym_name_input.value);
  gym.setLargeIconUrl(icon_url_input.value);

  writer = new proto.BinaryWriter();
  gym.serializeBinaryToWriter(writer);
  contents = writer.getResultBase64String();

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
