#!/bin/bash

# shellcheck disable=SC2046
# shellcheck disable=SC2164
cd $(dirname "$0")

trap 'docker rm -f oracle-xe 2>&1 >/dev/null' EXIT

function startOracle() {
	local image="$1"
	docker rm -f oracle-xe 2>/dev/null
	docker run --rm --name oracle-xe \
		-p 1521:1521 -e ORACLE_PASSWORD=oracle \
		-d "$image"
}

function doMeasure() {
	local image="$1"
	local jar=""
	startOracle "$image"
	if [ $? != 0 ] ; then
		echo "error staring container: $image"
		return
	fi
	jar=$(find . -path "./build/libs/*all.jar"|tr -d " "|head)
	if [[ "$jar" == "" || ! -f "$jar" ]] ; then
		./gradlew clean build
		jar=$(find . -path "./build/libs/*all.jar"|head)
	fi
	java -jar "$jar" '{}'
}

function measure() {
	local image="$1"
	local size=0
	local n=0
	n=$(docker images "$image" | wc -l)
	if [ "$n" != 2 ] ; then
		docker pull "$image" >&2
	fi
	size=$(docker images --format "{{.Size}}" "$image" | sed -e "s/[GM]/ &/")
	printf "%-48s: " "$image ($size)"
	time=$(doMeasure "$1" | \
		grep "Connection established after" | \
		sed -e "s/.*established after *//;s/seconds/s/")
	if [ "$time" == "" ] ; then
		time="?"
	fi
	echo "$time"
}

if [ $# == 1 ] ; then
	docker images --format "$1 ({{.Size}})" "$1" | sed -e "s/[GM]/ &/"
	doMeasure "$1"
else
	measure "wnameless/oracle-xe-11g-r2:latest"
	measure "gvenzl/oracle-xe:11-slim"
	measure "gvenzl/oracle-xe:slim"
	measure "gvenzl/oracle-xe:11-full"
fi
