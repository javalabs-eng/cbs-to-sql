package org.javalabs.tools.cbs2sql.cli.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 *
 * @author schan280
 */
public class CbToPgCli {
    
    private static final Helper HELPER = new Helper();
    
    public static void main(String[] args) throws Exception {
        BufferedReader br = null;
        String line = null;
            
        try {
            // Display the cli prompt
            ConsoleWriter.prompt();
            
            // Print the welcome message
            ConsoleWriter.println(HELPER.welcome());
            
            ConsoleWriter.prompt();
            
            br = new BufferedReader(new InputStreamReader(System.in));
            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.equals("quit") || line.equals("bye") || line.equals("exit")) {
                        break;
                    }
                    // Request for help.
                    // <openapi> help
                    if (line.equals("help")) {
                        ConsoleWriter.println(HELPER.welcome());
                    }
                    else {
                        // Execute specific command and supply the options
                        // <openapi> swagger -create -routing-file routing-config.xml -model-lib /path/to/ninja-rest.jar -out-file ~/openapi.yaml
                        String[] arr = line.split(" ");
                        if ((arr.length == 1 && HELPER.commands().contains(arr[0]))
                                || (arr.length == 2 && HELPER.commands().contains(arr[0]) && arr[1].equals("help"))) {

                            // Print the options for the chosen command
                            ConsoleWriter.println(HELPER.options(arr[0]));
                        }
                        else if (arr.length > 2 && HELPER.commands().contains(arr[0]) && !arr[1].equals("help")) {
                            // Captured the arguments and possibly execute the command.
                            String[] options = new String[arr.length - 1];
                            System.arraycopy(arr, 1, options, 0, options.length);

                            ExecutorBase cmd = HELPER.command(arr[0]);
                            cmd.start(options);
                        }
                        else {
                            if (line.trim().length() != 0) {
                                ConsoleWriter.println("Invalid command. Type help to see the available commands");
                            }
                        }
                    }
                }
                catch (Exception e) {
                    ConsoleWriter.println(e.getMessage());
                }
                ConsoleWriter.prompt();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
