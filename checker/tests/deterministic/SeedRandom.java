import java.util.Random;
import org.checkerframework.dataflow.qual.*;

class SeedRandom {

  Random Object = new Random();
  // Set the seed of the Object to be constant.
  Object.setSeed(1234);


  @MultipleRunDeterministic
  int getRandom() {
    Object.nextInt();
  }

  // TODO: How do we check the number of calls to the method
  // was same in multiple runs?

}
