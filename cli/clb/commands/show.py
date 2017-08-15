import base64

import boto3
from boto3.dynamodb.conditions import Attr
from google.protobuf.json_format import MessageToJson

from proto import Gym


def show(args):
    dynamodb = boto3.resource('dynamodb', endpoint_url=args.endpoint)

    table = dynamodb.Table(args.table)
    if args.user:
        response = table.scan(
                FilterExpression=Attr('user_id_key').eq(args.user)
        )
    else:
        response = table.scan()

    items = response['Items']
    for item in items:
        gym = item['gym']
        item_bytes = base64.standard_b64decode(gym)
        gym = Gym()
        gym.ParseFromString(item_bytes)
        json_string = MessageToJson(gym)
        print(json_string)

    if len(items) == 0:
        print("Nothing to show.")

    return 0
