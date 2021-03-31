// Tainting checker skip constructor result checking, so no
// "inconsistent.constructor.type" will be reported

import org.checkerframework.checker.tainting.qual.Untainted;

import java.nio.file.SimpleFileVisitor;

public class AnonymousProblem {
    SimpleFileVisitor s = new SimpleFileVisitor<String>() {};

    @Untainted AA a = new @Untainted AA() {};

    // :: warning: (super.invocation.invalid)
    @Untainted BB b = new @Untainted BB("aaa") {};

    // :: warning: (cast.unsafe.constructor.invocation)
    @Untainted CC c = new @Untainted CC();
}

interface AA {}

abstract class BB {
    BB(String s) {}
}

class CC {}
