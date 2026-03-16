#!/bin/bash

VERSION=$1;

echo "Selected version is $VERSION"

LOCK_FILE=$PWD/repo/$VERSION/lock.lock

echo "Try to lock this version..."

if test -f "$LOCK_FILE"; then
	echo "Failed. Version already locked by: $(<$LOCK_FILE)"
	exit
fi
TIME=$(date)

echo "$USER $TIME" > $LOCK_FILE

echo "Success. Version locked at $TIME"
