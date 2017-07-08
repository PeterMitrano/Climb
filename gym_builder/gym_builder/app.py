from flask import Flask, render_template
import Gym_pb2

app = Flask(__name__)

@app.route('/')
def root():
    return render_template('index.html')

@app.route('/gyms', methods=['GET'])
def get_gyms():
    gyms = Gym_pb2.Gyms()
    gym = gyms.gyms.add()
    gym.name = "Ascend PGH"
    wall = gym.walls.add()
    wall.name = "slab"
    return gyms.SerializeToString()

@app.route('/gyms', methods=['PUT'])
def put_gyms():
    return "inserting/updating a gym"

if __name__ == '__main__':
    app.debug = True
    app.run()

