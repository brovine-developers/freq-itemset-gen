package me.therin.mining.itemsets

import me.therin.mining.itemsets.fpgrowth.FPTree
import me.therin.mining.itemsets.apriori.Apriori
import me.therin.brovine.TestBaskets

def bs1 = new TestBaskets()
def bs2 = new TestBaskets()

def apriori = new Apriori<Character>(bs1)
def fpgrowth = new FPTree<Character>(bs2)

def ap_map = apriori.getFrequentItemsets(0.88, 0.92)
def fp_map = fpgrowth.getFrequentItemsets(0.88, 0.92)

