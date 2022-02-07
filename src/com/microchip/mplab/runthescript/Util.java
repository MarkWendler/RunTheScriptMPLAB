/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microchip.mplab.runthescript;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorPrint;
import org.openide.windows.InputOutput;

/**
 *
 * @author M18034
 */
public class Util {

    public static int checkPythonEnv(InputOutput io) {
        String ret = null;
        int retVal = 0;
        try {

            String cmd = "\"import" + " " + "importlib.util;" + " " + "print(importlib.util.find_spec('jep').origin[:-(len('__init__.py')+1)])\"";
            System.out.println("exec: " + cmd);

            ProcessBuilder builder = new ProcessBuilder("python", "-c", cmd);
            Process p = builder.start();

            Runtime.getRuntime().exec("cmd scilab ...");

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader in_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            int exitVal = p.waitFor();
            if (exitVal != 0) {
                io.getOut().println("Python exec return: " + exitVal);
                io.getErr().println("Python not found. Did you add Python and PIP to PATH?");
                String errorstr = in_error.lines().collect(Collectors.joining());
                io.getErr().println("Error" + errorstr);
                retVal++; //Count error
            } else { //Python installed correctly
                ret = in.readLine();
                if (ret != null) {
                    io.getOut().println("Used JEP path:" + ret);
                    /*
                    try {
                        addPath(ret);
                    } catch (Exception exception) {
                        retVal++; //Count error
                    }
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    URL[] urls = ((URLClassLoader) cl).getURLs();
                    io.getOut().println("SystemClassLoader Paths:");
                    for (URL url : urls) {
                        io.getOut().println(url.getFile());
                    }
                    */
                    addLibraryPath(ret);
                    io.getOut().println("java.library.path: " + System.getProperty("java.library.path"));
                } else {
                    io.getErr().println("JEP not installed? --> pip install jep");
                    //TODO install jep automatically?
                    retVal++; //Count error
                }
            }

        } catch (IOException ex) {
            io.getErr().println("Python not found. Did you add Python and PIP to PATH?");
            retVal++;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            retVal++;
        }
        if (retVal != 0) {
            io.getErr().println("------ DEPENDENCY CHECK FAILED -------");
        } else {
            try {
                IOColorPrint.print(io, "------ DEPENDENCY CHECK OK -------\n", Color.GREEN);
            } catch (Exception ex) {
                io.getErr().println("------ DEPENDENCY CHECK OK, But no color print-------");
            }
        }

        return 0; //Environment check succeed
    }

    public static void printPATH(InputOutput io) {
        io.getOut().println("-------- RUNTIME PATHS ---------- ");
        String path = System.getenv("PATH");

        if (path == null) {
            io.getErr().println("PATH environment variable is not defined!");
        } else {
            io.getOut().println(path);
        }
        io.getOut().println("--------------------------------");
    }

    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    private static void addLibraryPath(String pathToAdd) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        //check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }

    private static void addPath(String s) throws Exception {
        File f = new File(s);
        URI u = f.toURI();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u.toURL()});
    }
}
