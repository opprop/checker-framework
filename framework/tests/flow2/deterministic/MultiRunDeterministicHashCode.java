import org.checkerframework.dataflow.qual.*;

// Sample Class for HashSet with manual implementation of a deterministic hashCode
class ItemHashCode {
  int value;
  String name;

  public ItemhashCode(int v, String n) {
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

  // User Declared MultipleRunDeterministic
  // TODO: Check if the methods used in the hashCode are also MultipleRunDeterministic
  @MultipleRunDeterministic
  @Override
  public int hashCode() {
    // Using the Integer hashCode implementation
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    // Using the Integer equals implementation
    if (obj instanceof ItemHashCode) {
      return value == ((ItemHashCode)obj)
    }
  }

}
