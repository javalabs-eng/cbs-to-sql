package org.javalabs.tools.cbs2sql.cli.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 * Entry point for the Couchbase-to-SQL command-line application.
 *
 * <p>
 * This class implements the interactive command-line interface (CLI), allowing users to execute schema generation,
 * Couchbase export, and Couchbase import commands. It reads user input, validates commands, displays help information,
 * and delegates command execution to the appropriate executor.</p>
 *
 * <p>
 * The CLI continues processing commands until the user issues one of the supported exit commands such as {@code quit},
 * {@code exit}, or {@code bye}.</p>
 *
 * @author schan280
 */
public class CbToPgCli {

    private static final Helper HELPER = new Helper();

    /**
     * Starts the interactive command-line interface.
     *
     * <p>
     * This method displays the welcome banner, accepts commands from standard input, processes help requests, and
     * delegates valid commands to their corresponding executors. The application terminates when the user enters
     * {@code quit}, {@code exit}, or {@code bye}.</p>
     *
     * @param args the command-line arguments; currently ignored because the application operates in interactive mode
     * @throws Exception if an unrecoverable error occurs during command execution
     */
    public static void main(String[] args) throws Exception {
        try {
            // If user passes the entire command through command line, execute it.
            if (args.length > 2) {
                // cb-export -c deusw1cbecpn000037 -h lpdecpdb0005347.phx.aexp.com -u clientsetupuser -w ERzGm99xUk76ehAt -b clientSetupDB -o /Users/schan280/Projects/cbs-to-sql/data -n type -d port
                ExecutorBase cmd = HELPER.command(args[0]);
                if (cmd == null) {
                    ConsoleWriter.println(String.format("Invalid command name %s", args[0]));
                    System.exit(-1);
                }
                String[] options = new String[args.length - 1];
                System.arraycopy(args, 1, options, 0, options.length);

                cmd.start(options);
            }
            else {
                cli();
            }
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // Due to bug in couchbase java client background threads are not gracefully shutting down...
        // Runtime.getRuntime().halt(0);
    }
    
    private static void cli() throws IOException {
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
        finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
