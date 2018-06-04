/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microchip.mplab.runmyscript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.InputOutput;
import javax.script.*;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

@ActionID(
        category = "File",
        id = "com.microchip.mplab.runmyscript.runMyScript"
)
@ActionRegistration(
        iconBase = "com/microchip/mplab/runmyscript/Actions-system-run-icon-16.png",
        displayName = "#CTL_runMyScript"
)
@ActionReference(path = "Toolbars/File", position = 500)
@Messages("CTL_runMyScript=runMyScript")
public final class RunMyScript implements ActionListener {

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
        InputOutput outputWindow = IOProvider.getDefault().getIO("Run My Script", false);
        outputWindow.closeInputOutput();        
        outputWindow = IOProvider.getDefault().getIO("Run My Script", true);
        
        outputWindow.select();

        
        int i = currentFilePath.lastIndexOf('.');
        switch( currentFilePath.substring(i+1)){
            case "js":
                // Run JAVA script
                outputWindow.getOut().println("Starting javascript: " + currentFilePath);
                run(outputWindow, currentFilePath,"javascript");
                break;
            case "py":
            case "jy":
                outputWindow.getOut().println("Starting jython script: " + currentFilePath);
                run(outputWindow, currentFilePath,"python");
                break;
            default:
                outputWindow.getOut().println("File extension is not supported! Suported extensions: .js .py .jy");
        }

    }
    
    private void run(InputOutput ioWindow, String currentFilePath, String scriptType){
                // Running Javascript
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(scriptType);  
        
        if(engine == null){
            ioWindow.getOut().println("Script engine not found! Script is not supported.");
        }
        
        //Binding the script engine with the output windows
        engine.getContext().setWriter(ioWindow.getOut());
        engine.getContext().setErrorWriter(ioWindow.getErr());
        
        
        try {
            // evaluate JavaScript code
            engine.eval(new FileReader(currentFilePath));
        } catch (ScriptException ex) {
            ioWindow.getOut().println("Jython runtime error!");
            ioWindow.getOut().println(ex);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
       
    }
    
    
}
