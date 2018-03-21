package com.apptastic.insynsregistret;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class TransactionQueryTest {
    private BufferedReader reader;
    private Insynsregistret irMock;


    @Before
    public void setupTest() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "insynSample1.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());
    }


    @After
    public void teardownTest() throws IOException {
        if (reader != null)
            reader.close();
    }


    @Test
    public void queryByTransactionDate() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.transactions(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badTransactionFromDate() throws IOException {
        Date from = null;
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.transactions(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badTransactionToDate() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = null;

        TransactionQuery query = TransactionQueryBuilder.transactions(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badTransactionPastDays() throws UnsupportedEncodingException {
        TransactionQueryBuilder.transactionsLastDays(-1).build();
    }


    @Test
    public void queryByPublicationDate() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badPublicationFromDate() throws IOException {
        Date from = null;
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badPublicationToDate() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = null;

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void badPublicationsPastDays() throws UnsupportedEncodingException {
        TransactionQueryBuilder.publicationsLastDays(-1).build();
    }


    @Test
    public void validateTransaction() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertTrue(firstTransaction.isPresent());

        Transaction transaction = firstTransaction.get();

        assertEquals("2018-03-11 12:38:01", transaction.getPublicationDate());
        assertEquals("Empir Group AB", transaction.getIssuer());
        assertEquals("", transaction.getLeiCode());
        assertEquals("Alfanode AB", transaction.getNotifier());
        assertEquals("Alfanode AB", transaction.getPersonDischargingManagerialResponsibilities());
        assertEquals("VD", transaction.getPosition());
        assertEquals(false, transaction.isCloselyAssociated());
        assertEquals(false, transaction.isAmendment());
        assertEquals("", transaction.getDetailsOfAmendment());
        assertEquals(true, transaction.isInitialNotification());
        assertEquals(false, transaction.isLinkedToShareOptionProgramme());
        assertEquals("Förvärv", transaction.getNatureOfTransaction());
        assertEquals("Empir Group AB", transaction.getInstrument());
        assertEquals("SE0010769182", transaction.getIsin());
        assertEquals("2018-03-11 00:00:00", transaction.getTransactionDate());
        assertEquals(28227, transaction.getQuantity(), 0.0);
        assertEquals("Antal", transaction.getUnit());
        assertEquals(37.9, transaction.getPrice(), 0.0);
        assertEquals("SEK", transaction.getCurrency());
        assertEquals("Utanför handelsplats", transaction.getTradingVenue());
        assertEquals("Aktuell", transaction.getStatus());
    }


    @Test
    public void badPricePointInsteadOfComma() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "pricePointInsteadOfComma.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertTrue(firstTransaction.isPresent());

        Transaction transaction = firstTransaction.get();

        assertEquals("2018-03-11 12:38:01", transaction.getPublicationDate());
        assertEquals("Empir Group AB", transaction.getIssuer());
        assertEquals("", transaction.getLeiCode());
        assertEquals("Alfanode AB", transaction.getNotifier());
        assertEquals("Alfanode AB", transaction.getPersonDischargingManagerialResponsibilities());
        assertEquals("VD", transaction.getPosition());
        assertEquals(false, transaction.isCloselyAssociated());
        assertEquals(false, transaction.isAmendment());
        assertEquals("", transaction.getDetailsOfAmendment());
        assertEquals(true, transaction.isInitialNotification());
        assertEquals(false, transaction.isLinkedToShareOptionProgramme());
        assertEquals("Förvärv", transaction.getNatureOfTransaction());
        assertEquals("Empir Group AB", transaction.getInstrument());
        assertEquals("SE0010769182", transaction.getIsin());
        assertEquals("2018-03-11 00:00:00", transaction.getTransactionDate());
        assertEquals(28227, transaction.getQuantity(), 0.0);
        assertEquals("Antal", transaction.getUnit());
        assertEquals(37.9, transaction.getPrice(), 0.0);
        assertEquals("SEK", transaction.getCurrency());
        assertEquals("Utanför handelsplats", transaction.getTradingVenue());
        assertEquals("Aktuell", transaction.getStatus());
    }


    @Test
    public void badPrice() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "badPrice.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(1, transactionCount);
    }

    @Test
    public void badPriceNoLogging() throws IOException {
        Level defaultLevel = Logger.getLogger("com.apptastic.insynsregistret").getLevel();

        try {
            Logger.getLogger("com.apptastic.insynsregistret").setLevel(Level.SEVERE);

            ClassLoader classLoader = getClass().getClassLoader();
            reader = TestUtil.getExportedTransactionFile(classLoader, "badPrice.csv");

            irMock = spy(Insynsregistret.class);
            doReturn(reader).when(irMock).sendRequest(anyString(), any());


            Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
            Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

            TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

            long transactionCount = irMock.search(query).count();
            assertEquals(1, transactionCount);
        }
        finally {
            Logger.getLogger("com.apptastic.insynsregistret").setLevel(defaultLevel);
        }
    }


    @Test
    public void badQuantity() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "badQuantity.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(1, transactionCount, 0.0);
    }


    @Test
    public void emptyDownload() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "empty.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to)
                .issuer("Examples")
                .build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertFalse(firstTransaction.isPresent());
    }


    @Test
    public void noTransactions() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "noTransactions.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to)
                .personDischargingManagerialResponsibilities("dummy")
                .build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertFalse(firstTransaction.isPresent());
    }


    @Test
    public void liveSvQueryByTransactionDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.transactionsLastDays(10)
                .build();

        long transactionCount = ir.search(transactionQuery).count();
        assertTrue(transactionCount > 0);
    }


    @Test
    public void liveSvQueryByPublicationDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(10)
                .build();

        long transactionCount = ir.search(transactionQuery).count();
        assertTrue(transactionCount > 0);
    }


    @Test
    public void liveEnQueryByTransactionDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.transactionsLastDays(10)
                .language(Language.ENGLISH)
                .build();

        long transactionCount = ir.search(transactionQuery).count();
        assertTrue(transactionCount > 0);
    }


    @Test
    public void liveEnQueryByPublicationDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(10)
                .language(Language.ENGLISH)
                .build();

        long transactionCount = ir.search(transactionQuery).count();
        assertTrue(transactionCount > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void publicationsFromAfterToDate() {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,2).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        TransactionQueryBuilder.publications(from, to);
    }


    @Test(expected = IllegalArgumentException.class)
    public void transactionsFromAfterToDate() {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,2).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        TransactionQueryBuilder.transactions(from, to);
    }


    @Test
    public void urlEncodingWithAmpersand() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery query = TransactionQueryBuilder.publicationsLastDays(365 * 2)
                .issuer("H&M Hennes & Mauritz AB")
                .build();

        long count = ir.search(query)
                .map(Transaction::getIssuer)
                .count();

        assertTrue(count > 0);
    }

}
