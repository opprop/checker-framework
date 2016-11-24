import java.util.*;
import org.checkerframework.checker.tests.deterministic.*;
import org.checkerframework.dataflow.qual.*;

class HashSetTest {
  public static void main(String[] args) {
    // HashSet with default initial capacity (16) and load factor (0.75)
    Set<Item> hs = new HashSet<Item>();

    // Adding objects of NonDeterministic, default implementation of hashCode
    Item obj1 = new Item(1,"a");
    Item obj2 = new Item(2,"b");
    Item obj3 = new Item(3,"c");
    hs.add(obj1);
    hs.add(obj2);
    hs.add(obj3);

    // HashSet with default initial capacity (16) and load factor (0.75)
    Set<ItemHashCode> hs2 = new HashSet<ItemHashCode>();

    // Adding objects of MultipleRunDeterministic implementation of hashCode
    ItemHashCode objh1 = new ItemHashCode(1,"a");
    ItemHashCode objh2 = new ItemHashCode(2,"b");
    ItemHashCode objh3 = new ItemHashCode(3,"c");
    hs2.add(objh1);
    hs2.add(objh2);
    hs2.add(objh3);

    // All method calls within the SingleRunDeterministic must be either
    // SingleRunDeterministic or MultipleRunDeterministic
    @SingleRunDeterministic
    void good() {
      // TODO: The iterator() method is SingleRunDeterministic
      Iterator<Item> myIterator = hs.iterator();



    }


    @MultipleRunDeterministic
    void bad() {

    }




  }
}
