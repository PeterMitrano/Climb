const proto = require('google-protobuf');
const msgs = require('./proto/Gym_pb.js');

let gyms = new msgs.Gyms();
let new_wall;
let canvas, stage;
let background;
let down;
let points = [];
let gym_name_input;
let icon_url_input;
let floors_chart;
let floors_chart_height;
let floors;
let floor_number;
let adding_wall = false;
let adding_route = false;
let sign_in_out_button;
let google_user;
let drawer;

window.onload = function init() {
  initClient();

  floor_number = 0;
  canvas = document.getElementById('map-canvas');
  gym_name_input = document.getElementById('gym-name');
  icon_url_input = document.getElementById('large-icon-url');
  floors = document.getElementById('floors');
  floors_chart = document.getElementById('floors-chart');
  sign_in_out_button = document.getElementById('sign-in-out-button');
  drawer = document.getElementById('drawer');

  let sidebar = document.getElementById('sidebar');
  let new_wall_fab = document.getElementById('add-wall-fab');
  let new_route_fab = document.getElementById('add-route-fab');

  floors_chart_height = parseInt(window.getComputedStyle(floors_chart).height);

  sign_in_out_button.onclick = signInOut;
  new_wall_fab.onclick = function() {
    adding_wall = true;
  };
  new_route_fab.onclick = function() {
    adding_route = true;
  };

  // set the canvas size dynamically
  canvas.width = window.innerWidth - 150;
  canvas.height = window.innerHeight - 150;

  stage = new createjs.Stage('map-canvas');
  canvas.style.backgroundColor = '#d2d2d2';

  stage.autoClear = false;
  stage.enableDOMEvents(true);
  createjs.Touch.enable(stage);
  createjs.Ticker.framerate = 24;
  background = new createjs.Shape();
  new_wall = new createjs.Shape();

  stage.addEventListener('stagemousedown', handleMouseDown);
  stage.addEventListener('stagemouseup', handleMouseUp);
  stage.addEventListener('stagemousemove', handleMouseMove);

  stage.addChild(background);
  stage.addChild(new_wall);
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

  if (adding_wall) {
    // if (points.length > 0) {
    //   new_wall.autoClear = true;
    //   let g = new_wall.graphics;
    //   g.setStrokeStyle(4, 'round', 'round').
    //       beginStroke('black').
    //       moveTo(points[0].x, points[0].y);
    //   for (let i = 0; i < points.length; i++) {
    //     let p = points[i];
    //     g.lineTo(p.x, p.y);
    //   }
    //   g.lineTo(current_p.x, current_p.y);
    // }
  }
  stage.update();
}

function handleMouseClick(event, pt) {
  if (!event.primary) {
    return;
  }
  let PT_SIZE = 4;
  let g = new_wall.graphics;

  if (adding_wall) {
    points.push(pt);

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

function showFloor(event) {
  console.log(event);
}

let floor_divs = document.getElementsByClassName('floor-n');
for (let i = 0; i < floor_divs.length; i++) {
  let floor = floor_divs[i];
  floor.onclick = showFloor;
}

// DATABASE FUNCTIONS

// Make a http POST request to the backend server.
// The id token will be authenticated,
// and the gym for that user will be returned
function fetchGymData() {
  let id_token = google_user.getAuthResponse().id_token;
  let xhr = new XMLHttpRequest();
  xhr.open('POST', 'http://localhost:3000/gyms');
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  xhr.send('idtoken=' + id_token);
  xhr.onload = function() {
    try {
      gyms = msgs.Gyms.deserializeBinary(xhr.responseText);
      gyms.getGymsList().forEach(function(gym) {
        let gym_drawer_element = document.createElement('a');
        gym_drawer_element.classList.add('mdl-navigation__link');
        gym_drawer_element.href = "";
        gym_drawer_element.innerHTML = gym.getName();
        drawer.appendChild(gym_drawer_element);
      })
    } catch (err) {
      console.log("Failed to deserialize " + err);
    }
  };
}

// SIGN IN WITH GOOGLE
/**
 * The Sign-In client object.
 */
let auth2;

/**
 * Initializes the Sign-In client.
 */
let initClient = function() {
  gapi.load('auth2', function() {
    /**
     * Retrieve the singleton for the GoogleAuth library and set up the
     * client.
     */
    auth2 = gapi.auth2.init({
      client_id: '41352784373-92ucj15fdse277kre1458uorhd0vlacl.apps.googleusercontent.com',
    });

    // Listen for changes to current user.
    auth2.currentUser.listen(userChanged);
  });
};

/**
 * Handle successful sign-ins.
 */
let onSuccess = function(user) {
  console.log('success ' + user.getBasicProfile().getName());
};

/**
 * Handle sign-in failures.
 */
let onFailure = function(error) {
  console.log(error);
};

function userChanged(user) {
  // refresh info related to the user
  google_user = user;
  console.log(auth2.isSignedIn.get());
  if (auth2.isSignedIn.get()) {
    fetchGymData();
    sign_in_out_button.innerHTML = 'Sign Out';
  }
  else {
    sign_in_out_button.innerHTML = 'Sign In';
  }
}

function signInOut() {
  if (auth2.isSignedIn.get()) {
    auth2.signOut();
  }
  else {
    auth2.signIn().then(onSuccess, onFailure);
  }
}
