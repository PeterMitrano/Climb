import base64

import boto3

from proto import Gym
from utils import print_json


def remove(args):
    dynamodb = boto3.resource('dynamodb', endpoint_url=args.endpoint)

    table = dynamodb.Table(args.table)

    response = table.scan()

    remove_any = False
    for item in response['Items']:
        gym_encoded = item['gym']
        user = item['user_id_key']
        item_bytes = base64.standard_b64decode(gym_encoded)
        gym = Gym()
        gym.ParseFromString(item_bytes)

        should_remove = False
        if not args.uuid and not args.user and args.all:
            should_remove = True
            print("removed by all:")
        if gym.uuid == args.uuid:
            should_remove = True
            print("removed by uuid:")
        if user == args.user and (gym.name == args.name or args.all):
            should_remove = True
            print("removed by name")

        if should_remove:
            remove_any = True
            table.delete_item(Key={'gym': gym_encoded})
            print_json(gym, args.depth, args.user)

    if not remove_any:
        print("No items matching criteria")

    return 0
