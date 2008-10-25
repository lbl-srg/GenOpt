rem This file does nothing except generating 
rem a dummy simulation output and log file. 
rem This file is needed since
rem GenOpt needs to call an executable program
rem whenever a function evaluation is requested.
echo "Dummy file, needed since GenOpt needs a simulation output file" > f.txt
echo "Dummy file, needed since GenOpt needs a simulation log file" > sim.log
exit 0