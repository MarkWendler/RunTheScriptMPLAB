## RunTheScriptMPLAB  ([Releases](https://github.com/MarkWendler/RunTheScriptMPLAB/releases))

This is a plugin developed for MPLAB X

* Adds a "Run THe Script" button on the top bar.
* The plugin interprets the currently opened and active file with the JYTHON or the built in JavaScript engine.
* The plugin uses the file extension to decide which interpreted must be used:
* *.py, *.jy -> are interpreted with jython engine.
* *.js -> are interpreted by nashron

### Example scripts
* Jython examples: https://gist.github.com/MarkWendler/1a000238f1353c3fdd3550d4b3efe03a
* JavaScript examples: https://gist.github.com/MarkWendler/767b56a6a95bc7a405fd3019d852a772

### Usage
1. Install the plugin with MPLA X plugin manager
2. Restart MPLAB X
3. Create or use an existing MPLAB X project
3. Create a new script file in the project with the needed extension. (example in the important files folder) 
4. Edit the file, use the examples
5. Push the "run the script" button in the top bar. Make sure the script file is the active one.
