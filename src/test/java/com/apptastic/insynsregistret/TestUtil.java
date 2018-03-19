package com.apptastic.insynsregistret;

import java.io.*;
import java.nio.charset.Charset;

public class TestUtil {

    public static BufferedReader getExportedTransactionFile(ClassLoader classLoader, String transactionFile) {
        InputStream is = classLoader.getResourceAsStream(transactionFile);
        return new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-16LE")));
    }

}
