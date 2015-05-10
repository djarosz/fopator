#!/bin/bash

# Please run from main direcotry e.g.
# ./example/run.sh

N=1000

mvn clean install

mkdir -p ./target/fopator/{xml,xsl,fonts,pdf}
cp  ./src/test/resources/*.xsl ./target/fopator/xsl
for i in $(seq 1 $N)
do
	cp ./src/test/resources/*.xml ./target/fopator/xml/projectteam-test-$i.xml
done

time java -jar ./target/fopator*.jar --spring.config.location=./example/application.properties

echo "Processed $(ls -1 ./target/fopator/pdf | wc -l)/$N"