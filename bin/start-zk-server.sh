# Copyright 2016 Xiaomi, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

lib=$bin/../linden-core/target/lib
config=$bin/../config

CLASSPATH=$CLASSPATH:$lib/*

if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

exec $JAVA -cp $CLASSPATH org.apache.zookeeper.server.quorum.QuorumPeerMain $config/zookeeper.properties
