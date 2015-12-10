#!/bin/bash

echo "****** SETTING UP EXAMPLE ******"

echo "starting postgres"
gosu postgres pg_ctl -w start

echo "setting up example"
gosu postgres psql -h localhost -p 5432 -U postgres -a -f /docker-entrypoint-initdb.d/setup.sql

echo "stopping postgres"
gosu postgres pg_ctl stop
echo "stopped postgres"

echo ""
echo "****** FINISHED SETTING UP EXAMPLE ******"
