import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.ArrayList;

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
                Node ptr=this.searchnode(key);
                int i=this.getposition(key, ptr);
                
                if (key!=ptr.keys.get(i)){
                    return null;
                }
		return (T)((LeafNode)ptr).values.get(i);
	}

        public int getposition(K key,Node N)
        {
                int i=0;
                while ((i<N.keys.size())&&(key.compareTo((K)N.keys.get(i))>0)){
                    i++;
                }
                return i;
        }
        
        public Node searchnode(K key) {
                Node ptr= root;
                int i;
                while (!ptr.isLeafNode){
                    i=0;
                    while ((i<ptr.keys.size())&&(key.compareTo((K)ptr.keys.get(i))>0)){
                        i++;
                    }
                        ptr=(Node)((IndexNode)ptr).children.get(i);
                }
            return ptr;
        }
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) {
            if (root==null){
                ArrayList<K> insetlist= new ArrayList<K>();
                ArrayList<T> insetlistv= new ArrayList<T>();
                insetlist.add(key);
                Node childnode=new LeafNode(insetlist,insetlistv);
                ArrayList<Node<K,T>> childlist=new ArrayList<Node<K,T>>();
                childlist.add(childnode);
                root=new IndexNode(insetlist,childlist);
            }else{
                Node ptr=this.searchnode(key);
                int i=this.getposition(key, ptr);
                
            }
            

            
	}

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
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
