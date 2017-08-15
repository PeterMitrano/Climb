import argparse
import sys

def main(argv=None):
    parser = argparse.ArgumentParser()
    show_parser = parser.add_subparsers().add_parser('show')
    show_parser.add_argument('-u', '--user')

    print(parser.parse_args())

    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv))
