#!/bin/bash

VERSION=$1;

echo "Selected version is $VERSION"

LOCK_FILE=$PWD/repo/$VERSION/lock.lock

echo "Try to unlock this version..."

if test -f "$LOCK_FILE"; then
	echo "Success. Version unlocked by user: $USER"
	rm $LOCK_FILE
	exit
fi

echo "Failed. Version is not locked"
