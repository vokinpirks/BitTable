package com.vokinpirks.util;

import java.util.Collection;

import static java.util.Arrays.stream;

public class Enums {
    public static <E extends Enum<E>> String[] namesOf(final Class<E> clazz) {
        return stream(clazz.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    public static <E extends Enum<E>> String[] namesOf(final Collection<E> enumValues) {
        return enumValues.stream()
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
