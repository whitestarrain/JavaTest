package com.wsain.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * InnerWorkNode
 */
@FunctionalInterface
interface WorkNodeCallback {
    public void run(WorkNode currentNode);
}

public class WorkNode extends Thread {

    private static Random rand = new Random();

    private static volatile int count = 0;
    private int nodeCount;
    private int runtime;
    private String workId;
    public List<WorkNode> nextNodes;
    private boolean finishedStatus = false;

    WorkNodeCallback callback;

    public WorkNode() {
        this((workNode) -> {
        });
    }

    public WorkNode(WorkNodeCallback callback) {
        this.nodeCount = ++count;
        this.workId = "worknode" + this.nodeCount;
        this.runtime = rand.nextInt(1000);
        this.callback = callback;
    }

    @Override
    public int hashCode() {
        return this.workId.hashCode();
    }

    @Override
    public String toString() {
        return this.workId;
    }

    public String getWorkId() {
        return this.workId;
    }

    public boolean getFinishedStatus() {
        return this.finishedStatus;
    }

    private String getRuningMessage() {
        return this.workId +
                new String(new char[this.nodeCount])
                        .replace("\0", "-");
    }

    public static WorkNode getWorkTree(WorkNodeCallback callback) {
        WorkNode w1 = new WorkNode(callback);
        WorkNode w2 = new WorkNode(callback);
        WorkNode w3 = new WorkNode(callback);
        WorkNode w4 = new WorkNode(callback);
        WorkNode w5 = new WorkNode(callback);
        WorkNode w6 = new WorkNode(callback);
        WorkNode w7 = new WorkNode(callback);

        List<WorkNode> w1Childs = Arrays.asList(w2, w3);
        w1.nextNodes = w1Childs;

        List<WorkNode> w2Childs = Arrays.asList(w3, w4, w5);
        w2.nextNodes = w2Childs;

        List<WorkNode> w3Childs = Arrays.asList(w5, w6);
        w3.nextNodes = w3Childs;

        List<WorkNode> w6Childs = Arrays.asList(w7);
        w6.nextNodes = w6Childs;

        return w1;
    }

    @Override
    public void run() {
        try {
            System.out.println(this.getRuningMessage() + "start" + "(" + this.runtime + ")");
            // work
            Thread.sleep(this.runtime);
            // finish
            System.out.println(this.getRuningMessage() + "end" + "(" + this.runtime + ")");
            this.finishedStatus = true;
            if (null != this.callback) {
                this.callback.run(this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}