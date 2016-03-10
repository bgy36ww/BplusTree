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
    
        BPlusTree BT=new BPlusTree();
        BT.insert(0, "2");
        BT.insert(1, "4");
        BT.insert(2, "5");
        BT.insert(3, "7");
        BT.insert(4, "8");
        BT.insert(5, "9");   
        BT.insert(6, "10");
        BT.insert(7, "11");
        BT.insert(8, "12");
        BT.insert(9, "13");
        BT.insert(10, "14");
        BT.insert(11, "15");
        BT.insert(12, "16");
        
        System.out.println(((IndexNode)((IndexNode)(BT.root)).children.get(1)).keys);
   
    }    
}
