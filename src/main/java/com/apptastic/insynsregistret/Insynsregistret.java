/*
 * MIT License
 *
 * Copyright (c) 2018, Apptastic Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.apptastic.insynsregistret;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;


/**
 * Class makes it easier to automate data extraction from <a href="https://www.fi.se/sv/vara-register/insynsregistret">Insynsregistret</a>.
 *
 *
 * Insynsregistret is a Swedish financial registry maintained by the <a href="https://www.fi.se">Finansinspektionen</a> (FI).
 * It contains information regarding insider trading on <a href="http://www.nasdaqomxnordic.com">Nasdaq Stockholm</a> and
 * <a href="http://www.ngm.se">Nordic Growth Market</a> (NGM) and other trading venues.
 *
 * This registry publishes information about the trading activities that have taken place during
 * the day performed by the insiders and people close to them. The registry includes information
 * concerning the position the insider involved in a certain trading activity has, what kind of
 * activity it is (sell, buy or gift etc.), what kind of security that is traded and the quantity.
 *
 * All insider trading is reported to FI, which publishes the data on a daily basis to this public database.
 */
public class Insynsregistret {
    /**
     * Free text search for issuer names or person discharging managerial responsibilities (PDMR).
     * {@link FreeTextQuery} object is created by {@link FreeTextQueryBuilder} class.
     * @param query free text query
     * @return A {@link java.util.stream.Stream} of {@link java.lang.String} objects representing issuer names or PDMR names
     * @throws IOException exception
     */
    public Stream<String> search(FreeTextQuery query) throws IOException {
        BufferedReader response = sendRequest(query.getUrl(), Charset.forName("UTF-8"));

        return parseFreeTextQueryResponse(response);
    }


    /**
     * Search transaction in insynsregistret.
     * {@link TransactionQuery} object is created by {@link TransactionQueryBuilder} class.
     * @param query transaction query
     * @return A {@link java.util.stream.Stream} of {@link Transaction} objects
     * @throws IOException exception
     */
    public Stream<Transaction> search(TransactionQuery query) throws IOException {
        BufferedReader response = sendRequest(query.getUrl(), Charset.forName("UTF-16LE"));

        return parseTransactionResponse(response);
    }

    /**
     * Internal method for sending the http request.
     * @param url URL to send the request
     * @param encoding encoding for the payload in the response
     * @return The response for the request
     * @throws IOException exception
     */
    protected BufferedReader sendRequest(String url, Charset encoding) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip");
        InputStream inputStream = connection.getInputStream();

        if ("gzip".equals(connection.getContentEncoding()))
            inputStream = new GZIPInputStream(inputStream);

        Reader reader = new InputStreamReader(inputStream, encoding);

        return new BufferedReader(reader);
    }


    private Stream<String> parseFreeTextQueryResponse(BufferedReader reader) {
        return reader.lines().flatMap(this::splitString).distinct();
    }


    private Stream<String> splitString(final String text) {
        if (text.length() <= 2)
            return Stream.empty();

        LinkedList<String> tokens = new LinkedList<>();

        boolean found = true;
        int start;
        int end = 0;

        while (found) {
            start = text.indexOf('"', end + 1);
            end = text.indexOf('"', start + 1);
            found = start != -1 && end != -1;

            if (found) {
                String token = text.substring(start + 1, end);
                token = token.replace("\\u0026", "&").trim();

                tokens.add(token);
            }
        }

        return tokens.stream();
    }


    Stream<Transaction> parseTransactionResponse(BufferedReader reader) throws IOException {
        String header = reader.readLine();

        if (header == null)
            return Stream.empty();

        String[] headerColumns = splitLine(header, 32);
        TransactionAssigner assigner = new TransactionAssigner();
        int nofColumns = assigner.initialize(headerColumns);

        // Read the rest of the lines after the header
        return reader.lines()
                .parallel()
                .map(l -> splitLine(l, nofColumns))
                .map(assigner::createTransaction)
                .filter(this::badTransactionFilter);
    }


    String[] splitLine(String text, int nofColumns) {
        int index = 0;
        boolean found = true;
        int extraForQuotationMark = 0;
        int start = 0;
        int end;
        String[] tokens = new String[nofColumns];

        while (found && index < nofColumns) {
            end = text.indexOf(';', start);
            found = (end != -1);

            if (found && text.codePointAt(start) == '"') {
                // Data in column element is surounded with quotation marks. Find the ending quote mark.
                ++start;

                if (start == end)
                    end = text.indexOf(';', end + 1);

                while (found && text.codePointAt(end - 1) != '"') {
                    end = text.indexOf(';', end + 1);
                    found = end != -1;
                }

                extraForQuotationMark = 1;
                --end;
            }

            if (found) {
                String token = text.substring(start, end);
                token = token.replace("\\u0026", "&").trim();

                tokens[index] = token;
                start = end + 1 + extraForQuotationMark;
                extraForQuotationMark = 0;
                ++index;
            }
        }

        return tokens;
    }


    private boolean badTransactionFilter(Transaction transaction) {
        return !Double.isNaN(transaction.getPrice()) && !Double.isNaN(transaction.getQuantity());
    }


    static class TransactionAssigner {
        private static final String[] COLUMN_PUBLICATION_DATE = {"Publicerings datum", "Publication date"};
        private static final String[] COLUMN_ISSUER = {"Utgivare", "Issuer"};
        private static final String[] COLUMN_LEI_CODE = {"LEI-kod", "LEI-code"};
        private static final String[] COLUMN_NOTIFIER = {"Anmälningsskyldig", "Notifier"};
        private static final String[] COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES = {"Person i ledande ställning", "Person discharging managerial responsibilities"};
        private static final String[] COLUMN_POSITION = {"Befattning", "Position"};
        private static final String[] COLUMN_IS_CLOSELY_ASSOCIATE = {"Närstående", "Closely associated"};
        private static final String[] COLUMN_IS_AMENDMENT = {"Korrigering", "Amendment"};
        private static final String[] COLUMN_DETAILS_OF_AMENDMENT = {"Beskrivning av korrigering", "Details of amendment"};
        private static final String[] COLUMN_IS_INITIAL_NOTIFICATION = {"Är förstagångsrapportering", "Initial notification"};
        private static final String[] COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME = {"Är kopplad till aktieprogram", "Linked to share option programme"};
        private static final String[] COLUMN_NATURE_OF_TRANSACTION = {"Karaktär", "Nature of transaction"};
        private static final String[] COLUMN_INSTRUMENT = {"Instrument", "Instrument"};
        private static final String[] COLUMN_ISIN = {"ISIN", "ISIN"};
        private static final String[] COLUMN_TRANSACTION_DATE = {"Transaktions datum", "Transaction date"};
        private static final String[] COLUMN_QUANTITY = {"Volym", "Volume"};
        private static final String[] COLUMN_UNIT = {"Volymsenhet", "Unit"};
        private static final String[] COLUMN_PRICE = {"Pris", "Price"};
        private static final String[] COLUMN_CURRENCY = {"Valuta", "Currency"};
        private static final String[] COLUMN_TRADING_VENUE = {"Handelsplats", "Trading venue"};
        private static final String[] COLUMN_STATUS = {"Status", "Status"};
        private static final HashMap<String, BiConsumer<Transaction, String>> COLUMN_NAME_FIELD_MAPPER;
        private final ArrayList<BiConsumer<Transaction, String>> columnLookup;


        TransactionAssigner() {
            columnLookup = new ArrayList<>();
        }


        int initialize(String[] header) {
            int i;

            for (i = 0; i < header.length; ++i) {

                if (header[i] == null)
                    break;

                String headerColumnText = header[i].trim();
                columnLookup.add(i, COLUMN_NAME_FIELD_MAPPER.get(headerColumnText));
            }

            return i;
        }


        Transaction createTransaction(String[] columns) {
            Transaction transaction = new Transaction();
            int length = Math.min(columns.length, columnLookup.size());

            for (int i = 0; i < length; ++i) {
                BiConsumer<Transaction, String> consumer = columnLookup.get(i);

                if (consumer != null)
                    consumer.accept(transaction, columns[i]);
            }

            return transaction;
        }


        private static double parseDouble(String value) {
            double floatNumber;

            try {
                value = value.replace(',', '.');
                floatNumber = Double.valueOf(value);
            }
            catch (Exception e) {
                Logger logger = Logger.getLogger("com.apptastic.insynsregistret");

                if (logger.isLoggable(Level.WARNING))
                    logger.log(Level.WARNING, "Failed to parse double. ", e);

                floatNumber = Double.NaN;
            }

            return floatNumber;
        }


        private static boolean isTrue(String text, int langIndex) {
            if (langIndex == 1)
                return "Yes".equalsIgnoreCase(text.trim());
            else
                return "Ja".equalsIgnoreCase(text.trim());
        }


        static {
            COLUMN_NAME_FIELD_MAPPER = new HashMap<>(64);

            // Publication date
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PUBLICATION_DATE[Language.SWEDISH.getIndex()], Transaction::setPublicationDate);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PUBLICATION_DATE[Language.ENGLISH.getIndex()], Transaction::setPublicationDate);

            // Issuer
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_ISSUER[Language.SWEDISH.getIndex()], Transaction::setIssuer);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_ISSUER[Language.ENGLISH.getIndex()], Transaction::setIssuer);

            // LEI Code
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_LEI_CODE[Language.SWEDISH.getIndex()], Transaction::setLeiCode);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_LEI_CODE[Language.ENGLISH.getIndex()], Transaction::setLeiCode);

            // Notifier
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_NOTIFIER[Language.SWEDISH.getIndex()], Transaction::setNotifier);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_NOTIFIER[Language.ENGLISH.getIndex()], Transaction::setNotifier);

            // PDMR
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[Language.SWEDISH.getIndex()], Transaction::setPersonDischargingManagerialResponsibilities);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[Language.ENGLISH.getIndex()], Transaction::setPersonDischargingManagerialResponsibilities);

            // Position
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_POSITION[Language.SWEDISH.getIndex()], Transaction::setPosition);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_POSITION[Language.ENGLISH.getIndex()], Transaction::setPosition);

            // Is closely associated
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_CLOSELY_ASSOCIATE[Language.SWEDISH.getIndex()], (t, v) -> t.setCloselyAssociated(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_CLOSELY_ASSOCIATE[Language.ENGLISH.getIndex()], (t, v) -> t.setCloselyAssociated(isTrue(v, Language.ENGLISH.getIndex())) );

            // Is amendment
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_AMENDMENT[Language.SWEDISH.getIndex()], (t, v) -> t.setAmendment(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_AMENDMENT[Language.ENGLISH.getIndex()], (t, v) -> t.setAmendment(isTrue(v, Language.ENGLISH.getIndex())) );

            // Details of amendment
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_DETAILS_OF_AMENDMENT[Language.SWEDISH.getIndex()], Transaction::setDetailsOfAmendment);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_DETAILS_OF_AMENDMENT[Language.ENGLISH.getIndex()], Transaction::setDetailsOfAmendment);

            // Is initial notification
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_INITIAL_NOTIFICATION[Language.SWEDISH.getIndex()], (t, v) -> t.setInitialNotification(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_INITIAL_NOTIFICATION[Language.ENGLISH.getIndex()], (t, v) -> t.setInitialNotification(isTrue(v, Language.ENGLISH.getIndex())) );

            // Is linked to share option programme
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[Language.SWEDISH.getIndex()], (t, v) -> t.setLinkedToShareOptionProgramme(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[Language.ENGLISH.getIndex()], (t, v) -> t.setLinkedToShareOptionProgramme(isTrue(v, Language.ENGLISH.getIndex())) );

            // Nature of transaction
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_NATURE_OF_TRANSACTION[Language.SWEDISH.getIndex()], Transaction::setNatureOfTransaction);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_NATURE_OF_TRANSACTION[Language.ENGLISH.getIndex()], Transaction::setNatureOfTransaction);

            // Nature of transaction
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_INSTRUMENT[Language.SWEDISH.getIndex()], Transaction::setInstrument);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_INSTRUMENT[Language.ENGLISH.getIndex()], Transaction::setInstrument);

            // ISIN
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_ISIN[Language.SWEDISH.getIndex()], Transaction::setIsin);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_ISIN[Language.ENGLISH.getIndex()], Transaction::setIsin);

            // Transaction date
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_TRANSACTION_DATE[Language.SWEDISH.getIndex()], Transaction::setTransactionDate);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_TRANSACTION_DATE[Language.ENGLISH.getIndex()], Transaction::setTransactionDate);

            // Quantity
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_QUANTITY[Language.SWEDISH.getIndex()], (t, v) -> t.setQuantity(parseDouble(v)) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_QUANTITY[Language.ENGLISH.getIndex()], (t, v) -> t.setQuantity(parseDouble(v)) );

            // Transaction date
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_UNIT[Language.SWEDISH.getIndex()], Transaction::setUnit);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_UNIT[Language.ENGLISH.getIndex()], Transaction::setUnit);

            // Price
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PRICE[Language.SWEDISH.getIndex()], (t, v) -> t.setPrice(parseDouble(v)) );
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_PRICE[Language.ENGLISH.getIndex()], (t, v) -> t.setPrice(parseDouble(v)) );

            // Currency
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_CURRENCY[Language.SWEDISH.getIndex()], Transaction::setCurrency);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_CURRENCY[Language.ENGLISH.getIndex()], Transaction::setCurrency);

            // Trading venue
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_TRADING_VENUE[Language.SWEDISH.getIndex()], Transaction::setTradingVenue);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_TRADING_VENUE[Language.ENGLISH.getIndex()], Transaction::setTradingVenue);

            // Status
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_STATUS[Language.SWEDISH.getIndex()], Transaction::setStatus);
            COLUMN_NAME_FIELD_MAPPER.put(COLUMN_STATUS[Language.ENGLISH.getIndex()], Transaction::setStatus);
        }
    }

}
