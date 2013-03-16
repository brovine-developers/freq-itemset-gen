package me.therin.brovine;

/**
 * Transfac.java
 * <p/>
 * id - The lowest match ID associated with the transcription factor
 * name - The transfac's name, as a string
 *
 * @author tcirwin
 * @version 1
 * @date March 6, 2013
 */
public class Transfac {
   // ID of the transfac; immutable
   public final int id;
   // Name of the transfac; immutable
   public final String name;

   /**
    * Initialize a new immutable Transfac.
    *
    * @param id the unique ID of this transfac
    * @param name the name of this transfac
    */
   public Transfac(int id, String name) {
      this.id = id;
      this.name = name;
   }

   /**
    * The transfac ID acts as a hash code because it is unique.
    *
    * @return the ID of the Transfac which can be used as a hash code
    */
   @Override
   public int hashCode() {
      return id;
   }

   /**
    * Two factors are equal if their IDs are the same.
    *
    * @param o a transfac to compare to this
    * @return true if o is of type Transfac and o.id == this.id, false otherwise
    */
   @Override
   public boolean equals(Object o) {
      return (o instanceof Transfac) && ( ((Transfac) o).id == this.id );
   }

   @Override
   public String toString() { return name; }
}
