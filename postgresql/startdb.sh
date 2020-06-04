#!/bin/bash
folder=/tmp/$(logname)/mydb
PGDATA=$folder/data
PGSOCKETS=$folder/sockets
export PGDATA
export PGSOCKETS

#Initialize folders
rm -fr $PGDATA
mkdir -p $PGDATA
rm -fr $PGSOCKETS
mkdir -p $PGSOCKETS

#Initialize DB
initdb

#Start folder
pg_ctl -o "-c unix_socket_directories=$PGSOCKETS" -D $PGDATA -l $folder/logfile start
