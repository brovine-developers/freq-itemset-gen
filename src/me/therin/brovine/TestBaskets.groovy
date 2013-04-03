package me.therin.brovine

import me.therin.mining.itemsets.data.BasketIterator
import me.therin.mining.itemsets.data.Basket

/**
 * TestBaskets.java
 * 
 * TODO: Class description
 *
 * @author TODO: tcirwin
 * @version TODO: the version
 * @date Mar 09, 2013
 */
class TestBaskets implements BasketIterator<Character> {
    def data = [['b','a'], ['d','c','b'], ['e','d','c','a'], ['e', 'd', 'a'], ['c', 'b', 'a'], ['d', 'c', 'b', 'a'], ['a'],
    ['c', 'b', 'a'], ['d', 'b', 'a'], ['e', 'c', 'b']]
    def iterator = data.iterator()

    @Override
    List<Character> getUniqueItems() {
        return ['a', 'b', 'c', 'd', 'e']
    }

    @Override
    BasketIterator<Character> includeOnly(List<Basket<Character>> baskets) {
        return null
    }

    @Override
    boolean update() { return true }

    @Override
    boolean reset() {
        iterator = data.iterator()
        return true
    }

    @Override
    int size() { data.size() }

    @Override
    boolean hasNext() { iterator.hasNext() }

    @Override
    Basket<Character> next() { new Basket("str", iterator.next().reverse()) }

    @Override
    void remove() { throw new UnsupportedOperationException() }
}
