package com.yandex.kanban.util;

import com.yandex.kanban.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryStorage<T extends Task> {
    private Node<T> head;
    private Node<T> tail;
    private final Map<Integer, Node<T>> nodeMap = new HashMap<>();

    public void linkLast(T t) {
        Node<T> newNode = new Node<>(t);

        //in case of empty storage
        if (head == null) {
            head = newNode;
            nodeMap.put(head.t.getId(), newNode);
            return;
        }

        //in case of 1 element storage
        if (tail == null) {
            if (nodeMap.containsKey(t.getId())) {
                head = newNode;
                nodeMap.put(head.t.getId(), head);
            } else {
                tail = newNode;
                head.next = tail;
                tail.prev = head;
                nodeMap.put(tail.t.getId(), tail);
            }
            return;
        }

        //common case
        if (nodeMap.containsKey(t.getId())) {
            Node<T> oldNode = nodeMap.get(t.getId());
            nodeMap.put(t.getId(), newNode);
            if (oldNode == head) {
                head = oldNode.next;
                head.prev = null;
            } else if (oldNode == tail) {
                tail = newNode;
                tail.prev = oldNode.prev;
                oldNode.prev.next = tail;
                return;
            } else {
                oldNode.next.prev = oldNode.prev;
                oldNode.prev.next = oldNode.next;
            }
        }

        Node<T> oldTail = tail;
        tail = newNode;
        oldTail.next = tail;
        tail.prev = oldTail;
        nodeMap.put(t.getId(), newNode);

    }

    public List<T> getTasks() {
        List<T> tasks = new ArrayList<>();
        if (head == null) {
            return tasks;
        }
        Node<T> node = head;
        T t = node.t;
        while (t != null) {
            tasks.add(t);
            node = node.next;
            if (node == null) {
                break;
            } else {
                t = node.t;
            }
        }
        return tasks;
    }

    public void remove(int id) {
        if (!nodeMap.containsKey(id)) {
            return;
        }
        Node<T> node = nodeMap.get(id);
        nodeMap.remove(id);
        if (node == head) {
            if (head.next == null) {
                head = null;
            } else {
                head = node.next;
                head.prev = null;
            }
            return;
        } else if (node == tail) {
            tail = node.prev;
            tail.next = null;
            return;
        }
        Node<T> prevNode = node.prev;
        Node<T> nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    public void clear() {
        head = null;
        tail = null;
        nodeMap.clear();
    }

    static class Node<E> {
        private Node<E> prev;
        private Node<E> next;
        private final E t;

        private Node(E t) {
            this.t = t;
        }
    }
}