package me.therin.mining.itemsets

import me.therin.mining.itemsets.fpgrowth.FPTree
import me.therin.mining.itemsets.apriori.Apriori
import me.therin.brovine.TestBaskets

def minSups = [0.1, 0.2, 0.25, 0.88]

print("Running tests...")

for (def minSup : minSups) {
    def bs1 = new TestBaskets()
    def bs2 = new TestBaskets()

    def apriori = new Apriori<Character>(bs1)
    def fpgrowth = new FPTree<Character>(bs2)

    def ap_map = apriori.getFrequentItemsets(minSup, 1)
    def fp_map = fpgrowth.getFrequentItemsets(minSup, 1)

    // Check that b is a subset (or equal to) a and that |a| == |b|
    for (def entry : fp_map.keySet()) {
        assert ap_map.keySet().contains(entry)
    }

    assert ap_map.size() == fp_map.size()

    print("  Passed minSup = " + minSup)
}

print("Passed all tests")