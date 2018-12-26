#!/usr/bin/env bash

# recursive function iterate from tag to previous tag and show commits
function main {
   version=$(echo $1 | tr -d ^)

   if [[ -z "$1" ]]; then
     COUNT_BASE=HEAD # first it's based in HEAD
   else
     COUNT_BASE=$1
   fi
   code=$(git rev-list $COUNT_BASE --count)
   prev=$(git describe --abbrev=0 --tags $1 2>/dev/null)

   if [[ $? == 0 ]]; then
      git log --pretty=format:'{%n "version": "'$version'",%n "code": "'$code'",%n "date": "%ad",%n "message": "%f"%n},' $version...$prev
      main $prev^
   else # show all to first commit
      entries=$(git log --pretty=format:'{%n "version": "'$version'",%n "code": "'$code'",%n "date": "%ad",%n "message": "%f"%n},' $version...$(git rev-list --max-parents=0 HEAD))
      # https://unix.stackexchange.com/questions/144298/delete-the-last-character-of-a-string-using-string-manipulation-in-shell-script
      echo "${entries::-1}"
   fi
}

## Execute the script
echo "["
main $@
echo "]"

