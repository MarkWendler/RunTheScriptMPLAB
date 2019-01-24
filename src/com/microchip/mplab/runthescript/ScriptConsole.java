/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microchip.mplab.runthescript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Mark
 */
@ActionID(
        category = "File",
        id = "com.microchip.mplab.runthescript.ScriptConsoleOpen"
)
@ActionRegistration(
        iconBase = "com/microchip/mplab/runthescript/Actions-system-run-icon-24.png",
        displayName = "Open runTheScriptConsole"
)
@ActionReferences({
    @ActionReference(path = "Menu/Window"),   //Add to tools menu
    @ActionReference(path = "Menu/Tools")   //Add to tools menu
})
public class ScriptConsole implements ActionListener {

 @Override
    public void actionPerformed(ActionEvent e) {
    
        InputOutput outputWindow = IOProvider.getDefault().getIO("Run The Script", false);
        
        outputWindow.select();
        
        outputWindow.getOut().println("Hello MyConsole");
        
        //outputWindow.setInputVisible(true);

    }
    
}
