# Makefile to compile 

BINDIR=./bin
SRCDIR=./src
DOCDIR=./doc

.SUFFIXES: .java .class

${BINDIR}/%.class: ${SRCDIR}/%.java
	javac -g $< -cp ${BINDIR} -d ${BINDIR} 

CLASSES = \
	${BINDIR}/Score.class \
	${BINDIR}/WordDictionary.class \
	${BINDIR}/WordRecord.class \
	${BINDIR}/WordThread.class \
	${BINDIR}/Controller.class \
	${BINDIR}/WordPanel.class \
	${BINDIR}/WordApp.class \
	
default: classes

classes: $(CLASSES:${SRCDIR}/%.java=${BINDIR}/%.class)

run:
	cd bin && java WordApp "5" "3" "" && cd ..

clean:
	rm -f ${BINDIR}/*.class

docs:
	javadoc  -classpath ${BINDIR} -d ${DOCDIR} ${SRCDIR}/*.java

cleandocs:
	rm -rf ${DOCDIR}/*
