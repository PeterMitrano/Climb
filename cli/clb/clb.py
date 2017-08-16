#!/usr/bin/env python

import argparse
import sys

from commands import show, add, remove


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-t', '--table', default='gyms', help='table name')
    parser.add_argument('-d', '--depth', type=int, default=-1, help='max depth to print for json')
    parser.add_argument('-e', '--endpoint', default='http://localhost:8000',
                        help='endpoint url for database')

    subparsers = parser.add_subparsers()
    show_parser = subparsers.add_parser('show')
    show_parser.add_argument('-u', '--user', help='get only the gyms of this user')
    show_parser.add_argument('-n', '--name',
                             help='get only the gyms with this name (supports regex)')
    show_parser.set_defaults(func=show)

    add_parser = subparsers.add_parser('add')
    add_parser.add_argument('-u', '--user', required=True,
                            help='set owner of this gym to this user')
    source_group = add_parser.add_mutually_exclusive_group(required=True)
    source_group.add_argument('-f', '--file', help='file to read JSON from')
    source_group.add_argument('-d', '--data', help='json string to read from')
    add_parser.set_defaults(func=add)

    remove_parser = subparsers.add_parser('remove')
    remove_parser.add_argument('-i', '--uuid', help='gym uuid')
    remove_parser.add_argument('-u', '--user',
                               help='user name. You should also specify gym name, or this will '
                                    'not remove anything')
    remove_parser.add_argument('-N', '--dry-run', action="store_true",
                               help='Do not actually remove stuff')
    remove_parser.add_argument('-n', '--name', help='gym name. case insensitive (supports regex)')
    remove_parser.add_argument('-a', '--all', action="store_true",
                               help='if only all is supplied, it removes every item. If user is '
                                    'supplied, it removes all gyms owned by that user')
    remove_parser.set_defaults(func=remove)

    args = parser.parse_args()
    args.func(args)

    return 0


if __name__ == '__main__':
    sys.exit(main())
