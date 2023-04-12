==========================
= CS4120 Runtime Library =
==========================

What's under the runtime/ directory?
=============
include/ --- the Eta library interface files.

libeta/ --- the source code for the runtime library you'll want to link into
programs your compiler produces. This has the implementations of things like
_eta_alloc, and the various methods given in the io and conv modules.

demangle/ --- a little utility that can turn things like _Imain_paii into things
like main(int[][]). You can pass error messages from the linker through it to
make them nicer, or use it to help debug name encoding problems.

This gets compiled into etafilt.

examples/ --- the .s files are of most interest here, since they show what your
output might look like, and give examples of syntax to produce. With libeta
compiled, you can turn them into runnable form by doing something like this:
    gcc -o arr example/arr.s -L. -leta -lpthread

The -L. option tells to look for libraries in the current directory (.) and -leta
tells to link in libeta. -o arr tells to name the output arr (or arr.exe). If you
don't include the -o, it'll get named something like a.out or a.exe. GCC is used
for this since it knows how to find the assembler and the linker and what
standard libraries to include. Note that the C compiler (cc1) isn't actually
invoked here.

You can use a command like this to test your output as well. For convenience, we
also include a little linker script, linketa.sh. It links your code the same way,
but also passes the output through etafilt to get nicer link errors, and tries to
figure out where libeta is located so it can work if you invoke it from
a different directory. The makefile uses it to compile examples, like this:

    ./linketa.sh examples/fact.s -o examples/fact

The .s files were not produced by hand. They were created by running
    gcc -S -O0 -fno-stack-protector -I../libeta

on corresponding .c files, then hand-eliminating platform-dependent contents,
and adding the comments. To get ideas for instruction selection, you might want
to use the same command to compile your own C code.

Makefile --- tells the make command how to compile all of the above.

Building
========

The Makefile will build the Boehm-Demers-Weiser conservative garbage collector.
The runtime library relies on it for GC. See http://hboehm.info/gc/ for more
information.

The Eta runtime library and the garbage collector library are combined by the
Makefile into a single library: libeta.a

Linux VM:
-------------------------
This runtime has been tested to work on the released Docker virtual machine. To
build the runtime, run make in this archive's root directory.
