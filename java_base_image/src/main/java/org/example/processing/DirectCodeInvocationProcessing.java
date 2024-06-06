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

    public DirectCodeInvocationProcessing() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, MalformedURLException {
        String processingClassName = System.getenv("FAAS_MAIN_CLASS");
        if (processingClassName == null)
            processingClassName = "org.faas.Main";

        String processingMethodName = System.getenv("FAAS_MAIN_METHOD");
        if (processingMethodName == null)
            processingMethodName = "echo";

        String processingJarFileName = System.getenv("FAAS_PATH");
        if (processingJarFileName == null)
            processingJarFileName = "D:\\Documents\\Facultate\\An 4\\licenta\\Coduri\\java_echo\\target\\java_echo-1.0-SNAPSHOT-jar-with-dependencies.jar";

        File file = new File(processingJarFileName);
        URLClassLoader child = new URLClassLoader(
                new URL[] {file.toURI().toURL()},
                ClassLoader.getSystemClassLoader()
        );

        Class<?> processingClass = Class.forName(processingClassName, true, child);
        faasMethod = processingClass.getMethod(processingMethodName, String.class);

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
}
