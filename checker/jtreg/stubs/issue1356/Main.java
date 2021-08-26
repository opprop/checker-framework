/*
 * @test
 * @ignore
 * @summary Test case for Issue 1356.
 * https://github.com/typetools/checker-framework/issues/1356
 * @compile -XDrawDiagnostics -processor org.checkerframework.checker.regex.RegexChecker -Astubs=MyClass.astub mypackage/MyClass.java -Werror -AstubWarnIfNotFound
 * @compile -XDrawDiagnostics -processor org.checkerframework.checker.regex.RegexChecker mypackage/UseMyClass.java
 */

public class Main {}
