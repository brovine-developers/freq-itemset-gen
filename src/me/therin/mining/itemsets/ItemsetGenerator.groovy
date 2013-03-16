package me.therin.mining.itemsets

import me.therin.mining.itemsets.data.BasketIterator

/**
 * ItemsetGenerator.java
 * 
 * TODO: Class description
 *
 * @author tcirwin
 * @date March 08, 2013
 */
public abstract class ItemsetGenerator<Item> {
    protected BasketIterator<Item> baskets

    public ItemsetGenerator(BasketIterator<Item> baskets) { this.baskets = baskets }

    public abstract Map<List<Item>, Integer> getFrequentItemsets(double minSup);

    /**
     * Refreshes the generator to prepare for the next request. In other words,
     * this function will ensure that each client's response will not depend
     * on the responses previous
     *
     * @return whether the reset was successful
     */
    public abstract boolean reset()
}