package me.therin.mining.itemsets.apriori

import me.therin.mining.itemsets.ItemsetGenerator
import me.therin.mining.itemsets.data.BasketIterator

/**
 * Apriori.java
 * 
 * Implementation of the Apriori frequent itemset mining algorithm in Java.
 * Specific implementation is by Agarwal, R. et. al. "Mining Association Rules between
 * Sets of Items in Large Databases." in Proc. ACM SIGMOD. June 1993, 22(2):207-16.
 *
 * @author tcirwin
 * @date March 04, 2013
 */
public class Apriori<Item> extends ItemsetGenerator<Item> {

    Apriori(BasketIterator<Item> baskets) {
        super(baskets)
    }

    @Override
    Map<List<Item>, Integer> getFrequentItemsets(double minSup, double maxSup) {
        def items = baskets.uniqueItems
        def lvl1 = new LinkedHashMap<List<Item>, Integer>()
        int minCnt = Math.round(minSup * baskets.size())
        int maxCnt = Math.round(maxSup * baskets.size())

        for (final Item item : items) {
            lvl1.put(new ArrayList<Item>() {{ add(item) }}, 0)
        }

        return getFreqItemsets(lvl1, 1, minCnt, maxCnt)
    }

    /**
     * Helper method for getFrequentItemsets. Recursively checks for frequent
     * itemsets, starting at itemset size = `level`.
     *
     * @param cands the frequent itemset candidates, which must contain `level`
     * items each
     * @param level the frequent itemset size we're currently checking
     * @param minSup the minimum support count percentage, in decimal, for each
     * itemset
     * @return the list of frequent itemsets
     */
    private Map<List<Item>, Integer> getFreqItemsets(
     Map<List<Item>, Integer> cands, int level, int minCnt, int maxCnt) {
        def goodSets = new LinkedHashMap<List<Item>, Integer>()

        if (!cands.isEmpty()) {
            baskets.reset()

            while (baskets.hasNext()) {
                def basket = baskets.next()

                for (def entry : cands) {
                    if (subsetOf(entry.key, basket.getItems()))
                        entry.value++
                }
            }

            for (def entry : cands.entrySet()) {
                if (entry.value >= minCnt && entry.value <= maxCnt)
                    goodSets.put(entry.key, entry.value)
            }

            def nextCands = generateCandidates(goodSets, level + 1)
            return goodSets + getFreqItemsets(nextCands, level + 1, minCnt, maxCnt)
        }
        else return goodSets
    }

    /**
     * Generate the next itemset size candidates from the previous size's
     * frequent itemsets.
     *
     * @param prevGood
     * @param level
     * @return
     */
    public Map<List<Item>, Integer> generateCandidates(Map<List<Item>, Integer> prevGood, int level) {
        def goodNext = new LinkedHashMap<List<Item>, Integer>()
        def temp = prevGood.entrySet()
        def prevSet = new ArrayList()
        prevSet.addAll(temp)
        def size = prevGood.entrySet().size()
        List<Item> distincts

        (0..< size).collect{[prevSet[it], it]}.each { cand1, i ->
            (i + 1 ..< size).collect{prevSet[it]}.each { cand2 ->

                if ((distincts = differByOne(cand1.key, cand2.key)) != null) {
                    def doAdd

                    if (level - 3 >= 0) {
                        // For each subset we're interested in, check if it is
                        // frequent. Only include if all subsets are frequent
                        // (inject code). Essentially we're doing a map/reduce
                        // here (collect/inject), where we map the subsets to
                        // true or false indicating whether or not the subset
                        // is frequent, and then reducing the boolean values.
                        doAdd = (0..< cand2.key.size() - 1).collect {
                            def mustBePresent = cand2.key[0 ..< ((level - 3))] + distincts
                            return prevGood.keySet().collect {
                                mustBePresent == it
                            }
                              .inject(false) { acc, val -> return acc || val }
                        }.inject(true) { acc, val -> acc && val }
                    }
                    else doAdd = true

                    if (doAdd)
                        goodNext.put(cand1.key + distincts[1], 0)
                }
            }
        }

        return goodNext
    }

    /**
     * Checks whether the two equal size sorted lists `cand1` and `cand2`
     * differ only in their last items. Since they are of equal size and every
     * other index must equal, we must have `cand1[-1]` not in `cand2` and
     * vice versa. This function returns `[cand1[-1], cand2[-1]]` if the lists
     * meet the above criteria and null if not.
     *
     * @param cand1 a sorted list of Items where `cand1.size() == cand2.size()`
     * @param cand2 a sorted list of Items where `cand1.size() ==
     * cand2.size()`, whose last element is returned if these two lists differ
     * in only their last elements
     * @return `[cand1[-1], cand2[-1]]` if the two lists equal except for their
     * last element, null otherwise. Return value is undefined if the lists are
     * not sorted.
     */
    List<Item> differByOne(List<Item> cand1, List<Item> cand2) {
        // Make sure the lists are of equal size
        if (cand1.size() != cand2.size()) {
            System.err.println("""
The size of two lists sent to differByOne do not equal in size. This is
probably an issue.
""")
            return null
        }
        else {
            // Iterate over every element in cand1, 2 except the last
            (0..< cand1.size() - 1).collect{[cand1[it], cand2[it]]}
                    .each{ item1, item2 ->
                // If the corresponding indexes don't equal, return null
                if (!item1.equals(item2))
                    return null
            }

            // Return null if the last items equal, `[cand1[-1], cand2[-1]]`
            // otherwise
            return (!cand1[-1].equals(cand2[-1])) ?
              [cand1[-1], cand2[-1]] : null
        }
    }

    /**
     * Find whether list1 is a subset of list2. Both list1 and list2 must be
     * in sorted order, as defined by the objects inside them. Items in list1
     * and list2 will be compared using the traditional equals() method.
     *
     * @param list1 check whether this list is contained inside list2
     * @param list2 check whether this list contains list1
     * @return true if list1 is contained in list2, false otherwise
     */
    static boolean subsetOf(List list1, List list2) {
        int skipLim = list2.size() - list1.size()

        if (skipLim >= 0) {
            int skipTot = 0
            def item1, item2
            def itr1 = list1.iterator(), itr2 = list2.iterator()

            while (itr1.hasNext() && itr2.hasNext()) {
                item1 = itr1.next()
                item2 = itr2.next()

                while (!item1.equals(item2)) {
                    if (skipLim > skipTot) {
                        skipTot++
                        item2 = itr2.next()
                    }
                    else return false
                }
            }
        }

        return skipLim >= 0;
    }

    @Override
    boolean reset() {
        baskets.reset()
        return true
    }
}