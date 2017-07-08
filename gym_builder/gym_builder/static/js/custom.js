proto = require("google-protobuf");
msgs = require("gym_pb");

let gym = new msgs.Gym();
let canvas, stage;
let drawingCanvas;
let down;
let points = [];
let color = "#ff0000"
let gym_name_input;
let icon_url_input;

window.onload = function init() {
  canvas = document.getElementById('map-canvas');
  sidebar = document.getElementById('sidebar');
  upload_button = document.getElementById('upload-file-button');
  download_button = document.getElementById('download-file-button');
  gym_name_input = document.getElementById('gym-name');
  icon_url_input = document.getElementById('large-icon-url');

  upload_button.onclick = handleUpload;
  download_button.onclick = handleDownload;

  // set the canvas size dynamically
  canvas.width = window.innerWidth - sidebar.clientWidth - 118;
  canvas.height = window.innerHeight - 100;

  stage = new createjs.Stage("map-canvas");
  canvas.style.backgroundColor = "#d2d2d2";

  stage.autoClear = false;
  stage.enableDOMEvents(true);
  createjs.Touch.enable(stage);
  createjs.Ticker.framerate = 24;
  drawingCanvas = new createjs.Shape();

  stage.addEventListener("stagemousedown", handleMouseDown);
  stage.addEventListener("stagemouseup", handleMouseUp);
  stage.addEventListener("stagemousemove", handleMouseMove);

  p0 = new msgs.Point2D();
  p0.setX(0);
  p0.setY(0);

  p1 = new msgs.Point2D();
  p1.setX(1);
  p1.setY(1);

  polygon = new msgs.Polygon();
  polygon.setColorCode("#ff00ff");
  polygon.setPointsList([p0, p1]);

  route0 = new msgs.Route();
  route0.setName("Lappnor Project");
  route0.setPosition(p0);
  route0.setGrade(17);

  wall = new msgs.Wall();
  wall.setName("The Dawn Wall");
  wall.setPolygon(polygon);
  wall.setRoutesList([route0]);

  gym.setWallsList([wall]);
  gym.setName("Ascend PGH");
  gym.setLargeIconUrl("https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/header-images/02-Header-Visiting-Ascend.jpg");

  stage.addChild(drawingCanvas);
  stage.update();
}

function handleMouseMove(event) {
  if (!event.primary) { return; }
  let current_p = new createjs.Point(stage.mouseX, stage.mouseY);

  if (points.length > 0) {
    g = drawingCanvas.graphics;
    g.setStrokeStyle(4, 'round', 'round').beginStroke("#000").moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g = g.lineTo(p.x, p.y);
    }
    g = g.lineTo(current_p.x, current_p.y);
    g.endStroke();
  }
  stage.update();

}

function handleDownload(event) {
  gym.setName(gym_name_input.value);
  gym.setLargeIconUrl(icon_url_input.value);

  writer = new proto.BinaryWriter();
  gym.serializeBinaryToWriter(writer);
  contents = writer.getResultBase64String();

  let element = document.createElement('a');
  element.setAttribute('href', 'data:text/plain;base64,' + contents);
  element.setAttribute('download', 'my_gym.map');

  element.style.display = 'none';
  document.body.appendChild(element);

  element.click();

  document.body.removeChild(element);
}

function handleUpload(event) {
}

function handleMouseClick(event, pt) {
  if (!event.primary) { return; }
  points.push(pt);

  let g = new createjs.Graphics();

  if (points.length > 1) {
    g = drawingCanvas.graphics;
    g.setStrokeStyle(4, 'round', 'round').beginStroke("#000").moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length; i++) {
      let p = points[i];
      g = g.lineTo(p.x, p.y);
    }
    g.endStroke();
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
