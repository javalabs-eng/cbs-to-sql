package org.javalabs.tools.cbs2sql.cli.model;

/**
 *
 * @author schan280
 */
public class Document {
 
    private String id;
    private String content;
    
    public Document() {}

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
