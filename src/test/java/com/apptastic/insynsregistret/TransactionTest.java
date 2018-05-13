package com.apptastic.insynsregistret;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TransactionTest {
    private Transaction getDefaultTransaction() {
        Transaction transaction = new Transaction();

        transaction.setPublicationDate("2018-05-08");
        transaction.setIssuer("Hennes & Mauritz AB, H & M");
        transaction.setLeiCode("529900O5RR7R39FRDM42");
        transaction.setNotifier("Ramsbury Invest AB");
        transaction.setPersonDischargingManagerialResponsibilities("Stefan Persson");
        transaction.setPdmr("Stefan Persson");
        transaction.setPosition("Styrelseledamot/suppleant");
        transaction.setCloselyAssociated(true);
        transaction.setAmendment(false);
        transaction.setDetailsOfAmendment("");
        transaction.setInitialNotification(false);
        transaction.setLinkedToShareOptionProgramme(false);
        transaction.setNatureOfTransaction("Förvärv");
        transaction.setInstrument("Hennes & Mauritz AB, H & M ser. B");
        transaction.setIsin("SE0000106270");
        transaction.setTransactionDate("2018-05-03");
        transaction.setQuantity(4000000.0);
        transaction.setUnit("Antal");
        transaction.setPrice(149.6959);
        transaction.setCurrency("SEK");
        transaction.setTradingVenue("NASDAQ STOCKHOLM AB");
        transaction.setStatus("Aktuell");

        return transaction;
    }


    @Test
    public void equalsTest() {
        Transaction transaction1 = getDefaultTransaction();

        assertTrue(transaction1.equals(transaction1));
        assertFalse(transaction1.equals("Transaction"));
        assertEquals(transaction1, getDefaultTransaction());
        assertEquals(transaction1, new Transaction(transaction1));

        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setPublicationDate("2018-05-09");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setIssuer("Hennes & Mauritz AB");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setLeiCode("529900O5RR7R39FRDM43");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setNotifier("Ramsbury Invest");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setPersonDischargingManagerialResponsibilities("Stefan");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setPdmr("Stefan");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setPosition("VD");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setCloselyAssociated(false);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setAmendment(true);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setDetailsOfAmendment("?");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setInitialNotification(true);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setLinkedToShareOptionProgramme(true);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setNatureOfTransaction("Avyttring");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setInstrument("Hennes & Mauritz AB, H & M ser. A");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setIsin("SE0000106271");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setTransactionDate("2018-05-04");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setQuantity(4000001.0);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setUnit("Belopp");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setPrice(149.6958);
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setCurrency("USD");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setTradingVenue("Utanför handelsplats");
            assertNotEquals(transaction1, transaction2);
        }
        {
            Transaction transaction2 = getDefaultTransaction();
            transaction2.setStatus("Makulerad");
            assertNotEquals(transaction1, transaction2);
        }
    }


    @Test
    public void testSort() {
        Transaction transaction1 = getDefaultTransaction();
        transaction1.setTransactionDate("2018-05-02");

        Transaction transaction2 = getDefaultTransaction();
        transaction2.setTransactionDate("2018-05-04");

        Transaction transaction3 = getDefaultTransaction();
        transaction3.setTransactionDate("2018-05-03");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
        Collections.sort(transactions);

        assertEquals("2018-05-02", transactions.get(0).getTransactionDate());
        assertEquals("2018-05-03", transactions.get(1).getTransactionDate());
        assertEquals("2018-05-04", transactions.get(2).getTransactionDate());
    }

    @Test
    public void testHashMap() {
        Map<Transaction, String> transactions = new HashMap<>();

        Transaction transaction1 = getDefaultTransaction();
        transaction1.setTransactionDate("2018-05-02");
        transactions.put(transaction1, "Transaction1");

        Transaction transaction2 = getDefaultTransaction();
        transaction2.setTransactionDate("2018-05-04");
        transactions.put(transaction2, "Transaction2");

        Transaction transaction3 = getDefaultTransaction();
        transaction3.setTransactionDate("2018-05-03");
        transactions.put(transaction3, "Transaction3");

        assertEquals("Transaction1", transactions.get(transaction1));
        assertEquals("Transaction2", transactions.get(transaction2));
        assertEquals("Transaction3", transactions.get(transaction3));
    }

    @Test
    public void testTreeMap() {
        Map<Transaction, String> transactions = new TreeMap<>();

        Transaction transaction1 = getDefaultTransaction();
        transaction1.setTransactionDate("2018-05-02");
        transactions.put(transaction1, "Transaction1");

        Transaction transaction2 = getDefaultTransaction();
        transaction2.setTransactionDate("2018-05-04");
        transactions.put(transaction2, "Transaction2");

        Transaction transaction3 = getDefaultTransaction();
        transaction3.setTransactionDate("2018-05-03");
        transactions.put(transaction3, "Transaction3");

        assertEquals("Transaction1", transactions.get(transaction1));
        assertEquals("Transaction2", transactions.get(transaction2));
        assertEquals("Transaction3", transactions.get(transaction3));
    }
}
