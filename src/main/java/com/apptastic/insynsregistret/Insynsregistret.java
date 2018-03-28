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
        int langIndex = query.getLanguage().getIndex();
        BufferedReader response = sendRequest(query.getUrl(), Charset.forName("UTF-16LE"));

        return parseTransactionResponse(response, langIndex);
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


    Stream<Transaction> parseTransactionResponse(BufferedReader reader, int langIndex) throws IOException {
        String header = reader.readLine();

        if (header == null)
            return Stream.empty();

        String[] headerColumns = splitLine(header, 32);
        TransactionAssigner assigner = new TransactionAssigner();
        int nofColumns = assigner.initialize(headerColumns, langIndex);

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
            found = end != -1;

            if (found && text.codePointAt(start) == '"') {
                // Data in column element is with quotation marks. Find the ending quote mark.
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

        private ArrayList<BiConsumer<Transaction, String>> columnLookup;


        TransactionAssigner() {
            columnLookup = new ArrayList<>();
        }


        int initialize(String[] header, int langIndex) {
            int i;

            for (i = 0; i < header.length; ++i) {

                if (header[i] == null)
                    break;

                String headerColumnText = header[i].trim();

                if (COLUMN_PUBLICATION_DATE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setPublicationDate);
                else if (COLUMN_ISSUER[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setIssuer);
                else if (COLUMN_LEI_CODE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setLeiCode);
                else if (COLUMN_NOTIFIER[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setNotifier);
                else if (COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setPersonDischargingManagerialResponsibilities);
                else if (COLUMN_POSITION[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setPosition);
                else if (COLUMN_IS_CLOSELY_ASSOCIATE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setCloselyAssociated(isTrue(v, langIndex)) );
                else if (COLUMN_IS_AMENDMENT[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setAmendment(isTrue(v, langIndex)) );
                else if (COLUMN_DETAILS_OF_AMENDMENT[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setDetailsOfAmendment);
                else if (COLUMN_IS_INITIAL_NOTIFICATION[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setInitialNotification(isTrue(v, langIndex)) );
                else if (COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setLinkedToShareOptionProgramme(isTrue(v, langIndex)) );
                else if (COLUMN_NATURE_OF_TRANSACTION[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setNatureOfTransaction);
                else if (COLUMN_INSTRUMENT[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setInstrument);
                else if (COLUMN_ISIN[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setIsin);
                else if (COLUMN_TRANSACTION_DATE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setTransactionDate);
                else if (COLUMN_QUANTITY[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setQuantity(parseDouble(v)) );
                else if (COLUMN_UNIT[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setUnit);
                else if (COLUMN_PRICE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, (t, v) -> t.setPrice(parseDouble(v)) );
                else if (COLUMN_CURRENCY[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setCurrency);
                else if (COLUMN_TRADING_VENUE[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setTradingVenue);
                else if (COLUMN_STATUS[langIndex].equals(headerColumnText))
                    columnLookup.add(i, Transaction::setStatus);
                else
                    columnLookup.add(i, null);
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


        private double parseDouble(String value) {
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


        private boolean isTrue(String text, int langIndex) {
            if (langIndex == 1)
                return "Yes".equalsIgnoreCase(text.trim());
            else
                return "Ja".equalsIgnoreCase(text.trim());
        }

    }

}
