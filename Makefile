# Makefile to compile 

BINDIR=./bin
SRCDIR=./src
DOCDIR=./doc

.SUFFIXES: .java .class

${BINDIR}/%.class: ${SRCDIR}/%.java
	javac $< -cp ${BINDIR} -d ${BINDIR} 

# first build rule
${BINDIR}/C.class:${BINDIR}/Score.class ${BINDIR}/WordDictionary.class ${BINDIR}/WordRecord.class ${BINDIR}/WordPanel.class ${BINDIR}/WordApp.class 

run:
	cd bin & java ${BINDIR}/WordApp.class "5" "3" & cd ..

clean:
	rm -f ${BINDIR}/*.class

docs:
	javadoc  -classpath ${BINDIR} -d ${DOCDIR} ${SRCDIR}/*.java

cleandocs:
	rm -rf ${DOCDIR}/*
