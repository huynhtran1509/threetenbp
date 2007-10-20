/*
 * Copyright (c) 2007, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.calendar;

import java.io.Serializable;

import javax.time.calendar.field.HourOfDay;
import javax.time.period.PeriodView;
import javax.time.period.Periods;

/**
 * A calendrical representation of a time without a time zone, expressing
 * the time to nanosecond precision.
 * <p>
 * TimeHMS is an immutable calendrical that represents a time, often viewed as hour-minute-second.
 * This class does not store or represent a date or time zone.
 * Thus, for example, the value "14:45:32" can be stored in a TimeHMS.
 * <p>
 * TimeHMS is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 */
public final class TimeHMS
        implements Calendrical, Comparable<TimeHMS>, Serializable {

    /**
     * A serialization identifier for this instance.
     */
    private static final long serialVersionUID = 350703073L;

    /**
     * The second of day.
     */
    private final int secondOfDay;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of <code>TimeHMS</code>.
     * The seconds field will be set to zero.
     *
     * @param hourOfDay  the hour of day to represent, from 0 to 23
     * @param minuteOfHour  the minute of hour to represent, from 0 to 59
     * @return a TimeHMS object, never null
     * @throws IllegalCalendarFieldValueException if either field is invalid
     */
    public static TimeHMS time(int hourOfDay, int minuteOfHour) {
        return new TimeHMS(0);
    }

    /**
     * Obtains an instance of <code>TimeHMS</code>.
     *
     * @param hourOfDay  the hour of day to represent, from 0 to 23
     * @param minuteOfHour  the minute of hour to represent, from 0 to 59
     * @param secondOfMinute  the second of minute to represent, from 0 to 59
     * @return a TimeHMS object, never null
     * @throws IllegalCalendarFieldValueException if any field is invalid
     */
    public static TimeHMS time(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        return new TimeHMS(0);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param secondOfDay  the second of day to represent
     */
    private TimeHMS(int secondOfDay) {
        this.secondOfDay = secondOfDay;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the calendrical state which provides internal access to this
     * instance.
     *
     * @return the calendar state for this instance, never null
     */
    @Override
    public CalendricalState getCalendricalState() {
        return null;  // TODO
    }

    /**
     * Checks if the specified calendar field is supported.
     * <p>
     * This method queries whether this <code>TimeHMS</code> can
     * be queried using the specified calendar field.
     *
     * @param field  the field to query, not null
     * @return true if the field is supported
     */
    public boolean isSupported(TimeFieldRule field) {
        return field.isSupported(Periods.NANOS, Periods.DAYS);
    }

    /**
     * Gets the value of the specified calendar field.
     * <p>
     * This method queries the value of the specified calendar field.
     * If the calendar field is not supported then an exception is thrown.
     *
     * @param field  the field to query, not null
     * @return the value for the field
     * @throws UnsupportedCalendarFieldException if the field is not supported
     */
    public int get(TimeFieldRule field) {
        if (!isSupported(field)) {
            throw new UnsupportedCalendarFieldException("TimeHMS does not support field " + field.getName());
        }
        return 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour of day value.
     *
     * @return the hour of day, from 0 to 23
     */
    public int getHourOfDay() {
        return 0;
    }

    /**
     * Gets the hour of day instance.
     *
     * @return the hour of day, never null
     */
    public HourOfDay hourOfDay() {
        return HourOfDay.hourOfDay(getHourOfDay());
    }

    /**
     * Gets the minute of hour value.
     *
     * @return the minute of hour, from 0 to 59
     */
    public int getMinuteOfHour() {
        return 0;
    }

    /**
     * Gets the second of minute value.
     *
     * @return the second of minute, from 0 to 59
     */
    public int getSecondOfMinute() {
        return 0;
    }

    /**
     * Gets the nano of second value.
     *
     * @return the nano of second, from 0 to 999,999,999
     */
    public int getNanoOfSecond() {
        return 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this TimeHMS with the specified values altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param calendrical  the calendrical values to update to, not null
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS with(Calendrical calendrical) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the specified values altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param calendricals  the calendrical values to update to, no nulls
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS with(Calendrical... calendricals) {
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this TimeHMS with the hour of day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour of day to represent, from 0 to 23
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS withHourOfDay(int hourOfDay) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the minute of hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minuteOfHour  the minute of hour to represent, from 0 to 59
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS withMinuteOfHour(int minuteOfHour) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the second of minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondOfMinute  the second of minute to represent, from 0 to 59
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS withSecondOfMinute(int secondOfMinute) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the nano of second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano of second to represent, from 0 to 999,999,999
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS withNanoOfSecond(int nanoOfSecond) {
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this TimeHMS with the specified period added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param period  the period to add, not null
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plus(PeriodView period) {
        // TODO
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the specified periods added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param periods  the periods to add, no nulls
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plus(PeriodView... periods) {
        // TODO
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this TimeHMS with the specified number of hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plusHours(int hours) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the specified number of minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plusMinutes(int minutes) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the specified number of seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plusSeconds(int seconds) {
        return null;
    }

    /**
     * Returns a copy of this TimeHMS with the specified number of nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add
     * @return a new updated TimeHMS, never null
     */
    public TimeHMS plusNanos(int nanos) {
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this time to another time.
     *
     * @param other  the other time to compare to, not null
     * @return the comparator value, negative if less, postive if greater
     * @throws NullPointerException if <code>other</code> is null
     */
    public int compareTo(TimeHMS other) {
        return 0;
    }

    /**
     * Is this time after the specified time.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is after the specified time
     * @throws NullPointerException if <code>other</code> is null
     */
    public boolean isAfter(TimeHMS other) {
        return compareTo(other) > 0;
    }

    /**
     * Is this time before the specified time.
     *
     * @param other  the other time to compare to, not null
     * @return true if this point is before the specified time
     * @throws NullPointerException if <code>other</code> is null
     */
    public boolean isBefore(TimeHMS other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Is this time equal to the specified time.
     *
     * @param other  the other time to compare to, null returns false
     * @return true if this point is equal to the specified time
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TimeHMS) {
            TimeHMS timeHMS = (TimeHMS) other;
            return  true;
        }
        return false;
    }

    /**
     * A hashcode for this time.
     *
     * @return a suitable hashcode
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Outputs the string form of the time.
     *
     * @return the string form of the time
     */
    @Override
    public String toString() {
        return super.toString();
    }

}
