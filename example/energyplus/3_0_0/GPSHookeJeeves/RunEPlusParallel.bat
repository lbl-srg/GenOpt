@echo off
 if Not "%2" == "" goto :NoErr
 echo This is a modified version of the file RunEPlus.bat that is distributed
 echo with EnergyPlus. The modification allow to run multiple EnergyPlus
 echo versions in parallel.
 echo
 echo Modification done by Michael Wetter, 2008-11-13:
 echo Replace %2 with %3, %1 with %2 and introduced %1 as the program_path
 echo argument.
 echo
 echo usage: %0 ProgramPath (req) InputFileName (req) WeatherFileName (opt)
 echo Current Parameters:
 echo Program         : %program_path%%program_name%
 echo Input Path      : %input_path%
 echo Output Path     : %output_path%
 echo PostProcess Path: %post_proc%
 echo Weather Path    : %weather_path%
 echo Pausing         : %pausing%
 echo MaxCol          : %maxcol%
 goto :done

:NoErr
:Instructions:
:  Complete the following path and program names.
:  path names must have a following \ or errors will happen
:  does not have the capability to run input macro files (yet)
:   %program_path% contains the path to the executable as well as IDD and is
:                  the root directory
:   %program_name% contains the name of the executable (normally EnergyPlus.exe)
:   %input_path%   contains the path to the input file (passed in as first argument)
:                  if the extension is imf -- will run epmacro to process before
:                  executing energyplus
:   %output_path%  contains the path where the result files should be stored
:   %post_proc%    contains the path to the post processing program (ReadVarsESO)
:   %weather_path% contains the path to the weather files (used with optional argument 2)
:   %pausing%      contains Y if pause should occur between major portions of
:                  batch file (mostly commented out)
:   %maxcol%       contains "250" if limited to 250 columns otherwise contains
:                  "nolimit" if unlimited (used when calling readVarsESO)
 echo ===== %0 (Run EnergyPlus) %1 %2 %3 ===== Start =====
 set program_path=%~1
 set program_name=EnergyPlus.exe
 set input_path=
 set output_path=
 set post_proc=%1PostProcess\
 set weather_path=%1WeatherData\
 set pausing=N
 set maxcol=250
 
:  This batch file will perform the following steps:
:
:   1.  Clean up directory by deleting old working files from prior run
:   2.  Clean up target directory
:   3.  Copy %2.idf (input) into In.idf (or %2.imf to in.imf)
:   4.  Copy %3 (weather) into In.epw
:   5.  Execute EnergyPlus
:   6.  If available Copy %2.rvi (post processor commands) into Eplusout.inp
:   7.  Execute ReadVarsESO.exe (the Post Processing Program)
:   8.  If available Copy %2.mvi (post processor commands) into test.mvi
:       or create appropriate input to get meter output from eplusout.mtr
:   9.  Execute ReadVarsESO.exe (the Post Processing Program) for meter output
:  10.  Copy Eplusout.* to %2.*
:  11.  Clean up working directory.
:
:  1. Clean up working directory
IF EXIST eplusout.inp   DEL eplusout.inp
IF EXIST eplusout.end   DEL eplusout.end
IF EXIST eplusout.eso   DEL eplusout.eso
IF EXIST eplusout.rdd   DEL eplusout.rdd
IF EXIST eplusout.mdd   DEL eplusout.mdd
IF EXIST eplusout.dbg   DEL eplusout.dbg
IF EXIST eplusout.eio   DEL eplusout.eio
IF EXIST eplusout.err   DEL eplusout.err
IF EXIST eplusout.dxf   DEL eplusout.dxf
IF EXIST eplusout.csv   DEL eplusout.csv
IF EXIST eplusout.tab   DEL eplusout.tab
IF EXIST eplusout.txt   DEL eplusout.txt
IF EXIST eplusmtr.csv   DEL eplusmtr.csv
IF EXIST eplusmtr.tab   DEL eplusmtr.tab
IF EXIST eplusmtr.txt   DEL eplusmtr.txt
IF EXIST eplusout.sln   DEL eplusout.sln
IF EXIST epluszsz.csv   DEL epluszsz.csv
IF EXIST epluszsz.tab   DEL epluszsz.tab
IF EXIST epluszsz.txt   DEL epluszsz.txt
IF EXIST eplusssz.csv   DEL eplusssz.csv
IF EXIST eplusssz.tab   DEL eplusssz.tab
IF EXIST eplusssz.txt   DEL eplusssz.txt
IF EXIST eplusout.mtr   DEL eplusout.mtr
IF EXIST eplusout.mtd   DEL eplusout.mtd
IF EXIST eplusout.bnd   DEL eplusout.bnd
IF EXIST eplusout.dbg   DEL eplusout.dbg
IF EXIST eplusout.sci   DEL eplusout.sci
IF EXIST eplusmap.csv   DEL eplusmap.csv
IF EXIST eplusmap.txt   DEL eplusmap.txt
IF EXIST eplusmap.tab   DEL eplusmap.tab
IF EXIST eplustbl.csv   DEL eplustbl.csv
IF EXIST eplustbl.txt   DEL eplustbl.txt
IF EXIST eplustbl.tab   DEL eplustbl.tab
IF EXIST eplustbl.htm   DEL eplustbl.htm
IF EXIST eplusout.log   DEL eplusout.log
IF EXIST eplusout.svg   DEL eplusout.svg
IF EXIST eplusout.shd   DEL eplusout.shd
IF EXIST eplusout.wrl   DEL eplusout.wrl
IF EXIST eplusout.delightin   DEL eplusout.delightin
IF EXIST eplusout.delightout  DEL eplusout.delightout
IF EXIST eplusout.delighteldmp  DEL eplusout.delighteldmp
IF EXIST eplusout.delightdfdmp  DEL eplusout.delightdfdmp
IF EXIST eplusout.sparklog  DEL eplusout.sparklog
IF EXIST eplusscreen.csv  DEL eplusscreen.csv
IF EXIST in.imf         DEL in.imf
IF EXIST in.idf         DEL in.idf
IF EXIST out.idf        DEL out.idf
IF EXIST audit.out      DEL audit.out
IF EXIST eplusout.inp   DEL eplusout.inp
IF EXIST in.epw         DEL in.epw
IF EXIST in.stat        DEL in.stat
IF EXIST eplusout.audit DEL eplusout.audit
IF EXIST test.mvi       DEL test.mvi
IF EXIST audit.out DEL audit.out
IF EXIST expanded.idf   DEL expanded.idf
IF EXIST expandedidf.err   DEL expandedidf.err
IF EXIST readvars.audit   DEL readvars.audit
IF EXIST eplusout.sql  DEL eplusout.sql
IF EXIST sqlite.err  DEL sqlite.err
:if %pausing%==Y pause

:  2. Clean up target directory
IF NOT EXIST "%output_path%". MKDIR "%output_path%"

IF EXIST "%output_path%%2.epmidf" DEL "%output_path%%2.epmidf"
IF EXIST "%output_path%%2.epmdet" DEL "%output_path%%2.epmdet"
IF EXIST "%output_path%%2.eso" DEL "%output_path%%2.eso"
IF EXIST "%output_path%%2.rdd" DEL "%output_path%%2.rdd"
IF EXIST "%output_path%%2.mdd" DEL "%output_path%%2.mdd"
IF EXIST "%output_path%%2.eio" DEL "%output_path%%2.eio"
IF EXIST "%output_path%%2.err" DEL "%output_path%%2.err"
IF EXIST "%output_path%%2.dxf" DEL "%output_path%%2.dxf"
IF EXIST "%output_path%%2.csv" DEL "%output_path%%2.csv"
IF EXIST "%output_path%%2.tab" DEL "%output_path%%2.tab"
IF EXIST "%output_path%%2.txt" DEL "%output_path%%2.txt"
IF EXIST "%output_path%%2Meter.csv" DEL "%output_path%%2Meter.csv"
IF EXIST "%output_path%%2Meter.tab" DEL "%output_path%%2Meter.tab"
IF EXIST "%output_path%%2Meter.txt" DEL "%output_path%%2Meter.txt"
IF EXIST "%output_path%%2.det" DEL "%output_path%%2.det"
IF EXIST "%output_path%%2.sln" DEL "%output_path%%2.sln"
IF EXIST "%output_path%%2.Zsz" DEL "%output_path%%2.Zsz"
IF EXIST "%output_path%%2Zsz.csv" DEL "%output_path%%2Zsz.csv"
IF EXIST "%output_path%%2Zsz.tab" DEL "%output_path%%2Zsz.tab"
IF EXIST "%output_path%%2Zsz.txt" DEL "%output_path%%2Zsz.txt"
IF EXIST "%output_path%%2.ssz" DEL "%output_path%%2.ssz"
IF EXIST "%output_path%%2Ssz.csv" DEL "%output_path%%2Ssz.csv"
IF EXIST "%output_path%%2Ssz.tab" DEL "%output_path%%2Ssz.tab"
IF EXIST "%output_path%%2Ssz.txt" DEL "%output_path%%2Ssz.txt"
IF EXIST "%output_path%%2.mtr" DEL "%output_path%%2.mtr"
IF EXIST "%output_path%%2.mtd" DEL "%output_path%%2.mtd"
IF EXIST "%output_path%%2.bnd" DEL "%output_path%%2.bnd"
IF EXIST "%output_path%%2.dbg" DEL "%output_path%%2.dbg"
IF EXIST "%output_path%%2.sci" DEL "%output_path%%2.sci"
IF EXIST "%output_path%%2.svg" DEL "%output_path%%2.svg"
IF EXIST "%output_path%%2.shd" DEL "%output_path%%2.shd"
IF EXIST "%output_path%%2.wrl" DEL "%output_path%%2.wrl"
IF EXIST "%output_path%%2Screen.csv" DEL "%output_path%%2Screen.csv"
IF EXIST "%output_path%%2Map.csv" DEL "%output_path%%2Map.csv"
IF EXIST "%output_path%%2Map.tab" DEL "%output_path%%2Map.tab"
IF EXIST "%output_path%%2Map.txt" DEL "%output_path%%2Map.txt"
IF EXIST "%output_path%%2.audit" DEL "%output_path%%2.audit"
IF EXIST "%output_path%%2Table.csv" DEL "%output_path%%2Table.csv"
IF EXIST "%output_path%%2Table.tab" DEL "%output_path%%2Table.tab"
IF EXIST "%output_path%%2Table.txt" DEL "%output_path%%2Table.txt"
IF EXIST "%output_path%%2Table.htm" DEL "%output_path%%2Table.htm"
IF EXIST "%output_path%%2DElight.in" DEL "%output_path%%2DElight.in"
IF EXIST "%output_path%%2DElight.out" DEL "%output_path%%2DElight.out"
IF EXIST "%output_path%%2DElight.dfdmp" DEL "%output_path%%2DElight.dfdmp"
IF EXIST "%output_path%%2DElight.eldmp" DEL "%output_path%%2DElight.eldmp"
IF EXIST "%output_path%%2Spark.log" DEL "%output_path%%2Spark.log"
IF EXIST "%output_path%%2.expidf" DEL "%output_path%%2.expidf"
IF EXIST "%output_path%%2.rvaudit" DEL "%output_path%%2.rvaudit"
IF EXIST "%output_path%%2.sql" DEL "%output_path%%2.sql"

:  3. Copy input data file to working directory
echo Copying "%program_path%Energy+.idd" "%input_path%In.idd"
copy "%program_path%Energy+.idd" "%input_path%In.idd"
echo Copying "%program_path%Energy+.ini" "%input_path%Energy+.ini"
copy "%program_path%Energy+.ini" "%input_path%Energy+.ini"
if exist "%input_path%%2.imf" copy "%input_path%%2.imf" in.imf
if exist in.imf "%program_path%EPMacro"
if exist out.idf copy out.idf "%output_path%%2.epmidf"
if exist audit.out copy audit.out "%output_path%%2.epmdet"
if exist audit.out erase audit.out
if exist out.idf MOVE out.idf in.idf
if not exist in.idf copy "%input_path%%2.idf" In.idf
if exist in.idf "%program_path%ExpandObjects"
if exist expandedidf.err COPY expandedidf.err eplusout.end
if exist expanded.idf COPY expanded.idf "%output_path%%2.expidf"
if exist expanded.idf MOVE expanded.idf in.idf
if not exist in.idf copy "%input_path%%2.idf" In.idf

:  4. Test for weather file parameter and copy to working directory
 if "%3" == ""  goto exe
 if EXIST %weather_path%%3.epw copy %weather_path%%3.epw in.epw
 if EXIST %weather_path%%3.stat  copy %weather_path%%3.stat in.stat

:  5. Execute the program
:exe
 : Display basic parameters of the run
 echo Running "%program_path%%program_name%"
 cd 
 echo Program path: %program_path%
 echo Input File  : %input_path%%2.idf
 echo Output Files: %output_path%
 echo IDD file    : "%input_path%Energy+.idd"
 if NOT "%3" == "" echo Weather File: %weather_path%%3.epw
dir
 if %pausing%==Y pause
 
 ECHO Begin EnergyPlus processing . . . 
 IF NOT EXIST expandedidf.err  "%program_path%%program_name%"
 if %pausing%==Y pause
 

:  6&8. Copy Post Processing Program command file(s) to working directory
 IF EXIST %input_path%%2.rvi copy %input_path%%2.rvi eplusout.inp
 IF EXIST %input_path%%2.mvi copy %input_path%%2.mvi eplusmtr.inp

:  7&9. Run Post Processing Program(s)
if %maxcol%==250     SET rvset=
if %maxcol%==nolimit SET rvset=unlimited
: readvars creates audit in append mode.  start it off
echo %date% %time% ReadVars >readvars.audit

IF EXIST eplusout.inp %post_proc%ReadVarsESO.exe eplusout.inp %rvset%
IF NOT EXIST eplusout.inp %post_proc%ReadVarsESO.exe " " %rvset%
IF EXIST eplusmtr.inp %post_proc%ReadVarsESO.exe eplusmtr.inp %rvset%
IF NOT EXIST eplusmtr.inp echo eplusout.mtr >test.mvi
IF NOT EXIST eplusmtr.inp echo eplusmtr.csv >>test.mvi
IF NOT EXIST eplusmtr.inp %post_proc%ReadVarsESO.exe test.mvi %rvset%
IF EXIST eplusout.bnd %post_proc%HVAC-Diagram.exe

:  10. Move output files to output path
 IF EXIST eplusout.eso MOVE eplusout.eso "%output_path%%2.eso"
 IF EXIST eplusout.rdd MOVE eplusout.rdd "%output_path%%2.rdd"
 IF EXIST eplusout.mdd MOVE eplusout.mdd "%output_path%%2.mdd"
 IF EXIST eplusout.eio MOVE eplusout.eio "%output_path%%2.eio"
 IF EXIST eplusout.err MOVE eplusout.err "%output_path%%2.err"
 IF EXIST eplusout.dxf MOVE eplusout.dxf "%output_path%%2.dxf"
 IF EXIST eplusout.csv MOVE eplusout.csv "%output_path%%2.csv"
 IF EXIST eplusout.tab MOVE eplusout.tab "%output_path%%2.tab"
 IF EXIST eplusout.txt MOVE eplusout.txt "%output_path%%2.txt"
 IF EXIST eplusmtr.csv MOVE eplusmtr.csv "%output_path%%2Meter.csv"
 IF EXIST eplusmtr.tab MOVE eplusmtr.tab "%output_path%%2Meter.tab"
 IF EXIST eplusmtr.txt MOVE eplusmtr.txt "%output_path%%2Meter.txt"
 IF EXIST eplusout.sln MOVE eplusout.sln "%output_path%%2.sln"
 IF EXIST epluszsz.csv MOVE epluszsz.csv "%output_path%%2Zsz.csv"
 IF EXIST epluszsz.tab MOVE epluszsz.tab "%output_path%%2Zsz.tab"
 IF EXIST epluszsz.txt MOVE epluszsz.txt "%output_path%%2Zsz.txt"
 IF EXIST eplusssz.csv MOVE eplusssz.csv "%output_path%%2Ssz.csv"
 IF EXIST eplusssz.tab MOVE eplusssz.tab "%output_path%%2Ssz.tab"
 IF EXIST eplusssz.txt MOVE eplusssz.txt "%output_path%%2Ssz.txt"
 IF EXIST eplusout.mtr MOVE eplusout.mtr "%output_path%%2.mtr"
 IF EXIST eplusout.mtd MOVE eplusout.mtd "%output_path%%2.mtd"
 IF EXIST eplusout.bnd MOVE eplusout.bnd "%output_path%%2.bnd"
 IF EXIST eplusout.dbg MOVE eplusout.dbg "%output_path%%2.dbg"
 IF EXIST eplusout.sci MOVE eplusout.sci "%output_path%%2.sci"
 IF EXIST eplusmap.csv MOVE eplusmap.csv "%output_path%%2Map.csv"
 IF EXIST eplusmap.tab MOVE eplusmap.tab "%output_path%%2Map.tab"
 IF EXIST eplusmap.txt MOVE eplusmap.txt "%output_path%%2Map.txt"
 IF EXIST eplusout.audit MOVE eplusout.audit "%output_path%%2.audit"
 IF EXIST eplustbl.csv MOVE eplustbl.csv "%output_path%%2Table.csv"
 IF EXIST eplustbl.tab MOVE eplustbl.tab "%output_path%%2Table.tab"
 IF EXIST eplustbl.txt MOVE eplustbl.txt "%output_path%%2Table.txt"
 IF EXIST eplustbl.htm MOVE eplustbl.htm "%output_path%%2Table.htm"
 IF EXIST eplusout.delightin MOVE eplusout.delightin "%output_path%%2DElight.in"
 IF EXIST eplusout.delightout  MOVE eplusout.delightout "%output_path%%2DElight.out"
 IF EXIST eplusout.delighteldmp  MOVE eplusout.delighteldmp "%output_path%%2DElight.eldmp"
 IF EXIST eplusout.delightdfdmp  MOVE eplusout.delightdfdmp "%output_path%%2DElight.dfdmp"
 IF EXIST eplusout.svg MOVE eplusout.svg "%output_path%%2.svg"
 IF EXIST eplusout.shd MOVE eplusout.shd "%output_path%%2.shd"
 IF EXIST eplusout.wrl MOVE eplusout.wrl "%output_path%%2.wrl"
 IF EXIST eplusscreen.csv MOVE eplusscreen.csv "%output_path%%2Screen.csv"
 IF EXIST eplusout.sparklog MOVE eplusout.sparklog "%output_path%%2Spark.log"
 IF EXIST expandedidf.err copy expandedidf.err+eplusout.err "%output_path%%2.err"
 IF EXIST readvars.audit MOVE readvars.audit "%output_path%%2.rvaudit"
 IF EXIST eplusout.sql MOVE eplusout.sql "%output_path%%2.sql"

:   11.  Clean up directory.
 ECHO Removing extra files . . .
 IF EXIST eplusout.inp DEL eplusout.inp
 IF EXIST eplusmtr.inp DEL eplusmtr.inp
 IF EXIST in.idf       DEL in.idf
 IF EXIST in.imf       DEL in.imf
 IF EXIST in.epw       DEL in.epw
 IF EXIST in.stat      DEL in.stat
 IF EXIST eplusout.dbg DEL eplusout.dbg
 IF EXIST test.mvi DEL test.mvi
 IF EXIST expandedidf.err DEL expandedidf.err
 IF EXIST readvars.audit DEL readvars.audit
 IF EXIST sqlite.err  DEL sqlite.err
 
 :done
 echo ===== %0 %1 %2 ===== Complete =====
exit