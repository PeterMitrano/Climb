import base64

import boto3
from boto3.dynamodb.conditions import Attr

from proto import Gym
from utils import print_json


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
        user = item['user_id_key']
        item_bytes = base64.standard_b64decode(gym)
        gym = Gym()
        gym.ParseFromString(item_bytes)
        print_json(gym, args.depth, user)

    if len(items) == 0:
        print("Nothing to show.")

    return 0
