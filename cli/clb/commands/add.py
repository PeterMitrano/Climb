import base64
import json

import boto3
from boto3.dynamodb.conditions import Key
from google.protobuf.json_format import MessageToJson
from google.protobuf.json_format import Parse

from proto import Gym
from utils import print_json


def add(args):
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

    response = table.scan(
        FilterExpression=Key('gym').eq(gym_encoded)
    )

    if len(response['Items']) > 0:
        print("Item already exists:")
        response = table.get_item(Key={'gym': gym_encoded})
        print_json(response['Item'], args.depth, args.user)
    else:
        table.put_item(
            Item={
                'gym': gym_encoded,
                'user_id_key': args.user,
            }
        )

        print('Successfully added item')
        print_json(gym, args.depth, args.user)

    return 0
