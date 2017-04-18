package editor;

import javafx.scene.text.Text;
import javafx.scene.Group;

class UndoRedo {
	private class Node {
		private LinkedList.Node textNode;
		private int action;
		private LinkedList.Node savedLocation;
		private Node prev;
		private Node next;

		Node(LinkedList.Node t, int a, LinkedList.Node s, Node p, Node n) {
			textNode = t;
			action = a;
			savedLocation = s;
			prev = p;
			next = n;
		}
	}

	static final int REMOVE = 0;
	static final int INSERT = 1;
	static final int MAXSIZE = 100;

	private Node frontSentinel;
	private Node backSentinel;
	private int size;
	private int position;
	private Node currentNode;

	private LinkedList<Text> text;
	private Group root;

	public UndoRedo(LinkedList<Text> t, Group root) {
		size = 0;
		position = 0;
		frontSentinel = new Node(null, -1, null, null, null);
		backSentinel = new Node(null, -1, null, null, null);
		frontSentinel.next = backSentinel;
		backSentinel.prev = frontSentinel;
		currentNode = frontSentinel;
		text = t;
		this.root = root;
	}

	public void add(LinkedList.Node t, int a, LinkedList.Node s) {
		position += 1;
		if (position > 100) {
			deleteFront();
			position = 100;
		}
		Node newNode = new Node(t, a, s, currentNode, backSentinel);
		currentNode.next = newNode;
		backSentinel.prev = newNode;
		currentNode = newNode;
		size = position;
	}

	public Text undo() {
		Text rv = null;
		if (position > 0) {
			text.setCursorNode(currentNode.savedLocation);
			int tempAction = 1 - currentNode.action;
			if (tempAction == REMOVE) {
				text.removeNode();
				root.getChildren().remove((Text) currentNode.textNode.item);
			} else {
				text.insertNode(currentNode.textNode);
				root.getChildren().add((Text) currentNode.textNode.item);
				rv = (Text) currentNode.textNode.item;
			}
			position -= 1;
			currentNode = currentNode.prev;
		}
		return rv;
	}

	public Text redo() {
		Text rv = null;
		if (position < size) {
			position += 1;
			currentNode = currentNode.next;
			text.setCursorNode(currentNode.savedLocation);
			if (currentNode.action == REMOVE) {
				text.removeNode();
				root.getChildren().remove((Text) currentNode.textNode.item);
			} else {
				text.insertNode(currentNode.textNode);
				root.getChildren().add((Text) currentNode.textNode.item);
				rv = (Text) currentNode.textNode.item;
			}
		}
		return rv;
	}

	private void deleteFront() {
		Node newFront = frontSentinel.next.next;
		frontSentinel.next = newFront;
		newFront.prev = frontSentinel;
	}
}
