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
  

## Completing the Implementation
You are provided with starter code which includes the classes described below. The [documentation for the Zookeeper Client Java API](https://zookeeper.apache.org/doc/r3.6.1/apidocs/zookeeper-server/org/apache/zookeeper/ZooKeeper.html) will likely be useful to you in completing the implementation.

### `ClusterHealer.java`
This class is responsible for initialising and monitoring the health of the cluster. You are provided with 2 
instance variables and one 
implemented method. Your task is to complete the implementation of this class, by writing the following methods:

- Constructor: `public ClusterHealer(int numberOfWorkers, String pathToProgram)`
  - initialises the instance variables
- `connectToZookeeper()`
    - instantiates a Zookeeper client, creating a connection to the Zookeeper server.
- `process(WatchedEvent event)`
    - Handle Zookeeper events related to: 
        - Changes in the number of workers currently running 
        - Connecting to the Zookeeper server
            - Should print out this message to System.out: `Successfully connected to Zookeeper`
        - Disconnecting from the Zookeeper server
            - Should print out this message to System.out: `Disconnected from Zookeeper`
- `initialiseCluster()`
    - Checks if the `/workers` parent znode exists, and create it if it doesn't. 
        - Decide for yourself what type of znode it should be (e.g.persistent, ephemeral etc.).
     - Checks if workers need to be launched.
- `checkRunningWorkers()`
    - Check how many workers are currently running. If less than the required number, then start a new worker.
- `run()`
    - Keep the application running waiting for Zookeeper events.
- `close()` 
    - Close the Zookeeper client connection.
         
#### Already Implemented
- `startWorker()`
    - You are given a completed helper method which starts a new worker using the path provided as a command line argument. **You do not need to make any changes to this method**.
    
### `Application.java`
This class is already implemented for you. It contains a main method which creates a new `ClusterHealer` instance and calls methods on it to:
- connect to Zookeeper
- initialise the cluster
- keep running waiting for Zookeeper events

**You should not make any changes to this class. If there are errors in this class, then the method signatures for 
`ClusterHealer` haven't been properly implemented**.   


## Building and Running the Cluster Healer Application
### Building
Use the maven `package` goal (either from the IntelliJ tool window or running `mvn package` on the command line in the 
project root folder) to 
build an executable jar from 
your 
code. 
The jar will be called `cluster-healer-1.0-SNAPSHOT-jar-with-dependencies.jar`and it will be in the `target` folder 
in your project. Tests have been disabled by default, so as long as your code compiles correctly you can build it 
into a jar and test it manually by running it (see below). This is the recommended approach, i.e. make 
changes, build, run and see if it behaves as expected.

### Running
Running this command from the `cluster-healer` project directory will start up 3 worker instances using the provided `faulty-worker.jar`, and will monitor the cluster to ensure that 3 instances are always running. **Ensure that you've started the Zookeeper server first**.
```
java -jar target/cluster-healer-1.0-SNAPSHOT-jar-with-dependencies.jar 3 ../faulty-worker.jar
```

## Grading
This assignment will be graded in 2 ways:
- Using automated tests on your repository to verify that the expected functionality has been implemented.
- By reviewing your code and examining your repository's commit history to assess:
    - The quality and coding style of your implementation of the functionality.
    - Your adherence to coding best practices regarding formatting, variable naming etc.
    - Your adherence to software development best practices in using git and GitHub. 
      - **This means regularly committing your work as you go, using meaningful commit messages**
 
Marks will be awarded as follows:
- 60% Functionality
- 40% Quality 

## Submitting
**Please ensure that you complete your work in the repository created for you by GitHub Classroom.** To submit, all you need to do is to ensure that you push your code to the repository on GitHub. On the submission date, the most recent commit in the GitHub repository will be treated as your submission. It is not necessary to submit code on Moodle or via email. 
