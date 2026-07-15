package org.javalabs.tools.cbs2sql.cli.rel;

import java.lang.reflect.Method;
import java.sql.Array;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import org.javalabs.tools.cbs2sql.cli.util.GeneralUtility;

/**
 *
 * @author schan280
 */
public class MatchingColumnRule {

    public void apply(Table table, Column another, Column current) throws RuntimeException, ReflectiveOperationException {
        if (another.getType().equals(current.getType())) {
            // SQL data type matches.
            // Following checks must be performed.
            //
            // Check 1: If both are string column. If so, check and increase the length accordingly.
            if (another.getJavaType() == String.class && current.getJavaType() == String.class) {
                if (another.getLength() > current.getLength()) {
                    current.setLength(another.getLength());
                }
            }
            // Check 2: The previous value for column is null, but the new value non-null, or vice-versa
            //          In which case toggle the nullable attribute.
            Object newVal = table.getData().get(another.getOrder());
            if ((newVal != null && current.getNullable()) || (newVal == null && ! current.getNullable())) {
                current.setNullable(Boolean.TRUE);
            }
        }
        else {
            // For the same column name, we now got a different data type.
            if (current.getJavaType() != Array.class) {
                Object newVal = table.getData().get(another.getOrder());
                
                // Case 1:
                // Old json snippet: { "rate": 4.25 }   -> data type was Float/Double    (NUMBER(10,2))
                // New json snippet: { "rate": "" }     -> data type is String           (VARCHAR)
                // If new value is null or empty, retain the existing sql data type.
                if (newVal == null) {
                    // Do not make any change. newVal is anyway null.
                }
                
                // Case 2: It is possible due to erroneous data present in json that one decimal field was represented 
                // as empty string (""), however, in subsequent document, the field is turned out to be decimal with value
                // such as 5.71, etc.
                // In this case, we have to change the column data type.
                // 
                else {
                    // Case 2a:
                    // Old json snippet: { "rate": "" } or { "rate": null }   -> data type was String        (VARCHAR)
                    // New json snippet: { "rate": 4.25 } -> data type is Float/Double   (NUMBER(10,2))
                    if (current.getNullable()) {
                        // Does not matter whether there was an empty or null value, change the data type
                        current.setType(another.getType());
                        current.setJavaType(another.getJavaType());
                    }
                    else {
                        // Case 2b:
                        // Old json snippet: { "rate": 8.9 }     -> data type was String        (NUMBER(10,2))
                        // New json snippet: { "rate": "4.25" }  -> data type is Float/Double   (VARCHAR)
                        //
                        // In short, the current column is non-null. Convert the new column type to current column type.
                        // First come first serve basis.
                        if (another.getJavaType() == String.class) {
                            if (GeneralUtility.isPrimitiveNumeric(current.getJavaType())) {
                                Method method = current.getJavaType().getMethod("valueOf", String.class);
                                newVal = method.invoke(null, (String)newVal);

                                table.getData().set(another.getOrder(), newVal);
                            }
                            else {
                                ConsoleWriter.println(String.format(
                                        "Previous data type for %s was %s. Expecting numeric type"
                                        , another.getName(), another.getJavaType()));
                            }
                        }
                        // Case 2c:
                        // Old json snippet: { "rate": "8.9" } -> data type was String        (VARCHAR)
                        // New json snippet: { "rate": 4.25 }  -> data type is Float/Double   (NUMBER(10,2))
                        //
                        // In short, the current column is non-null. Convert the new column type to current column type.
                        // First come first serve basis.
                        else if (current.getJavaType() == String.class) {
                            newVal = String.valueOf(newVal);
                            table.getData().set(another.getOrder(), newVal);
                        }
                        else {
                            // Case 2d:
                            // Old json snippet: { "rate": 6 }    -> data type was Integer/Long    (NUMBER/INT)
                            // New json snippet: { "rate": 7.35 } -> data type is Float/Double     (NUMBER(10,2))
                            // We will upgrade, and not downgrade.
                            // If already upgraded, will skip ...
                            
                            if (GeneralUtility.rankOf(another.getJavaType()) > GeneralUtility.rankOf(current.getJavaType())) {
                                current.setJavaType(another.getJavaType());
                                current.setType(another.getType());
                            }
                        }
                    }
                }
            }
            else {
                // For non-Array data type.
                Object newVal = table.getData().get(another.getOrder());
                
                // Case 1:
                // Old json snippet: { "bin": [345, 33] }   -> data type was INT[]
                // New json snippet: { "bin": [] }          -> data type is TEXT[]
                // If new value is null or empty, retain the existing sql data type.
                if (newVal == null) {
                    // Do Nothing.
                }
                else {
                    // Case 1b:
                    // Old json snippet: { "bin": [] }        -> data type was TEXT[]
                    // New json snippet: { "bin": [345, 33] } -> data type is INT[]
                    // If new value is non-null, change sql data type.
                    current.setType(another.getType());
                }
            }
        }
    }
}
