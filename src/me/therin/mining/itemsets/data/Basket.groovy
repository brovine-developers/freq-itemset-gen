package me.therin.mining.itemsets.data

/**
 * Basket.java
 * 
 * TODO: Class description
 *
 * @author TODO: tcirwin
 * @version TODO: the version
 * @date 03 09, 2013
 */
public class Basket<Item> {
    // The immutable name of this basket
    public final String name;
    // The items that are in this basket
    protected List<Item> items;

    public Basket(String name) { this(name, new ArrayList<Item>()); }

    public Basket(String name, List<Item> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() { return name }
    public List<Item> getItems() { return items }
    public void add(Item t) { items.add(t); }
}