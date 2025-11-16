package ru.yandex.javacourse.schedule.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private final MyOwnLinkedList history = new MyOwnLinkedList();
	private final HashMap<Integer, Node> nodeMap = new HashMap<>();

	@Override
	public List<Task> getHistory() {
		return history.getTasks();
	}

	@Override
	public void add(Task task) {
		if (task == null) {
			return;
		}
		if (!nodeMap.containsKey(task.getId())) {
			Node newNode = new Node(task);
			nodeMap.put(task.getId(), newNode);
			history.linkLast(newNode);
		} else {
			Node updatedNode = nodeMap.get(task.getId());
			history.removeNode(updatedNode);
			updatedNode.setValue(task);
			history.linkLast(updatedNode);
		}
	}

	@Override
	public void remove(int id) {
		if (nodeMap.containsKey(id)) {
			Node deletedNode = nodeMap.get(id);
			history.removeNode(deletedNode);
			nodeMap.remove(id);
		}
	}

	private class MyOwnLinkedList {
		private Node head;
		private Node tail;

		private MyOwnLinkedList() {
			this.head = null;
			this.tail = null;
		}

		private void linkLast(Node newNode) {
			if (head == null){
				head = newNode;
			}
			if(tail != null) {
				tail.setNext(newNode);
				newNode.setPrev(tail);
			}
			tail = newNode;
		}

		private ArrayList<Task> getTasks(){
			ArrayList<Task> list = new ArrayList<>();
			Node curNode = head;
			while (curNode != null) {
				list.add(curNode.getValue());
				curNode = curNode.getNext();
			}
			return list;
		}

		private void removeNode(Node node){
			if (node == head) {
				head = node.getNext();
			}
			if (node == tail) {
				tail = node.getPrev();

			}
			if (node.getNext() != null) {
				node.getNext().setPrev(node.getPrev());
			}
			if (node.getPrev() != null) {
				node.getPrev().setNext(node.getNext());
			}
			node.setNext(null);
			node.setPrev(null);
		}
	}
}
