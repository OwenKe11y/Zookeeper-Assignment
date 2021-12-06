# Cluster Healing with Zookeeper

## Introduction
Your task is to implement a cluster healer application which will use Zookeeper to monitor and automatically heal a 
cluster of
 worker nodes. The cluster healer will launch the requested number of workers, then monitor the cluster to ensure 
that the
  requested number of workers is running. If a worker dies, the cluster healer should launch more workers to keep the
   total number of workers at the requested number.

## Workers
You are provided with a worker application, `faulty-worker.jar`. On startup, this application connects to Zookeeper 
and creates an ephemeral, sequential znode called `worker_` under the `/workers` parent znode, e.g. 
`/workers/worker_0000001`. This worker application is programmed to continually crash at random intervals, which 
will cause the znode it created to be removed (since it is ephemeral).

## Cluster Healer Operation
On startup, the cluster healer should 
- connect to Zookeeper
- check if the `/workers` parent znode exists and if it doesn't, create it.
- start the requested number of workers
    - the number of workers and the path to the worker application are passed in as command-line arguments.
- watch the cluster to check if workers die
    - replacement workers should be started when workers die. 
    - the number of running workers should always be the requested number.
  

