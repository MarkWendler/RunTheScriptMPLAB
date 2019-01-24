/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microchip.mplab.runthescript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.InputOutput;
import javax.script.*;
import jep.Jep;
import jep.JepConfig;
import jep.JepException;
import jep.PyConfig;
import org.openide.awt.ActionReferences;
import org.openide.cookies.EditorCookie;
import org.openide.windows.IOProvider;
import org.python.util.PythonInterpreter;

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
     @ActionReference(path = "Editors/text/x-java/Popup")   //Add to file popup
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
        switch( currentFilePath.substring(i+1)){
            case "js":
                // Run JAVA script
                outputWindow.getOut().println("Starting javascript: " + currentFilePath);
                runJava(outputWindow, currentFilePath);
                break;
            case "py":
                outputWindow.getOut().println("Starting pythonscript: " + currentFilePath);
                runPython(outputWindow, currentFilePath);
                
            case "jy":
                // Run python script
                outputWindow.getOut().println("Starting jython script: " + currentFilePath);
                runJython(outputWindow, currentFilePath);
                break;
            default:
                outputWindow.getOut().println("File extension is not supported! Suported extensions: .js .py .jy");
        }
    }

    private void runPython(InputOutput ioWindow, String currentFilePath){
                // Running Jython script
                
        System.setProperty("java.library.path", "C:\\Users\\Mark\\AppData\\Local\\Programs\\Python\\Python37-32\\Lib\\site-packages\\jep");
                printLibPath(ioWindow); 
        printClassPath(ioWindow);
        
        System.load("C:\\Users\\Mark\\AppData\\Local\\Programs\\Python\\Python37-32\\Lib\\site-packages\\jep\\jep.dll");
        PyConfig conf = new PyConfig();
        conf.setPythonHome("C:\\Users\\Mark\\AppData\\Local\\Programs\\Python\\Python37-32");   
        
        
        JepConfig config = new JepConfig();
        config.setRedirectOutputStreams(true);
        

        
        Jep jepInterpreter = null;
        try {
            jepInterpreter = new Jep(config);
        } catch (JepException ex) {
            ioWindow.getOut().println("Python JEP not found! Try to set python path and install jep: https://github.com/ninia/jep/wiki");
            return;
        }
            
        //Binding the script engine with the output windows
        //jepInterpreter.
         //       setErr(ioWindow.getOut());
        //scriptInterpreter.setOut(ioWindow.getErr());
        
        
        try {
            // evaluate JavaScript code
            jepInterpreter.eval(currentFilePath);
        } catch(Exception ex){
            ioWindow.getOut().println("Runtime error! Check the following description:");
            ioWindow.getOut().println(ex);
        } 
       
    }

    
    private void runJava(InputOutput ioWindow, String currentFilePath){
                // Running Javascript
                
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");  
        
        if(engine == null){
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
    
    private void runJython(InputOutput ioWindow, String currentFilePath){
                // Running Jython script
        PythonInterpreter scriptInterpreter = new PythonInterpreter(); 
        
        if(scriptInterpreter == null){
            ioWindow.getOut().println("Jython script engine not found! Script is not supported.");
            return;
        }
        
        //Binding the script engine with the output windows
        scriptInterpreter.setErr(ioWindow.getOut());
        scriptInterpreter.setOut(ioWindow.getErr());
        
        
        try {
            // evaluate JavaScript code
            scriptInterpreter.execfile(currentFilePath);
        } catch(Exception ex){
            ioWindow.getOut().println("Runtime error! Check the following description:");
            ioWindow.getOut().println(ex);
        } 
       
    }
    
        public static void printClassPath(InputOutput ioWindow){


        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
        	ioWindow.getOut().println(url.getFile());
        }
    }
    public static void printLibPath(InputOutput ioWindow){
        
        String property = System.getProperty("java.library.path");
        StringTokenizer parser = new StringTokenizer(property, ";");
        while (parser.hasMoreTokens()) {
            ioWindow.getOut().println(parser.nextToken());
        }
    }
}
