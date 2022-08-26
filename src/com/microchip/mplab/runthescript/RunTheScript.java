/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microchip.mplab.runthescript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.InputOutput;
import javax.script.*;
import jep.Interpreter;
import jep.JepConfig;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import jep.SubInterpreter;
import org.openide.awt.ActionReferences;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.python.util.PythonInterpreter;
import com.microchip.mplab.mdbcore.symbolview.interfaces.SymbolViewProvider;

@ActionID(
        category = "File",
        id = "com.microchip.mplab.runthescript.runTheScript"
)
@ActionRegistration(
        iconBase = "com/microchip/mplab/runthescript/Actions-system-run-icon-24.png",
        displayName = "runTheScript"
)
@ActionReferences({
    @ActionReference(path = "Toolbars/File", position = 500), //Add to buttons
    @ActionReference(path = "Menu/Tools"),   //Add to tools menu
    @ActionReference(path = "Editors/text/x-java/Popup") //Add to file popup
})

public final class RunTheScript implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();

        String currentFilePath = "";
        try {
            DataObject dataLookup = activeTC.getLookup().lookup(DataObject.class);
            EditorCookie cookie = dataLookup.getLookup().lookup(EditorCookie.class);
            cookie.saveDocument();
            currentFilePath = FileUtil.toFile(dataLookup.getPrimaryFile()).getAbsolutePath();
        } catch (Exception ex) {
            currentFilePath = "";
            ex.printStackTrace();
        }

        // Get output window
        InputOutput outputWindow = IOProvider.getDefault().getIO("Run The Script", false);

        outputWindow.select();

        //Find file type
        int i = currentFilePath.lastIndexOf('.');
        switch (currentFilePath.substring(i + 1)) {
            case "js":
                // Run JAVA script
                outputWindow.getOut().println("Starting javascript: " + currentFilePath);
                runJava(outputWindow, currentFilePath);
                break;
            case "py":
                outputWindow.getOut().println("Starting pythonscript: " + currentFilePath);
                runPython(outputWindow, currentFilePath);
                break;
            case "jy":
                // Run python script
                outputWindow.getOut().println("Starting jython script: " + currentFilePath);
                runJython(outputWindow, currentFilePath);
                break;
            default:
                outputWindow.getOut().println("File extension is not supported! Suported extensions: .js .py .jy");
        }
    }

    private void runPython(InputOutput ioWindow, String currentFilePath) {
        // Running Jython script
        Util.printPATH(ioWindow);

        Util.checkPythonEnv(ioWindow);

        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JepConfig config = new JepConfig();
        config.redirectStdout(stream);
        try {
            SharedInterpreter.setConfig(config);
        }
        catch(jep.JepException ex){
            //ioWindow.getErr().println("Shared interpreter already created. Does not set config again.");
            // make silent if the config is set already.
        }

        //Binding the script engine with the output windows
        //jepInterpreter.
        //       setErr(ioWindow.getOut());
        //scriptInterpreter.setOut(ioWindow.getErr());
        ioWindow.getOut().println("File: " + currentFilePath);
        try (Interpreter interp = new SharedInterpreter()) {
            // evaluate JavaScript code
            
            interp.runScript(currentFilePath);
            stream.flush();
            ioWindow.getOut().print(stream.toString());
            stream.reset();
            interp.close();
        } catch (Throwable ex) {
            ioWindow.getErr().println("Runtime error! Check the following description:");
            ioWindow.getErr().println(ex.getMessage());
        }
        

    }

    private void runJava(InputOutput ioWindow, String currentFilePath) {
        // Running Javascript

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        if (engine == null) {
            ioWindow.getOut().println("Python script engine not found! Script is not supported.");
            return;
        }

        //Binding the script engine with the output windows
        engine.getContext().setWriter(ioWindow.getOut());
        engine.getContext().setErrorWriter(ioWindow.getErr());

        try {
            // evaluate JavaScript code
            engine.eval(new FileReader(currentFilePath));
        } catch (Exception ex) {
            ioWindow.getOut().println("Runtime error! Check the following description:");
            ioWindow.getOut().println(ex);
        }
    }

    private void runJython(InputOutput ioWindow, String currentFilePath) {
        // Running Jython script
        PythonInterpreter scriptInterpreter = new PythonInterpreter();

        if (scriptInterpreter == null) {
            ioWindow.getOut().println("Jython script engine not found! Script is not supported.");
            return;
        }

        //Binding the script engine with the output windows
        scriptInterpreter.setErr(ioWindow.getOut());
        scriptInterpreter.setOut(ioWindow.getErr());

        try {
            // evaluate JavaScript code
            scriptInterpreter.execfile(currentFilePath);
        } catch (Exception ex) {
            ioWindow.getOut().println("Runtime error! Check the following description:");
            ioWindow.getOut().println(ex);
        }

    }

    public static void printClassPath(InputOutput ioWindow) {

        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader) cl).getURLs();

        for (URL url : urls) {
            ioWindow.getOut().println(url.getFile());
        }
    }

    public static void printLibPath(InputOutput ioWindow) {

        String property = System.getProperty("java.library.path");
        StringTokenizer parser = new StringTokenizer(property, ";");
        while (parser.hasMoreTokens()) {
            ioWindow.getOut().println(parser.nextToken());
        }
    }
}
