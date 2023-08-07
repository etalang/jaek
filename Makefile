zip:
	rm -f submission.zip
	rm -rf tempzipdir/
	mkdir tempzipdir/
	cp -R src tempzipdir
	rm -f tempzipdir/src/main/java/JFlexLexer.java
	rm -f tempzipdir/src/main/java/parser.java
	rm -f tempzipdir/src/main/java/SymbolTable.java
	rm -f tempzipdir/src/main/java/JFlexLexer.java~
	rm -f tempzipdir/src/main/java/parser.java~
	rm -f tempzipdir/src/main/java/SymbolTable.java~
	cp -R production/dependencies tempzipdir
	cp production/build.gradle tempzipdir
	cp production/etac tempzipdir
	cp production/etac-build tempzipdir
	cp cup tempzipdir
	cp -R benchmarks tempzipdir
	cd tempzipdir; zip -r ../submission.zip . -x '__MACOSX'
	rm -rf tempzipdir/

test-vm:
	make zip
	mv submission.zip ../shared
	rm -rf ../shared/production
	unzip ../shared/submission.zip -d ../shared/production