package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

class Node {
    private Node next;
    private Node prev;
    private Task value;

    public Task getValue() {
        return value;
    }

    public void setValue(Task value) {
        if (value != null) {
            this.value = value;
        } else {
            throw new NullPointerException();
        }
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node(Task value) {
        if (value != null) {
            this.value = value;
        } else {
            throw new NullPointerException();
        }
    }


}
