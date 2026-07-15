package org.javalabs.tools.cbs2sql.cli.main;

/**
 * Abstract executor class.
 *
 * @author Sudiptasish Chanda
 */
public interface ExecutorBase {
    
    /**
     * Return the command name.
     * @return String
     */
    String name();
    
    /**
     * Return the description of this command.
     * @return String
     */
    String description();
    
    /**
     * Execute this command.
     * @param options 
     * @throws java.lang.Exception 
     */
    void start(String[] options) throws Exception;
}
