package me.therin.mining.itemsets.apriori

import me.therin.mining.itemsets.data.BasketIterator
import me.therin.mining.itemsets.data.Basket

def lists = [
  [[[1,2,3], [1,2,4], [2,3,4], [1,3,4], [2,4,5]], [[1,2,3,4]: 0]],
  [[[1,2,3], [1,2,4], [2,3,4], [2,4,5]], [:]]
]

lists.each { idx, val ->
    BasketIterator bi = new BasketIterator<Integer>() {

        @Override
        List<Integer> getUniqueItems() { null }

        @Override
        BasketIterator<Integer> includeOnly(List<Basket<Integer>> baskets) { null }

        @Override
        boolean update() { false }

        @Override
        boolean reset() { false}

        @Override
        int size() { 0 }

        @Override
        boolean hasNext() { false }

        @Override
        Basket<Integer> next() { null }

        @Override
        void remove() {  }
    }

    Apriori ap = new Apriori(bi)

    Map<List<Integer>, Integer> prevLists = idx.inject([:]) { acc, it -> acc.put(it, 0); acc }

    assert ap.generateCandidates(prevLists, 4) == val
}