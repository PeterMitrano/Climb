import base64
import json

import boto3
from google.protobuf.json_format import MessageToJson
from google.protobuf.json_format import Parse

from proto import Gym


def add(args):
    dynamodb = boto3.resource('dynamodb')

    table = dynamodb.Table(args.table)

    gym = Gym()

    if args.file:
        file_text = json.load(args.file)
        Parse(file_text, gym)
    elif args.data:
        data = args.data.replace("'", "\"")
        Parse(data, gym)

    gym_bytes = gym.SerializeToString()
    gym_encoded = base64.standard_b64encode(gym_bytes)

    table.put_item(
        Item={
            'gym': gym_encoded,
            'user_id_key': args.user,
        }
    )

    print('Successfully added item')
    print(MessageToJson(gym))

    return 0
