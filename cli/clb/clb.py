#!/usr/bin/env python

import argparse
import sys

from commands import show


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-t', '--table', default='gyms', help='table name')
    parser.add_argument('-d', '--database', default='http://localhost:8000', help='url for database')
    show_parser = parser.add_subparsers().add_parser('show')
    show_parser.add_argument('-u', '--user', help='get only the gyms of this user')
    show_parser.set_defaults(func=show)

    args = parser.parse_args()
    args.func(args)

    return 0


if __name__ == '__main__':
    sys.exit(main())
