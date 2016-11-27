import org.checkerframework.dataflow.qual.*;

class BasicObject {

  @MultipleRunDeterministic
  void m() {}

  @SingleRunDeterministic
  void s() {}

  @NonDeterministic
  void n() {}

  public BasicObject() {}

}
