package com.apptasticsoftware.insynsregistret;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;


class InsynsregistretTest {

    @Test
    void checkColumnsLangSwedishOld() throws ParseException, IOException {
        LocalDate date = LocalDate.of(2018, 3, 11);
        TransactionQuery query = TransactionQueryBuilder.publications(toDate(date), toDate(date))
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
    void checkColumnsLangSwedish() throws ParseException, IOException {
        LocalDate date = LocalDate.of(2018, 3, 11);
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
    void checkColumnsLangEnglishOld() throws ParseException, IOException {
        LocalDate date = LocalDate.of(2018, 3, 11);
        TransactionQuery query = TransactionQueryBuilder.publications(toDate(date), toDate(date))
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


    @Test
    void checkColumnsLangEnglish() throws ParseException, IOException {
        LocalDate date = LocalDate.of(2018, 3, 11);
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

    private static Date toDate(LocalDate localDate) {
        return java.util.Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}
