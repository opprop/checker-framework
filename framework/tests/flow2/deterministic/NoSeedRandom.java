import java.util.Random;
import org.checkerframework.dataflow.qual.*;

class NoSeedRandom {

  @NonDeterministic
  void good() {
      Random Object = new Random();
      int bound = 10;

      boolean b;
      b = nextBoolean();
      int i;
      i = nextInt();
      i = nextInt(bound);
      double d;
      d = nextDouble();
      d = nextGaussian()
      float f;
      f = nextFloat();
      long l;
      l = nextLong();

  }

  @SingleRunDeterministic
  void bad() {
    
  }

}
