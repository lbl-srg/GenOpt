#!/bin/bash
############################################
# Script to compare all examples in this
# directory on Linux.
#
# MWetter@lbl.gov   June 13, 2003
############################################

FILES=`find ./example -name optLinux.ini`
for file in $FILES; do
    RDIR=`echo $file | sed -e 's/optLinux.ini//'`
    if [ -d $RDIR ]; then
	echo "========================================"
	echo "====== $RDIR"
	diff -b dist/pub/go_prg/$RDIR/OutputListingAll.txt $RDIR/OutputListingAll.txt
#	cat $RDIR/GenOpt.log
    fi
done

