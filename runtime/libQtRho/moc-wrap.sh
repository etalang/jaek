#!/bin/sh
#
# Assume we get the file list name in first parameter
FILE=`echo $1|sed "s#@##"`

# cypath will convert relative path to our things fine,
# then we need to convert \cygdrive\X\ back to letters
cat $FILE|cygpath -w -f - |sed "s#\\\\cygdrive\\\\\(\w\)#\1:#" > $FILE.2
moc @$FILE.2




