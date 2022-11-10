
package com.wsain.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkNodeScheduling {
    // 也可以不使用volatile，添加锁
    // 这里统计任务的结束完成数量来判断是否进行完成所有任务。
    // 也可以使用正在运行的任务，但是实际写起来有些别扭。
    public volatile static int finishCount;

    public static void main(String[] args) throws InterruptedException {
        Map<String, Set<WorkNode>> parentMap = new HashMap<>();
        BlockingQueue<WorkNode> runnableNodeQueue = new LinkedBlockingQueue<>();
        Set<String> runNodeSet = new HashSet<>();
        WorkNodeCallback callback = (curWorkNode) -> {
            finishCount++;
            if (null == curWorkNode.nextNodes || 0 == curWorkNode.nextNodes.size()) {
                return;
            }

            for (WorkNode nextNode : curWorkNode.nextNodes) {
                Set<WorkNode> parentNodeSet = parentMap.get(nextNode.getWorkId());
                for (WorkNode parentNode : parentNodeSet) {
                    if (!parentNode.getFinishedStatus()) {
                        break;
                    }
                    try {
                        synchronized (WorkNodeScheduling.class) {
                            if (runNodeSet.contains(nextNode.getWorkId())) {
                                break;
                            }
                            runNodeSet.add(nextNode.getWorkId());
                            runnableNodeQueue.put(nextNode);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        WorkNode root = WorkNode.getWorkTree(callback);
        getParnentMap(parentMap, root);
        Set<String> allWorkNodeIdSet = new HashSet<>();
        for (Entry<String, Set<WorkNode>> entrySet : parentMap.entrySet()) {
            allWorkNodeIdSet.add(entrySet.getKey());
            for (WorkNode value : entrySet.getValue()) {
                allWorkNodeIdSet.add(value.getWorkId());
            }
        }
        int allNodeNumber = allWorkNodeIdSet.size();
        runnableNodeQueue.put(root);
        while (finishCount < allNodeNumber) {
            // 队列 阻塞取，每次只获取一个。
            WorkNode runnableNode = runnableNodeQueue.take();
            // 获取节点开始执行
            if (1 == allNodeNumber - finishCount) {
                // 最后一个执行节点使用主线程执行。
                runnableNode.run();
            } else {
                runnableNode.start();
            }
        }
        System.out.println("执行完成");
    }

    public static void getParnentMap(Map<String, Set<WorkNode>> parentMap, WorkNode node) {
        List<WorkNode> nextNodes = node.nextNodes;
        if (null == nextNodes || nextNodes.size() == 0) {
            return;
        }
        for (WorkNode nextNode : nextNodes) {
            Set<WorkNode> parentNodeList = parentMap.computeIfAbsent(nextNode.getWorkId(),
                    key -> new HashSet<WorkNode>());
            parentNodeList.add(node);
            getParnentMap(parentMap, nextNode);
        }
    }
}