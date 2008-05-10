#!/bin/bash
FILES=`find genopt -name *.java`
for ff in $FILES; do
    echo $ff
##    replaceString "GenOpt(R) 2.0.0 beta1 (Jun. 23, 2003)" "GenOpt(R) 2.0.0 (Jan. 5, 2004)" $ff

##sed -e 's/* Copyright (c) 1998-2003/* GenOpt Copyright (c) 1998-2008/g' $ff > $ff.AAAA

##mv $ff.AAAA $ff

##    sed -e 's/(Jun. 16, 2003)/(Jun. 23, 2003)/g' $ff > $ff.AAAA
##    mv $ff.AAAA $ff
done
