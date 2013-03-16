package me.therin.mining.itemsets.apriori

import me.therin.mining.itemsets.ItemsetGenerator
import me.therin.mining.itemsets.data.BasketIterator

/**
 * Apriori.java
 * 
 * TODO: Class description
 *
 * @author TODO: tcirwin
 * @version TODO: the version
 * @date March 04, 2013
 */
public class Apriori<Item> extends ItemsetGenerator<Item> {

    Apriori(BasketIterator<Item> baskets) {
        super(baskets)
    }

    @Override
    Map<List<Item>, Integer> getFrequentItemsets(double minSup) {

    }

    @Override
    boolean reset() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }
}