
package com.campuslocker.model;

import java.util.LinkedList;
import java.util.Queue;

public class Waitlist {

    private Queue<Student> queueList;

    public Waitlist() {
        queueList = new LinkedList<>();
    }

    public void addToQueue(Student student) {
        if (!queueList.contains(student)) {
            queueList.offer(student);
        }
    }

    public Student getNextInLine() {
        return queueList.peek();
    }

    public Student removeFromQueue() {
        return queueList.poll();
    }

    public boolean isEmpty() {
        return queueList.isEmpty();
    }

    public int getQueueSize() {
        return queueList.size();
    }

    public Queue<Student> getQueueList() {
        return new LinkedList<>(queueList);
    }
}

