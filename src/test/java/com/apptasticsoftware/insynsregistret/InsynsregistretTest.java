package com.apptasticsoftware.insynsregistret;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;


class InsynsregistretTest {

    @Test
    void checkColumnsLangSwedish() throws IOException {
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
    void checkColumnsLangEnglish() throws IOException {
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

    static class InsynsregistretDummy extends Insynsregistret {
        public BufferedReader sendTransactionRequest(TransactionQuery query) throws IOException {
            return super.sendRequest(query.getUrl(), StandardCharsets.UTF_16LE);
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
