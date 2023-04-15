CFLAGS=-g -Wall -O2 -m64 -fno-stack-protector -fno-omit-frame-pointer
#CFLAGS=-g -Wall -O2 -fno-stack-protector
CC=gcc

# Boehm-Demers-Weiser collector
GCLIBDIR = gc-7.6.4
GCLIBZIP = gc-7.6.4.tar.gz
GCLIB = $(GCLIBDIR)/.libs/libgc.a

# libatomic_ops (dependency of Boehm GC)
LIBATOMIC = libatomic_ops-7.6.2
LIBATOMICZIP = $(LIBATOMIC).tar.gz

# libncurses (for the curses library support)
NCURSESLIBDIR = ncurses-6.3
NCURSESLIBZIP = $(NCURSESLIBDIR).tar.gz
NCURSESLIB = $(NCURSESLIBDIR)/lib/libncurses.a

LIBETA_EXTRAS = libeta/curses.o

ABI_FLAG = $(shell ../platform-flags.sh)
CINCS = -I./libeta
CFLAGS_EXAMPLES = -masm=intel -O0 -fno-stack-protector -fno-asynchronous-unwind-tables $(ABI_FLAG) $(CINCS)

ABI_FLAG=$(shell ./platform-flags.sh)


all: etafilt libeta.a examples/arr examples/fact

etafilt: demangle/demangle.o
	gcc $(ABI_FLAG) -o $@ $^

libeta.a: $(GCLIB) $(NCURSESLIB) libeta/libeta.o $(LIBETA_EXTRAS) 
	rm -rf $@ gc_tmp nc_tmp
	mkdir gc_tmp; cd gc_tmp; ar -x ../$(GCLIB)
	mkdir nc_tmp; cd nc_tmp; ar -x ../$(NCURSESLIB)
	ar rcs $@ libeta/libeta.o gc_tmp/*.o nc_tmp/*.o libeta/curses.o
	rm -rf gc_tmp nc_tmp

%.s: %.c
	$(CC) $(CFLAGS_EXAMPLES) -o $@ -S $<
	perl -ni -e 'if (!/InlineAsm/) { print $$_ }' $*.s

libeta/libeta.o: libeta/libeta.c $(GCLIB)
	gcc $(CFLAGS) -I$(NCURSESLIBDIR)/include -I$(GCLIBDIR)/include $(ABI_FLAG) -c -o $@ $<

libeta/curses.o: libeta/curses.c $(NCURSESLIB)
	gcc $(CFLAGS) -I$(NCURSESLIBDIR)/include -I$(GCLIBDIR)/include $(ABI_FLAG) -c -o $@ $<

%.o: %.c
	gcc $(CFLAGS) $(ABI_FLAG) -c -o $@ $<

$(GCLIB): $(GCLIBDIR)/configure
	cd $(GCLIBDIR); ./configure; make; make check

$(GCLIBDIR)/configure:
	# extract and install dependencies
	rm -f $(GCLIB)
	rm -rf $(GCLIBDIR)
	tar -xzvf deps/$(GCLIBZIP)
	tar -xzvf deps/$(LIBATOMICZIP)
	mv $(LIBATOMIC) $(GCLIBDIR)/libatomic_ops
	if [ -f ../gcconfig.h ] ; \
	then cp ../gcconfig.h $(GCLIBDIR)/include/private/ ; \
	fi;

$(NCURSESLIB): $(NCURSESLIBDIR)/configure
	cd $(NCURSESLIBDIR); ./configure; make; make check

$(NCURSESLIBDIR)/configure:
	# extract and install dependencies
	rm -f $(NCURSESLIB)
	rm -rf $(NCURSESLIBDIR)
	tar -xzvf deps/$(NCURSESLIBZIP)

examples/fact: examples/fact.s
	./linketa.sh examples/fact.s -o examples/fact
	gcc -E examples/fact.s -o examples/fact.sx

examples/arr: examples/arr.s
	./linketa.sh examples/arr.s -o examples/arr
	gcc -E examples/arr.s -o examples/arr.sx

examples/curses: examples/curses.s
	./linketa.sh examples/curses.s -o examples/curses

clean:
	rm -f libeta.a etafilt
	rm -f libeta/libeta.o
	rm -f demangle/demangle.o
	rm -f examples/fact examples/arr examples/fact-init examples/*.s examples/*.sx
	rm -f *~

clobber: clean
	$(MAKE) -C $(GCLIBDIR) clean
	$(MAKE) -C $(NCURSESLIBDIR) clean
	cp ../gcconfig.h $(GCLIBDIR)/include/private/

dust: clobber
	rm -f $(GCLIBZIP)
	rm -rf $(GCLIBDIR)
	rm -f $(NCURSESLIBZIP)
	rm -rf $(NCURSESLIBDIR)

FILES = runtime/demangle runtime/libeta \
        runtime/linketa.sh runtime/Makefile runtime/README.txt runtime/$(GCLIBDIR)\
        runtime/examples/*.c runtime/examples/*.s runtime/include \
        runtime/ChangeLog.txt runtime/platform-flags.sh

tarballs: $(GCLIBDIR)/configure $(NCURSESLIBDIR)/configure
	make clean
	cd .. && tar cvz --exclude=CVS --exclude="*~" ${FILES} > runtime.tar.gz
	cd .. && zip runtime.zip -r ${FILES} -x '*CVS*' -x "*~"
