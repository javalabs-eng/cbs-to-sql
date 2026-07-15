package org.javalabs.tools.cbs2sql.cli.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author schan280
 */
public final class DateUtil {
    
    private static final String FORMAT_1 = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    private static final String FORMAT_2 = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String FORMAT_3 = "yyyy-MM-dd HH:mm:ss";
    private static final String FORMAT_4 = "yyyy-MM-dd";
    
    private static final ThreadLocal<SimpleDateFormat> DF_1 = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                // Return a new SimpleDateFormat instance for each thread
                return new SimpleDateFormat(FORMAT_1);
            }
        };
    
    private static final ThreadLocal<SimpleDateFormat> DF_2 = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                // Return a new SimpleDateFormat instance for each thread
                return new SimpleDateFormat(FORMAT_2);
            }
        };
    
    private static final ThreadLocal<SimpleDateFormat> DF_3 = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                // Return a new SimpleDateFormat instance for each thread
                return new SimpleDateFormat(FORMAT_3);
            }
        };
    
    private static final ThreadLocal<SimpleDateFormat> DF_4 = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                // Return a new SimpleDateFormat instance for each thread
                return new SimpleDateFormat(FORMAT_4);
            }
        };
    
    public static Date parse(String str) {
        Date date = null;
        
        try {
            date = DF_1.get().parse(str);
        }
        catch (ParseException e1) {
            try {
                date = DF_2.get().parse(str);
            }
            catch (ParseException e2) {
                try {
                    date = DF_3.get().parse(str);
                }
                catch (ParseException e3) {
                    try {
                        date = DF_4.get().parse(str);
                    }
                    catch (ParseException e4) {
                        throw new RuntimeException(e4);
                    }
                }
            }
        }
        return date;
    }
}
