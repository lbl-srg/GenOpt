#!/bin/bash
set -e
FIL=manual

latex $FIL
bibtex $FIL
latex $FIL
latex $FIL

dvips -o $FIL.ps $FIL.dvi
ps2pdf14 $FIL.ps $FIL.pdf
rm -f *.ps
rm -f *.dvi
rm *.aux
rm $FIL.log
rm $FIL.blg
rm $FIL.bbl
