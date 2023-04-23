package com.shinoaki.demo.spring3webmaventest.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Xun
 * @date 2023/4/22 1:04 星期六
 */
public class PackageClassUtils {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<Class<?>> subClasses = getClasses("com.shinoaki.demo.spring3webmaventest");
        subClasses.forEach(System.out::println);
    }

    public static List<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        List<Class<?>> classesList = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            //判断是否是jar
            if (resource.getProtocol().equals("jar")) {
                try (JarFile jarFile = new JarFile(resource.getFile().split("!")[0].substring(5))) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class") && name.startsWith(packagePath)) {
                            /*String className = name.replace("/", ".").substring(0, name.length() - 6);
                            Class<?> clazz = Class.forName(className);
                            if (!clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation() && packageName.equals(clazz.getPackage().getName())) {
                                subClasses.add(clazz);
                            }*/
                            toClass(name, packageName).ifPresent(classesList::add);
                        }
                    }
                }
            } else if (resource.getProtocol().equals("file")) {
                //如果是文件则进入解析
                String path = resource.getPath();
                path = path.replace("%20", " ");
                List<Class<?>> classes = findClasses(new File(path), packageName);
                classesList.addAll(classes);
            }
        }
        return classesList;
    }

    /**
     * 加载类
     *
     * @param directory   文件路径
     * @param packageName 包路径
     * @return 解析结果
     * @throws ClassNotFoundException class反射异常
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else {
                    /*String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (!clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation() && packageName.equals(clazz.getPackage().getName())) {
                        classes.add(clazz);
                    }*/
                    toClass(packageName + "." + file.getName(), packageName).ifPresent(classes::add);
                }
            }
        }
        return classes;
    }

    private static Optional<Class<?>> toClass(String fileName, String packageName) throws ClassNotFoundException {
        if (fileName.endsWith(".class")) {
            String className = fileName.replace("/", ".").substring(0, fileName.length() - 6);
            Class<?> clazz = Class.forName(className);
            if (!clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation() && packageName.equals(clazz.getPackage().getName())) {
                return Optional.of(clazz);
            }
        }
        return Optional.empty();
    }
}
