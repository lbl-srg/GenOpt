#!/bin/bash

TEXFIL=`find ./genopt \( -name '*.java' \)`
for ff in $TEXFIL; do
#    echo "$ff"
    cou=`grep -c "Redistribution not " $ff`
    if [ $cou != 0 ]; then
	echo "$ff"
#	grep 'extraro' $ff
    fi
done
