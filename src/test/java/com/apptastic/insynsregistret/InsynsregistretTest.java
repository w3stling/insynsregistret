package com.apptastic.insynsregistret;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.assertEquals;


public class InsynsregistretTest {

    @Test
    public void checkColumnsLangSwedish() throws ParseException, IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse("2018-03-11");
        TransactionQuery query = TransactionQueryBuilder.publications(date, date)
                                                        .issuer("Empir Group AB")
                                                        .language(Language.SWEDISH)
                                                        .build();

        InsynsregistretDummy a = new InsynsregistretDummy();
        BufferedReader reader = a.sendTransactionRequest(query);
        String header = reader.readLine();
        String[] headerColumns = header.split(";");


        long nofColumnMappers = a.numberOfColumnMappers(headerColumns);
        assertEquals(22, nofColumnMappers);

        long nofKnowColumns = a.numberOfKnownColumns(headerColumns);
        assertEquals(22, nofKnowColumns);

        assertEquals(22, headerColumns.length);
    }


    @Test
    public void checkColumnsLangEnglish() throws ParseException, IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse("2018-03-11");
        TransactionQuery query = TransactionQueryBuilder.publications(date, date)
                .issuer("Empir Group AB")
                .language(Language.ENGLISH)
                .build();

        InsynsregistretDummy a = new InsynsregistretDummy();
        BufferedReader reader = a.sendTransactionRequest(query);
        String header = reader.readLine();
        String[] headerColumns = header.split(";");


        long nofColumnMappers = a.numberOfColumnMappers(headerColumns);
        assertEquals(22, nofColumnMappers);

        long nofKnowColumns = a.numberOfKnownColumns(headerColumns);
        assertEquals(22, nofKnowColumns);

        assertEquals(22, headerColumns.length);
    }




    class InsynsregistretDummy extends Insynsregistret {
        public BufferedReader sendTransactionRequest(TransactionQuery query) throws IOException {
            return super.sendRequest(query.getUrl(), Charset.forName("UTF-16LE"));
        }

        public int numberOfColumnMappers(String[] headerColumns) {
            TransactionMapper mapper = new TransactionMapper();
            return mapper.initialize(headerColumns);
        }

        public long numberOfKnownColumns(String[] headerColumns) {
            TransactionMapper mapper = new TransactionMapper();
            mapper.initialize(headerColumns);
            return mapper.columnIndexFieldMapping.stream()
                                                 .filter(Objects::nonNull)
                                                 .count();
        }
    }

}
