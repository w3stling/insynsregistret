/*
 * MIT License
 *
 * Copyright (c) 2022, Apptastic Software
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
package com.apptasticsoftware.insynsregistret;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;


/**
 * Class makes it easier to automate data extraction from <a href="https://www.fi.se/sv/vara-register/insynsregistret">Insynsregistret</a>.
 * <p>
 * Insynsregistret is a Swedish financial registry maintained by the <a href="https://www.fi.se">Finansinspektionen</a> (FI).
 * It contains information regarding insider trading on <a href="http://www.nasdaqomxnordic.com">Nasdaq Stockholm</a> and
 * <a href="http://www.ngm.se">Nordic Growth Market</a> (NGM) and other trading venues.
 * <p>
 * This registry publishes information about the trading activities that have taken place during
 * the day performed by the insiders and people close to them. The registry includes information
 * concerning the position the insider involved in a certain trading activity has, what kind of
 * activity it is (sell, buy or gift etc.), what kind of security that is traded and the quantity.
 * <p>
 * All insider trading is reported to FI, which publishes the data on a daily basis to this public database.
 */
public class Insynsregistret {
    private final boolean processTransactionInParallel;
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public Insynsregistret() {
        var parallelProperty = System.getProperty("insynsregistret.parallel", "false");
        processTransactionInParallel = parallelProperty.equalsIgnoreCase("true");
    }

    /**
     * Free text search for issuer names or person discharging managerial responsibilities (PDMR).
     * {@link FreeTextQuery} object is created by {@link FreeTextQueryBuilder} class.
     * @param query free text query
     * @return A {@link java.util.stream.Stream} of {@link java.lang.String} objects representing issuer names or PDMR names
     * @throws IOException exception
     */
    public Stream<String> search(FreeTextQuery query) throws IOException {
        var response = sendRequest(query.getUrl(), StandardCharsets.UTF_8);

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
        var response = sendRequest(query.getUrl(), StandardCharsets.UTF_16LE);

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
        var req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept-Encoding", "gzip")
                .header("User-Agent", HTTP_USER_AGENT)
                .GET()
                .build();

        try {
            var httpClient = createHttpClient();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            var inputStream = resp.body();

            if (Optional.of("gzip").equals(resp.headers().firstValue("Content-Encoding")))
                inputStream = new GZIPInputStream(inputStream);

            var reader = new InputStreamReader(inputStream, encoding);
            return new BufferedReader(reader);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }


    private Stream<String> parseFreeTextQueryResponse(BufferedReader reader) {
        return reader.lines().flatMap(this::splitString).distinct();
    }


    private Stream<String> splitString(final String text) {
        if (text.length() <= 2)
            return Stream.empty();

        var tokens = new LinkedList<String>();
        var found = true;
        var start = 0;
        var end = 0;

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
        // Read header
        var header = reader.readLine();

        if (header == null)
            return Stream.empty();

        String[] headerColumns = splitLine(header, 32);
        var mapper = new TransactionMapper();
        var nofColumns = mapper.initialize(headerColumns);

        // Read the rest of the lines after the header
        Stream<String> lineStream = reader.lines();

        if (processTransactionInParallel)
            lineStream = lineStream.parallel();

        return lineStream.map(l -> splitLine(l, nofColumns))
                .map(mapper::createTransaction)
                .filter(this::badTransactionFilter);
    }


    String[] splitLine(String text, int nofColumns) {
        var index = 0;
        var found = true;
        var extraForQuotationMark = 0;
        var start = 0;
        var end = 0;
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
                var token = text.substring(start, end);
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


    static class TransactionMapper {
        private static final String[] COLUMN_PUBLICATION_DATE = {"Publiceringsdatum", "Publication date"};
        private static final String[] COLUMN_ISSUER = {"Emittent", "Issuer"};
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
        private static final String[] COLUMN_INSTRUMENT_TYPE = {"Instrumenttyp", "Intrument type"};
        private static final String[] COLUMN_INSTRUMENT_NAME = {"Instrumentnamn", "Instrument name"};
        private static final String[] COLUMN_ISIN = {"ISIN", "ISIN"};
        private static final String[] COLUMN_TRANSACTION_DATE = {"Transaktionsdatum", "Transaction date"};
        private static final String[] COLUMN_QUANTITY = {"Volym", "Volume"};
        private static final String[] COLUMN_UNIT = {"Volymsenhet", "Unit"};
        private static final String[] COLUMN_PRICE = {"Pris", "Price"};
        private static final String[] COLUMN_CURRENCY = {"Valuta", "Currency"};
        private static final String[] COLUMN_TRADING_VENUE = {"Handelsplats", "Trading venue"};
        private static final String[] COLUMN_STATUS = {"Status", "Status"};
        private static final HashMap<String, BiConsumer<Transaction, String>> COLUMN_NAME_FIELD_MAPPING;
        final ArrayList<BiConsumer<Transaction, String>> columnIndexFieldMapping;


        TransactionMapper() {
            columnIndexFieldMapping = new ArrayList<>();
        }


        int initialize(String[] header) {
            int i;

            for (i = 0; i < header.length; ++i) {

                if (header[i] == null)
                    break;

                var headerColumnText = header[i].trim();
                columnIndexFieldMapping.add(i, COLUMN_NAME_FIELD_MAPPING.get(headerColumnText));
            }

            return i;
        }


        Transaction createTransaction(String[] columns) {
            var transaction = new Transaction();
            var length = Math.min(columns.length, columnIndexFieldMapping.size());

            for (var i = 0; i < length; ++i) {
                var fieldMapping = columnIndexFieldMapping.get(i);

                if (fieldMapping != null)
                    fieldMapping.accept(transaction, columns[i]);
            }

            return transaction;
        }


        private static double parseDouble(String value) {
            var floatNumber = 0.0;

            try {
                value = value.replace(',', '.');
                floatNumber = Double.parseDouble(value);
            }
            catch (Exception e) {
                var logger = Logger.getLogger("com.apptastic.insynsregistret");

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
            COLUMN_NAME_FIELD_MAPPING = new HashMap<>(64);

            // Publication date
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PUBLICATION_DATE[Language.SWEDISH.getIndex()], Transaction::setPublicationDate);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PUBLICATION_DATE[Language.ENGLISH.getIndex()], Transaction::setPublicationDate);

            // Issuer
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_ISSUER[Language.SWEDISH.getIndex()], Transaction::setIssuer);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_ISSUER[Language.ENGLISH.getIndex()], Transaction::setIssuer);

            // LEI Code
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_LEI_CODE[Language.SWEDISH.getIndex()], Transaction::setLeiCode);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_LEI_CODE[Language.ENGLISH.getIndex()], Transaction::setLeiCode);

            // Notifier
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_NOTIFIER[Language.SWEDISH.getIndex()], Transaction::setNotifier);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_NOTIFIER[Language.ENGLISH.getIndex()], Transaction::setNotifier);

            // PDMR
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[Language.SWEDISH.getIndex()], Transaction::setPdmr);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PERSON_DISCHARGING_MANAGERIAL_RESPONSIBILITIES[Language.ENGLISH.getIndex()], Transaction::setPdmr);

            // Position
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_POSITION[Language.SWEDISH.getIndex()], Transaction::setPosition);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_POSITION[Language.ENGLISH.getIndex()], Transaction::setPosition);

            // Is closely associated
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_CLOSELY_ASSOCIATE[Language.SWEDISH.getIndex()], (t, v) -> t.setCloselyAssociated(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_CLOSELY_ASSOCIATE[Language.ENGLISH.getIndex()], (t, v) -> t.setCloselyAssociated(isTrue(v, Language.ENGLISH.getIndex())) );

            // Is amendment
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_AMENDMENT[Language.SWEDISH.getIndex()], (t, v) -> t.setAmendment(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_AMENDMENT[Language.ENGLISH.getIndex()], (t, v) -> t.setAmendment(isTrue(v, Language.ENGLISH.getIndex())) );

            // Details of amendment
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_DETAILS_OF_AMENDMENT[Language.SWEDISH.getIndex()], Transaction::setDetailsOfAmendment);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_DETAILS_OF_AMENDMENT[Language.ENGLISH.getIndex()], Transaction::setDetailsOfAmendment);

            // Is initial notification
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_INITIAL_NOTIFICATION[Language.SWEDISH.getIndex()], (t, v) -> t.setInitialNotification(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_INITIAL_NOTIFICATION[Language.ENGLISH.getIndex()], (t, v) -> t.setInitialNotification(isTrue(v, Language.ENGLISH.getIndex())) );

            // Is linked to share option programme
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[Language.SWEDISH.getIndex()], (t, v) -> t.setLinkedToShareOptionProgramme(isTrue(v, Language.SWEDISH.getIndex())) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_IS_LINKED_TO_SHARE_OPTION_PROGRAMME[Language.ENGLISH.getIndex()], (t, v) -> t.setLinkedToShareOptionProgramme(isTrue(v, Language.ENGLISH.getIndex())) );

            // Nature of transaction
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_NATURE_OF_TRANSACTION[Language.SWEDISH.getIndex()], Transaction::setNatureOfTransaction);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_NATURE_OF_TRANSACTION[Language.ENGLISH.getIndex()], Transaction::setNatureOfTransaction);

            // Instrument type
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_INSTRUMENT_TYPE[Language.SWEDISH.getIndex()], Transaction::setInstrumentType);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_INSTRUMENT_TYPE[Language.ENGLISH.getIndex()], Transaction::setInstrumentType);

            // Instrument name
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_INSTRUMENT_NAME[Language.SWEDISH.getIndex()], Transaction::setInstrumentName);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_INSTRUMENT_NAME[Language.ENGLISH.getIndex()], Transaction::setInstrumentName);

            // ISIN
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_ISIN[Language.SWEDISH.getIndex()], Transaction::setIsin);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_ISIN[Language.ENGLISH.getIndex()], Transaction::setIsin);

            // Transaction date
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_TRANSACTION_DATE[Language.SWEDISH.getIndex()], Transaction::setTransactionDate);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_TRANSACTION_DATE[Language.ENGLISH.getIndex()], Transaction::setTransactionDate);

            // Quantity
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_QUANTITY[Language.SWEDISH.getIndex()], (t, v) -> t.setQuantity(parseDouble(v)) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_QUANTITY[Language.ENGLISH.getIndex()], (t, v) -> t.setQuantity(parseDouble(v)) );

            // Transaction date
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_UNIT[Language.SWEDISH.getIndex()], Transaction::setUnit);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_UNIT[Language.ENGLISH.getIndex()], Transaction::setUnit);

            // Price
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PRICE[Language.SWEDISH.getIndex()], (t, v) -> t.setPrice(parseDouble(v)) );
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_PRICE[Language.ENGLISH.getIndex()], (t, v) -> t.setPrice(parseDouble(v)) );

            // Currency
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_CURRENCY[Language.SWEDISH.getIndex()], Transaction::setCurrency);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_CURRENCY[Language.ENGLISH.getIndex()], Transaction::setCurrency);

            // Trading venue
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_TRADING_VENUE[Language.SWEDISH.getIndex()], Transaction::setTradingVenue);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_TRADING_VENUE[Language.ENGLISH.getIndex()], Transaction::setTradingVenue);

            // Status
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_STATUS[Language.SWEDISH.getIndex()], Transaction::setStatus);
            COLUMN_NAME_FIELD_MAPPING.put(COLUMN_STATUS[Language.ENGLISH.getIndex()], Transaction::setStatus);
        }
    }


    private HttpClient createHttpClient() {
        HttpClient httpClient;

        try {
            SSLContext context = SSLContext.getInstance("TLSv1.3");
            context.init(null, null, null);

            httpClient = HttpClient.newBuilder()
                    .sslContext(context)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
        }

        return httpClient;
    }

}
