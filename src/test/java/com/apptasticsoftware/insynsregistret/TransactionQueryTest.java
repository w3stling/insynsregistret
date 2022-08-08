package com.apptasticsoftware.insynsregistret;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class TransactionQueryTest {
    private BufferedReader reader;
    private Insynsregistret irMock;


    @BeforeEach
    void setupTest() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "insynSample1.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());
    }


    @AfterEach
    void teardownTest() throws IOException {
        if (reader != null)
            reader.close();
    }


    @Test
    void queryByTransactionDateOld() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.transactions(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }

    @Test
    void queryByTransactionDate() throws IOException {
        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.transactions(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }

    @Test
    void badTransactionFromDate() {
        LocalDate from = null;
        LocalDate to = LocalDate.of(2018, 3,1);

        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.transactions(from, to).build());
    }

    @Test
    void badTransactionToDate() {
        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = null;

        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.transactions(from, to).build());
    }

    @Test
    void badTransactionPastDays() {
        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.transactionsLastDays(-1).build());
    }


    @Test
    void queryByPublicationDateOld() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }


    @Test
    void queryByPublicationDate() throws IOException {
        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(375, transactionCount);
    }

    @Test
    void badPublicationFromDate() {
        LocalDate from = null;
        LocalDate to = LocalDate.of(2018, 3,1);

        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.publications(from, to).build());
    }

    @Test
    void badPublicationToDate() {
        LocalDate from = LocalDate.of(2018,3,1);
        LocalDate to = null;

        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.publications(from, to).build());
    }


    @Test
    void badPublicationsPastDays() {
        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.publicationsLastDays(-1).build());
    }


    @Disabled("Investigating")
    @Test
    void validateTransactionOld() throws IOException {
        Date from = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();
        Date to = new GregorianCalendar(2018, Calendar.MARCH,1).getTime();

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertTrue(firstTransaction.isPresent());

        Transaction transaction = firstTransaction.get();

        assertEquals("Empir Group AB", transaction.getIssuer());
        assertEquals("", transaction.getLeiCode());
        assertEquals("Alfanode AB", transaction.getNotifier());
        assertEquals("Alfanode AB", transaction.getPdmr());
        assertEquals("VD", transaction.getPosition());
        assertEquals(false, transaction.isCloselyAssociated());
        assertEquals(false, transaction.isAmendment());
        assertEquals("", transaction.getDetailsOfAmendment());
        assertEquals(true, transaction.isInitialNotification());
        assertEquals(false, transaction.isLinkedToShareOptionProgramme());
        assertEquals("Förvärv", transaction.getNatureOfTransaction());
        //assertEquals("Empir Group AB", transaction.getInstrumentName());
        assertEquals("SE0010769182", transaction.getIsin());
        assertEquals(28227, transaction.getQuantity(), 0.0);
        assertEquals("Antal", transaction.getUnit());
        assertEquals(37.9, transaction.getPrice(), 0.0);
        assertEquals("SEK", transaction.getCurrency());
        assertEquals("Utanför handelsplats", transaction.getTradingVenue());
        assertEquals("Aktuell", transaction.getStatus());
    }

    @Disabled("Investigating")
    @Test
    void validateTransaction() throws IOException {
        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertTrue(firstTransaction.isPresent());

        Transaction transaction = firstTransaction.get();

        assertEquals("Empir Group AB", transaction.getIssuer());
        assertEquals("", transaction.getLeiCode());
        assertEquals("Alfanode AB", transaction.getNotifier());
        assertEquals("Alfanode AB", transaction.getPdmr());
        assertEquals("VD", transaction.getPosition());
        assertEquals(false, transaction.isCloselyAssociated());
        assertEquals(false, transaction.isAmendment());
        assertEquals("", transaction.getDetailsOfAmendment());
        assertEquals(true, transaction.isInitialNotification());
        assertEquals(false, transaction.isLinkedToShareOptionProgramme());
        assertEquals("Förvärv", transaction.getNatureOfTransaction());
        //assertEquals("Empir Group AB", transaction.getInstrumentName());
        assertEquals("SE0010769182", transaction.getIsin());
        assertEquals(28227, transaction.getQuantity(), 0.0);
        assertEquals("Antal", transaction.getUnit());
        assertEquals(37.9, transaction.getPrice(), 0.0);
        assertEquals("SEK", transaction.getCurrency());
        assertEquals("Utanför handelsplats", transaction.getTradingVenue());
        assertEquals("Aktuell", transaction.getStatus());
    }


    @Disabled("Investigating")
    @Test
    void badPricePointInsteadOfComma() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "pricePointInsteadOfComma.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertTrue(firstTransaction.isPresent());

        Transaction transaction = firstTransaction.get();

        assertEquals("Empir Group AB", transaction.getIssuer());
        assertEquals("", transaction.getLeiCode());
        assertEquals("Alfanode AB", transaction.getNotifier());
        assertEquals("Alfanode AB", transaction.getPdmr());
        assertEquals("VD", transaction.getPosition());
        assertEquals(false, transaction.isCloselyAssociated());
        assertEquals(false, transaction.isAmendment());
        assertEquals("", transaction.getDetailsOfAmendment());
        assertEquals(true, transaction.isInitialNotification());
        assertEquals(false, transaction.isLinkedToShareOptionProgramme());
        assertEquals("Förvärv", transaction.getNatureOfTransaction());
        //assertEquals("Empir Group AB", transaction.getInstrumentName());
        assertEquals("SE0010769182", transaction.getIsin());
        assertEquals(28227, transaction.getQuantity(), 0.0);
        assertEquals("Antal", transaction.getUnit());
        assertEquals(37.9, transaction.getPrice(), 0.0);
        assertEquals("SEK", transaction.getCurrency());
        assertEquals("Utanför handelsplats", transaction.getTradingVenue());
        assertEquals("Aktuell", transaction.getStatus());
    }


    @Test
    void badPrice() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "badPrice.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        LocalDate from = LocalDate.of(2018, Calendar.MARCH,1);
        LocalDate to = LocalDate.of(2018, Calendar.MARCH,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(1, transactionCount);
    }


    @Test
    void badPriceNoLogging() throws IOException {
        Level defaultLevel = Logger.getLogger("com.apptastic.insynsregistret").getLevel();

        try {
            Logger.getLogger("com.apptastic.insynsregistret").setLevel(Level.SEVERE);

            ClassLoader classLoader = getClass().getClassLoader();
            reader = TestUtil.getExportedTransactionFile(classLoader, "badPrice.csv");

            irMock = spy(Insynsregistret.class);
            doReturn(reader).when(irMock).sendRequest(anyString(), any());


            LocalDate from = LocalDate.of(2018, Calendar.MARCH,1);
            LocalDate to = LocalDate.of(2018, Calendar.MARCH,1);

            TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

            long transactionCount = irMock.search(query).count();
            assertEquals(1, transactionCount);
        }
        finally {
            Logger.getLogger("com.apptastic.insynsregistret").setLevel(defaultLevel);
        }
    }


    @Test
    void badQuantity() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "badQuantity.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to).build();

        long transactionCount = irMock.search(query).count();
        assertEquals(1, transactionCount, 0.0);
    }


    @Test
    void emptyDownload() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "empty.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to)
                .issuer("Examples")
                .build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertFalse(firstTransaction.isPresent());
    }


    @Test
    void noTransactions() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        reader = TestUtil.getExportedTransactionFile(classLoader, "noTransactions.csv");

        irMock = spy(Insynsregistret.class);
        doReturn(reader).when(irMock).sendRequest(anyString(), any());


        LocalDate from = LocalDate.of(2018, 3,1);
        LocalDate to = LocalDate.of(2018, 3,1);

        TransactionQuery query = TransactionQueryBuilder.publications(from, to)
                .pdmr("dummy")
                .build();

        Optional<Transaction> firstTransaction = irMock.search(query).findFirst();
        assertFalse(firstTransaction.isPresent());
    }

    @Test
    void transactionQueryBuilder() throws IOException {
        TransactionQuery query1 = TransactionQueryBuilder.publicationsLastDays(30)
                .pdmr("test1")
                .build();

        assertTrue(query1.getUrl().contains("test1"));

        TransactionQuery query2 = TransactionQueryBuilder.publicationsLastDays(30)
                .pdmr("test2")
                .build();

        assertTrue(query2.getUrl().contains("test2"));
    }


    @Test
    void liveSvQueryByTransactionDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.transactionsLastDays(10)
                .build();

        List<Transaction> transactions = ir.search(transactionQuery)
                .collect(Collectors.toList());

        long count = transactions.stream()
                .filter(t -> t.getPublicationDate() != null)
                .count();

        assertTrue(count > 0);

        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getPublicationDate());
        }
    }


    @Test
    void liveSvQueryByPublicationDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(5)
                .build();

        List<Transaction> transactions = ir.search(transactionQuery)
                .collect(Collectors.toList());

        long count = transactions.stream()
                .filter(t -> t.getPublicationDate() != null)
                .count();

        assertTrue(count > 0);

        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getPublicationDate());
            assertEquals(19, transaction.getPublicationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).length());
            assertNotNull(transaction.getTransactionDate());
            assertEquals(19, transaction.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).length());
        }
    }

    @Test
    void noUnknownInstrumentType() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(15)
                .build();

        List<Transaction> transactions = ir.search(transactionQuery)
                .filter(t -> t.getInstrumentType() != null && !t.getInstrumentType().isBlank())
                .filter(t -> t.getInstrumentTypeDescription() == Transaction.InstrumentType.UNKNOWN)
                .collect(Collectors.toList());

        assertEquals(0, transactions.size());
    }

    @Test
    void liveEnQueryByTransactionDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.transactionsLastDays(10)
                .language(Language.ENGLISH)
                .build();

        List<Transaction> transactions = ir.search(transactionQuery)
                .collect(Collectors.toList());

        long count = transactions.stream()
                .filter(t -> t.getPublicationDate() != null)
                .count();

        assertTrue(count > 0);

        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getPublicationDate());
            assertEquals(19, transaction.getPublicationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).length());
            assertNotNull(transaction.getTransactionDate());
            assertEquals(19, transaction.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).length());
        }
    }


    @Test
    void liveEnQueryByPublicationDate() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(5)
                .language(Language.ENGLISH)
                .build();

        List<Transaction> transactions = ir.search(transactionQuery)
                                           .collect(Collectors.toList());

        long count = transactions.stream()
                                 .filter(t -> t.getPublicationDate() != null)
                                 .count();

        assertTrue(count > 0);

        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getPublicationDate());
        }
    }


    @Test
    void bigSampleTransaction() throws IOException {
        System.setProperty("insynsregistret.parallel", "true");
        Insynsregistret ir = new Insynsregistret();
        System.setProperty("insynsregistret.parallel", "false");

        ClassLoader classLoader = getClass().getClassLoader();
        BufferedReader reader = TestUtil.getExportedTransactionFile(classLoader, "insynBigSample.csv");
        long transactionCount = ir.parseTransactionResponse(reader).count();

        assertEquals(20436, transactionCount);
    }


    @Test
    void publicationsFromAfterToDate() {
        LocalDate from = LocalDate.of(2018, 3,2);
        LocalDate to = LocalDate.of(2018, 3,1);
        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.publications(from, to));
    }


    @Test
    void transactionsFromAfterToDate() {
        LocalDate from = LocalDate.of(2018, 3,2);
        LocalDate to = LocalDate.of(2018, 3,1);
        assertThrows(IllegalArgumentException.class, () ->
                TransactionQueryBuilder.transactions(from, to));
    }


    @Test
    void urlEncodingWithAmpersand() throws IOException {
        Insynsregistret ir = new Insynsregistret();

        TransactionQuery query = TransactionQueryBuilder.publicationsLastDays(365 * 2)
                .issuer("Hennes & Mauritz AB, H & M")
                .build();

        long count = ir.search(query)
                .map(Transaction::getIssuer)
                .count();

        assertTrue(count > 0);
    }


    @Test
    void splitLineTest() {
        Insynsregistret ir = new Insynsregistret();

        String[] columns = ir.splitLine("Test1;Test2;Test3;Test4;Test5;", 5);
        assertEquals(5, columns.length);

        columns = ir.splitLine("Test1;;Test3;Test4;Test5;", 5);
        assertEquals(5, columns.length);

        columns = ir.splitLine(";;;;;", 5);
        assertEquals(5, columns.length);

        columns = ir.splitLine("Test1;\"Test2.1;Test2.2;Test2.3\";Test3;Test4;Test5;", 5);
        assertEquals(5, columns.length);

        columns = ir.splitLine("Test1;\";\";Test3;Test4;Test5;", 5);
        assertEquals(5, columns.length);

        columns = ir.splitLine("2018-03-16 05:39:06;A City Media AB;549300ZUOH6WG0H70883;Velocita AB;Oskar Lindström;Styrelseledamot/suppleant;Ja;Ja;\"Korrigering av finansiellt instrument till \"\"Aktier\"\" \";;;Avyttring;Aktier;SE0001920760;2018-03-14 00:00:00;30000;Antal;120;SEK;Utanför handelsplats;Aktuell;", 21);
        assertEquals(21, columns.length);

        columns = ir.splitLine("2018-03-16 05:39:06;\"Korrigering av finansiellt instrument till \"\"Aktier\"\" \";", 2);
        assertEquals(2, columns.length);

        columns = ir.splitLine("Test1;\"Test2.1;Test2.2;Test2.3\"", 2);
        assertEquals(2, columns.length);

    }


    @Test
    void transactionAssignerTest() {
        String[] headerColumns = new String[] {"Hepp1", "Emittent", "ISIN"};
        String[] dataColumns = new String[] {"Test1", "Swedish Match", "SE0000310336"};

        Insynsregistret.TransactionMapper assigner = new Insynsregistret.TransactionMapper();
        assigner.initialize(headerColumns);
        Transaction transaction = assigner.createTransaction(dataColumns);

        assertNotNull(transaction);
        assertEquals("Swedish Match", transaction.getIssuer());
        assertEquals("SE0000310336", transaction.getIsin());
    }

}
