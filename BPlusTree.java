import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 * TODO: Rename to BPlusTree
 */
public class BPlusTree<K extends Comparable<K>, T> {

	public Node<K,T> root;
	public static final int D = 2;
        
	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public T search(K key) {
		// Search from root
        return tree_search(root, key);
    }

    public T tree_search(Node<K,T> startNode, K key) {
        // If the starting node is a leafNode
        if (startNode.isLeafNode) {
        	// Get the position of the key in the list of keys
        	int position = startNode.keys.indexOf(key);
        	if (position == -1) return null;
        	// Get its value from the list of values.
        	return (T)((LeafNode)startNode).values.get(position); 
        }
        
        // If not, find the right subtree to start the search.
        else {
        	// If the key is smaller than all of the keys in the current node, start searching from the leftmost child.
        	if (key.compareTo(startNode.keys.get(0)) < 0) {
        		return tree_search((Node<K,T>)((IndexNode)startNode).children.get(0), key);
        	}
        	// If the key is greater then all of the keys in the current node, start searching from the rightmost child.
        	else if (key.compareTo(startNode.keys.get(startNode.keys.size() - 1)) >= 0) {
        		return tree_search((Node<K,T>)((IndexNode)startNode).children.get(((IndexNode)startNode).children.size() - 1), key);
        	}
        	// Else, find the index i in the list of keys such that K(i) <= key < K(i+1), then search from children(i+1).
        	else {
        		ListIterator<K> iterator = startNode.keys.listIterator();
        		while (iterator.hasNext()) {
        			if (iterator.next().compareTo(key) > 0) {
        				int position = iterator.previousIndex();
        				return tree_search((Node<K,T>)((IndexNode)startNode).children.get(position), key);
        			}
        		}
        	}
        }
        return null;
    }

    /**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) {
		// If root does not exist, create a leaf node containing the key and the value.
		if (root == null) {
			root = new LeafNode(key, value);
		}
		else
			// Insert from root using the recursive method
			tree_insert(root, key, value);
		return;
	}
	
	public Entry<K, Node<K,T>> tree_insert(Node<K,T> startNode, K key, T value) {
		// If we are trying to insert starting from an index node
		if (!startNode.isLeafNode) {

			// Find the index i in the list of keys such that K(i) <= key < K(i+1)
			ListIterator<K> iterator = startNode.keys.listIterator();
			int insert_position = -1;
			if (key.compareTo(startNode.keys.get(0)) < 0) 
				insert_position = 0;
			else if (key.compareTo(startNode.keys.get(startNode.keys.size() - 1)) >= 0) 
				insert_position = startNode.keys.size();
			else {
				while (iterator.hasNext()) {
					if (iterator.next().compareTo(key) > 0) {
						insert_position = iterator.previousIndex() + 1;
						break;
					}
				}
			}
			// Insert starting from that child node. 
			Entry<K, Node<K,T>> indexToInsert = tree_insert((Node<K,T>)((IndexNode)startNode).children.get(insert_position), key, value);
			// If there are no index node coming form the bottom to insert, then we're done.
			if (indexToInsert == null) 
				return null;
			// Else, we need to recursively insert the index node coming from the bottom to the current index node.
			else {
				K index_key = indexToInsert.getKey();
				Node<K,T> index_childNode = indexToInsert.getValue();
				// Find the position to insert the key and insert it.
				if (index_key.compareTo(startNode.keys.get(0)) < 0) {
					startNode.keys.add(0, index_key);
					((IndexNode)startNode).children.add(1, index_childNode);
				}
				else if (index_key.compareTo(startNode.keys.get(startNode.keys.size() - 1)) > 0) {
					startNode.keys.add(index_key);
					((IndexNode)startNode).children.add(index_childNode);
				}
				else {
					ListIterator<K> key_iterator = startNode.keys.listIterator();
					while (key_iterator.hasNext()) {
						if (key_iterator.next().compareTo(index_key) > 0) {					
							startNode.keys.add(key_iterator.previousIndex(), index_key);
							((IndexNode)startNode).children.add(key_iterator.previousIndex() + 1, index_childNode);
							break;
						}
					}
				}
				// Now, check whether the starting index node has overflowed.
				if (!startNode.isOverflowed()) return null;
				else {
					// If it has overflowed, check whether the start node is the root node.
					if (startNode != root)
						// If not, just split the start node and return the index node to be pushed up.
						return splitIndexNode((IndexNode)startNode);
					else {
						// If it is the root node, split it and set the new index node to be the new root node.
						Entry<K, Node<K,T>> split_result = splitIndexNode((IndexNode)startNode);
						Node<K,T> right_child = split_result.getValue();
						K root_key = split_result.getKey();
						Node<K,T> new_root = new IndexNode(root_key, startNode, right_child);
						root = new_root;
						return null;
					}
				}
			}		
		}
			
		// If we are inserting directly into a leaf node
		else {
			((LeafNode)startNode).insertSorted(key, value);
			// Check if the node is overflowed. 
			if (startNode.isOverflowed()) {
				//If it is, check whether the start node is the root node.
				if (startNode != root) {
					return splitLeafNode((LeafNode)startNode);	
				}
				else {
					// If it is the root node, split it and set the new index node to be the new root node.
					Entry<K, Node<K,T>> split_result = splitLeafNode((LeafNode)startNode);
					Node<K,T> right_child = split_result.getValue();
					K root_key = split_result.getKey();
					Node<K,T> new_root = new IndexNode(root_key, startNode, right_child);
					root = new_root;
					return null;
				}
			}
			// If the node is not overflowed, return null and we're done.
			else return null;
		}
	}
	
        

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<splitingKey, RightNode>
	 * 
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {
		int n = leaf.keys.size();
		LeafNode right = new LeafNode(leaf.keys.subList(D, n),leaf.values.subList(D, n));
		K pushed_key = leaf.keys.get(D);
		for (int i = D; i < n; i++) {
			leaf.keys.remove(D);
			leaf.values.remove(D);
		}
		// Update the previous leaf pointers and next leaf pointers.
		if (leaf.nextLeaf != null) {
			leaf.nextLeaf.previousLeaf = right;
			right.nextLeaf = leaf.nextLeaf;
			leaf.nextLeaf = right;
			right.previousLeaf = leaf;
		}
		else {
			leaf.nextLeaf = right;
			right.previousLeaf = leaf;
		}
		Entry<K, Node<K,T>> pushed_entry = new SimpleEntry<K,Node<K,T>>(pushed_key, right);
		return pushed_entry;
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {
		int n = index.keys.size();
		int m = n + 1;
		IndexNode right = new IndexNode(index.keys.subList(D+1, n),index.children.subList(D+1, m));
		K pushed_key = index.keys.get(D);
		for (int i = D; i < n; i++) {
            index.keys.remove(D);
            }
		for (int i = D+1; i < m; i++){
            index.children.remove(D+1);
            }
		Entry<K, Node<K,T>> pushed_entry = new SimpleEntry<K,Node<K,T>>(pushed_key, right);
		return pushed_entry;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {
		// If there is only one key left, just set root to null.
		if (root.isLeafNode && root.keys.size() == 1) {
			root = null;
		}
		else {
			tree_delete(root, key);
			return;
		}
	}
	
	public boolean tree_delete(Node<K,T> startNode, K key) {
		// If we are deleting from a LeafNode
		if (startNode.isLeafNode) {
			// Search for the key and delete the key and the value from their lists respectively.
			int position = startNode.keys.indexOf(key);
			startNode.keys.remove(position);
			((LeafNode)startNode).values.remove(position);
		}
		
		// If we are deleting from an IndexNode
		else {
			// Find the index i in the list of keys such that K(i) <= key < K(i+1)
			ListIterator<K> iterator = startNode.keys.listIterator();
			int delete_position = -1;
			if (key.compareTo(startNode.keys.get(0)) < 0) 
				delete_position = 0;
			else if (key.compareTo(startNode.keys.get(startNode.keys.size() - 1)) >= 0) 
				delete_position = startNode.keys.size();
			else {
				while (iterator.hasNext()) {
					if (iterator.next().compareTo(key) > 0) {
						delete_position = iterator.previousIndex() + 1;
						break;
					}
				}
			}
			// We need to deal with situations where the child node is underflowed after deletion.
			if (tree_delete((Node<K,T>)((IndexNode)startNode).children.get(delete_position), key)) {
				// Check the siblings of the underflowed child and handle the underflow with the fullest sibling.
				Node<K,T> leftForBalance = null;
				Node<K,T> rightForBalance = null;
				int size_left = -1;
				int size_right = -1;
				
				if (delete_position == 0)
					rightForBalance = (Node<K,T>)((IndexNode)startNode).children.get(delete_position + 1);
				else if (delete_position == startNode.keys.size() - 1)
					leftForBalance = (Node<K,T>)((IndexNode)startNode).children.get(delete_position - 1);
				else {
					rightForBalance = (Node<K,T>)((IndexNode)startNode).children.get(delete_position + 1);
					leftForBalance = (Node<K,T>)((IndexNode)startNode).children.get(delete_position - 1);
				}
				if (rightForBalance != null)
					size_right = rightForBalance.keys.size();
				if (leftForBalance != null)
					size_left = leftForBalance.keys.size();
				
				// Compare and use the fullest sibling next to it.
				if (((Node<K,T>)((IndexNode)startNode).children.get(delete_position)).isLeafNode) {
					int merge_splitkey = -1;
					if (size_left > size_right) 
						merge_splitkey = handleLeafNodeUnderflow((LeafNode)((IndexNode)startNode).children.get(delete_position), (LeafNode)leftForBalance, (IndexNode)startNode);
					else 
						merge_splitkey = handleLeafNodeUnderflow((LeafNode)((IndexNode)startNode).children.get(delete_position), (LeafNode)rightForBalance, (IndexNode)startNode);
					// If a merge has happened rather than a redistribution, we need to delete the splitkey and the pointer to its left in the startNode.
					if (merge_splitkey != -1) {
						startNode.keys.remove(merge_splitkey);
						((IndexNode)startNode).children.remove(merge_splitkey);
					}
				}
				else {
					int merge_splitkey = -1;
					if (size_left > size_right)
						merge_splitkey = handleIndexNodeUnderflow((IndexNode)((IndexNode)startNode).children.get(delete_position), (IndexNode)leftForBalance, (IndexNode)startNode);
					else
						merge_splitkey = handleIndexNodeUnderflow((IndexNode)((IndexNode)startNode).children.get(delete_position), (IndexNode)rightForBalance, (IndexNode)startNode);
					if (merge_splitkey != -1) {
						startNode.keys.remove(merge_splitkey);
						((IndexNode)startNode).children.remove(merge_splitkey);
					}
				}
			}
		}
		return startNode.isUnderflowed();
	}

	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode<K,T> left, LeafNode<K,T> right,
			IndexNode<K,T> parent) {
		// Get the size of the nodes.
		int left_size = left.keys.size();
		int right_size = right.keys.size();
		
		// See if the size of the right node is large enough for redistribution.
		if (left_size + right_size >= 2 * D) {
			// We need to know which node is to the left in the tree.
			if (right.keys.get(0).compareTo(left.keys.get(0)) > 0) {
				int index = parent.keys.indexOf(right.keys.get(0));
				for (int i = 0; i < (D - left_size); i++) {
					left.keys.add(right.keys.get(0));
					right.keys.remove(0);
					left.values.add(right.values.get(0));
					right.values.remove(0);
				}
				parent.keys.remove(index);
				parent.keys.add(index, right.keys.get(0));
				return -1;
			}
			else {
				int index = parent.keys.indexOf(left.keys.get(0));
				for (int i = 0; i < (D - left_size); i++) {
					left.keys.add(0, right.keys.get(right.keys.size() - 1));
					right.keys.remove(right.keys.size() - 1);
					left.values.add(right.values.get(right.keys.size() - 1));
					right.values.remove(right.keys.size() - 1);
				}
				parent.keys.remove(index);
				parent.keys.add(index, left.keys.get(0));
				return -1;
			}			
		}
		// If redistribution is not possible, we need to merge the two nodes. Here we need to know the which node is to the left in the tree.
		else {
			if (right.keys.get(0).compareTo(left.keys.get(0)) > 0) {
				for (int i = 0; i < right_size; i++) {
					left.keys.add(right.keys.get(i));
					left.values.add(right.values.get(i));
				}
				// Set the previous and next pointers.
				right.nextLeaf.previousLeaf = left;
				left.nextLeaf = right.nextLeaf;
				// Return the index of the key corresponding to the first key in the node to the right.
				int index = parent.keys.indexOf(right.keys.get(0));
				return index;
			}
			else {
				for (int i = 0; i < left_size; i++) {
					right.keys.add(left.keys.get(i));
					right.values.add(left.values.get(i));
				}
				left.nextLeaf.previousLeaf = right;
				right.nextLeaf = left.nextLeaf;
				int index = parent.keys.indexOf(left.keys.get(0));
				return index;
			}			
		}
	}
		
	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode<K,T> leftIndex,
			IndexNode<K,T> rightIndex, IndexNode<K,T> parent) {
		// Get the size of the nodes.
		int left_size = leftIndex.keys.size();
		int right_size = rightIndex.keys.size();
		int parent_size = parent.keys.size();
		
		// See if a redistribution is possible.
		if (left_size + right_size >= 2 * D) {
			if (rightIndex.keys.get(0).compareTo(leftIndex.keys.get(0)) > 0) {
				// First we need to find the index of the largest key that is smaller than the first key in the node to the right.
				int index = -1;
				for (int i = 0; i < parent_size; i++) {
					if (parent.keys.get(i).compareTo(rightIndex.keys.get(0)) > 0) {
						index = i - 1;
						break;
					}
				}
				if (index == -1) index = parent_size - 1;
				// Add that key in the parent node to the child node to the left.
				leftIndex.keys.add(parent.keys.get(index));
				// Add keys in the right child node to the left child node.
				for (int i = 0; i < (D - left_size - 1); i++) {
					leftIndex.keys.add(rightIndex.keys.get(0));
					rightIndex.keys.remove(0);
					leftIndex.children.add(rightIndex.children.get(0));
					rightIndex.children.remove(0);
				}
				leftIndex.children.add(rightIndex.children.get(0));
				parent.keys.remove(index);
				parent.keys.add(index, rightIndex.keys.get(0));
				rightIndex.keys.remove(0);
				rightIndex.children.remove(0);
				return -1;
			}
			else {
				int index = -1;
				for (int i = 0; i < parent_size; i++) {
					if (parent.keys.get(i).compareTo(leftIndex.keys.get(0)) > 0) {
						index = i - 1;
						break;
					}
				}
				if (index == -1) index = parent_size - 1;
				leftIndex.keys.add(0, parent.keys.get(index));
				for (int i = 0; i < (D - left_size - 1); i++) {
					leftIndex.keys.add(0, rightIndex.keys.get(rightIndex.keys.size() - 1));
					rightIndex.keys.remove(rightIndex.keys.size() - 1);
					leftIndex.children.add(0, rightIndex.children.get(rightIndex.children.size() - 1));
					rightIndex.children.remove(rightIndex.children.size() - 1);
				}
				leftIndex.children.add(0, rightIndex.children.get(rightIndex.children.size() - 1));
				parent.keys.remove(index);
				parent.keys.add(index, rightIndex.keys.get(rightIndex.keys.size() - 1));
				rightIndex.keys.remove(rightIndex.keys.size() - 1);
				rightIndex.children.remove(rightIndex.children.size() - 1);
				return -1;
			}
		}
		else {
			// If a redistribution is not possible, we need to merge the two IndexNodes.
			if (rightIndex.keys.get(0).compareTo(leftIndex.keys.get(0)) > 0) {
				// First we need to find the index of the largest key that is smaller than the first key in the node to the right.
				int index = -1;
				for (int i = 0; i < parent_size; i++) {
					if (parent.keys.get(i).compareTo(rightIndex.keys.get(0)) > 0) {
						index = i - 1;
						break;
					}
				}
				if (index == -1) index = parent_size - 1;
				// Then we need to add that key into the child node to the left.
				leftIndex.keys.add(parent.keys.get(index));
				// Then, add all the keys in the right child node to the left child node for merging.
				for (int j = 0; j < right_size; j++) {
					leftIndex.keys.add(rightIndex.keys.get(j));
					leftIndex.children.add(rightIndex.children.get(j));
				}
				return index;
			}
			else {
				int index = -1;
				for (int i = 0; i < parent_size; i++) {
					if (parent.keys.get(i).compareTo(leftIndex.keys.get(0)) > 0) {
						index = i - 1;
						break;
					}
				}
				if (index == -1) index = parent_size - 1;
				rightIndex.keys.add(parent.keys.get(index));
				for (int j = 0; j < left_size; j++) {
					rightIndex.keys.add(leftIndex.keys.get(j));
					rightIndex.children.add(leftIndex.children.get(j));
				}
				return index;
			}
		}		
	}

}