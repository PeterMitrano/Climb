from flask import Flask, render_template
import Gym_pb2

app = Flask(__name__)

@app.route('/')
def root():
    return render_template('index.html')

@app.route('/gyms', methods=['GET'])
def get_gyms():
    # eventually we will contact DB for this
    gyms = Gym_pb2.Gyms()
    ascend = gyms.gyms.add()
    ascend.name = "Ascend PGH"
    north = gyms.gyms.add()
    north.name = "Climb North"
    return gyms.SerializeToString()

@app.route('/gyms', methods=['PUT'])
def put_gyms():
    # could automatically insert into DB. might be a bad idea...
    return "inserting/updating a gym"

if __name__ == '__main__':
    app.debug = True
    app.run()

