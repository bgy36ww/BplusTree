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
            //check for special case. Since leave node does not have m+1 values.
            if (N.isLeafNode){
            i--;}
            //create return variable
            Entry<K,Node<K,T>> entry;
            if (!N.isLeafNode){
            //recur through the node
                entry=recinsert(key,value,(Node)((IndexNode)N).children.get(i),depth+1);
                
                if (entry!=null){
                    ((IndexNode)N).insertSorted(entry, i);
                }
                //check overflow and return
                if (N.isOverflowed()){
                    //check if it's the root node
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
                    //if reached leaf node
                    //insert key and value
                    ((LeafNode)N).insertSorted(key, value);
                   //check overflow
                   //return entry if node has been split
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
            //create new node
            LeafNode nLeaf=new LeafNode(leaf.keys.subList(D, n),leaf.values.subList(D, n));
            //get the key for parent node
            K tkey=leaf.keys.get(D);
            //remove excess node
            for (int i=D;i<n;i++){
            leaf.keys.remove(D);
            leaf.values.remove(D);
            }
            //connect nodes
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
            //create return entry
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
            //create new index node for return
            IndexNode nindex=new IndexNode(index.keys.subList(D+1, n),index.children.subList(D+1, m));
            K tkey=index.keys.get(D);
            //delete copied node
            for (int i=D;i<n;i++){
            index.keys.remove(D);
            }
            for (int i=D+1;i<m;i++){
            index.children.remove(D+1);
            }
            //create return node
            Entry<K, Node<K,T>> reentry=new SimpleEntry<K,Node<K,T>>(tkey,nindex);

            return reentry;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {
              //using recursion function here
              recdelete(key,root);
              //default if no root just do nothing
              if (root!=null){
              if (root.keys.size()==0){
              	root=(Node)((IndexNode)root).children.get(0);
              }}
              
	}
        
        public boolean recdelete(K key, Node N){
            int i=0;
            //find the position to delete
            i=this.binarysearch(N.keys, key);
            //help on leafnode
            if (N.isLeafNode){
            i--;
            if (key.compareTo((K)N.keys.get(i))==0){
            //found node to delete
            N.keys.remove(i);
            ((LeafNode)N).values.remove(i);
            }

            }else{
                //go into index node and continue finding the node to delete
            	if (recdelete(key, ((Node)((IndexNode)N).children.get(i)))){
            		Node Secnode;
            		Node Node1=null;
            		Node Node2=null;
                        //create three nodes for relocation if one node is underflow
            		int size1=-1;
            		int size2=-1;
                        //obtain left node if and only if there is a left node
            		if (i>0){
            			Node1=((Node)((IndexNode)N).children.get(i-1));
            			size1=Node1.keys.size();
            		}
                        int k=0;
                        if (((Node)(((IndexNode)N).children.get(i))).isLeafNode){k=1;}
            		//obtain right node if and only if there is a right node, also use k to help positon the pointer if the children is leafnode
                        if (i<N.keys.size()-k){
            			Node2=((Node)((IndexNode)N).children.get(i+1));
            			size2=Node2.keys.size();
            		}
                        //determine which node has more nodes to redistribute
            		Secnode=size1>size2?Node1:Node2;
                        //do leaf redistribute if children is leaf node
            		if (((Node)(((IndexNode)N).children.get(i))).isLeafNode){
            			int pos=-1;
                                //determine left and right
            			if (size1>size2){
            			pos=this.handleLeafNodeUnderflow((LeafNode)Secnode,(LeafNode)(((IndexNode)N).children.get(i)),(IndexNode)N);
            			}else{
            			pos=this.handleLeafNodeUnderflow((LeafNode)(((IndexNode)N).children.get(i)),(LeafNode)Secnode,(IndexNode)N);
            			}
                                
            			if (pos!=-1){
                                    ((IndexNode)N).children.remove(pos+1);
                                    ((IndexNode)N).keys.remove(pos);
                                }	
            			}
                        //do index node redistribute otherwise
            		else{
            			int pos=-1;
                                //determine left and right
            			if (size1>size2){
            			pos=this.handleIndexNodeUnderflow((IndexNode)Secnode,(IndexNode)(((IndexNode)N).children.get(i)),(IndexNode)N);
            			}else{
            			pos=this.handleIndexNodeUnderflow((IndexNode)(((IndexNode)N).children.get(i)),(IndexNode)Secnode,(IndexNode)N);
            			}
                                
                                if (pos!=-1){
                                    ((IndexNode)N).children.remove(pos+1);
                                    ((IndexNode)N).keys.remove(pos);
                                }
            		}
            		
            		
            	}
            }
            //return current node status, if underflow, the parent will do handle
            return N.isUnderflowed();
        }

        //search tree for finding the right node
        public int binarysearch(ArrayList<K> keys, K dkey ){
            int s=0;
            int ed=keys.size()-1;
            if (dkey.compareTo(keys.get(s))<0){
                return s;
            }else if (dkey.compareTo(keys.get(ed))>=0){
                return ed;
            }
            while (s<=ed){
                //key is inside s and ed
                int mid= s+(ed-s)/2;
                if (dkey.compareTo(keys.get(mid))<0) ed=mid-1;
                else if (dkey.compareTo(keys.get(mid))>0) s=mid+1;
                else return mid;
                
            }
            return -1;
            
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
                //position of the subnode in upper node
                int pos;
                //where should the transfer begin on right node
                pos=this.binarysearch(parent.keys, right.keys.get(0));
                pos--;
                //take record of three nodes properties for later uses
		int l1=left.values.size();
		int l2=right.values.size();
		int p1=parent.keys.size();
                //if not enough keys on left and right
		if (l1+l2<2*D){
                    
			K tkey=parent.keys.get(pos);
                        //pull down and redistribute nodes
			for (int i=0;i<l2;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.values.add(right.values.get(0));
				right.values.remove(0);
			}
			left.nextLeaf=right.nextLeaf;
                        right.nextLeaf.previousLeaf=left;
			right=null;
                        return pos;
		}else{
                    //if there is enough keys, do redistribution
                    //detemine direction of the redistribution
                        if (l2>l1){
			int numtomove=D-l1;
			for (int i=0;i<numtomove;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.values.add(right.values.get(0));
				right.values.remove(0);
			}
                        }
                        else{
                        int numtomove=D-l2;
                        for (int i=0;i<numtomove;i++){
				right.keys.add(0,left.keys.get(left.keys.size()-1));
				left.keys.remove(left.keys.size()-1);
				right.values.add(0,left.values.get(left.values.size()-1));
				left.values.remove(left.values.size()-1);
			}    
                                }
                        //modified parent node with the correct keys
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
			IndexNode<K,T> right, IndexNode<K,T> parent) {
                int pos=0;
                //where should the transfer begin on right node
                if (right!=null){
                pos=this.binarysearch(parent.keys, right.keys.get(0));
                pos--;
                }
		int l1=0;
                //make sure there exists a node on left or right
                if (left!=null){
                    l1=left.keys.size();
                }
                int l2=0;
                if (right!=null){
                    l2=right.keys.size();
                }
                //if there is not enough keys on left and right
		if (l1+l2<2*D){
                        //move keys from left to right
			left.keys.add(parent.keys.get(pos));
			for (int i=0;i<l2;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.children.add(right.children.get(0));
				right.children.remove(0);
			}
                        if (l2>0){
			left.children.add(right.children.get(0));
			right.children.remove(0);}
			//destory right
			right=null;
                        //give parent position to delete the key and children
                        return pos;
		}else{
                    //if there is enough keys
                    //detemine redistribution direction
                        if (l2>l1){
                        left.keys.add(parent.keys.get(pos));
			int numtomove=D-l1;
			for (int i=0;i<numtomove;i++){
				left.keys.add(right.keys.get(0));
				right.keys.remove(0);
				left.children.add(right.children.get(0));
				right.children.remove(0);
			}
                        parent.keys.remove(pos);
			parent.keys.add(pos,left.keys.get(left.keys.size()));
                        left.keys.remove(left.keys.size());
                        }
                        else{
                        right.keys.add(0,parent.keys.get(pos));
                        int numtomove=D-l2;
                        for (int i=0;i<numtomove;i++){
				right.keys.add(0,left.keys.get(left.keys.size()-1));
				left.keys.remove(left.keys.size()-1);
				right.children.add(0,left.children.get(left.children.size()-1));
				left.children.remove(left.children.size()-1);
			}    
                        //modified parent node with the correct key
                        parent.keys.remove(pos);
			parent.keys.add(pos,right.keys.get(0));
                        right.keys.remove(0);
                                }

                        return -1;
                        
		}
		
		
	}

}
