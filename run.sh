#!/bin/bash

./clean.sh

./build.sh

echo "Finished compiling, trying to run"

java -cp target/classes Main