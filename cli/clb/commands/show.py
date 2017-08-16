import base64
import re

import boto3

from proto import Gym
from utils import print_json


def show(args):
    dynamodb = boto3.resource('dynamodb', endpoint_url=args.endpoint)

    table = dynamodb.Table(args.table)

    response = table.scan()

    show_any = False
    for item in response['Items']:
        gym = item['gym']
        user = item['user_id_key']
        item_bytes = base64.standard_b64decode(gym)
        gym = Gym()
        gym.ParseFromString(item_bytes)

        should_show = False
        if args.user:
            if args.user == user:
                should_show = True
        elif args.name:
            if re.search(args.name, gym.name, re.IGNORECASE):
                should_show = True
        else:
            should_show = True

        if should_show:
            show_any = True
            print_json(gym, args.depth, user)

    if not show_any:
        print("Nothing to show.")

    return 0
