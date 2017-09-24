package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Special annotation tag to mark that certain methods should be type checked as arithmetic or
 * comparison operations. For example, the following methods behave logically like their equivalent
 * arithmetic or comparison operators:
 *
 * <p>In java.lang.Integer:
 *
 * <p>boolean equals(Object arg0);
 *
 * <p>int compareTo(Integer arg0);
 *
 * <p>static int compare(int arg0, int arg1);
 *
 * <p>static int compareUnsigned(int arg0, int arg1);
 *
 * <p>static int divideUnsigned(int arg0, int arg1);
 *
 * <p>static int remainderUnsigned(int arg0, int arg1);
 *
 * <p>static int sum(int a, int b);
 *
 * <p>Usage: You must specify a corresponding operation from CheckOp. Then assign position ids to
 * the arguments. The position ids are 0 based indices, where 0 means the first parameter of the
 * method. Note that -1 is a special index representing the receiver type of the method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface UnitsTypeCheckAsOp {
    /**
     * operation
     *
     * @return the operation for this relation
     */
    CheckOp op();

    // TODO: these are currently string type because stub parser cannot handle integer arguments, change to int once supported
    // also change UnitsRelationsEnforcer

    /**
     * left hand side argument position, default 0
     *
     * @return the index of the left hand side argument
     */
    String lhsPos() default "0"; // 0 means first argument of method

    /**
     * right hand side argument position, default 1
     *
     * @return the index of the right hand side argument
     */
    String rhsPos() default "1"; // 1 means second argument of method
}
