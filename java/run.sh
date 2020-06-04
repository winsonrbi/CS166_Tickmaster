#!/bin/bash
root=$(realpath $(dirname "$0"))
root=$(dirname $root)
dbname=$(logname)_db

cd $root/java

# Example: ./run.sh
java -cp lib/*:bin/ Ticketmaster $dbname $PGPORT $(logname)
