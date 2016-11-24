import org.checkerframework.dataflow.qual.*;
import org.checkerframework.checker.tests.deterministic.*;

class BasicTest {
  BasicObject Object = new BasicObject();

  @MultipleRunDeterministic
  void multiGood() {
    Object.m();
  }

  @MultipleRunDeterministic
  void multiBad() {
    Object.s();
    //TODO: error
    Object.n();
    //TODO: error
  }

  @SingleRunDeterministic
  void singleGood() {
    Object.m();
    Object.s();
  }

  @SingleRunDeterministic
  void singleBad() {
    Object.n();
    //TODO: error
  }

  @NonDeterministic
  void nonGood() {
    Object.m();
    Object.s();
    Object.n();
  }


}
