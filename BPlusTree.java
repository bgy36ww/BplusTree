import java.util.AbstractMap;
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
        	// Get its value from the list of values.
        	return (T)((LeafNode)startNode).values.get(position);  // Corrected 3/9/16
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
        	// Else, find the index i in the list of keys such that K(i) <= key < K(i+1), then search from children(i).
        	else {
        		ListIterator<K> iterator = ((IndexNode)startNode).keys.listIterator();
        		while (iterator.hasNext()) {
        			if (iterator.next().compareTo(key) > 0) {
        				int position = iterator.previousIndex() + 1;
        				return tree_search((Node<K,T>)((IndexNode)startNode).children.get(position), key);
        			}
        		}
        	}
        }
        return null;
    }




        //method create indexnode
        public Node createInode(K key, Node lnode){
            ArrayList<K> insetlist= new ArrayList<K>();
            insetlist.add(key);
            ArrayList<Node<K,T>> childlist=new ArrayList<Node<K,T>>();
            childlist.add(lnode);
            Node newindexnode=new IndexNode(insetlist,childlist);
            return newindexnode;
        }
        //create leafnode function
        //should I change the Node into more specific type?
        public Node createLnode(K key, T value){
            ArrayList<K> insetlist= new ArrayList<K>();
            ArrayList<T> insetlistv= new ArrayList<T>();
            insetlist.add(key);
            Node childnode=new LeafNode(insetlist,insetlistv);
            return childnode;
        }
        /**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) {
        //Check if the root has been created or not.
            if (root==null){
        //create root node and first leaf
        //set up root node
                Node lnode=createLnode(key,value);
                
                root=this.createInode(key, lnode);
            }
            
            else{
        //find the right position and insert
        //with recursive method
                recinsert(key,value,root,0);
            }
            
            

            
	}
        
        public Entry<K, Node<K,T>> recinsert(K key, T value, Node N, int depth){
            
            int i=0;
            while ((i<N.keys.size())&&(key.compareTo((K)N.keys.get(i))>0)){
                i++;
            }
            Entry<K,Node<K,T>> entry;
            if (!N.isLeafNode){
            //recur through the node
                entry=recinsert(key,value,(Node)((IndexNode)N).children.get(i),depth+1);
                
                if (entry!=null){
                //find node and merge them
                //what should i be?
                //may need to debug it later
                //May need to conside about the root node
                    ((IndexNode)N).insertSorted(entry, i);
                }
                //check overflow and return
                if (N.isOverflowed()){

                   if (depth!=0){
                   entry=this.splitIndexNode((IndexNode)N);

                   return entry;
                   }else
                   {
                       entry=this.splitIndexNode((IndexNode)N);
                       IndexNode newroot=(IndexNode)this.createInode(entry.getKey(), entry.getValue());
                       //maybe I should do this according to the add format
                       //need to revisit here and test.
                       newroot.children.add(root);
                       //is this wrong?
                       root=newroot;
                       return null;
                   }
                }
             
            }else {
                   ((LeafNode)N).insertSorted(key, value);
                   if (N.isOverflowed()){
                   entry=this.splitLeafNode((LeafNode)N);
                   ((LeafNode)N).nextLeaf=(LeafNode)entry.getValue();
                   ((LeafNode)entry.getValue()).previousLeaf=((LeafNode)N);
                   return entry;
                   }
            }
            
            return null;
        }


	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<splitingKey, RightNode>
	 * 
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {

		return null;
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {

		return null;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {

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
		return -1;

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
		return -1;
	}

}
