Andreas.Kissavos@equa.se             20091024
Mika.Vuolle@equa.fi




Instructions for running Genopt3.0 with IDA ICE:

You will need the ida.exe file (contact Equa for this if you do not have it), and if you want to have .prn-files saved, a recent idakrn.dll (not included in standard ida-builds right now).

!NB: If you do not have a very recent version of idakrn.dll, you will have to remove all the output .prn-files from the template file, otherwise 
IDA ICE will crash. 

First create a template file by running IDA ICE, which creates an ida_lisp.ida-file. Use this as a template (see Genopt manual).

Place all configuration files in the directory from where you start your calculations (called WORKDIR from now on in this document).

In command.txt: add all parameters you want to vary and the optimization function you want to use.

In IDA4.cfg: add/subtract error messages you are/are not interested in.

In optIDA4.ini: Edit all Paths in the Input file section to point to the WORKDIR
                 Edit ObjectiveFunctionLocation to be the line where the thing you want to optimize is to be found (see manual).

In WORKDIR: execute java -jar \path\to\genopt.jar
           

press the start-button (green arrow). Navigate to optIDA4.ini Open. That's it!