import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.test.*;
import tests.util.*;

class Deterministic {
  // Basic Object with MultipleRunDeterministic, SingleRunDeterministic, NonDeterministic methods
  BasicObject Object = new BasicObject();

  @MultipleRunDeterministic
  void multiGood() {
    Object.m();
  }

  @MultipleRunDeterministic
  void multiBad() {
    //:: error: (purity.not.multiplerundeterministic.call)
    Object.s(); // Calling a @SingleRunDeterministic method
    //:: error: (purity.not.multiplerundeterministic.call)
    Object.n(); // Calling a @NonDeterministic method
  }

  @SingleRunDeterministic
  void singleGood() {
    Object.m();
    Object.s();
  }

  @SingleRunDeterministic
  void singleBad() {
    //:: error: (purity.not.singlerundeterminisitic.call)
    Object.n(); // Calling a @NonDeterministic method
  }

  @NonDeterministic
  void nonGood() {
    Object.m();
    Object.s();
    Object.n();
  }


}
