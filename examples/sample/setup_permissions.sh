#   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
#   Licensed under the Apache License, Version 2.0 (the "License").
#   You may not use this file except in compliance with the License.
#   A copy of the License is located at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   or in the "license" file accompanying this file. This file is distributed
#   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
#   express or implied. See the License for the specific language governing
#   permissions and limitations under the License.

#!/bin/bash

admin='admin:admin'
testuser="testuser"

if [ -z "$1" ]; then
   echo "Please provide endpoint hostname:port"
   exit 1
fi

endpoint="$1"

echo "Creating user '${testuser}' and associating with replication_backend role"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/internalusers/${testuser}?pretty" -H 'Content-Type: application/json' -d'
{
  "password": "testuser",
  "backend_roles": ["replication_backend"]
}
'
echo
echo "-----"

echo "Creating actiongroup 'follower-replication-action-group' and associating index level permissions to start/stop replication"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/actiongroups/follower-replication-action-group" -H 'Content-Type: application/json' -d'
{
  "allowed_actions": [
    "indices:admin/close",
    "indices:admin/close[s]",
    "indices:admin/create",
    "indices:admin/mapping/put",
    "indices:admin/open",
    "indices:admin/opendistro/replication/index/start",
    "indices:admin/opendistro/replication/index/stop",
    "indices:data/read/opendistro/replication/file_metadata",
    "indices:data/write/index",
    "indices:data/write/opendistro/replication/changes",
    "indices:data/write/replication",
    "indices:monitor/stats"
  ]
}
'
echo
echo "-----"

echo "Creating actiongroup 'follower-replication-cluster-action-group' and associating cluster level permissions for replication."
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/actiongroups/follower-replication-cluster-action-group" -H 'Content-Type: application/json' -d'
{
  "allowed_actions": [
    "cluster:monitor/state",
    "cluster:admin/snapshot/restore",
    "cluster:admin/opendistro/replication/autofollow/update"
  ]
}
'
echo
echo "-----"


echo "Creating actiongroup 'leader-replication-action-group' and associating index level permissions for replication."
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/actiongroups/leader-replication-action-group" -H 'Content-Type: application/json' -d'
{
  "allowed_actions": [
    "indices:data/read/opendistro/replication/file_chunk",
    "indices:data/read/opendistro/replication/file_metadata",
    "indices:admin/opendistro/replication/resources/release",
    "indices:data/read/opendistro/replication/changes",
    "indices:admin/mappings/get",
    "indices:monitor/stats"
  ]
}
'
echo
echo "-----"


echo "Creating actiongroup 'leader-replication-cluster-action-group' and associating cluster level permissions for replication."
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/actiongroups/leader-replication-cluster-action-group" -H 'Content-Type: application/json' -d'
{
  "allowed_actions": [
    "cluster:monitor/state"
  ]
}
'
echo
echo "-----"

echo "Creating role 'replication_follower_role' and associating for index pattern '*' and actiongroups ['follower-replication-action-group', 'follower-replication-cluster-action-group']"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/roles/replication_follower_role" -H 'Content-Type: application/json' -d'
{
  "cluster_permissions": [
    "follower-replication-cluster-action-group"
  ],
  "index_permissions": [{
    "index_patterns": [
      "*"
    ],
    "allowed_actions": [
      "follower-replication-action-group"
    ]
  }]
}
'
echo
echo "-----"

echo "Creating role 'replication_leader_role' and associating for index pattern '*' and actiongroup ['leader-replication-action-group', 'leader-replication-cluster-action-group']"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/roles/replication_leader_role" -H 'Content-Type: application/json' -d'
{
  "cluster_permissions": [
    "leader-replication-cluster-action-group"
  ],
  "index_permissions": [{
    "index_patterns": [
      "*"
    ],
    "allowed_actions": [
      "leader-replication-action-group"
    ]
  }]
}
'
echo
echo "-----"


echo "Mapping role 'replication_follower_role' to 'replication_backend' backend role"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/rolesmapping/replication_follower_role?pretty" -H 'Content-Type: application/json' -d'
{
  "backend_roles" : [
    "replication_backend"
  ]
}
'
echo
echo "-----"

echo "Mapping role 'replication_leader_role' to 'replication_backend' backend role"
curl -ks -u $admin -XPUT "https://${endpoint}/_opendistro/_security/api/rolesmapping/replication_leader_role?pretty" -H 'Content-Type: application/json' -d'
{
  "backend_roles" : [
    "replication_backend"
  ]
}
'
echo
echo "-----"

echo

