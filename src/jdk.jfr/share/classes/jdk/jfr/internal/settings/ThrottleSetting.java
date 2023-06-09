/*
 * Copyright (c) 2020, 2023, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, Datadog, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.jfr.internal.settings;

import java.util.Objects;
import java.util.Set;

import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.MetadataDefinition;
import jdk.jfr.Name;
import jdk.jfr.internal.PlatformEventType;
import jdk.jfr.internal.Type;

@MetadataDefinition
@Label("Event Emission Throttle")
@Description("Throttles the emission rate for an event")
@Name(Type.SETTINGS_PREFIX + "Throttle")
public final class ThrottleSetting extends JDKSettingControl {
    static final String OFF_TEXT = "off";
    private static final long OFF = -2;
    private String value = "0/s";
    private final PlatformEventType eventType;

    public ThrottleSetting(PlatformEventType eventType) {
       this.eventType = Objects.requireNonNull(eventType);
    }

    @Override
    public String combine(Set<String> values) {
        long max = OFF;
        String text = "off";
        for (String value : values) {
            long l = parseValueSafe(value);
            if (l > max) {
                text = value;
                max = l;
            }
        }
        return text;
    }

    private static long parseValueSafe(String s) {
        long value = 0L;
        try {
            value = parseThrottleValue(s);
        } catch (NumberFormatException nfe) {
        }
        return value;
    }

    @Override
    public void setValue(String s) {
        long size = 0;
        long millis = 1000;
        try {
            size = parseThrottleValue(s);
            millis = parseThrottleTimeUnit(s);
            this.value = s;
        } catch (NumberFormatException nfe) {
        }
        eventType.setThrottle(size, millis);
    }

    @Override
    public String getValue() {
        return value;
    }

    private static long parseThrottleValue(String s) {
        if (s.equals(OFF_TEXT)) {
            return OFF;
        }
        String parsedValue = parseThrottleString(s, true);
        long normalizedValue = 0;
        try {
            normalizedValue = ThrottleUnit.normalizeValueAsMillis(Long.parseLong(parsedValue), s);
        } catch (NumberFormatException nfe) {
            throwThrottleNumberFormatException(s);
        }
        return normalizedValue;
    }

    private static long parseThrottleTimeUnit(String s) {
        return ThrottleUnit.asMillis(s);
    }

    // Expected input format is "x/y" where x is a non-negative long
    // and y is a time unit. Split the string at the delimiter.
    static String parseThrottleString(String s, boolean value) {
        String[] split = s.split("/");
        if (split.length != 2) {
            throwThrottleNumberFormatException(s);
        }
        return value ? split[0].trim() : split[1].trim();
    }

    private static void throwThrottleNumberFormatException(String s) {
        throw new NumberFormatException("'" + s + "' is not valid. Should be a non-negative numeric value followed by a delimiter. i.e. '/', and then followed by a unit e.g. 100/s.");
    }
}

