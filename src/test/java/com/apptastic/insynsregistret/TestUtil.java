package com.apptastic.insynsregistret;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;

public class TestUtil {

    public static BufferedReader getExportedTransactionFile(ClassLoader classLoader, String transactionFile) {
        InputStream is = classLoader.getResourceAsStream(transactionFile);
        return new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-16LE")));
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
