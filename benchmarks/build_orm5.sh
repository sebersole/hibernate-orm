#!/usr/bin/env bash

 ../gradlew -p orm5 --no-daemon clean runShadow --args='-wi 10 -i 20 -f 1 -o orm5_report.jmh'