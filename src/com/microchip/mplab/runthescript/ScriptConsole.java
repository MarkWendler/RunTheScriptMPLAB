/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microchip.mplab.runthescript;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import jep.Interpreter;
import jep.JepConfig;
import jep.SharedInterpreter;
import jep.SubInterpreter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
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
    @ActionReference(path = "Menu/Tools") //Add to tools menu
})
public class ScriptConsole implements ActionListener, Runnable {

    @Override
    public void actionPerformed(ActionEvent e) {

        run();
    }

    /**
     * Open Output Window and ask for some input.
     */
    public static void consoleThread() throws IOException {
        assert !EventQueue.isDispatchThread();
        InputOutput ioWindow = IOProvider.getDefault().getIO("ioName", true);
        ioWindow.select();

        Util.printPATH(ioWindow);

        Util.checkPythonEnv(ioWindow);

        try {
            BufferedReader br = new BufferedReader(ioWindow.getIn());

//create outputStream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ByteArrayOutputStream streamError = new ByteArrayOutputStream();
            JepConfig config = new JepConfig();
            config.redirectStdout(stream);
            config.redirectStdErr(streamError);
//        SharedInterpreter.setConfig(config);  

            Interpreter interp = new SubInterpreter(config);


            ioWindow.getOut().println("--- Welcome to JEP console emulator  --- Help: ?");
            ioWindow.getOut().print(">>>");

            String consoleInput = null;
            while (true) {
                consoleInput = br.readLine();
                if (consoleInput.equalsIgnoreCase("exit")) {

                    break;
                }
                if (consoleInput.equalsIgnoreCase("?")) {
                    ioWindow.getOut().print("exit: closes the interpreter ");
                    ioWindow.getOut().print("x2c_scope: closes the interpreter ");
                    ioWindow.getOut().print("x2c_symbol: closes the interpreter ");
                } else { //run script
                    try {
                        interp.eval(consoleInput);
                    } catch (Exception ex) {
                        ioWindow.getErr().print(ex.toString());
                    }
                }
                stream.flush();
                streamError.flush();
                ioWindow.getErr().println(streamError.toString());
                ioWindow.getOut().println(stream.toString());
                ioWindow.getOut().print(">>>");
                stream.reset();
                streamError.reset();
            }
            interp.close();
            br.close();
            ioWindow.closeInputOutput();
        } catch (Exception ex) {
            ioWindow.getErr().print(ex.toString());
        }

        //io.getOut().close();
        //io.getErr().close();
        //io.closeInputOutput();
    }

    @Override
    public void run() {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    consoleThread();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

}
