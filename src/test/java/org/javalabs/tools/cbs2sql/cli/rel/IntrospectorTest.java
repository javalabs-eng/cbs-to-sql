package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.Collection;
import java.util.LinkedHashMap;
import org.javalabs.tools.cbs2sql.cli.bo.ConverterBO;
import org.javalabs.tools.cbs2sql.cli.model.Transform;
import org.javalabs.tools.cbs2sql.cli.util.MapperUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author schan280
 */
public class IntrospectorTest {
    
    @Test
    public void testConvertSimpleJson() {
        String json = """
                      {
                        "name": "Sudiptasish Chanda",
                        "id": "123",
                        "age": 46,
                        "salary": 10045.5,
                        "created_time": "2026-09-22 13:44:32",
                        "joined_date": "2026-09-22",
                        "active": true
                      }""";
        
        Transform payload = new Transform();
        payload.setDataset("person");
        payload.setDocument(MapperUtil.decode(json.getBytes(), LinkedHashMap.class));
        
        Collection<Result> results = new Introspector().introspect(payload);
        assertEquals(1, results.size());
        
        for (Result result : results) {
            Table table = result.getTable();
            assertEquals(payload.getDataset(), table.getName());
            
            assertEquals(7, table.getColumns().size());
            
            for (int i = 0; i < table.getColumns().size(); i ++) {
                Column col = table.getColumns().get(i);
                if (i == 0) {
                    // First column should be the id.
                    assertEquals("id", col.getName());
                }
                if (col.getName().equals("name")) {
                    assertEquals("VARCHAR", col.getType());
                }
                else if (col.getName().equals("age")) {
                    assertEquals("INT", col.getType());
                }
                else if (col.getName().equals("salary")) {
                    assertEquals("NUMERIC(10,2)", col.getType());
                }
                else if (col.getName().equals("created_time")) {
                    assertEquals("TIMESTAMP", col.getType());
                }
                else if (col.getName().equals("joined_date")) {
                    assertEquals("DATE", col.getType());
                }
                else if (col.getName().equals("active")) {
                    assertEquals("SMALLINT", col.getType());
                }
            }
        }
    }

    // @Test
    public void testConvertJsonToSql() {
        try {
            Transform payload = new Transform();
            payload.setFilename("/Users/schan280/Projects/cbs-to-sql/src/test/resources/person.json");
            
            String sql = new ConverterBO().convertJsonToCsv(payload);
            
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // @Test
    public void testConvertJsonToCsv() {
        try {
            Transform payload = new Transform();
            payload.setFilename("/Users/schan280/Projects/cbs-to-sql/src/test/resources/organization.json");
            
            String sql = new ConverterBO().convertJsonToCsv(payload);
            System.out.println(sql);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // @Test
    public void testConvertJsonToCsvInBulk() {
        Transform payload = new Transform();
        payload.setDirectory("/Users/schan280/Projects/cb-to-pg/data");
        payload.setFilename("client_institution.json");
        payload.setFlatten("Y");
        payload.setDataset(payload.getFilename().substring(0, payload.getFilename().indexOf(".")));
        
        try {
            String sql = new ConverterBO().convertJsonToCsv(payload);
            System.out.println(sql);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}
