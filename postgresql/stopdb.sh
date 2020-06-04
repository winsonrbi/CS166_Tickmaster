#!/bin/bash
folder=/tmp/$(logname)/mydb
PGDATA=$folder/data
PGSOCKETS=$folder/sockets
export PGDATA
export PGSOCKETS
pg_ctl -o "-c unix_socket_directories=$PGSOCKETS" -D $PGDATA -l $folder/logfile stop
