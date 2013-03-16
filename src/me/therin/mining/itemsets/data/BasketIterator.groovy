package me.therin.mining.itemsets.data

/**
 * BasketIterator.java
 * 
 * TODO: Class description
 *
 * @author TODO: tcirwin
 * @version TODO: the version
 * @date 03 09, 2013
 */
public interface BasketIterator<Item> extends Iterator<Basket<Item>> {
    /**
     * Gets the list of items possible in the baskets in ascending order
     *
     * @return list of possible basket items in ascending order
     */
    public List<Item> getUniqueItems()

    /**
     * Returns a new BasketIterator containing only the baskets listed
     *
     * @param baskets the baskets to include
     * @return a new BasketIterator
     */
    public BasketIterator<Item> includeOnly(List<Basket<Item>> baskets)

    /**
     * Forces the iterator to update any data sources it is using
     *
     * @return whether the update was successful (regardless of whether
     * anything was actually changed)
     */
    public boolean update()

    /**
     * Refreshes the basket to prepare for the next request. In other words,
     * this function will ensure that each client's response will not depend
     * on the responses previous
     *
     * @return whether the reset was successful
     */
    public boolean reset()

    /**
     * Returns the number of baskets available
     *
     * @return the total number of baskets
     */
    public int size()
}