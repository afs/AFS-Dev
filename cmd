#!/bin/sh

CP="/home/afs/Jena/jena-arq/classes:/home/afs/Jena/jena-core/classes:/home/afs/Jena/jena-tdb/classes:/home/afs/.m2/repo/commons-codec/commons-codec/1.4/commons-codec-1.4.jar:/home/afs/.m2/repo/org/apache/httpcomponents/httpclient/4.1.2/httpclient-4.1.2.jar:/home/afs/.m2/repo/org/apache/httpcomponents/httpcore/4.1.3/httpcore-4.1.3.jar:/home/afs/.m2/repo/org/apache/jena/jena-iri/0.9.1-SNAPSHOT/jena-iri-0.9.1-SNAPSHOT.jar:/home/afs/.m2/repo/junit/junit/4.8.2/junit-4.8.2.jar:/home/afs/.m2/repo/log4j/log4j/1.2.16/log4j-1.2.16.jar:/home/afs/.m2/repo/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar:/home/afs/.m2/repo/org/slf4j/slf4j-log4j12/1.6.4/slf4j-log4j12-1.6.4.jar:/home/afs/.m2/repo/org/slf4j/jcl-over-slf4j/1.6.4/jcl-over-slf4j-1.6.4.jar:/home/afs/.m2/repo/xerces/xercesImpl/2.10.0/xercesImpl-2.10.0.jar:/home/afs/.m2/repo/xml-apis/xml-apis/1.4.01/xml-apis-1.4.01.jar"

CP="target/classes:target/test-classes:$CP"

JVM_ARGS="${JVM_ARGS:--Xmx1200M}"

java -cp "$CP" $JVM_ARGS "$@"