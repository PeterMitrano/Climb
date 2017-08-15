#!/usr/bin/env python

from distutils.core import setup

setup(name='clb',
      version='0.1',
      description='CLI for climb database',
      author='Peter Mitrano',
      author_email='mitranopeter@gmail.com',
      url='https://github.com/petermitrano/climb/',
      packages=['clb'], requires=['boto3', 'google']
      )
