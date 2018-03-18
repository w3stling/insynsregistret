package com.apptastic.insynsregistret;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;


public class FreeTextQueryTest {


    @Test
    public void getIssuer_Leo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("Leo").build();

        Insynsregistret ir = new Insynsregistret();
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertEquals("LeoVegas AB", issuer.get());
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
        Optional<String> issuer = ir.search(query).findFirst();

        assertTrue(issuer.isPresent());
        assertEquals("ÅF AB", issuer.get());
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


    @Test(expected = IllegalArgumentException.class)
    public void getIssuer_null() throws UnsupportedEncodingException {
        FreeTextQueryBuilder.issuer(null).build();
    }


    @Test
    public void getPDMR_Jo() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.personDischargingManagerialResponsibilities("Jo").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void getPDMR_null() throws UnsupportedEncodingException {
        FreeTextQueryBuilder.personDischargingManagerialResponsibilities(null).build();
    }


    @Test
    public void noMatch() throws IOException {
        FreeTextQuery query = FreeTextQueryBuilder.issuer("-").build();

        Insynsregistret ir = new Insynsregistret();
        long resultCount = ir.search(query).count();
        assertTrue(resultCount == 0);
    }
}
