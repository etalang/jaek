zip:
	rm -f submission.zip
	rm -rf tempzipdir/
	mkdir tempzipdir/
	cp -R src tempzipdir
	rm -f tempzipdir/src/main/java/JFlexLexer.java
	cp -R production/dependencies tempzipdir
	cp production/build.gradle tempzipdir
	cp production/etac tempzipdir
	cp production/etac-build tempzipdir
	cd tempzipdir; zip -r ../submission.zip . -x '__MACOSX'
	rm -rf tempzipdir/