#!/bin/bash
FILES=`find src -name *.java`
for ff in $FILES; do
    echo $ff
##    replaceString "GenOpt(R) 2.0.0 beta1 (Jun. 23, 2003)" "GenOpt(R) 2.0.0 (Jan. 5, 2004)" $ff

sed -e 's/GenOpt(R) 3.0.0 alpha 1 (November 12, 2008)/GenOpt(R) 3.0.0 alpha 2 (November 18, 2008)/g' $ff > $ff.AAAA
mv $ff.AAAA $ff

##    sed -e 's/(Jun. 16, 2003)/(Jun. 23, 2003)/g' $ff > $ff.AAAA
##    mv $ff.AAAA $ff
done
