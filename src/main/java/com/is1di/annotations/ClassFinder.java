package com.is1di.annotations;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassFinder {

    public static <T> List<Class<? extends T>> findAllClassesUsingClassLoader(String packageName, Class<T> clazz) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        List<String> list = reader.lines().collect(Collectors.toList());
        List<Class<? extends T>> result = new ArrayList<>();
        if (list.isEmpty()) {
            return new ArrayList<>();
        } else {
            for (String pkgOrClass : list) {
                if (pkgOrClass.endsWith(".class")) {
                    Class<T> mbNullClass = getClass(pkgOrClass, packageName, clazz);
                    if (mbNullClass != null) {
                        result.add(mbNullClass);
                    }
                } else {
                    result.addAll(findAllClassesUsingClassLoader(packageName + "." + pkgOrClass, clazz));
                }
            }
        }
        return result;
    }

    private static <T> Class<T> getClass(String className, String packageName, Class<T> clazz) {
        try {
            var cl = Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
            if (clazz.isAssignableFrom(cl) && !clazz.equals(cl)) {
                return (Class<T>) cl;
            }
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
