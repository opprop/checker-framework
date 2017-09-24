import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Acceleration;
import org.checkerframework.checker.units.qual.Area;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.Current;
import org.checkerframework.checker.units.qual.K;
import org.checkerframework.checker.units.qual.Length;
import org.checkerframework.checker.units.qual.Luminance;
import org.checkerframework.checker.units.qual.Mass;
import org.checkerframework.checker.units.qual.Substance;
import org.checkerframework.checker.units.qual.Temperature;
import org.checkerframework.checker.units.qual.Volume;
import org.checkerframework.checker.units.qual.cd;
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
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.checker.units.qual.time.duration.h;
import org.checkerframework.checker.units.qual.time.duration.min;
import org.checkerframework.checker.units.qual.time.duration.s;
import org.checkerframework.checker.units.qual.time.instant.CALh;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;

class AdditionTest {
    // Addition is legal when the operands have the same units.
    // Addition is illegal when the operands have different units.

    // =========================================================================
    // Dimensions
    // The following set of assignments tests that variables with a dimension
    // qualifier can store a value with a unit of its dimension. The variables
    // are also used later in the methods to test addition operations.

    // Acceleration
    @Acceleration int aAcceleration = 5 * UnitsTools.mPERs2;
    @Acceleration int bAcceleration = 5 * UnitsTools.mPERs2;

    // Area
    @Area int aArea = 5 * UnitsTools.m2;
    @Area int bArea = 5 * UnitsTools.mm2;
    @Area int cArea = 5 * UnitsTools.km2;

    // Current
    @Current int aCurrent = 5 * UnitsTools.A;
    @Current int bCurrent = 5 * UnitsTools.A;

    // Length
    @Length int aLength = 5 * UnitsTools.m;
    @Length int bLength = 5 * UnitsTools.mm;
    @Length int cLength = 5 * UnitsTools.km;

    // Luminance
    @Luminance int aLuminance = 5 * UnitsTools.cd;
    @Luminance int bLuminance = 5 * UnitsTools.cd;

    // Mass
    @Mass int aMass = 5 * UnitsTools.kg;
    @Mass int bMass = 5 * UnitsTools.g;

    // Substance
    @Substance int aSubstance = 5 * UnitsTools.mol;
    @Substance int bSubstance = 5 * UnitsTools.mol;

    // Temperature
    @Temperature int aTemperature = 5 * UnitsTools.K;
    @Temperature int bTemperature = 5 * UnitsTools.C;

    // TimeDuration
    @TimeDuration int aTimeDur = 5 * UnitsTools.min;
    @TimeDuration int bTimeDur = 5 * UnitsTools.h;

    // TimeInstant
    @TimeInstant int aTimePt = 5 * UnitsTools.CALmin;
    @TimeInstant int bTimePt = 5 * UnitsTools.CALh;

    // Volume
    @Volume int aVolume = 5 * UnitsTools.m3;
    @Volume int bVolume = 5 * UnitsTools.mm3;
    @Volume int cVolume = 5 * UnitsTools.km3;

    // =========================================================================
    // Units
    // The following set of assignments tests that variables with a unit
    // qualifier can store a value with the same unit. The variables
    // are also used later in the methods to test addition operations.

    // Amperes
    @A int aAmpere = 5 * UnitsTools.A;
    @A int bAmpere = 5 * UnitsTools.A;

    // Candela
    @cd int aCandela = 5 * UnitsTools.cd;
    @cd int bCandela = 5 * UnitsTools.cd;

    // Celsius
    @C int aCelsius = 5 * UnitsTools.C;
    @C int bCelsius = 5 * UnitsTools.C;

    // Gram
    @g int aGram = 5 * UnitsTools.g;
    @g int bGram = 5 * UnitsTools.g;

    // Hour
    @h int aHour = 5 * UnitsTools.h;
    @h int bHour = 5 * UnitsTools.h;

    // Kelvin
    @K int aKelvin = 5 * UnitsTools.K;
    @K int bKelvin = 5 * UnitsTools.K;

    // Kilogram
    @kg int aKilogram = 5 * UnitsTools.kg;
    @kg int bKilogram = 5 * UnitsTools.kg;

    // Kilometer
    @km int aKilometer = 5 * UnitsTools.km;
    @km int bKilometer = 5 * UnitsTools.km;

    // Square kilometer
    @km2 int aSquareKilometer = 5 * UnitsTools.km2;
    @km2 int bSquareKilometer = 5 * UnitsTools.km2;

    // Kilometer cubed
    @km3 int aKilometerCubed = 5 * UnitsTools.km3;
    @km3 int bKilometerCubed = 5 * UnitsTools.km3;

    // Kilometer per hour
    @kmPERh int aKilometerPerHour = 5 * UnitsTools.kmPERh;
    @kmPERh int bKilometerPerHour = 5 * UnitsTools.kmPERh;

    // Meter
    @m int aMeter = 5 * UnitsTools.m;
    @m int bMeter = 5 * UnitsTools.m;

    // Square meter
    @m2 int aSquareMeter = 5 * UnitsTools.m2;
    @m2 int bSquareMeter = 5 * UnitsTools.m2;

    // Meter cubed
    @m3 int aMeterCubed = 5 * UnitsTools.m3;
    @m3 int bMeterCubed = 5 * UnitsTools.m3;

    // Meter per second
    @mPERs int aMeterPerSecond = 5 * UnitsTools.mPERs;
    @mPERs int bMeterPerSecond = 5 * UnitsTools.mPERs;

    // Meter per second square
    @mPERs2 int aMeterPerSecondSquare = 5 * UnitsTools.mPERs2;
    @mPERs2 int bMeterPerSecondSquare = 5 * UnitsTools.mPERs2;

    // Minute
    @min int aMinute = 5 * UnitsTools.min;
    @min int bMinute = 5 * UnitsTools.min;

    // Millimeter
    @mm int aMillimeter = 5 * UnitsTools.mm;
    @mm int bMillimeter = 5 * UnitsTools.mm;

    // Square millimeter
    @mm2 int aSquareMillimeter = 5 * UnitsTools.mm2;
    @mm2 int bSquareMillimeter = 5 * UnitsTools.mm2;

    // Millimeter cubed
    @mm3 int aMillimeterCubed = 5 * UnitsTools.mm3;
    @mm3 int bMillimeterCubed = 5 * UnitsTools.mm3;

    // Mole
    @mol int aMole = 5 * UnitsTools.mol;
    @mol int bMole = 5 * UnitsTools.mol;

    // Second
    @s int aSecond = 5 * UnitsTools.s;
    @s int bSecond = 5 * UnitsTools.s;

    void dimensions() {
        // Acceleration
        @Acceleration int cAcceleration = aAcceleration + bAcceleration;
        cAcceleration = aMeterPerSecondSquare + bMeterPerSecondSquare;
        //:: error: (assignment.type.incompatible)
        cAcceleration = aAcceleration + bArea;

        // Area
        @Area int cArea = aArea + bArea;
        cArea = aSquareMeter + bSquareKilometer;
        cArea = bSquareMeter + aSquareMillimeter;
        cArea = aSquareKilometer + bSquareMillimeter;
        //:: error: (assignment.type.incompatible)
        cArea = aArea + bAcceleration;

        // Current
        @Current int cCurrent = aCurrent + bCurrent;
        cCurrent = aAmpere + bAmpere;
        //:: error: (assignment.type.incompatible)
        cCurrent = aCurrent + bLength;

        // Length
        @Length int cLength = aLength + bLength;
        cLength = aMeter + bKilometer;
        cLength = bMeter + aMillimeter;
        cLength = aKilometer + bMillimeter;
        //:: error: (assignment.type.incompatible)
        cLength = aLength + bCurrent;

        // Luminance
        @Luminance int cLuminance = aLuminance + bLuminance;
        cLuminance = aCandela + bCandela;
        //:: error: (assignment.type.incompatible)
        cLuminance = aLuminance + bMass;

        // Mass
        @Mass int cMass = aMass + bMass;
        cMass = aGram + bGram;
        //:: error: (assignment.type.incompatible)
        cMass = aMass + bLuminance;

        // Substance
        @Substance int cSubstance = aSubstance + bSubstance;
        cSubstance = aMole + bMole;
        //:: error: (assignment.type.incompatible)
        cSubstance = aSubstance + bTemperature;

        // Temperature
        @Temperature int cTemperature = aTemperature + bTemperature;
        cTemperature = aCelsius + bKelvin;
        //:: error: (assignment.type.incompatible)
        cTemperature = aTemperature + bSubstance;

        // TimeDuration
        @TimeDuration int cTimeDur = aTimeDur + bTimeDur;
        cTimeDur = aSecond + bMinute;
        cTimeDur = bSecond + aHour;
        cTimeDur = aMinute + bHour;
        //:: error: (assignment.type.incompatible)
        cTimeDur = aTimeDur + bVolume;

        // TimeInstant
        @TimeInstant int cTimePt = aTimePt + bTimeDur;
        cTimePt = aTimeDur + bTimePt;
        //:: error: (assignment.type.incompatible)
        cTimePt = aTimePt + bTimePt;

        // Volume
        @Volume int cVolume = aVolume + bVolume;
        cVolume = aMeterCubed + bKilometerCubed;
        cVolume = bMeterCubed + aMillimeterCubed;
        cVolume = aKilometerCubed + bMillimeterCubed;
        //:: error: (assignment.type.incompatible)
        cVolume = aVolume + bTimeDur;
    }

    void units() {
        // Amperes
        @A int sAmpere = aAmpere + bAmpere;
        //:: error: (assignment.type.incompatible)
        sAmpere = aAmpere + bMeter;

        // Candela
        @cd int sCandela = aCandela + bCandela;
        //:: error: (assignment.type.incompatible)
        sCandela = aTemperature + bCandela;

        // Celsius
        @C int sCelsius = aCelsius + bCelsius;
        //:: error: (assignment.type.incompatible)
        sCelsius = aCelsius + bMillimeter;

        // Gram
        @g int sGram = aGram + bGram;
        //:: error: (assignment.type.incompatible)
        sGram = aGram + bAmpere;

        // Hour
        @h int sHour = aHour + bHour;
        //:: error: (assignment.type.incompatible)
        sHour = aSquareMeter + bHour;

        // Kelvin
        @K int sKelvin = aKelvin + bKelvin;
        //:: error: (assignment.type.incompatible)
        sKelvin = aKelvin + bSecond;

        // Kilogram
        @kg int sKilogram = aKilogram + bKilogram;
        //:: error: (assignment.type.incompatible)
        sKilogram = aKilogram + bKilometer;

        // Kilometer
        @km int sKilometer = aKilometer + bKilometer;
        //:: error: (assignment.type.incompatible)
        sKilometer = aCandela + bKilometer;

        // Square kilometer
        @km2 int sSquareKilometer = aSquareKilometer + bSquareKilometer;
        //:: error: (assignment.type.incompatible)
        sSquareKilometer = aSquareKilometer + bAmpere;

        // Kilometer cubed
        @km3 int sKilometerCubed = aKilometerCubed + bKilometerCubed;
        //:: error: (assignment.type.incompatible)
        sKilometerCubed = aKilometerCubed + aSquareKilometer;

        // Kilometer per hour
        @kmPERh int sKilometerPerHour = aKilometerPerHour + bKilometerPerHour;
        //:: error: (assignment.type.incompatible)
        sKilometerPerHour = aKilometerPerHour + bMeterPerSecond;

        // Meter
        @m int sMeter = aMeter + bMeter;
        //:: error: (assignment.type.incompatible)
        sMeter = aHour + bMeter;

        // Square meter
        @m2 int sSquareMeter = aSquareMeter + bSquareMeter;
        //:: error: (assignment.type.incompatible)
        sSquareMeter = aSquareMeter + bGram;

        // Meter cubed
        @m3 int sMeterCubed = aMeterCubed + bMeterCubed;
        //:: error: (assignment.type.incompatible)
        sMeterCubed = aMeterCubed + aSquareMeter;

        // Meter per second
        @mPERs int sMeterPerSecond = aMeterPerSecond + bMeterPerSecond;
        //:: error: (assignment.type.incompatible)
        sMeterPerSecond = aMeterPerSecond + bMeter;

        // Meter per second square
        @mPERs2 int sMeterPerSecondSquare = aMeterPerSecondSquare + bMeterPerSecondSquare;
        //:: error: (assignment.type.incompatible)
        sMeterPerSecondSquare = aMeterPerSecondSquare + bMeter;

        // Minute
        @min int sMinute = aMinute + bMinute;
        //:: error: (assignment.type.incompatible)
        sMinute = aMole + bMinute;

        // Millimeter
        @mm int sMillimeter = aMillimeter + bMillimeter;
        //:: error: (assignment.type.incompatible)
        sMillimeter = aMillimeter + bHour;

        // Square millimeter
        @mm2 int sSquareMillimeter = aSquareMillimeter + bSquareMillimeter;
        //:: error: (assignment.type.incompatible)
        sSquareMillimeter = aSquareMillimeter + aMillimeter;

        // Millimeter cubed
        @mm3 int sMillimeterCubed = aMillimeterCubed + bMillimeterCubed;
        //:: error: (assignment.type.incompatible)
        sMillimeterCubed = aMillimeterCubed + aSquareMillimeter;

        // Mole
        @mol int sMole = aMole + bMole;
        //:: error: (assignment.type.incompatible)
        sMole = aCandela + bMole;

        // Second
        @s int sSecond = aSecond + bSecond;
        //:: error: (assignment.type.incompatible)
        sSecond = aSecond + bSquareKilometer;

        // CALh
        //:: error: (assignment.type.incompatible)
        @CALh int sCALhr = aTimePt + aTimePt;
    }
}
