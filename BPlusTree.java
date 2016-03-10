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
        	System.out.println("Leaf");
        	// Get the position of the key in the list of keys
        	int position = startNode.keys.indexOf(key);
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
        //Check if the root has been created or not.
            if (root==null){
        //create root node and first leaf
        //set up root node
                Node lnode=new LeafNode(key,value);
                root=lnode;
            }
            else{
        //find the right position and insert
        //with recursive method
                recinsert(key,value,root,0);   
            }            
	}
        
        public Entry<K, Node<K,T>> recinsert(K key, T value, Node N, int depth){
            
            //find position
            int i=0;
            while ((i<N.keys.size())&&(key.compareTo((K)N.keys.get(i))>=0)){
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

                       IndexNode newroot=new IndexNode(entry.getKey(), root, entry.getValue());

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
                       IndexNode newroot=new IndexNode(entry.getKey(), root, entry.getValue());
                       root=newroot;
                   }

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
            if (leaf.nextLeaf != null) {
            leaf.nextLeaf.previousLeaf = nLeaf;
            nLeaf.nextLeaf = leaf.nextLeaf;
            leaf.nextLeaf = nLeaf;
            nLeaf.previousLeaf = leaf;
            }
            else {
            leaf.nextLeaf=nLeaf;
            nLeaf.previousLeaf=leaf;
            }
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
            int m=index.children.size();
 
            IndexNode nindex=new IndexNode(index.keys.subList(D+1, n),index.children.subList(D+1, m));
            K tkey=index.keys.get(D);
            for (int i=D;i<n;i++){
            index.keys.remove(D);
            }
            for (int i=D+1;i<m;i++){
            index.children.remove(D+1);
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
              
              recdelete(key,root);
              if (root.keys.size()==0){
              	root=(Node)((IndexNode)root).children.get(0);
              }
	}
        
        public boolean recdelete(K key, Node N){
            int i=0;
            while ((i<N.keys.size())&&(key.compareTo((K)N.keys.get(i))>=0)){
                i++;
            }
            
            if ((N.isLeafNode)&&(key.compareTo((K)N.keys.get(i))==0)){
            //found node to delete
            N.keys.remove(i);
            ((LeafNode)N).values.remove(i);
            

            }else{
            	if (recdelete(key, ((Node)((IndexNode)N).children.get(i)))){
            		Node Secnode;
            		Node Node1=null;
            		Node Node2=null;
            		int size1=-1;
            		int size2=-1;
            		if (i>0){
            			Node1=((Node)((IndexNode)N).children.get(i-1));
            			size1=Node1.keys.size();
            		}
            		if (i<N.keys.size()-1){
            			Node2=((Node)((IndexNode)N).children.get(i+1));
            			size2=Node2.keys.size();
            		}
            		Secnode=size1>size2?Node1:Node2;
            		if (((Node)(((IndexNode)N).children.get(i))).isLeafNode){
            			int pos=-1;
            			if (size1>size2){
            			pos=this.handleLeafNodeUnderflow((LeafNode)Secnode,(LeafNode)(((IndexNode)N).children.get(i)),(IndexNode)N,i);
            			}else{
            			pos=this.handleLeafNodeUnderflow((LeafNode)(((IndexNode)N).children.get(i)),(LeafNode)Secnode,(IndexNode)N,i);
            			}
            			
            				
            			}
            		else{
            			int pos=-1;
            			if (size1>size2){
            			pos=this.handleIndexNodeUnderflow((IndexNode)Secnode,(IndexNode)(((IndexNode)N).children.get(i)),(IndexNode)N,i);
            			}else{
            			pos=this.handleIndexNodeUnderflow((IndexNode)(((IndexNode)N).children.get(i)),(IndexNode)Secnode,(IndexNode)N,i);
            			}
            		}
            		
            		
            	}
            }
            return N.isUnderflowed();
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
			IndexNode<K,T> parent, int pos) {
		
		int l1=left.values.size();
		int l2=right.values.size();
		int p1=parent.keys.size();
		if (l1+l2<2*D){
			parent.children.remove(pos+1);
			K tkey=parent.keys.get(pos);
			parent.keys.remove(pos);
			for (int i=0;i<l2;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.values.add(right.values.get(0));
				right.values.remove(0);
			}
			left.nextLeaf=right.nextLeaf;
			right=null;
		}else{
			int numtomove=D-l1;
			for (int i=0;i<numtomove;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.values.add(right.values.get(0));
				right.values.remove(0);
			}
			parent.keys.remove(pos);
			parent.keys.add(pos,right.keys.get(0));
		}
		
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
	public int handleIndexNodeUnderflow(IndexNode<K,T> left,
			IndexNode<K,T> right, IndexNode<K,T> parent, int pos) {
		int l1=left.keys.size();
		int l2=right.keys.size();
		int p1=parent.keys.size();
		if (l1+l2<2*D){
			parent.children.remove(pos+1);
			K tkey=parent.keys.get(pos);
			parent.keys.remove(pos);
			left.keys.add(tkey);
			for (int i=0;i<l2;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.children.add(right.children.get(0));
				right.children.remove(0);
			}
			left.children.add(right.children.get(0));
			right.children.remove(0);
			
			right=null;
		}else{
			int numtomove=D-l1;
			left.keys.add(parent.keys.get(pos));
			
			
			for (int i=0;i<numtomove;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.children.add(right.children.get(0));
				right.children.remove(0);
			}
			parent.keys.remove(pos);
			parent.keys.add(pos,right.keys.get(0));
		}
		
		
		return -1;
	}

}
