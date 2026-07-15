package org.javalabs.tools.cbs2sql.cli.rel;

import org.junit.jupiter.api.Test;

/**
 *
 * @author schan280
 */
public class RegExpTest {

    @Test
    public void testDate() {
        String regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])[ T]([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.{0,1}\\d{0,}Z?$";
        String date = "2020-05-01T23:58:06Z";
        
        System.out.println(date.matches(regex));
    }
}
