GenOpt® Release Note
====================

This file gives short instructions for installing and running GenOpt.
For more details, please consult the GenOpt manual which can be
downloaded from
[http://simulationresearch.lbl.gov/GO](http://simulationresearch.lbl.gov/GO).

* * * * *

Contents
--------

[Running GenOpt](#run)\
 [Directories](#dire)\
 [Copyright Notice](#copyright)\
 [License Agreement](#license)\

* * * * *

Running GenOpt
--------------

### Running GenOpt from the file explorer

To run GenOpt from the file explorer, double-click on the file
`genopt.jar`. This will start the graphical user interface. From the
graphical user interface, select `File`, `Start...` and select a GenOpt
initialization file.

### Running GenOpt from the command line

GenOpt can also be run as a console application, either with or without
the graphical user interface. To run GenOpt as a console application
with graphical user interface, open a shell, go to this directory and
type

      java -jar genopt.jar [initializationFile]

where `[initializationFile]` is an optional argument that can be
replaced with the GenOpt initialization file (see example below). To
start GenOpt without the graphical user interface, type

      java -classpath genopt.jar genopt.GenOpt [initializationFile]

For example, to run the example file provided with GenOpt that minimizes
a quadratic function using the Hooke-Jeeves algorithm, type on Mac OSX

      java -jar genopt.jar example/quad/GPSHookeJeeves/optMacOSX.ini

on Linux

      java -jar genopt.jar example/quad/GPSHookeJeeves/optLinux.ini

and on Windows

      java -jar genopt.jar example\quad\GPSHookeJeeves\optWinXP.ini

For further information and how to use your simulation software to
evaluate the cost function, please read the manual.

* * * * *

Directories
-----------

### example

This directory contains examples that use EnergyPlus, IDA, Java or
Dymola to evaluate the cost function. To illustrate the use of GenOpt,
the directory `example/quad` contains several examples that either
minimize a simple quadratic cost function or use the quadratic function
to do parametric runs. These examples are recommended as a starting
point to learn how GenOpt works. To learn how inequality constraints on
the cost function can be implemented, see `example/quad/constraint`.

### cfg

This directory contains files that show how to configure different
simulation programs on various operating systems. The files in this
directory contain comments that explain for which program and operating
system they were written.

### src

The `src` directory contains the source code of GenOpt. The source code
of the optimization algorithms can be found in the directory
`src/genopt/algorithm`. You do not have to compile these files. They are
only of interest if you want to implement your own optimization
algorithms.

### documentation

This directory contains the [manual](./documentation/manual.pdf) and the
[code documentation](./documentation/jdoc/index.html). The code
documentation is only needed by developers.

* * * * *

Copyright Notice
----------------

GenOpt Copyright (c) 1998-2011, The Regents of the University of
California, through Lawrence Berkeley National Laboratory (subject to
receipt of any required approvals from the U.S. Dept. of Energy). All
rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov.

NOTICE. This software was developed under partial funding from the U.S.
Department of Energy. As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly. Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.

* * * * *

License Agreement
-----------------

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

-   Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
-   Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
-   Neither the name of the University of California, Lawrence Berkeley
    National Laboratory, U.S. Dept. of Energy nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

* * * * *

For more information, visit the GenOpt [web
pages](http://simulationresearch.lbl.gov/GO).

Please send comments or questions to Michael Wetter, LBNL,
[MWetter@lbl.gov](mailto:MWetter@lbl.gov) \
 May 4, 2009.
