package me.therin.mining.itemsets

import me.therin.mining.itemsets.apriori.Apriori
import me.therin.brovine.Transfac
import me.therin.brovine.TransfacBaskets
import me.therin.mining.itemsets.fpgrowth.FPTree

// Initialize algorithms
def minSup = 0.86
def tfs = new TransfacBaskets()
def tfs2 = new TransfacBaskets()
def apriori = new Apriori<Transfac>(tfs)
def fpgrowth = new FPTree<Transfac>(tfs2)

// Get frequent itemsets using each algorithm
def apItemsets = apriori.getFrequentItemsets(minSup, 0.96)
def fgItemsets = fpgrowth.getFrequentItemsets(minSup, 0.96)

def checkList = new HashSet<List<Transfac>>()

// Add all items from set one
checkList.addAll(apItemsets.keySet())

// For each item in fgItemsets, remove the corresponding item in the checklist.
// After this, if the checklist is empty, fgItemsets must have apItemsets as a
// subset.
fgItemsets.keySet().each {
    checkList.remove(it)
}

// Ensure that apItemsets and fgItemsets represent the same set. To do this we
// merely check that the two sets have the same size.
// Proof. Assume |A| == |B| and A is a subset of B. For A and B to be
// not equal, we must have some item in A and !B or vice versa. If an item is
// in A and !B, then A is not a subset of B. If an item is !A and B, then B
// must contain at least |A| + 1 items, because A is a subset of B. But we
// assumed the two sets are of the same size, so this is not possible. Thus
// our sets must be equal.

// The checklist should be empty, indicating that there aren't any items
// in one itemset that aren't also in the other
assert checkList.isEmpty()

// We should have the same number of items in each result
assert fgItemsets.keySet().size() == apItemsets.keySet().size()