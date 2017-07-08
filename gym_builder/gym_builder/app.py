from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def root():
    return render_template('index.html')

@app.route('/gyms', methods=['GET'])
def get_gyms():
    return "list of gyms"

@app.route('/gyms', methods=['PUT'])
def put_gyms():
    return "inserting/updating a gym"

if __name__ == '__main__':
    app.debug = True
    app.run()

