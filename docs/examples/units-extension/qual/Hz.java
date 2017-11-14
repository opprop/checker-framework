package qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsRelation;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.framework.qual.SubtypeOf;

/** Hertz (Hz), a unit of frequency. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Frequency.class)
@UnitsRelation(op = Op.DIV, lhs = Dimensionless.class, rhs = s.class, res = Hz.class)
public @interface Hz {
    Prefix value() default Prefix.one;
}
