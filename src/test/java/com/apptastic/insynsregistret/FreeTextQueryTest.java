package com.apptastic.insynsregistret;

import static org.junit.Assert.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;

import java.io.*;
import java.util.Optional;


public class FreeTextQueryTest {


    @Test
    public void getIssuer_Leo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Leo").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertTrue(issuer.get().toLowerCase().contains("leovegas") || issuer.get().toLowerCase().contains("leo vegas") );
    }


    @Test
    public void getIssuer_OresundInvestmentAb() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Öre").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertEquals("Öresund, Investment AB", issuer.get());
    }


    @Test
    public void getIssuer_AfAb() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("ÅF").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).skip(1).findFirst();

        assertTrue(issuer.isPresent());
        assertTrue(issuer.get().toLowerCase().contains("åf pöyry ab"));
    }


    @Test
    public void getIssuer_Hm() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Hennes & Mauritz").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertEquals("Hennes & Mauritz AB, H & M", issuer.get());
    }


    @Test
    public void getIssuer_Ericsson() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Ericsson").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount > 0);
    }


    @Test
    public void getIssuer_EmptyString() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertEquals(0, resultCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIssuer_null() throws UnsupportedEncodingException {
        FreeTextQueryBuilder.issuer(null).build();
    }


    @Test
    public void getPDMR_Jo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.pdmr("Jo").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void getPDMR_null() throws UnsupportedEncodingException {
        FreeTextQueryBuilder.pdmr(null).build();
    }


    @Test
    public void freeTestQueryNullParams() throws IOException {
        FreeTextQuery query = new FreeTextQuery(null, null);
        assertEquals("", query.getUrl());
    }

    @Test
    public void badResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("[\"Test1\",\"Test2\",\"Test3]"));

        Insynsregistret mock = spy(Insynsregistret.class);
        doReturn(reader).when(mock).sendRequest(anyString(), any());

        FreeTextQuery query = FreeTextQueryBuilder.issuer("test").build();
        long count = mock.search(query).count();
        assertEquals(2, count);
    }

    @Test
    public void emptyResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("[]"));

        Insynsregistret mock = spy(Insynsregistret.class);
        doReturn(reader).when(mock).sendRequest(anyString(), any());

        FreeTextQuery query = FreeTextQueryBuilder.issuer("test").build();
        long count = mock.search(query).count();
        assertEquals(0, count);
    }
}
