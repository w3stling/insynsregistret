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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    private Logger logger;


    /**
     * Default constructor.
     */
    public Insynsregistret() {
        logger = Logger.getGlobal();
    }


    /**
     * Free text search for issuer names or person discharging managerial responsibilities (PDMR).
     * {@link FreeTextQuery} object is created by {@link FreeTextQueryBuilder} class.
     * @param query free text query
     * @return A {@link java.util.stream.Stream} of {@link java.lang.String} objects representing issuer names or PDMR names
     * @throws IOException exception
     */
    public Stream<String> search(FreeTextQuery query) throws IOException {
        BufferedReader response = sendRequest(query.getUrl(), Charset.forName("UTF-8"));

        return parseStringArray(response);
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

        return parseTransactions(response, langIndex);
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


    private Stream<String> parseStringArray(BufferedReader reader) {
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


    private Stream<Transaction> parseTransactions(BufferedReader reader, int langIndex) throws IOException {
        Pattern pattern = Pattern.compile(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        String header = reader.readLine();

        if (header == null)
            return Stream.empty();

        Map<String, Integer> columnIndex = new HashMap<>();
        String[] headerColumns = pattern.split(header);
        parseHeaderLine(headerColumns, columnIndex);

        return reader.lines()
                .map(pattern::split)
                .map(columns -> toTransaction(columns, columnIndex, langIndex))
                .filter(this::badTransactionFilter);
    }


    private boolean badTransactionFilter(Transaction transaction) {
        return !Double.isNaN(transaction.getPrice()) && !Double.isNaN(transaction.getQuantity());
    }


    private void parseHeaderLine(String[] header, Map<String, Integer> columnIndexMap) {
        for (int i = 0; i < header.length; ++i)
            columnIndexMap.put(header[i].trim(), i);
    }


    private Transaction toTransaction(String[] columns, Map<String, Integer> columnIndexMap, int langIndex) {
        final Transaction transaction = new Transaction();

        // Assign all transaction fields if present.
        columnIndexMap.computeIfPresent(COLUMN_PUBLICATION_DATE[langIndex], (key, index) -> { transaction.setPublicationDate(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_ISSUER[langIndex], (key, index) -> { transaction.setIssuer(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_LEI_CODE[langIndex], (key, index) -> { transaction.setLeiCode(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_NOTIFIER[langIndex], (key, index) -> { transaction.setNotifier(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[langIndex], (key, index) -> { transaction.setPersonDischargingManagerialResponsibilities(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_POSITION[langIndex], (key, index) -> { transaction.setPosition(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_IS_CLOSELY_ASSOCIATE[langIndex], (key, index) -> { transaction.setCloselyAssociated(isTrue(columns[index], langIndex)); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_IS_AMENDMENT[langIndex], (key, index) -> { transaction.setAmendment(isTrue(columns[index], langIndex)); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_DETAILS_OF_AMENDMENT[langIndex], (key, index) -> { transaction.setDetailsOfAmendment(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_IS_INITIAL_NOTIFICATION[langIndex], (key, index) -> { transaction.setInitialNotification(isTrue(columns[index], langIndex)); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[langIndex], (key, index) -> { transaction.setLinkedToShareOptionProgramme(isTrue(columns[index], langIndex)); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_NATURE_OF_TRANSACTION[langIndex], (key, index) -> { transaction.setNatureOfTransaction(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_INSTRUMENT[langIndex], (key, index) -> { transaction.setInstrument(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_ISIN[langIndex], (key, index) -> { transaction.setIsin(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_TRANSACTION_DATE[langIndex], (key, index) -> { transaction.setTransactionDate(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_QUANTITY[langIndex], (key, index) -> { transaction.setQuantity(parseDouble(columns[index])); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_UNIT[langIndex], (key, index) -> { transaction.setUnit(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_PRICE[langIndex], (key, index) -> { transaction.setPrice(parseDouble(columns[index])); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_CURRENCY[langIndex], (key, index) -> { transaction.setCurrency(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_TRADING_VENUE[langIndex], (key, index) -> { transaction.setTradingVenue(columns[index]); return index; } );
        columnIndexMap.computeIfPresent(COLUMN_STATUS[langIndex], (key, index) -> { transaction.setStatus(columns[index]); return index; } );

        return transaction;
    }


    private double parseDouble(String value) {
        double floatNumber;

        try {
            value = value.replace(',', '.');
            floatNumber = Double.valueOf(value);
        }
        catch (Exception e) {
            if (logger != null && logger.isLoggable(Level.WARNING))
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
