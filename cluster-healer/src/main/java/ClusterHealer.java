import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClusterHealer implements Watcher {

    // Zookeeper final variables
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/workers";

    // New private variable of type zookeeper
    private ZooKeeper zooKeeper;

    // Current ZnodeName
    private String currentZnodeName;

    // Path to the worker jar
    private String pathToProgram;

    // The number of worker instances we need to maintain at all times
    private int numberOfWorkers;


    // Class Constructor
    public ClusterHealer(int numberOfWorkers, String pathToProgram) {
        this.numberOfWorkers = numberOfWorkers;
        this.pathToProgram = pathToProgram;
    }

    /**
     * Check if the `/workers` parent znode exists, and create it if it doesn't. Decide for yourself what type of znode
     * it should be (e.g.persistent, ephemeral etc.). Check if workers need to be launched.
     */
    public void initialiseCluster() throws InterruptedException, KeeperException, IOException {

        // .exists() method which returns null or a Stat
        Stat nodeExists = zooKeeper.exists(ELECTION_NAMESPACE, this);
        System.out.println("Checking if znode: '" + ELECTION_NAMESPACE + "' exists in zookeeper");
        System.out.println();


        // If the parent znode doesn't exist, create one.
        if (nodeExists == null) {
            System.out.println("Parent znode: '" + ELECTION_NAMESPACE + "' does not exist, creating one now");
            String znodeFullPath = zooKeeper.create(ELECTION_NAMESPACE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
            System.out.println("znode: '" + ELECTION_NAMESPACE + "' created!!");
        }

        // If it does exist, print the number of workers and prompt the user the parent znode exists
        if (nodeExists != null) {
            System.out.println("znode: '" + ELECTION_NAMESPACE + "' already exists in zookeeper");
            System.out.println("There are currently " + zooKeeper.getChildren(ELECTION_NAMESPACE, true).size() + " workers");

        }
        // Initial check for running workers
        checkRunningWorkers();
    }


    /**
     * Instantiates a Zookeeper client, creating a connection to the Zookeeper server.
     */
    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    /**
     * Keeps the application running waiting for Zookeeper events.
     */
    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    /**
     * Closes the Zookeeper client connection.
     */
    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    /**
     * Handles Zookeeper events related to: - Connecting and disconnecting from the Zookeeper server. - Changes in the
     * number of workers currently running.
     *
     * @param event A Zookeeper event
     */

    @Override
    public void process(WatchedEvent event) {

        // Switch statement for managing events
        switch (event.getType()) {

            // Prompt user they have connected or disconnected to zookeeper
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;

            // Check the workers if any nodes have been changed
            case NodeChildrenChanged:
                try {
                    System.out.println("There are currently " + zooKeeper.getChildren(ELECTION_NAMESPACE, true).size() + " workers");
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkRunningWorkers();
                break;
        }
    }

    /**
     * Checks how many workers are currently running.
     * If less than the required number, then start a new worker.
     */
    public void checkRunningWorkers() {

        try {
            if (zooKeeper.getChildren(ELECTION_NAMESPACE, true).size() < numberOfWorkers) {
                startWorker();
            } else {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a new worker using the path provided as a command line parameter.
     *
     * @throws IOException
     */
    public void startWorker() throws IOException {
        File file = new File(pathToProgram);
        String command = "java -jar " + file.getName();
        System.out.println(String.format("Launching worker instance : %s ", command));
        Runtime.getRuntime().exec(command, null, file.getParentFile());
    }
}
