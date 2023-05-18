#!/bin/sh
#
# This is a very simple script that uses gcc to link in a given .s
# file to the eta runtime library, and uses etafilt to help
# decode error messages
#
# Use this like ./linkqt.sh -o binary foo.s
#
DIR=$(dirname $0)
ABI_FLAG=$($DIR/platform-flags.sh)

# echo "ABI_FLAG = $ABI_FLAG"

gcc $ABI_FLAG "$@" -Wl,--export-dynamic -L$DIR \
		-lQtRho -leta -lQtGui -lQtCore -lpthread -ldl -lstdc++ 2>&1 \
	| $DIR/etafilt
