package org.checkerframework.checker.units;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.K;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.cd;
import org.checkerframework.checker.units.qual.degrees;
import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.h;
import org.checkerframework.checker.units.qual.kg;
import org.checkerframework.checker.units.qual.km;
import org.checkerframework.checker.units.qual.km2;
import org.checkerframework.checker.units.qual.kmPERh;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.m2;
import org.checkerframework.checker.units.qual.mPERs;
import org.checkerframework.checker.units.qual.mPERs2;
import org.checkerframework.checker.units.qual.min;
import org.checkerframework.checker.units.qual.mm;
import org.checkerframework.checker.units.qual.mm2;
import org.checkerframework.checker.units.qual.mol;
import org.checkerframework.checker.units.qual.ms;
import org.checkerframework.checker.units.qual.radians;
import org.checkerframework.checker.units.qual.s;

/**
 * Utility constants to assign units to types and methods to convert between them.
 *
 * <p>All constants are declared as public static final with a value of 1.
 *
 * <p>To assign a unit to a number, simply multiply with the corresponding unit.
 *
 * <p>E.g. {@code @m int x = 5 * UnitsTools.m}
 */
// Developer notes: add fromTo methods for all useful unit combinations here.

// Forcefully suppress all warnings here, since this tools class provides a means to assign types to
// value literals and convert between types
@SuppressWarnings("units")
public class UnitsTools {
    // Acceleration
    public static final @mPERs2 int mPERs2 = 1;

    // Angle
    public static final @radians double rad = 1;
    public static final @degrees double deg = 1;

    public static @radians double toRadians(@degrees double angdeg) {
        return Math.toRadians(angdeg);
    }

    public static @degrees double toDegrees(@radians double angrad) {
        return Math.toDegrees(angrad);
    }

    // Area
    public static final @mm2 int mm2 = 1;
    public static final @m2 int m2 = 1;
    public static final @km2 int km2 = 1;

    // Current
    public static final @A int A = 1;

    // Luminance
    public static final @cd int cd = 1;

    // Lengths
    public static final @mm int mm = 1;
    public static final @m int m = 1;
    public static final @km int km = 1;

    public static @m int fromMilliMeterToMeter(@mm int mm) {
        return mm / 1000;
    }

    public static @mm int fromMeterToMilliMeter(@m int m) {
        return m * 1000;
    }

    public static @km int fromMeterToKiloMeter(@m int m) {
        return m / 1000;
    }

    public static @m int fromKiloMeterToMeter(@km int km) {
        return km * 1000;
    }

    // Mass
    public static final @g int g = 1;
    public static final @kg int kg = 1;

    public static @kg int fromGramToKiloGram(@g int g) {
        return g / 1000;
    }

    public static @g int fromKiloGramToGram(@kg int kg) {
        return kg * 1000;
    }

    // Speed
    public static final @mPERs int mPERs = 1;
    public static final @kmPERh int kmPERh = 1;

    public static @kmPERh double fromMeterPerSecondToKiloMeterPerHour(@mPERs double mps) {
        return mps * 3.6d;
    }

    public static @mPERs double fromKiloMeterPerHourToMeterPerSecond(@kmPERh double kmph) {
        return kmph / 3.6d;
    }

    // Substance
    public static final @mol int mol = 1;

    // Temperature
    public static final @K int K = 1;
    public static final @C int C = 1;

    /** Converts kelvins to celcius by subtracting 273.15 */
    public static @C float fromKelvinToCelsius(@K float k) {
        return k - 273.15f;
    }

    /** Converts celcius to kelvins by adding 273.15 */
    public static @K float fromCelsiusToKelvin(@C float c) {
        return c + 273.15f;
    }

    // Time
    public static final @h int h = 1;
    public static final @min int min = 1;
    public static final @s int s = 1;
    public static final @ms int ms = 1;

    // Dimensionless Conversion Method
    /**
     * This method takes in any variable or reference and returns the same variable or reference
     * with the Dimensionless unit instead of what it had.
     *
     * @param x a variable or reference with any unit
     * @return x with the Dimensionless unit replacing the unit it had
     */
    public static final @Dimensionless <T> T toDimensionless(@UnknownUnits T x) {
        // Updated method signature for when we compile UnitsTools using Java version 8:
        // public static final <@UnknownUnits T> @Dimensionless T toDimensionless(T x) {
        return x;
    }
}
