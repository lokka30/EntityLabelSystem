package io.github.lokka30.entitylabelsystem.bukkit.util;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class ReflectionUtil {

    public static Field getDeclaredField(final Class<?> clazz, final String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
