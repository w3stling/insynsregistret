package com.apptasticsoftware.insynsregistret;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


class FreeTextQueryTest {


    @Test
    void getIssuer_Leo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Leo").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertTrue(issuer.get().toLowerCase().contains("leovegas") || issuer.get().toLowerCase().contains("leo vegas") );
    }


    @Test
    void getIssuer_OresundInvestmentAb() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Öre").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query)
                                    .filter(n -> n.contains("Öresund"))
                                    .findFirst();

        assertTrue(issuer.isPresent());
    }


    @Test
    void getIssuer_AfAb() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("ÅF").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertTrue(issuer.get().toLowerCase().contains("åf pöyry ab"));
    }


    @Test
    void getIssuer_Hm() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Hennes & Mauritz").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertEquals("H & M Hennes & Mauritz AB", issuer.get());
    }


    @Test
    void getIssuer_Ericsson() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Ericsson").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount > 0);
    }


    @Test
    void getIssuer_EmptyString() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertEquals(0, resultCount);
    }

    @Test
    void getIssuer_null() {
        assertThrows(IllegalArgumentException.class, () ->
                FreeTextQueryBuilder.issuer(null));
    }


    @Test
    void getPDMR_Jo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.pdmr("Jo").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount > 0);
    }


    @Test
    void getPDMR_null() {
        assertThrows(IllegalArgumentException.class, () ->
            FreeTextQueryBuilder.pdmr(null));
    }


    @Test
    void freeTestQueryNullParams() throws IOException {
        FreeTextQuery query = new FreeTextQuery(null, null);
        assertEquals("", query.getUrl());
    }

    @Test
    void badResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("[\"Test1\",\"Test2\",\"Test3]"));

        Insynsregistret mock = spy(Insynsregistret.class);
        doReturn(reader).when(mock).sendRequest(anyString(), any());

        FreeTextQuery query = FreeTextQueryBuilder.issuer("test").build();
        long count = mock.search(query).count();
        assertEquals(2, count);
    }

    @Test
    void emptyResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("[]"));

        Insynsregistret mock = spy(Insynsregistret.class);
        doReturn(reader).when(mock).sendRequest(anyString(), any());

        FreeTextQuery query = FreeTextQueryBuilder.issuer("test").build();
        long count = mock.search(query).count();
        assertEquals(0, count);
    }
}
