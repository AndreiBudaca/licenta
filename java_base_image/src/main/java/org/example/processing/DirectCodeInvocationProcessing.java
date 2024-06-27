package org.example.processing;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DirectCodeInvocationProcessing implements Processing {

    private final Method faasMethod;
    private final Object faasObject;

    public DirectCodeInvocationProcessing() throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, InstantiationException, MalformedURLException {
        File file = new File(EnvConfiguration.processingJarFileName);
        URLClassLoader child = new URLClassLoader(
                new URL[] {file.toURI().toURL()},
                ClassLoader.getSystemClassLoader()
        );

        Class<?> processingClass = Class.forName(EnvConfiguration.processingClassName, true, child);
        faasMethod = processingClass.getMethod(EnvConfiguration.processingMethodName, String.class);

        if (Modifier.isStatic(faasMethod.getModifiers())) {
            faasObject = null;
        }
        else {
            System.out.println("Creating an object...");
            Constructor<?> faasConstructor = processingClass.getConstructor();
            faasObject = faasConstructor.newInstance();
        }
    }

    @Override
    public String process(String data) throws InvocationTargetException, IllegalAccessException {
        return faasMethod.invoke(faasObject, data).toString();
    }

    private static class EnvConfiguration {
        public final static String processingClassName = System.getenv("FAAS_MAIN_CLASS") == null ?
                "org.faas.Main" :
                System.getenv("FAAS_MAIN_CLASS");

        public final static String processingMethodName = System.getenv("FAAS_MAIN_METHOD") == null ?
                "echo" :
                System.getenv("FAAS_MAIN_METHOD");

        public final static String processingJarFileName = System.getenv("FAAS_PATH") == null ?
                "D:\\Documents\\Facultate\\An 4\\licenta\\Coduri\\java_echo\\target\\java_echo-1.0-SNAPSHOT-jar-with-dependencies.jar" : System.getenv("FAAS_PATH");
    }
}
