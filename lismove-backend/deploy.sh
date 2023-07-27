#!/usr/bin/env bash
if [[ $# -eq 0 ]]; then
  echo "No arguments supplied"
  exit 1
fi

label="lismove-$1"
aws_env="lismove-test"

if [[ $2 == "prod" ]]; then
  echo "Prod environment"
  label="lismove-$2-$1"
  aws_env="Lismove-env"
fi

rm target/latest.jar
mvn versions:set -DnewVersion=$1
mvn package -DskipTests
cp target/lismove-$1.jar target/latest.jar
eb deploy -l="$label" ${aws_env} --profile=intellimens
