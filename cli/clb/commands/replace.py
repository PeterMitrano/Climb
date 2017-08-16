import base64

import boto3
from google.protobuf.json_format import Parse

from proto import Gym
from utils import print_json


def replace(args):
    dynamodb = boto3.resource('dynamodb', endpoint_url=args.endpoint)

    table = dynamodb.Table(args.table)

    gym = Gym()

    if args.file:
        file_bytes = open(args.file, 'rb').read()
        Parse(file_bytes, gym)
    elif args.data:
        data = args.data.replace("'", "\"")
        Parse(data, gym)

    gym_bytes = gym.SerializeToString()
    gym_encoded = base64.standard_b64encode(gym_bytes)

    response = table.scan()

    for item in response['Items']:
        itr_gym_encoded = item['gym']
        itr_user = item['user_id_key']
        itr_bytes = base64.standard_b64decode(itr_gym_encoded)
        itr_gym = Gym()
        itr_gym.ParseFromString(itr_bytes)

        if itr_gym.uuid == args.uuid:
            print('Successfully replaced item')
            print_json(itr_gym, args.depth, itr_user)
            print("With item")
            print_json(gym, args.depth, itr_user)

            table.delete_item(Key={'gym': itr_gym_encoded})
            table.put_item(Item={'gym': gym_encoded, 'user_id_key': itr_user})

    return 0
