var canvas, stage;
var drawingCanvas;
var oldPt;
var oldMidPt;
var color;
var stroke;
var colors;
var index;
var points = [];

function init() {
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

  stage.addChild(drawingCanvas);
  stage.update();
}

function handleMouseClick(event) {
  if (!event.primary) { return; }
  console.log("click");
}

function handleMouseDown(event) {
  if (!event.primary) { return; }
  color = "#ffffff"
  stroke = Math.random() * 30 + 10 | 0;
  oldPt = new createjs.Point(stage.mouseX, stage.mouseY);
  oldMidPt = oldPt.clone();
  stage.addEventListener("stagemousemove", handleMouseMove);
}

function handleMouseMove(event) {
  if (!event.primary) { return; }
  var midPt = new createjs.Point(oldPt.x + stage.mouseX >> 1, oldPt.y + stage.mouseY >> 1);
  drawingCanvas.graphics.clear().setStrokeStyle(stroke, 'round', 'round').beginStroke(color).moveTo(midPt.x, midPt.y).curveTo(oldPt.x, oldPt.y, oldMidPt.x, oldMidPt.y);
  oldPt.x = stage.mouseX;
  oldPt.y = stage.mouseY;
  oldMidPt.x = midPt.x;
  oldMidPt.y = midPt.y;
  stage.update();
}

function handleMouseUp(event) {
  if (!event.primary) { return; }
  stage.removeEventListener("stagemousemove", handleMouseMove);
}

