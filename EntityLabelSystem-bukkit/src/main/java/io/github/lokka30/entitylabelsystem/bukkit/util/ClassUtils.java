package io.github.lokka30.entitylabelsystem.bukkit.util;

import java.util.HashMap;

public class ClassUtils {

    private static final HashMap<String, Boolean> classpathExistsMap = new HashMap<>();

    public static boolean classExists(final String classpath) {
        if(!classpathExistsMap.containsKey(classpath)) {
            try {
                Class.forName(classpath);
                classpathExistsMap.put(classpath, true);
            } catch (ClassNotFoundException e) {
                classpathExistsMap.put(classpath, false);
            }
        }

        return classpathExistsMap.get(classpath);
    }

}
