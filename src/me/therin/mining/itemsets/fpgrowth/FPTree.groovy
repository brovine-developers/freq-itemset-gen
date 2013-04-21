package me.therin.mining.itemsets.fpgrowth

import me.therin.mining.itemsets.ItemsetGenerator
import me.therin.mining.itemsets.data.BasketIterator

/**
 * me.therin.mining.itemsets.FPGrowth.FPTree.java
 * 
 * An FP tree
 *
 * @author tcirwin
 * @date February 10, 2013
 */
public class FPTree<Item> extends ItemsetGenerator<Item> {
    private def itemAcc = new HashMap<Item, Node<Item>>()
    private def root = new Node(-1, null, itemAcc)
    private def suffix = new LinkedList<Item>()
    private int minSup
    private int maxSup
    private boolean first = true

    FPTree(BasketIterator<Item> baskets) {
        super(baskets)
        baskets.each { addToTree(root, (List<Item>) it.items.reverse().clone(), itemAcc) }
        first = true
    }

    FPTree(BasketIterator<Item> baskets, boolean bo) {
        super(baskets)
        first = false
    }

    Node getRoot() { return root }

    static void addToTree(Node tree, List items, def itemAcc) {
        if (!items.empty) {
            def item = items.pop()
            def child = null

            if ((child = tree.findChild(item)) != null) {
                child.inc()
                addToTree(child, items, itemAcc)
            }
            else {
                def node = new Node(item, tree, itemAcc)
                tree.addChild(node)
                addToTree(tree.getChild(-1), items, itemAcc)
            }
        }
    }

    @Override
    Map<List<Item>, Integer> getFrequentItemsets(double supPct, double maxPct) {
        //System.err.println((int) Math.round(supPct * baskets.size()))
        this.minSup = (int) Math.round(supPct * baskets.size())
        this.maxSup = (int) Math.round(maxPct * baskets.size())

        // If we need to trim high-count elements, call copyTree() to clone this,
        // which calls removeInfrequent() on the cloned tree
        def tree = (maxPct < 1) ? copyTree() : this;

        tree.getFrqItm(baskets.getUniqueItems().reverse(), minSup, maxSup)
    }

    private Map<List<Item>, Integer> getFrqItm(List<Item> reverse, int minSup, int maxSup) {
        Map<List<Item>, Integer> lists = new HashMap()

        if (!first) {
            if (root.count >= minSup && root.count <= maxSup) {
                lists.put(suffix, root.count)
            }
            else return lists
        }

        if (root.children.size() < 1)
            return lists

        for (int i = 0; i < reverse.size(); i++) {
            def item = reverse.get(i)
            def list = (reverse.size() > 1) ?
              reverse.subList(i + 1, reverse.size()) : new ArrayList<Item>()
            def tree = this.getPrefixTree(item)

            if (tree != null)
                lists.putAll(tree.getFrqItm(list, minSup, maxSup))
        }

        return lists
    }

    FPTree<Item> copyTree() {
        def tree = this.clone()
        tree.itemAcc = new HashMap<Item, Node<Item>>()

        // Clone the suffix list
        tree.suffix = this.suffix.clone()

        tree.root = tree.root.copyTree(tree.itemAcc)
        tree.removeInfrequent()

        //println(tree.toString() + ": " + tree.root.count)
        return tree
    }

    FPTree<Item> getPrefixTree(Item item) {
        def tree = this.clone()
        tree.itemAcc = new HashMap<Item, Node<Item>>()

        // Clone the suffix list and add the new item on it
        tree.first = false
        tree.suffix = this.suffix.clone()
        if (item != null)
            tree.suffix.add(0, item)

        if (itemAcc.containsKey(item) || item == null) {
            tree.root = tree.root.findPrefixTree(item, tree.itemAcc)
            if (tree.root == null) tree.root = new Node(-1, null, new HashMap<Item, Node<Item>>())
            tree.removeInfrequent()
        }
        else {
            tree = null
        }

        //println(tree.toString() + ": " + tree.root.count)
        return tree
    }

    private void removeInfrequent() {
        for (Node<Item> i in itemAcc.values()) {
            int cnt = 0
            def cur = i
            while (cur != null) {
                cnt += cur.count
                cur = cur.next
            }

            if ((cnt < minSup || cnt > maxSup) && i.item != -1) {
                while (i != null) {
                    if (i.parent != null)
                        i.parent.children.remove(i)

                    for (child in i.children) {
                        child.parent = i.parent
                        i.parent.addChild(child)
                    }

                    i = i.next
                }
            }
        }
    }

    def preTraverse = { it, agg, act, lvl = 0, cur = "" ->
        cur = agg(cur, act(it, lvl))

        it.getChildren().each { child ->
            cur = preTraverse(child, agg, act, lvl + 1, cur)
        }

        return cur
    }

    String serialize() {
        def agg = { tot, add -> return tot + add }

        def act = { it, lvl ->
            def next = it.nextItem != null ? "[${it.nextItem.get()},${it.getCount()}]" : ""
            return "$lvl:${it.get()},${it.getCount()}${next};"
        }

        return preTraverse(root, agg, act)
    }

    String getLinkedItems() {
        def lists = ""
        itemAcc.each {
            def item = it.value;
            lists += "${it.key}: "

            while (item != null) {
                lists += "${item.name()}, "
                item = item.getNext()
            }

            lists += "\n"
        }

        return lists
    }

    @Override
    public boolean reset() {
        itemAcc = new HashMap<Item, Node<Item>>()
        root = new Node(-1, null, itemAcc)
        suffix = new LinkedList<Item>()
        minSup = -1

        baskets.reset()
        baskets.each { addToTree(root, (List<Item>) it.items.reverse(), itemAcc) }
        return true
    }

    /**
     * This gives you a shallow copy of the tree
     *
     * @return a shallow copy of `this`
     */
    @Override
    FPTree<Item> clone() {
        def tree = new FPTree<Item>(baskets, false)

        tree.root = this.root
        tree.itemAcc = this.itemAcc
        tree.suffix = this.suffix
        tree.maxSup = this.maxSup
        tree.minSup = this.minSup
        tree.first = this.first

        return tree
    }

    @Override
    String toString() { return root.toString() }
}
