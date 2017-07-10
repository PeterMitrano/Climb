from flask import Flask, render_template
from google.protobuf.message import DecodeError
import base64
import Gym_pb2

app = Flask(__name__)

@app.route('/')
def root():
    return render_template('index.html')

@app.route('/gyms', methods=['GET'])
def get_gyms():
    # load up the serialized protocol buffer from disc and serve http request
    f = open('./gyms.bas64', 'rb')
    b64 = f.read()
    f.close()
    data = base64.b64decode(b64)
    try:
        gyms = Gym_pb2.Gyms.FromString(data)
        return gyms.SerializeToString(), 200
    except DecodeError:
        return "File could not be parsed into valid Protbuf object", 500


if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0')

