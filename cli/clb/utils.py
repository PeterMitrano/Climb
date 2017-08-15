import json

from google.protobuf.json_format import MessageToJson

from proto import Gym


def print_json(m, depth, user=None, indent=2):
    if isinstance(m, Gym):
        json_string = MessageToJson(m)
        j = json.loads(json_string)
    elif isinstance(m, dict):
        j = m
    elif isinstance(m, str):
        j = json.loads(m)
    else:
        raise TypeError("Unsupported type " + str(type(m)))

    # now we have m as a dict, so we can limit depth if necessary
    j = limit_depth({'user': user, 'gym': j}, max_depth=depth)
    print(json.dumps(j, indent=indent, sort_keys=True))


def limit_depth(j, depth=0, max_depth=-1):
    if isinstance(j, list):
        new_j = []
        for item in j:
            new_j.append(limit_depth(item, depth, max_depth))
    elif isinstance(j, dict):
        new_j = {}
        for k, v in j.items():
            if isinstance(v, dict):
                if depth == max_depth:
                    new_j[k] = "{...}"
                else:
                    new_j[k] = limit_depth(v, depth + 1, max_depth)
            elif isinstance(v, list):
                if depth == max_depth:
                    new_j[k] = "[...]"
                else:
                    new_j[k] = limit_depth(v, depth + 1, max_depth)
            else:
                new_j[k] = v
    else:
        raise TypeError("Unsupported type " + str(type(j)))

    return new_j
