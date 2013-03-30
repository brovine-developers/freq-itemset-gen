package me.therin.mining.itemsets.apriori

assert !Apriori.subsetOf([1, 2, 3], [4, 5, 6])
assert !Apriori.subsetOf([1, 2, 3], [2, 3, 4])
assert !Apriori.subsetOf([1, 2, 3, 4], [1, 2, 3])
assert !Apriori.subsetOf([4, 5, 6, 7], [1, 2, 3])
assert Apriori.subsetOf([2, 3], [1, 2, 3, 4])
assert Apriori.subsetOf([1, 2, 3], [1, 2, 3])
assert Apriori.subsetOf([1], [1])
assert Apriori.subsetOf([], [])
assert Apriori.subsetOf([12], [2, 4, 6, 10, 12, 14])
assert Apriori.subsetOf([10, 14], [2, 4, 6, 10, 12, 14])
assert Apriori.subsetOf([2, 6, 14], [2, 4, 6, 10, 12, 14])
assert !Apriori.subsetOf([2, 6, 7, 14], [2, 4, 6, 10, 12, 14])