// Sample Class for HashSet with default implementation of hashCode
// using the System.identityHashCode
class Item {
  int value;
  String name;

  public Item(int v, String n) {
    this.setValue(v);
    this.setName(n);
  }

  public void setValue(int v) {
    value = v;
  }

  public void setName(String n) {
    name = n;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

}
