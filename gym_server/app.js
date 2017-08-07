const AWS = require('aws-sdk');
const proto = require('google-protobuf');
const express = require('express');
const msgs = require('./public/js/proto/Gym_pb.js');
const google_auth = require('google-auth-library');
const body_parser = require('body-parser');
const app = express();

const port = 8081;
const client_id = '41352784373-92ucj15fdse277kre1458uorhd0vlacl.apps.googleusercontent.com';
const table_name = 'gyms';
const user_id_key = 'user_id_key';

app.use(body_parser.urlencoded({extended: false}));

if (process.env.DEBUG) {
  AWS.config.update({
    region: 'region',
    endpoint: 'http://localhost:8000',
    accessKeyId: 'accessKeyId',
    secretAccessKey: 'secretAccessKey',
  });
}
else {
  AWS.config.update({
    region: 'us-east-1',
  });
}

let auth = new google_auth;
let google_client = new auth.OAuth2(client_id, '', '');
let dynamodb = new AWS.DynamoDB();
let dynamo_client = new AWS.DynamoDB.DocumentClient();

app.use('/', express.static(__dirname + '/public'));

app.use('/gyms', function(request, response) {
  dynamodb.listTables().promise().then(function(data) {
    if (data.TableNames.indexOf(table_name) >= 0) {
      if (request.method === 'POST') {
        token = request.body.idtoken;
        if (token === undefined) {
          response.status(401).send('missing token');
        }
        else {
          google_client.verifyIdToken(token, client_id, function(e, login) {
            if (e) {
              console.log('invalid token ' + e);
              response.status(401).send('invalid token ' + e);
            }
            else {
              let payload = login.getPayload();
              let user_id = payload['sub'];

              // look up the gym(s) corresponding to the user_id
              let params = {
                TableName: table_name,
                KeyConditionExpression: '#id = :iiii',
                ExpressionAttributeNames: {
                  '#id': user_id_key,
                },
                ExpressionAttributeValues: {
                  ':iiii': user_id,
                },
              };
              dynamo_client.query(params).promise().then(function(data) {
                console.log(data.Items);
                response.status(204).send('Unimplemented');
              });
            }
          });
        }
      }
      else if (request.method === 'GET') {
        let params = {TableName: table_name};

        function onScan(err, data) {
          if (err) {
            console.error('Unable to scan the table. Error JSON:',
                JSON.stringify(err, null, 2));
          } else {
            let gym_list = [];
            data.Items.forEach(function(datum) {
              let gym_string = datum['gym'];
              let gym = new msgs.Gym();
              let reader = new proto.BinaryReader(gym_string);
              msgs.Gym.deserializeBinaryFromReader(gym, reader);
              gym_list.push(gym);
            });

            let writer = new proto.BinaryWriter();
            let gyms = new msgs.Gyms();
            gyms.setGymsList(gym_list);
            gyms.serializeBinaryToWriter(writer);
            let gym_result = writer.getResultBase64String();
            response.status(200).send(gym_result);

            // continue scanning if we have more movies, because
            // scan can retrieve a maximum of 1MB of data
            if (typeof data.LastEvaluatedKey !== 'undefined') {
              console.log('Scanning for more...');
              params.ExclusiveStartKey = data.LastEvaluatedKey;
              dynamo_client.scan(params, onScan);
            }
          }
        }

        dynamo_client.scan(params, onScan);
      }
      else {
        response.status(405).send('Method not allowed');
      }
    } else {
      console.log(table_name + ' table not found. Available tables are');
      console.log(data.TableNames);
      response.status(500).send('Requested table not found in database.');
    }
  }).catch(function(error) {
    console.log(error);
    response.status(500).send('Internal Server Error');
  });
});

app.listen(port, (err) => {
  if (err) {
    return console.log('Error', err);
  }
});
