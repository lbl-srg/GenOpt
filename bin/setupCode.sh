#!/bin/bash
FILES=`find src -name *.java`
for ff in $FILES; do
    echo $ff
    sed -e 's/GenOpt(R) 3.0.0 beta 1/GenOpt(R) 3.0.0 beta 2/g' $ff > $ff.AAAA
mv $ff.AAAA $ff
    sed -e 's/(February 20, 2009)/(February 23, 2009)/g' $ff > $ff.AAAA
mv $ff.AAAA $ff
done

FIL=src/genopt/GenOpt.java
sed -e 's/beta1/beta2/g' $FIL > $FIL.AAAA
if [ "$?" != "0" ]; then
    echo "Error in replacing string in $FIL"
    echo "Exit with error"
    exit 1
fi
mv $FIL.AAAA $FIL

FIL=install/info.html
sed -e 's/3.0.0 beta 1/3.0.0 beta 2/g' $FIL > $FIL.AAAA
if [ "$?" != "0" ]; then
    echo "Error in replacing string in $FIL"
    echo "Exit with error"
    exit 1
fi
mv $FIL.AAAA $FIL


FIL=install/install.xml
sed -e 's/3.0.0 beta 1/3.0.0 beta 2/g' $FIL > $FIL.AAAA
if [ "$?" != "0" ]; then
    echo "Error in replacing string in $FIL"
    echo "Exit with error"
    exit 1
fi
mv $FIL.AAAA $FIL
