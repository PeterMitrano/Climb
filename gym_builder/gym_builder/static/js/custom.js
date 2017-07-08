proto = require("./Gym_pb.js");

let gym = new proto.Gym();
let canvas, stage;
let drawingCanvas;
let down;
let points = [];
let color = "#ff0000"

window.onload = function init() {
  // set the canvas size dynamically
  canvas = document.getElementById('map-canvas');
  sidebar = document.getElementById('sidebar');
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
    // click event
    handleMouseClick(event, up);
  }
}
