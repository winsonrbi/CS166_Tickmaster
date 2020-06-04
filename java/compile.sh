#!/bin/bash
root=$(realpath $(dirname "$0"))
root=$(dirname $root)

cd $root/java

rm -rf bin/*.class
javac -cp ".;lib/postgresql-42.1.4.jar;" src/Ticketmaster.java -d bin/
