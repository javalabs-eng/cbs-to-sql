package org.javalabs.tools.cbs2sql.cli.bo;

import org.javalabs.tools.cbs2sql.cli.model.Transform;
import org.javalabs.tools.cbs2sql.cli.rel.Introspector;

/**
 *
 * @author schan280
 */
public class ConverterBO {
    
    private final Introspector introspector;
    
    public ConverterBO() {
        this.introspector = new Introspector();
    }
    
    public String convertJsonToCsv(Transform payload) {
        return introspector.introspectAndGenerateScript(payload);
    }
}
