package org.javalabs.tools.cbs2sql.cli.storage;

import java.util.Map;

/**
 *
 * @author schan280
 */
public interface QueryConfig {
    
    String getKey();
    
    String getResource();
    
    Boolean isVerbose();
    
    Map<String, Object> getParameters();
    
    Object getRecords();
}
