package org.checkerframework.checker.units;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.K;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.cd;
import org.checkerframework.checker.units.qual.deg;
import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.kg;
import org.checkerframework.checker.units.qual.km;
import org.checkerframework.checker.units.qual.km2;
import org.checkerframework.checker.units.qual.km3;
import org.checkerframework.checker.units.qual.kmPERh;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.m2;
import org.checkerframework.checker.units.qual.m3;
import org.checkerframework.checker.units.qual.mPERs;
import org.checkerframework.checker.units.qual.mPERs2;
import org.checkerframework.checker.units.qual.mm;
import org.checkerframework.checker.units.qual.mm2;
import org.checkerframework.checker.units.qual.mm3;
import org.checkerframework.checker.units.qual.mol;
import org.checkerframework.checker.units.qual.rad;
import org.checkerframework.checker.units.qual.time.duration.century;
import org.checkerframework.checker.units.qual.time.duration.day;
import org.checkerframework.checker.units.qual.time.duration.decade;
import org.checkerframework.checker.units.qual.time.duration.era;
import org.checkerframework.checker.units.qual.time.duration.forever;
import org.checkerframework.checker.units.qual.time.duration.h;
import org.checkerframework.checker.units.qual.time.duration.halfday;
import org.checkerframework.checker.units.qual.time.duration.millennia;
import org.checkerframework.checker.units.qual.time.duration.min;
import org.checkerframework.checker.units.qual.time.duration.month;
import org.checkerframework.checker.units.qual.time.duration.ms;
import org.checkerframework.checker.units.qual.time.duration.ns;
import org.checkerframework.checker.units.qual.time.duration.quarteryear;
import org.checkerframework.checker.units.qual.time.duration.s;
import org.checkerframework.checker.units.qual.time.duration.us;
import org.checkerframework.checker.units.qual.time.duration.week;
import org.checkerframework.checker.units.qual.time.duration.year;
import org.checkerframework.checker.units.qual.time.instant.CALcentury;
import org.checkerframework.checker.units.qual.time.instant.CALday;
import org.checkerframework.checker.units.qual.time.instant.CALdecade;
import org.checkerframework.checker.units.qual.time.instant.CALera;
import org.checkerframework.checker.units.qual.time.instant.CALforever;
import org.checkerframework.checker.units.qual.time.instant.CALh;
import org.checkerframework.checker.units.qual.time.instant.CALhalfday;
import org.checkerframework.checker.units.qual.time.instant.CALmillennia;
import org.checkerframework.checker.units.qual.time.instant.CALmin;
import org.checkerframework.checker.units.qual.time.instant.CALmonth;
import org.checkerframework.checker.units.qual.time.instant.CALms;
import org.checkerframework.checker.units.qual.time.instant.CALns;
import org.checkerframework.checker.units.qual.time.instant.CALquarteryear;
import org.checkerframework.checker.units.qual.time.instant.CALs;
import org.checkerframework.checker.units.qual.time.instant.CALus;
import org.checkerframework.checker.units.qual.time.instant.CALweek;
import org.checkerframework.checker.units.qual.time.instant.CALyear;

/**
 * Utility class containing constants to assign units to types and methods to convert between them.
 *
 * <p>All constants are declared as {@code public static final} with a value of {@code 1}.
 *
 * <p>To assign a unit to a number, simply multiply with the corresponding unit.
 *
 * <p>E.g. {@code @m int x = 5 * UnitsTools.m;}.
 */
// Developer notes: add fromTo methods for all useful unit combinations here.

// Forcefully suppress all warnings here, since this tools class provides a
// means to assign types to value literals and convert between types
@SuppressWarnings("units")
public class UnitsTools {
    // Acceleration
    public static final @mPERs2 int mPERs2 = 1;

    // Angle
    public static final @rad double rad = 1;
    public static final @deg double deg = 1;

    public static @rad double toRadians(@deg double angdeg) {
        return Math.toRadians(angdeg);
    }

    public static @deg double toDegrees(@rad double angrad) {
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

    public static @C int fromKelvinToCelsius(@K int k) {
        return k - (int) 273.15;
    }

    public static @K int fromCelsiusToKelvin(@C int c) {
        return c + (int) 273.15;
    }

    // Time Duration
    public static final @century int century = 1;
    public static final @day int day = 1;
    public static final @decade int decade = 1;
    public static final @era int era = 1;
    public static final @forever int forever = 1;
    public static final @h int h = 1;
    public static final @halfday int halfday = 1;
    public static final @millennia int millennia = 1;
    public static final @min int min = 1;
    public static final @month int month = 1;
    public static final @ms int ms = 1;
    public static final @ns int ns = 1;
    public static final @quarteryear int quarteryear = 1;
    public static final @s int s = 1;
    public static final @us int us = 1;
    public static final @week int week = 1;
    public static final @year int year = 1;

    // Time Instant
    public static final @CALcentury int CALcentury = 1;
    public static final @CALday int CALday = 1;
    public static final @CALdecade int CALdecade = 1;
    public static final @CALera int CALera = 1;
    public static final @CALforever int CALforever = 1;
    public static final @CALh int CALh = 1;
    public static final @CALhalfday int CALhalfday = 1;
    public static final @CALmillennia int CALmillennia = 1;
    public static final @CALmin int CALmin = 1;
    public static final @CALmonth int CALmonth = 1;
    public static final @CALms int CALms = 1;
    public static final @CALns int CALns = 1;
    public static final @CALquarteryear int CALquarteryear = 1;
    public static final @CALs int CALs = 1;
    public static final @CALus int CALus = 1;
    public static final @CALweek int CALweek = 1;
    public static final @CALyear int CALyear = 1;

    public static @min int fromSecondToMinute(@s int s) {
        return s / 60;
    }

    public static @s int fromMinuteToSecond(@min int min) {
        return min * 60;
    }

    public static @h int fromMinuteToHour(@min int min) {
        return min / 60;
    }

    public static @min int fromHourToMinute(@h int h) {
        return h * 60;
    }

    // Volume
    public static final @mm3 int mm3 = 1;
    public static final @m3 int m3 = 1;
    public static final @km3 int km3 = 1;

    // Dimensionless Conversion Method
    /**
     * This method takes in any variable or reference and returns the same variable or reference
     * with the Dimensionless unit instead of what it had.
     *
     * @param x a variable or reference with any unit
     * @return x with the Dimensionless unit replacing the unit it had
     */
    public static final @Dimensionless <T> T toDimensionless(@UnknownUnits T x) {
        // for version 8:
        // public static final <@UnknownUnits T> @Dimensionless T toDimensionless(T x) {
        return x;
    }
}
