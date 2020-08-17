#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -e

run_consumer() {
  docker exec -it $1 /usr/bin/kafka-console-consumer --from-beginning --property print.key=true \
    --property key.separator="-" --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer \
    --topic $2 --bootstrap-server kafka:9092
  #    docker exec -it $1 /usr/bin/kafka-console-consumer --from-beginning --topic $2 --bootstrap-server kafka:9092
}

CONTAINER="azkarra-cp-broker"
TOPIC="streams-wordcount-output"
SERVICE="cp-broker"

if [ -z $(docker-compose ps -q $SERVICE) ] || [ -z $(docker ps -q --no-trunc | grep $(docker-compose ps -q $SERVICE)) ]; then
  echo "Docker service $SERVICE is not running, please run command docker-compose up -d before using this script."
  exit 1
else
  run_consumer $CONTAINER $TOPIC
fi

exit 0
