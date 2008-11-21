#!/bin/bash
FILES=`find src -name *.java`
for ff in $FILES; do
    echo $ff
    sed -e 's/GenOpt(R) 3.0.0 alpha 2 (November 18, 2008)/GenOpt(R) 3.0.0 alpha 3 (November 20, 2008)/g' $ff > $ff.AAAA
mv $ff.AAAA $ff
done

FIL=install/info.html
sed -e 's/3.0.0 alpha 2/3.0.0 alpha 3/g' $FIL > $FIL.AAAA
if [ "$?" != "0" ]; then
    echo "Error in replacing string in $FIL"
    echo "Exit with error"
    exit 1
fi
mv $FIL.AAAA $FIL


FIL=install/install.xml
sed -e 's/3.0.0 alpha 2/3.0.0 alpha 3/g' $FIL > $FIL.AAAA
if [ "$?" != "0" ]; then
    echo "Error in replacing string in $FIL"
    echo "Exit with error"
    exit 1
fi
mv $FIL.AAAA $FIL
