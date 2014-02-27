package melb.mSafe.utilities;

import java.util.LinkedList;

/**
 * This LimitedLinkedList extends from a LinkedList and has a maximum capacity
 * If the list is full and another object is added, the first object of the list
 * will be removed
 * 
 * @author Daniel Langerenken
 * 
 * @param <E>
 *            - Item which should be stored in the LinkedList
 */
public class LimitedLinkedList<E> extends LinkedList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3315405812345932811L;

	private int maxSize;

	public LimitedLinkedList(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public synchronized boolean add(E object) {
		boolean success = super.add(object);
		while (this.size() >= maxSize) {
			removeFirst();
		}
		return success;
	}
}
