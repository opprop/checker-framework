import org.checkerframework.dataflow.qual.*;

class BasicObject {

  @MultipleRunDeterministic
  void m() {}

  @SingleRunDeterministic
  void s() {}

  // TODO: We don't need this annotation?
  @NonDeterministic
  void n() {}

  public BasicObject() {}

}
