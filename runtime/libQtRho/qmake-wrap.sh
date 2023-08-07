#!/bin/sh
#
qmake $* |sed "s#\(\w\):#/cygdrive/\1#"|sed "s#\\\\#/#g"


