const AWS = require('aws-sdk');

if (process.env.DEBUG) {
  console.log("USING DEBUG");
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

let dynamodb = new AWS.DynamoDB();

let params = {
  TableName: 'Gyms',
  KeySchema: [
    {AttributeName: 'gym', KeyType: 'HASH'},  //Partition key
  ],
  AttributeDefinitions: [
    {AttributeName: 'gym', AttributeType: 'S'},
  ],
  ProvisionedThroughput: {
    ReadCapacityUnits: 1,
    WriteCapacityUnits: 1,
  },
};

dynamodb.createTable(params).promise().then(function(data) {
  console.log('Created table');
}).catch(console.log.bind(console));
