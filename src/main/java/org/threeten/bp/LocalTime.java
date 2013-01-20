/*
 * Copyright (c) 2007-2013, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.bp;

import static org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.MICRO_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR;
import static org.threeten.bp.temporal.ChronoField.NANO_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.NANO_OF_SECOND;
import static org.threeten.bp.temporal.ChronoField.SECOND_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE;
import static org.threeten.bp.temporal.ChronoUnit.NANOS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;

import org.threeten.bp.format.DateTimeBuilder;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatters;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.jdk8.DefaultInterfaceTemporalAccessor;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoLocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.ChronoZonedDateTime;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalAdder;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.TemporalQueries;
import org.threeten.bp.temporal.TemporalQuery;
import org.threeten.bp.temporal.TemporalSubtractor;
import org.threeten.bp.temporal.TemporalUnit;
import org.threeten.bp.temporal.ValueRange;

/**
 * A time without time-zone in the ISO-8601 calendar system,
 * such as {@code 10:15:30}.
 * <p>
 * {@code LocalTime} is an immutable date-time object that represents a time,
 * often viewed as hour-minute-second.
 * Time is represented to nanosecond precision.
 * For example, the value "13:45.30.123456789" can be stored in a {@code LocalTime}.
 * <p>
 * It does not store or represent a date or time-zone.
 * Instead, it is a description of the local time as seen on a wall clock.
 * It cannot represent an instant on the time-line without additional information
 * such as an offset or time-zone.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. This API assumes that all calendar systems use the same
 * representation, this class, for time-of-day.
 *
 * <h3>Specification for implementors</h3>
 * This class is immutable and thread-safe.
 */
public final class LocalTime
        extends DefaultInterfaceTemporalAccessor
        implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable {

    /**
     * The minimum supported {@code LocalTime}, '00:00'.
     * This is the time of midnight at the start of the day.
     */
    public static final LocalTime MIN;
    /**
     * The maximum supported {@code LocalTime}, '23:59:59.999999999'.
     * This is the time just before midnight at the end of the day.
     */
    public static final LocalTime MAX;
    /**
     * The time of midnight at the start of the day, '00:00'.
     */
    public static final LocalTime MIDNIGHT;
    /**
     * The time of noon in the middle of the day, '12:00'.
     */
    public static final LocalTime NOON;
    /**
     * Constants for the local time of each hour.
     */
    private static final LocalTime[] HOURS = new LocalTime[24];
    static {
        for (int i = 0; i < HOURS.length; i++) {
            HOURS[i] = new LocalTime(i, 0, 0, 0);
        }
        MIDNIGHT = HOURS[0];
        NOON = HOURS[12];
        MIN = HOURS[0];
        MAX = new LocalTime(23, 59, 59, 999_999_999);
    }

    /**
     * Hours per day.
     */
    static final int HOURS_PER_DAY = 24;
    /**
     * Minutes per hour.
     */
    static final int MINUTES_PER_HOUR = 60;
    /**
     * Minutes per day.
     */
    static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    /**
     * Seconds per minute.
     */
    static final int SECONDS_PER_MINUTE = 60;
    /**
     * Seconds per hour.
     */
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * Seconds per day.
     */
    static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    /**
     * Milliseconds per day.
     */
    static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L;
    /**
     * Microseconds per day.
     */
    static final long MICROS_PER_DAY = SECONDS_PER_DAY * 1000_000L;
    /**
     * Nanos per second.
     */
    static final long NANOS_PER_SECOND = 1000_000_000L;
    /**
     * Nanos per minute.
     */
    static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;
    /**
     * Nanos per hour.
     */
    static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * Nanos per day.
     */
    static final long NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY;

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 6414437269572265201L;

    /**
     * The hour.
     */
    private final byte hour;
    /**
     * The minute.
     */
    private final byte minute;
    /**
     * The second.
     */
    private final byte second;
    /**
     * The nanosecond.
     */
    private final int nano;

    //-----------------------------------------------------------------------
    /**
     * Obtains the current time from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current time.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current time using the system clock and default time-zone, not null
     */
    public static LocalTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current time from the system clock in the specified time-zone.
     * <p>
     * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current time.
     * Specifying the time-zone avoids dependence on the default time-zone.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @param zone  the zone ID to use, not null
     * @return the current time using the system clock, not null
     */
    public static LocalTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * Obtains the current time from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current time.
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current time, not null
     */
    public static LocalTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        // inline OffsetTime factory to avoid creating object and InstantProvider checks
        final Instant now = clock.instant();  // called once
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        long secsOfDay = now.getEpochSecond() % SECONDS_PER_DAY;
        secsOfDay = (secsOfDay + offset.getTotalSeconds()) % SECONDS_PER_DAY;
        if (secsOfDay < 0) {
            secsOfDay += SECONDS_PER_DAY;
        }
        return LocalTime.ofSecondOfDay(secsOfDay, now.getNano());
    }

    //------------------------get-----------------------------------------------
    /**
     * Obtains an instance of {@code LocalTime} from an hour and minute.
     * <p>
     * The second and nanosecond fields will be set to zero by this factory method.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return the local time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    public static LocalTime of(int hour, int minute) {
        HOUR_OF_DAY.checkValidValue(hour);
        if (minute == 0) {
            return HOURS[hour];  // for performance
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return new LocalTime(hour, minute, 0, 0);
    }

    /**
     * Obtains an instance of {@code LocalTime} from an hour, minute and second.
     * <p>
     * The nanosecond field will be set to zero by this factory method.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return the local time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    public static LocalTime of(int hour, int minute, int second) {
        HOUR_OF_DAY.checkValidValue(hour);
        if ((minute | second) == 0) {
            return HOURS[hour];  // for performance
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        return new LocalTime(hour, minute, second, 0);
    }

    /**
     * Obtains an instance of {@code LocalTime} from an hour, minute, second and nanosecond.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the local time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        HOUR_OF_DAY.checkValidValue(hour);
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour, minute, second, nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalTime} from a second-of-day value.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param secondOfDay  the second-of-day, from {@code 0} to {@code 24 * 60 * 60 - 1}
     * @return the local time, not null
     * @throws DateTimeException if the second-of-day value is invalid
     */
    public static LocalTime ofSecondOfDay(long secondOfDay) {
        SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
        secondOfDay -= hours * SECONDS_PER_HOUR;
        int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
        secondOfDay -= minutes * SECONDS_PER_MINUTE;
        return create(hours, minutes, (int) secondOfDay, 0);
    }

    /**
     * Obtains an instance of {@code LocalTime} from a second-of-day value, with
     * associated nanos of second.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param secondOfDay  the second-of-day, from {@code 0} to {@code 24 * 60 * 60 - 1}
     * @param nanoOfSecond  the nano-of-second, from 0 to 999,999,999
     * @return the local time, not null
     * @throws DateTimeException if the either input value is invalid
     */
    public static LocalTime ofSecondOfDay(long secondOfDay, int nanoOfSecond) {
        SECOND_OF_DAY.checkValidValue(secondOfDay);
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
        secondOfDay -= hours * SECONDS_PER_HOUR;
        int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
        secondOfDay -= minutes * SECONDS_PER_MINUTE;
        return create(hours, minutes, (int) secondOfDay, nanoOfSecond);
    }

    /**
     * Obtains an instance of {@code LocalTime} from a nanos-of-day value.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param nanoOfDay  the nano of day, from {@code 0} to {@code 24 * 60 * 60 * 1,000,000,000 - 1}
     * @return the local time, not null
     * @throws DateTimeException if the nanos of day value is invalid
     */
    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
        nanoOfDay -= hours * NANOS_PER_HOUR;
        int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
        nanoOfDay -= minutes * NANOS_PER_MINUTE;
        int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
        nanoOfDay -= seconds * NANOS_PER_SECOND;
        return create(hours, minutes, seconds, (int) nanoOfDay);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalTime} from a temporal object.
     * <p>
     * A {@code TemporalAccessor} represents some form of date and time information.
     * This factory converts the arbitrary temporal object to an instance of {@code LocalTime}.
     * <p>
     * The conversion extracts the {@link ChronoField#NANO_OF_DAY NANO_OF_DAY} field.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used in queries via method reference, {@code LocalTime::from}.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the local time, not null
     * @throws DateTimeException if unable to convert to a {@code LocalTime}
     */
    public static LocalTime from(TemporalAccessor temporal) {
        if (temporal instanceof LocalTime) {
            return (LocalTime) temporal;
        } else if (temporal instanceof ChronoLocalDateTime) {
            return ((ChronoLocalDateTime<?>) temporal).getTime();
        } else if (temporal instanceof ChronoZonedDateTime) {
            return ((ChronoZonedDateTime<?>) temporal).getTime();
        }
        // handle builder as a special case
        if (temporal instanceof DateTimeBuilder) {
            DateTimeBuilder builder = (DateTimeBuilder) temporal;
            LocalTime time = builder.extract(LocalTime.class);
            if (time != null) {
                return time;
            }
        }
        try {
            return ofNanoOfDay(temporal.getLong(NANO_OF_DAY));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain LocalTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalTime} from a text string such as {@code 10:15}.
     * <p>
     * The string must represent a valid time and is parsed using
     * {@link org.threeten.bp.format.DateTimeFormatters#isoLocalTime()}.
     *
     * @param text the text to parse such as "10:15:30", not null
     * @return the parsed local time, not null
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public static LocalTime parse(CharSequence text) {
        return parse(text, DateTimeFormatters.isoLocalTime());
    }

    /**
     * Obtains an instance of {@code LocalTime} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a time.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed local time, not null
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalTime.class);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a local time from the hour, minute, second and nanosecond fields.
     * <p>
     * This factory may return a cached value, but applications must not rely on this.
     *
     * @param hour  the hour-of-day to represent, validated from 0 to 23
     * @param minute  the minute-of-hour to represent, validated from 0 to 59
     * @param second  the second-of-minute to represent, validated from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, validated from 0 to 999,999,999
     * @return the local time, not null
     */
    private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {
        if ((minute | second | nanoOfSecond) == 0) {
            return HOURS[hour];
        }
        return new LocalTime(hour, minute, second, nanoOfSecond);
    }

    /**
     * Constructor, previously validated.
     *
     * @param hour  the hour-of-day to represent, validated from 0 to 23
     * @param minute  the minute-of-hour to represent, validated from 0 to 59
     * @param second  the second-of-minute to represent, validated from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, validated from 0 to 999,999,999
     */
    private LocalTime(int hour, int minute, int second, int nanoOfSecond) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
        this.nano = nanoOfSecond;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     * <p>
     * This checks if this time can be queried for the specified field.
     * If false, then calling the {@link #range(TemporalField) range} and
     * {@link #get(TemporalField) get} methods will throw an exception.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The supported fields are:
     * <ul>
     * <li>{@code NANO_OF_SECOND}
     * <li>{@code NANO_OF_DAY}
     * <li>{@code MICRO_OF_SECOND}
     * <li>{@code MICRO_OF_DAY}
     * <li>{@code MILLI_OF_SECOND}
     * <li>{@code MILLI_OF_DAY}
     * <li>{@code SECOND_OF_MINUTE}
     * <li>{@code SECOND_OF_DAY}
     * <li>{@code MINUTE_OF_HOUR}
     * <li>{@code MINUTE_OF_DAY}
     * <li>{@code HOUR_OF_AMPM}
     * <li>{@code CLOCK_HOUR_OF_AMPM}
     * <li>{@code HOUR_OF_DAY}
     * <li>{@code CLOCK_HOUR_OF_DAY}
     * <li>{@code AMPM_OF_DAY}
     * </ul>
     * All other {@code ChronoField} instances will return false.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.doIsSupported(TemporalAccessor)}
     * passing {@code this} as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field is supported on this time, false if not
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return ((ChronoField) field).isTimeField();
        }
        return field != null && field.doIsSupported(this);
    }

    /**
     * Gets the range of valid values for the specified field.
     * <p>
     * The range object expresses the minimum and maximum valid values for a field.
     * This time is used to enhance the accuracy of the returned range.
     * If it is not possible to return the range, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return
     * appropriate range instances.
     * All other {@code ChronoField} instances will throw a {@code DateTimeException}.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.doRange(TemporalAccessor)}
     * passing {@code this} as the argument.
     * Whether the range can be obtained is determined by the field.
     *
     * @param field  the field to query the range for, not null
     * @return the range of valid values for the field, not null
     * @throws DateTimeException if the range for the field cannot be obtained
     */
    @Override  // override for Javadoc
    public ValueRange range(TemporalField field) {
        return super.range(field);
    }

    /**
     * Gets the value of the specified field from this time as an {@code int}.
     * <p>
     * This queries this time for the value for the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this time, except {@code NANO_OF_DAY} and {@code MICRO_OF_DAY}
     * which are too large to fit in an {@code int} and throw a {@code DateTimeException}.
     * All other {@code ChronoField} instances will throw a {@code DateTimeException}.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.doGet(TemporalAccessor)}
     * passing {@code this} as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override  // override for Javadoc and performance
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return super.get(field);
    }

    /**
     * Gets the value of the specified field from this time as a {@code long}.
     * <p>
     * This queries this time for the value for the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this time.
     * All other {@code ChronoField} instances will throw a {@code DateTimeException}.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.doGet(TemporalAccessor)}
     * passing {@code this} as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == NANO_OF_DAY) {
                return toNanoOfDay();
            }
            if (field == MICRO_OF_DAY) {
                return toNanoOfDay() / 1000;
            }
            return get0(field);
        }
        return field.doGet(this);
    }

    private int get0(TemporalField field) {
        switch ((ChronoField) field) {
            case NANO_OF_SECOND: return nano;
            case NANO_OF_DAY: throw new DateTimeException("Field too large for an int: " + field);
            case MICRO_OF_SECOND: return nano / 1000;
            case MICRO_OF_DAY: throw new DateTimeException("Field too large for an int: " + field);
            case MILLI_OF_SECOND: return nano / 1000_000;
            case MILLI_OF_DAY: return (int) (toNanoOfDay() / 1000_000);
            case SECOND_OF_MINUTE: return second;
            case SECOND_OF_DAY: return toSecondOfDay();
            case MINUTE_OF_HOUR: return minute;
            case MINUTE_OF_DAY: return hour * 60 + minute;
            case HOUR_OF_AMPM: return hour % 12;
            case CLOCK_HOUR_OF_AMPM: int ham = hour % 12; return (ham % 12 == 0 ? 12 : ham);
            case HOUR_OF_DAY: return hour;
            case CLOCK_HOUR_OF_DAY: return (hour == 0 ? 24 : hour);
            case AMPM_OF_DAY: return hour / 12;
        }
        throw new DateTimeException("Unsupported field: " + field.getName());
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    public int getSecond() {
        return second;
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int getNano() {
        return nano;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted copy of this time.
     * <p>
     * This returns a new {@code LocalTime}, based on this one, with the time adjusted.
     * The adjustment takes place using the specified adjuster strategy object.
     * Read the documentation of the adjuster to understand what adjustment will be made.
     * <p>
     * A simple adjuster might simply set the one of the fields, such as the hour field.
     * A more complex adjuster might set the time to the last hour of the day.
     * <p>
     * The result of this method is obtained by invoking the
     * {@link TemporalAdjuster#adjustInto(Temporal)} method on the
     * specified adjuster passing {@code this} as the argument.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @return a {@code LocalTime} based on {@code this} with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public LocalTime with(TemporalAdjuster adjuster) {
        // optimizations
        if (adjuster instanceof LocalTime) {
            return (LocalTime) adjuster;
        }
        return (LocalTime) adjuster.adjustInto(this);
    }

    /**
     * Returns a copy of this time with the specified field set to a new value.
     * <p>
     * This returns a new {@code LocalTime}, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the hour, minute or second.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the adjustment is implemented here.
     * The supported fields behave as follows:
     * <ul>
     * <li>{@code NANO_OF_SECOND} -
     *  Returns a {@code LocalTime} with the specified nano-of-second.
     *  The hour, minute and second will be unchanged.
     * <li>{@code NANO_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified nano-of-day.
     *  This completely replaces the time and is equivalent to {@link #ofNanoOfDay(long)}.
     * <li>{@code MICRO_OF_SECOND} -
     *  Returns a {@code LocalTime} with the nano-of-second replaced by the specified
     *  micro-of-second multiplied by 1,000.
     *  The hour, minute and second will be unchanged.
     * <li>{@code MICRO_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified micro-of-day.
     *  This completely replaces the time and is equivalent to using {@link #ofNanoOfDay(long)}
     *  with the micro-of-day multiplied by 1,000.
     * <li>{@code MILLI_OF_SECOND} -
     *  Returns a {@code LocalTime} with the nano-of-second replaced by the specified
     *  milli-of-second multiplied by 1,000,000.
     *  The hour, minute and second will be unchanged.
     * <li>{@code MILLI_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified milli-of-day.
     *  This completely replaces the time and is equivalent to using {@link #ofNanoOfDay(long)}
     *  with the milli-of-day multiplied by 1,000,000.
     * <li>{@code SECOND_OF_MINUTE} -
     *  Returns a {@code LocalTime} with the specified second-of-minute.
     *  The hour, minute and nano-of-second will be unchanged.
     * <li>{@code SECOND_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified second-of-day.
     *  The nano-of-second will be unchanged.
     * <li>{@code MINUTE_OF_HOUR} -
     *  Returns a {@code LocalTime} with the specified minute-of-hour.
     *  The hour, second-of-minute and nano-of-second will be unchanged.
     * <li>{@code MINUTE_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified minute-of-day.
     *  The second-of-minute and nano-of-second will be unchanged.
     * <li>{@code HOUR_OF_AMPM} -
     *  Returns a {@code LocalTime} with the specified hour-of-am-pm.
     *  The AM/PM, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     * <li>{@code CLOCK_HOUR_OF_AMPM} -
     *  Returns a {@code LocalTime} with the specified clock-hour-of-am-pm.
     *  The AM/PM, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     * <li>{@code HOUR_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified hour-of-day.
     *  The minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     * <li>{@code CLOCK_HOUR_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified clock-hour-of-day.
     *  The minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     * <li>{@code AMPM_OF_DAY} -
     *  Returns a {@code LocalTime} with the specified AM/PM.
     *  The hour-of-am-pm, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     * </ul>
     * <p>
     * In all cases, if the new value is outside the valid range of values for the field
     * then a {@code DateTimeException} will be thrown.
     * <p>
     * All other {@code ChronoField} instances will throw a {@code DateTimeException}.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.doWith(Temporal, long)}
     * passing {@code this} as the argument. In this case, the field determines
     * whether and how to adjust the instant.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the result, not null
     * @param newValue  the new value of the field in the result
     * @return a {@code LocalTime} based on {@code this} with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public LocalTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case NANO_OF_SECOND: return withNano((int) newValue);
                case NANO_OF_DAY: return LocalTime.ofNanoOfDay(newValue);
                case MICRO_OF_SECOND: return withNano((int) newValue * 1000);
                case MICRO_OF_DAY: return plusNanos((newValue - toNanoOfDay() / 1000) * 1000);
                case MILLI_OF_SECOND: return withNano((int) newValue * 1000_000);
                case MILLI_OF_DAY: return plusNanos((newValue - toNanoOfDay() / 1000_000) * 1000_000);
                case SECOND_OF_MINUTE: return withSecond((int) newValue);
                case SECOND_OF_DAY: return plusSeconds(newValue - toSecondOfDay());
                case MINUTE_OF_HOUR: return withMinute((int) newValue);
                case MINUTE_OF_DAY: return plusMinutes(newValue - (hour * 60 + minute));
                case HOUR_OF_AMPM: return plusHours(newValue - (hour % 12));
                case CLOCK_HOUR_OF_AMPM: return plusHours((newValue == 12 ? 0 : newValue) - (hour % 12));
                case HOUR_OF_DAY: return withHour((int) newValue);
                case CLOCK_HOUR_OF_DAY: return withHour((int) (newValue == 24 ? 0 : newValue));
                case AMPM_OF_DAY: return plusHours((newValue - (hour / 12)) * 12);
            }
            throw new DateTimeException("Unsupported field: " + field.getName());
        }
        return field.doWith(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalTime} with the hour-of-day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return a {@code LocalTime} based on this time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    public LocalTime withHour(int hour) {
        if (this.hour == hour) {
            return this;
        }
        HOUR_OF_DAY.checkValidValue(hour);
        return create(hour, minute, second, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the minute-of-hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return a {@code LocalTime} based on this time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    public LocalTime withMinute(int minute) {
        if (this.minute == minute) {
            return this;
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return create(hour, minute, second, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the second-of-minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return a {@code LocalTime} based on this time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    public LocalTime withSecond(int second) {
        if (this.second == second) {
            return this;
        }
        SECOND_OF_MINUTE.checkValidValue(second);
        return create(hour, minute, second, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the nano-of-second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return a {@code LocalTime} based on this time with the requested nanosecond, not null
     * @throws DateTimeException if the nanos value is invalid
     */
    public LocalTime withNano(int nanoOfSecond) {
        if (this.nano == nanoOfSecond) {
            return this;
        }
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour, minute, second, nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalTime} with the time truncated.
     * <p>
     * Truncating the time returns a copy of the original time with fields
     * smaller than the specified unit set to zero.
     * For example, truncating with the {@link ChronoUnit#MINUTES minutes} unit
     * will set the second-of-minute and nano-of-second field to zero.
     * <p>
     * Not all units are accepted. The {@link ChronoUnit#DAYS days} unit and time
     * units with an exact duration can be used, other units throw an exception.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param unit  the unit to truncate to, not null
     * @return a {@code LocalTime} based on this time with the time truncated, not null
     * @throws DateTimeException if unable to truncate
     */
    public LocalTime truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        } else if (unit == ChronoUnit.DAYS) {
            return MIDNIGHT;
        } else if (unit.isDurationEstimated()) {
            throw new DateTimeException("Unit must not have an estimated duration");
        }
        long nod = toNanoOfDay();
        long dur = unit.getDuration().toNanos();
        if (dur >= NANOS_PER_DAY) {
            throw new DateTimeException("Unit must not be a date unit");
        }
        nod = (nod / dur) * dur;
        return ofNanoOfDay(nod);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date with the specified period added.
     * <p>
     * This method returns a new time based on this time with the specified period added.
     * The adder is typically {@link Period} but may be any other type implementing
     * the {@link TemporalAdder} interface.
     * The calculation is delegated to the specified adjuster, which typically calls
     * back to {@link #plus(long, TemporalUnit)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adder  the adder to use, not null
     * @return a {@code LocalTime} based on this time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public LocalTime plus(TemporalAdder adder) {
        return (LocalTime) adder.addTo(this);
    }

    /**
     * Returns a copy of this time with the specified period added.
     * <p>
     * This method returns a new time based on this time with the specified period added.
     * This can be used to add any period that is defined by a unit, for example to add hours, minutes or seconds.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount of the unit to add to the result, may be negative
     * @param unit  the unit of the period to add, not null
     * @return a {@code LocalTime} based on this time with the specified period added, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    @Override
    public LocalTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
                case MILLIS: return plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000_000);
                case SECONDS: return plusSeconds(amountToAdd);
                case MINUTES: return plusMinutes(amountToAdd);
                case HOURS: return plusHours(amountToAdd);
                case HALF_DAYS: return plusHours((amountToAdd % 2) * 12);
                case DAYS: return this;
            }
            throw new DateTimeException("Unsupported unit: " + unit.getName());
        }
        return unit.doPlus(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalTime} with the specified period in hours added.
     * <p>
     * This adds the specified number of hours to this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hoursToAdd  the hours to add, may be negative
     * @return a {@code LocalTime} based on this time with the hours added, not null
     */
    public LocalTime plusHours(long hoursToAdd) {
        if (hoursToAdd == 0) {
            return this;
        }
        int newHour = ((int) (hoursToAdd % HOURS_PER_DAY) + hour + HOURS_PER_DAY) % HOURS_PER_DAY;
        return create(newHour, minute, second, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in minutes added.
     * <p>
     * This adds the specified number of minutes to this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutesToAdd  the minutes to add, may be negative
     * @return a {@code LocalTime} based on this time with the minutes added, not null
     */
    public LocalTime plusMinutes(long minutesToAdd) {
        if (minutesToAdd == 0) {
            return this;
        }
        int mofd = hour * MINUTES_PER_HOUR + minute;
        int newMofd = ((int) (minutesToAdd % MINUTES_PER_DAY) + mofd + MINUTES_PER_DAY) % MINUTES_PER_DAY;
        if (mofd == newMofd) {
            return this;
        }
        int newHour = newMofd / MINUTES_PER_HOUR;
        int newMinute = newMofd % MINUTES_PER_HOUR;
        return create(newHour, newMinute, second, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in seconds added.
     * <p>
     * This adds the specified number of seconds to this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondstoAdd  the seconds to add, may be negative
     * @return a {@code LocalTime} based on this time with the seconds added, not null
     */
    public LocalTime plusSeconds(long secondstoAdd) {
        if (secondstoAdd == 0) {
            return this;
        }
        int sofd = hour * SECONDS_PER_HOUR +
                    minute * SECONDS_PER_MINUTE + second;
        int newSofd = ((int) (secondstoAdd % SECONDS_PER_DAY) + sofd + SECONDS_PER_DAY) % SECONDS_PER_DAY;
        if (sofd == newSofd) {
            return this;
        }
        int newHour = newSofd / SECONDS_PER_HOUR;
        int newMinute = (newSofd / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
        int newSecond = newSofd % SECONDS_PER_MINUTE;
        return create(newHour, newMinute, newSecond, nano);
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in nanoseconds added.
     * <p>
     * This adds the specified number of nanoseconds to this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToAdd  the nanos to add, may be negative
     * @return a {@code LocalTime} based on this time with the nanoseconds added, not null
     */
    public LocalTime plusNanos(long nanosToAdd) {
        if (nanosToAdd == 0) {
            return this;
        }
        long nofd = toNanoOfDay();
        long newNofd = ((nanosToAdd % NANOS_PER_DAY) + nofd + NANOS_PER_DAY) % NANOS_PER_DAY;
        if (nofd == newNofd) {
            return this;
        }
        int newHour = (int) (newNofd / NANOS_PER_HOUR);
        int newMinute = (int) ((newNofd / NANOS_PER_MINUTE) % MINUTES_PER_HOUR);
        int newSecond = (int) ((newNofd / NANOS_PER_SECOND) % SECONDS_PER_MINUTE);
        int newNano = (int) (newNofd % NANOS_PER_SECOND);
        return create(newHour, newMinute, newSecond, newNano);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this time with the specified period subtracted.
     * <p>
     * This method returns a new time based on this time with the specified period subtracted.
     * The subtractor is typically {@link Period} but may be any other type implementing
     * the {@link TemporalSubtractor} interface.
     * The calculation is delegated to the specified adjuster, which typically calls
     * back to {@link #minus(long, TemporalUnit)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param subtractor  the subtractor to use, not null
     * @return a {@code LocalTime} based on this time with the subtraction made, not null
     * @throws DateTimeException if the subtraction cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public LocalTime minus(TemporalSubtractor subtractor) {
        return (LocalTime) subtractor.subtractFrom(this);
    }

    /**
     * Returns a copy of this time with the specified period subtracted.
     * <p>
     * This method returns a new time based on this time with the specified period subtracted.
     * This can be used to subtract any period that is defined by a unit, for example to subtract hours, minutes or seconds.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount of the unit to subtract from the result, may be negative
     * @param unit  the unit of the period to subtract, not null
     * @return a {@code LocalTime} based on this time with the specified period subtracted, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    @Override
    public LocalTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalTime} with the specified period in hours subtracted.
     * <p>
     * This subtracts the specified number of hours from this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hoursToSubtract  the hours to subtract, may be negative
     * @return a {@code LocalTime} based on this time with the hours subtracted, not null
     */
    public LocalTime minusHours(long hoursToSubtract) {
        return plusHours(-(hoursToSubtract % HOURS_PER_DAY));
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in minutes subtracted.
     * <p>
     * This subtracts the specified number of minutes from this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutesToSubtract  the minutes to subtract, may be negative
     * @return a {@code LocalTime} based on this time with the minutes subtracted, not null
     */
    public LocalTime minusMinutes(long minutesToSubtract) {
        return plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY));
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in seconds subtracted.
     * <p>
     * This subtracts the specified number of seconds from this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToSubtract  the seconds to subtract, may be negative
     * @return a {@code LocalTime} based on this time with the seconds subtracted, not null
     */
    public LocalTime minusSeconds(long secondsToSubtract) {
        return plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY));
    }

    /**
     * Returns a copy of this {@code LocalTime} with the specified period in nanoseconds subtracted.
     * <p>
     * This subtracts the specified number of nanoseconds from this time, returning a new time.
     * The calculation wraps around midnight.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToSubtract  the nanos to subtract, may be negative
     * @return a {@code LocalTime} based on this time with the nanoseconds subtracted, not null
     */
    public LocalTime minusNanos(long nanosToSubtract) {
        return plusNanos(-(nanosToSubtract % NANOS_PER_DAY));
    }

    //-----------------------------------------------------------------------
    /**
     * Queries this time using the specified query.
     * <p>
     * This queries this time using the specified query strategy object.
     * The {@code TemporalQuery} object defines the logic to be used to
     * obtain the result. Read the documentation of the query to understand
     * what the result of this method will be.
     * <p>
     * The result of this method is obtained by invoking the
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} method on the
     * specified query passing {@code this} as the argument.
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     * @throws DateTimeException if unable to query (defined by the query)
     * @throws ArithmeticException if numeric overflow occurs (defined by the query)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        if (query == TemporalQueries.chrono() || query == TemporalQueries.zoneId() ||
                query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        return query.queryFrom(this);
    }

    /**
     * Adjusts the specified temporal object to have the same time as this object.
     * <p>
     * This returns a temporal object of the same observable type as the input
     * with the time changed to be the same as this.
     * <p>
     * The adjustment is equivalent to using {@link Temporal#with(TemporalField, long)}
     * passing {@link ChronoField#NANO_OF_DAY} as the field.
     * <p>
     * In most cases, it is clearer to reverse the calling pattern by using
     * {@link Temporal#with(TemporalAdjuster)}:
     * <pre>
     *   // these two lines are equivalent, but the second approach is recommended
     *   temporal = thisLocalTime.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalTime);
     * </pre>
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the target object to be adjusted, not null
     * @return the adjusted object, not null
     * @throws DateTimeException if unable to make the adjustment
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(NANO_OF_DAY, toNanoOfDay());
    }

    /**
     * Calculates the period between this time and another time in
     * terms of the specified unit.
     * <p>
     * This calculates the period between two times in terms of a single unit.
     * The start and end points are {@code this} and the specified time.
     * The result will be negative if the end is before the start.
     * The {@code Temporal} passed to this method must be a {@code LocalTime}.
     * For example, the period in hours between two times can be calculated
     * using {@code startTime.periodUntil(endTime, HOURS)}.
     * <p>
     * The calculation returns a whole number, representing the number of
     * complete units between the two times.
     * For example, the period in hours between 11:30 and 13:29 will only
     * be one hour as it is one minute short of two hours.
     * <p>
     * This method operates in association with {@link TemporalUnit#between}.
     * The result of this method is a {@code long} representing the amount of
     * the specified unit. By contrast, the result of {@code between} is an
     * object that can be used directly in addition/subtraction:
     * <pre>
     *   long period = start.periodUntil(end, HOURS);   // this method
     *   dateTime.plus(HOURS.between(start, end));      // use in plus/minus
     * </pre>
     * <p>
     * The calculation is implemented in this method for {@link ChronoUnit}.
     * The units {@code NANOS}, {@code MICROS}, {@code MILLIS}, {@code SECONDS},
     * {@code MINUTES}, {@code HOURS} and {@code HALF_DAYS} are supported.
     * Other {@code ChronoUnit} values will throw an exception.
     * <p>
     * If the unit is not a {@code ChronoUnit}, then the result of this method
     * is obtained by invoking {@code TemporalUnit.between(Temporal, Temporal)}
     * passing {@code this} as the first argument and the input temporal as
     * the second argument.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param endTime  the end time, which must be a {@code LocalTime}, not null
     * @param unit  the unit to measure the period in, not null
     * @return the amount of the period between this time and the end time
     * @throws DateTimeException if the period cannot be calculated
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public long periodUntil(Temporal endTime, TemporalUnit unit) {
        if (endTime instanceof LocalTime == false) {
            Objects.requireNonNull(endTime, "endTime");
            throw new DateTimeException("Unable to calculate period between objects of two different types");
        }
        LocalTime end = (LocalTime) endTime;
        if (unit instanceof ChronoUnit) {
            long nanosUntil = end.toNanoOfDay() - toNanoOfDay();  // no overflow
            switch ((ChronoUnit) unit) {
                case NANOS: return nanosUntil;
                case MICROS: return nanosUntil / 1000;
                case MILLIS: return nanosUntil / 1000_000;
                case SECONDS: return nanosUntil / NANOS_PER_SECOND;
                case MINUTES: return nanosUntil / NANOS_PER_MINUTE;
                case HOURS: return nanosUntil / NANOS_PER_HOUR;
                case HALF_DAYS: return nanosUntil / (12 * NANOS_PER_HOUR);
            }
            throw new DateTimeException("Unsupported unit: " + unit.getName());
        }
        return unit.between(this, endTime).getAmount();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a local date-time formed from this time at the specified date.
     * <p>
     * This combines this time with the specified date to form a {@code LocalDateTime}.
     * All possible combinations of date and time are valid.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param date  the date to combine with, not null
     * @return the local date-time formed from this time and the specified date, not null
     */
    public LocalDateTime atDate(LocalDate date) {
        return LocalDateTime.of(date, this);
    }

    /**
     * Returns an offset time formed from this time and the specified offset.
     * <p>
     * This combines this time with the specified offset to form an {@code OffsetTime}.
     * All possible combinations of time and offset are valid.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the offset to combine with, not null
     * @return the offset time formed from this time and the specified offset, not null
     */
    public OffsetTime atOffset(ZoneOffset offset) {
        return OffsetTime.of(this, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts the time as seconds of day,
     * from {@code 0} to {@code 24 * 60 * 60 - 1}.
     *
     * @return the second-of-day equivalent to this time
     */
    public int toSecondOfDay() {
        int total = hour * SECONDS_PER_HOUR;
        total += minute * SECONDS_PER_MINUTE;
        total += second;
        return total;
    }

    /**
     * Extracts the time as nanos of day,
     * from {@code 0} to {@code 24 * 60 * 60 * 1,000,000,000 - 1}.
     *
     * @return the nano of day equivalent to this time
     */
    public long toNanoOfDay() {
        long total = hour * NANOS_PER_HOUR;
        total += minute * NANOS_PER_MINUTE;
        total += second * NANOS_PER_SECOND;
        total += nano;
        return total;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this {@code LocalTime} to another time.
     * <p>
     * The comparison is based on the time-line position of the local times within a day.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * @param other  the other time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if {@code other} is null
     */
    @Override
    public int compareTo(LocalTime other) {
        int cmp = Integer.compare(hour, other.hour);
        if (cmp == 0) {
            cmp = Integer.compare(minute, other.minute);
            if (cmp == 0) {
                cmp = Integer.compare(second, other.second);
                if (cmp == 0) {
                    cmp = Integer.compare(nano, other.nano);
                }
            }
        }
        return cmp;
    }

    /**
     * Checks if this {@code LocalTime} is after the specified time.
     * <p>
     * The comparison is based on the time-line position of the time within a day.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is after the specified time
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isAfter(LocalTime other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this {@code LocalTime} is before the specified time.
     * <p>
     * The comparison is based on the time-line position of the time within a day.
     *
     * @param other  the other time to compare to, not null
     * @return true if this point is before the specified time
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isBefore(LocalTime other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this time is equal to another time.
     * <p>
     * The comparison is based on the time-line position of the time within a day.
     * <p>
     * Only objects of type {@code LocalTime} are compared, other types return false.
     * To compare the date of two {@code TemporalAccessor} instances, use
     * {@link ChronoField#NANO_OF_DAY} as a comparator.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other time
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalTime) {
            LocalTime other = (LocalTime) obj;
            return hour == other.hour && minute == other.minute &&
                    second == other.second && nano == other.nano;
        }
        return false;
    }

    /**
     * A hash code for this time.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        long nod = toNanoOfDay();
        return (int) (nod ^ (nod >>> 32));
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this time as a {@code String}, such as {@code 10:15}.
     * <p>
     * The output will be one of the following ISO-8601 formats:
     * <p><ul>
     * <li>{@code HH:mm}</li>
     * <li>{@code HH:mm:ss}</li>
     * <li>{@code HH:mm:ss.SSS}</li>
     * <li>{@code HH:mm:ss.SSSSSS}</li>
     * <li>{@code HH:mm:ss.SSSSSSSSS}</li>
     * </ul><p>
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this time, not null
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(18);
        int hourValue = hour;
        int minuteValue = minute;
        int secondValue = second;
        int nanoValue = nano;
        buf.append(hourValue < 10 ? "0" : "").append(hourValue)
            .append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
            if (nanoValue > 0) {
                buf.append('.');
                if (nanoValue % 1000_000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000_000) + 1000).substring(1));
                } else if (nanoValue % 1000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000) + 1000_000).substring(1));
                } else {
                    buf.append(Integer.toString((nanoValue) + 1000_000_000).substring(1));
                }
            }
        }
        return buf.toString();
    }

    /**
     * Outputs this time as a {@code String} using the formatter.
     * <p>
     * This time will be passed to the formatter
     * {@link DateTimeFormatter#print(TemporalAccessor) print method}.
     *
     * @param formatter  the formatter to use, not null
     * @return the formatted time string, not null
     * @throws DateTimeException if an error occurs during printing
     */
    public String toString(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.print(this);
    }

    //-----------------------------------------------------------------------
    private Object writeReplace() {
        return new Ser(Ser.LOCAL_TIME_TYPE, this);
    }

    /**
     * Defend against malicious streams.
     * @return never
     * @throws InvalidObjectException always
     */
    private Object readResolve() throws ObjectStreamException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        if (nano == 0) {
            if (second == 0) {
                if (minute == 0) {
                    out.writeByte(~hour);
                } else {
                    out.writeByte(hour);
                    out.writeByte(~minute);
                }
            } else {
                out.writeByte(hour);
                out.writeByte(minute);
                out.writeByte(~second);
            }
        } else {
            out.writeByte(hour);
            out.writeByte(minute);
            out.writeByte(second);
            out.writeInt(nano);
        }
    }

    static LocalTime readExternal(DataInput in) throws IOException {
        int hour = in.readByte();
        int minute = 0;
        int second = 0;
        int nano = 0;
        if (hour < 0) {
            hour = ~hour;
        } else {
            minute = in.readByte();
            if (minute < 0) {
                minute = ~minute;
            } else {
                second = in.readByte();
                if (second < 0) {
                    second = ~second;
                } else {
                    nano = in.readInt();
                }
            }
        }
        return LocalTime.of(hour, minute, second, nano);
    }

}
