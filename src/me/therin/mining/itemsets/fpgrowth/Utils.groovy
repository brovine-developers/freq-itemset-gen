package me.therin.mining.itemsets.fpgrowth

/**
 * me.therin.mining.itemsets.Utilswth.Utils.java
 * 
 * Array utilities
 *
 * @author tcirwin
 * @date February 10, 2013
 */
class Utils {

    static def cloneArr = { arr ->
        def arrCopy = []

        arr.each {
            if (it.getClass().isArray())
                arrCopy.add(cloneArr(it))
            else
                arrCopy.add(it.clone())
        }

        return arrCopy
    }
}
