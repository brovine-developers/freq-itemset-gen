package me.therin.mining.itemsets

import me.therin.mining.itemsets.fpgrowth.FPTree
import me.therin.mining.itemsets.apriori.Apriori
import me.therin.brovine.TestBaskets
import me.therin.brovine.TransfacBaskets

def minSups = [0.82, 0.86, 0.87, 0.88]
def maxSups = [0.9, 0.92, 0.94, 0.96]

println("Running tests...")

for (def maxSup : maxSups) {
    for (def minSup : minSups) {
        def bs1 = new TransfacBaskets()
        def bs2 = new TransfacBaskets()

        def apriori = new Apriori<Character>(bs1)
        def fpgrowth = new FPTree<Character>(bs2)

        long start = System.currentTimeMillis()
        def ap_map = apriori.getFrequentItemsets(minSup, maxSup)
        long ap_tot = System.currentTimeMillis() - start
        start = System.currentTimeMillis()
        def fp_map = fpgrowth.getFrequentItemsets(minSup, maxSup)
        long fp_tot = System.currentTimeMillis() - start

        println("Apriori: $ap_tot ms, FPGrowth: $fp_tot ms.")

        // Check that b is a subset (or equal to) a and that |a| == |b|
        for (def entry : fp_map.keySet()) {
            assert ap_map.keySet().contains(entry)
        }

        assert ap_map.size() == fp_map.size()

        println("  Passed minSup = $minSup, maxSup = $maxSup")
    }
}

println("Passed all tests")