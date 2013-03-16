package me.therin.mining.itemsets.fpgrowth

/**
 * me.therin.mining.itemsets.fpgrowth.Node.java
 *
 * A node of an FP tree
 *
 * @author tcirwin
 * @date February 10, 2013
 */
public class Node<Item> {
    private Node<Item> nextItem = null

    private List<Node<Item>> children
    private Node<Item> parent
    public final Item item
    private int count = 1
    
    Node(def item, def parent, def itemAcc) {
        def lastItem
        children = []
        this.parent = parent
        this.item = item

        if ((lastItem = itemAcc.get(item)) != null) {
            this.nextItem = lastItem
        }

        itemAcc.put(item, this)
    }

    int getCount() { count }
    void inc() { count++ }
    
    void addChild(Node child) { children = children + child }
    Node<Item> getChild(int num) { children[num] }
    List<Node> getChildren() { return children }

    Node<Item> getParent() { parent }
    void setParent(Node par) { parent = par }

    Item get() { return item }
    Node<Item> getNext() { return nextItem }
    
    Node findChild(def item) {
        for (def it : children) {
            if (it.item == item) return it
        }
        
        return null
    }

    Node findPrefixTree(Item item, def itemAcc) {
        int cnt = 0
        boolean cont = false
        Node n = null

        for (def child in children) {
            if (!child.item.equals(item)) {
                child = child.findPrefixTree(item, itemAcc)

                if (child != null) {
                    if (n == null) n = new Node(this.item, null, itemAcc)
                    n.addChild(child)
                    child.parent = n
                    cont = true
                }
                else continue;
            }
            else {
                if (n == null) n = new Node(this.item, null, itemAcc)
                cont = true
            }

            cnt += (child.count >= 0) ? child.count : 0;
        }

        if (cont) n.count = cnt
        return n
    }

    @Override
    String toString() { toStringHelper(0) }

    @Override
    int hashCode() {
        ((1 << 13) - 1) * (
          getCount() + (
            ((1 << 7) - 1) * (
              get().hashCode() + (
                ((1 << 5) - 1) * ((parent == null) ? 1 : parent.hashCode())
              ))))
    }

    String name() { String.format("0x%04X", hashCode() & 0x000FFFFF) }
    
    String toStringHelper(def space) {
        def str = ""
        
        for (def cool = space; cool > 0; cool--) { str += " " }
        str += "$item@${name()}" + ((nextItem != null) ? ">${nextItem.name()}" : "") +
          ": $count hits\n"
        
        for (def it : children) {
            str += it.toStringHelper(space + 3)
        }

        return str
    }
}
