package me.therin.brovine;

import me.therin.mining.itemsets.data.Basket;

import java.util.ArrayList;
import java.util.List;

/**
 * Gene.java
 * <p/>
 * A Gene that has a name and a list of transcription factors that occur within
 * the gene
 *
 * @author tcirwin
 * @version 1
 * @date March 6, 2013
 */
public class Gene extends Basket<Transfac> {
   public Gene(String name) { super(name, new ArrayList<Transfac>()); }
   public Gene(String name, List<Transfac> factors) { super(name, factors); }

   public void add(Transfac t) { items.add(t); }
}
