package org.javalabs.tools.cbs2sql.cli.util;

import java.io.PrintStream;

/**
 * Console writer.
 * 
 * <p>
 * It leverages the underlying {@link PrintStream} attached to output device to print s line.
 * It is the most common and straightforward way to print output to the console.
 *
 * @author Sudiptasish Chanda
 */
public class ConsoleWriter {
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static final PrintStream CONSOLE = System.out;
    private static final Long PAUSE = Long.valueOf(System.getProperty("pause.time", "1"));
    
    /**
     * Print a text on the console.
     * It will add a new line at the end of the paragraph/text.
     * 
     * @param text  Text to be printed 
     */
    public static void println(String text) {
        CONSOLE.println(text);
    }
    
    public static void timingPrintln(String text) {
        timingPrintln(text, null);
    }
    
    /**
     * Prints a time-stamped, colored message to the system console.
     * 
     * <p>This method prints a single line of text to the console. It automatically 
     * prefixes the message with the current system time. It also applies the chosen 
     * text color formatting, which is useful for highlighting performance benchmarks, 
     * log steps, or execution errors in terminal displays.</p>
     * 
     * <p>Example usage:</p>
     * <pre>{@code
     * // Prints something like: "[12:15:30.450] Task finished successfully" in green text
     * ConsoleUtil.timingPrintln("Task finished successfully", ConsoleColor.GREEN);
     * }</pre>
     *
     * @param txt   the message text to print, may be null or empty
     * @param color the color code or name to apply to the printed terminal text (e.g., Ansi escape codes)
     */ 
    public static void timingPrintln(String txt, String color) {
        timingPrint(txt);
        CONSOLE.println();
    }
    
    /**
     * It will time a print.
     * 
     * <p>
     * The time based printing happens in the same thread. It will cause the main thread to sleep,
     * which may not be ideal as it will cause your program to basically stop. Therefore it is recommended
     * you use this api with utmost caution.
     * 
     * @param text  Text to be printed
     * @throws RuntimeException 
     */
    public static void timingPrint(String text) {
        try {
            for (int i = 0; i < text.length(); i ++) {
                CONSOLE.print(text.charAt(i));
                Thread.sleep(PAUSE);
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Add the openapi prompt.
     */
    public static void prompt() {
        CONSOLE.print("<cbs2sql> ");
    }
}
