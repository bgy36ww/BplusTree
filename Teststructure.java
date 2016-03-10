/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Waver
 */
public class Teststructure {
    

    public static void main(String[] args) {
        int i=0;
        int k=0;
        Node tt=new LeafNode(i,k);
        System.out.println(((LeafNode)tt).values);
    
        BPlusTree BT=new BPlusTree();
        String value="2";
        BT.insert(0, "2");
        BT.insert(1, "4");
        BT.insert(2, "5");
        BT.insert(3, "7");
        BT.insert(4, "8");
        System.out.println(BT.search(2));
   
    }    
}
