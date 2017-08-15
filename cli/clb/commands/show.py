import json
import boto3
from boto3.dynamodb.conditions import Attr


def show(args):
    dynamodb = boto3.resource('dynamodb')

    table = dynamodb.Table(args.table)
    if args.user:
        response = table.scan(
            FilterExpression=Attr('user_id_key').eq(args.user)

        )
    else:
        response = table.scan()

    items = response['Items']
    print(json.dumps(items, indent=2))

    return 0
