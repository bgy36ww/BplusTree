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
            insetlistv.add(value);
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
                root=lnode;
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
                System.out.println(key+"compareto"+(K)N.keys.get(i)+"  result as "+(key.compareTo((K)N.keys.get(i))));
                i++;
            }
            if (N.isLeafNode){
            i--;}
            Entry<K,Node<K,T>> entry;
            if (!N.isLeafNode){
            //recur through the node
                entry=recinsert(key,value,(Node)((IndexNode)N).children.get(i),depth+1);
                
                if (entry!=null){
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
                       //
                       IndexNode newroot=(IndexNode)this.createInode(entry.getKey(), root);
                       newroot.children.add(entry.getValue());
                       root=newroot;
                       return null;
                   }
                }
             
            }else {
                   ((LeafNode)N).insertSorted(key, value);
                   if (N.isOverflowed()){
                   if (depth!=0){
                        entry=this.splitLeafNode((LeafNode)N);
                   }
                   else{
                       entry=this.splitLeafNode((LeafNode)N);
                       IndexNode newroot=(IndexNode)this.createInode(entry.getKey(), root);
                       newroot.children.add(entry.getValue());
                       root=newroot;
                   }
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
            int n=leaf.keys.size();
            LeafNode nLeaf=new LeafNode(leaf.keys.subList(D, n),leaf.values.subList(D, n));

            K tkey=leaf.keys.get(D);
            for (int i=D;i<n;i++){
            leaf.keys.remove(D);
            leaf.values.remove(D);
            }
            //System.out.println(tkey);
            Entry<K, Node<K,T>> reentry=new SimpleEntry<K,Node<K,T>>(tkey,nLeaf);
            return reentry;
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {
            int n=index.keys.size();
            IndexNode nindex=new IndexNode(index.keys.subList(D+1, n),index.children.subList(D+1, n));
            System.out.println(nindex.keys);
            K tkey=index.keys.get(D);
            for (int i=D;i<n;i++){
            index.keys.remove(i);
            index.children.remove(i);
            }
            Entry<K, Node<K,T>> reentry=new SimpleEntry<K,Node<K,T>>(tkey,nindex);
            return reentry;
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
