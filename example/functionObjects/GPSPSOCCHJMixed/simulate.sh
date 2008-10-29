#!/bin/bash
# This file does nothing except generating 
# a dummy simulation output and log file. 
# This file is needed since
# GenOpt needs to call an executable program
# whenever a function evaluation is requested.
echo "Dummy file, needed since GenOpt needs a simulation output file" > f.txt
echo "Dummy file, needed since GenOpt needs a simulation log file" > sim.log
exit 0