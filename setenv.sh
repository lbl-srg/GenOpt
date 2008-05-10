#!/bin/bash
########################################################
# This file sets environment variables for GenOpt
# on Mac OS X, Linux or Windows (from a cygwin shell).
# 
# To run it, change to the directory of this file and
# type
#  source ./setenv.sh
# To force reseting variables, use
#  source ./setenv.sh -f
#
# MWetter@lbl.gov                             2007-05-09
########################################################
if [ "$1" != "-f" ]; then
    if test ${GenOptEnvSet}; then
	echo "GenOpt environment already set. Doing nothing."
	echo "To set again, use 'source setenv.sh -f'"
	return 1
    fi
else
    echo "Forcing reset of environment variables"
fi

export GenOptDir=`pwd`
export PATH=${GenOptDir}/bin:"${PATH}"
export CLASSPATH=${GenOptDir}:$CLASSPATH

export GenOptEnvSet="true" # used by Makefile
