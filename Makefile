# Adapted from TA Omkar
all: etac.jar

etac.jar: bin/lexer/JFlexLexer.class
etac.jar: src/main/Main.kt
	kotlinc -include-runtime -cp "bin:lib/clikt-jvm-3.5.1.jar" -d etac.jar $<

bin/lexer/JFlexLexer.class: src/lexer/JFlexLexer.java
	javac -cp bin -d bin $<

src/lexer/JFlexLexer.java: src/lexer/Lexer.flex
	jflex -d src/lexer $<

zip: clean
	zip -r submission.zip \
        Makefile config.mk README.md \
        etac etac-build \
        src tests

clean:
	rm -rf etac.jar etac.zip src/lexer/JFlexLexer.java* bin/*

.PHONY: all zip clean