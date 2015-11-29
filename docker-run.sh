#!/bin/bash

doc up -d kafka postgres elasticsearch grafana

echo "Waiting for data stores to start..."
sleep 5

doc up -d --no-recreate bottledwater schemaregistry #does schemaregistry need to be up before bottledwater starts?
