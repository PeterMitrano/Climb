#!/usr/bin/env python

import argparse
import sys

from commands import show, add


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-t', '--table', default='gyms', help='table name')
    parser.add_argument('-d', '--database', default='http://localhost:8000', help='url for database')

    subparsers = parser.add_subparsers()
    show_parser = subparsers.add_parser('show')
    show_parser.add_argument('-u', '--user', help='get only the gyms of this user')
    show_parser.set_defaults(func=show)

    add_parser = subparsers.add_parser('add')
    add_parser.add_argument('-u', '--user', required=True, help='set owner of this gym to this user')
    source_group = add_parser.add_mutually_exclusive_group(required=True)
    source_group.add_argument('-f', '--file', help='file to read JSON from')
    source_group.add_argument('-d', '--data', help='json string to read from')
    add_parser.set_defaults(func=add)

    args = parser.parse_args()
    args.func(args)

    return 0


if __name__ == '__main__':
    sys.exit(main())
