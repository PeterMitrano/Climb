import base64

import boto3
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

    response = table.scan()

    already_exists = False
    itr_gym = "{ no gym }"
    for item in response['Items']:
        itr_gym_encoded = item['gym']
        itr_bytes = base64.standard_b64decode(itr_gym_encoded)
        itr_gym = Gym()
        itr_gym.ParseFromString(itr_bytes)

        if itr_gym_encoded == gym_encoded:
            already_exists = True
            break
        elif itr_gym.uuid == gym.uuid:
            already_exists = True
            break

    if already_exists:
        print("Item already exists:")
        print_json(itr_gym, args.depth, args.user)
    else:
        table.put_item(Item={'gym': gym_encoded, 'user_id_key': args.user})
        print('Successfully added item')
        print_json(gym, args.depth, args.user)

    return 0
