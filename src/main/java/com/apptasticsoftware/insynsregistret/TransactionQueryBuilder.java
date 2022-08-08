/*
 * MIT License
 *
 * Copyright (c) 2020, Apptastic Software
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

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


/**
 * Builder class for creating a query for searching inside trade transactions via the {@link Insynsregistret} class.
 */
public class TransactionQueryBuilder {
    private LocalDate fromTransactionDate;
    private LocalDate toTransactionDate;
    private LocalDate fromPublicationDate;
    private LocalDate toPublicationDate;
    private String issuer;
    private String pdmr;
    private Language language;


    private TransactionQueryBuilder() {

    }

    /**
     * Query inside trade transactions between the given dates.
     * @param from from date (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     *
     * @deprecated Use LocalDate class instead of Date class
     */
    @SuppressWarnings("squid:S1133")
    @Deprecated(since="2.2.0")
    public static TransactionQueryBuilder transactions(Date from, Date to) {
        LocalDate fromDate = toLocalDate(from);
        LocalDate toDate = toLocalDate(to);
        return transactions(fromDate, toDate);
    }

    /**
     * Query inside trade transactions between the given dates.
     * @param from from date (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     */
    public static TransactionQueryBuilder transactions(LocalDate from, LocalDate to) {
        if (from == null)
            throw new IllegalArgumentException("From transaction date is null");
        else if (to == null)
            throw new IllegalArgumentException("To transaction date is null");
        else if (from.toEpochDay() > to.toEpochDay())
            throw new IllegalArgumentException("From date after to date is not allowed");

        TransactionQueryBuilder builder = new TransactionQueryBuilder();
        builder.fromTransactionDate = from;
        builder.toTransactionDate = to;

        return builder;
    }

    /**
     * Query inside trade transactions from a given number of days back in time from today's date.
     * @param days number of days back in time
     * @return builder object
     */
    public static TransactionQueryBuilder transactionsLastDays(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Past transaction days is a negative number");

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        TransactionQueryBuilder builder = new TransactionQueryBuilder();

        builder.fromTransactionDate = from;
        builder.toTransactionDate = to;

        return builder;
    }

    /**
     * Query inside trade transactions was published between the given dates.
     * Usually transactions are published with 3 days from then the transaction occurred.
     * @param from from date  (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     *
     * @deprecated Use LocalDate class instead of Date class
     */
    @SuppressWarnings("squid:S1133")
    @Deprecated(since="2.2.0")
    public static TransactionQueryBuilder publications(Date from, Date to) {
        LocalDate fromDate = toLocalDate(from);
        LocalDate toDate = toLocalDate(to);
        return publications(fromDate, toDate);
    }

    /**
     * Query inside trade transactions was published between the given dates.
     * Usually transactions are published with 3 days from then the transaction occurred.
     * @param from from date  (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     */
    public static TransactionQueryBuilder publications(LocalDate from, LocalDate to) {
        if (from == null)
            throw new IllegalArgumentException("From publication date is null");
        else if (to == null)
            throw new IllegalArgumentException("To publication date is null");
        else if (from.toEpochDay() > to.toEpochDay())
            throw new IllegalArgumentException("From date after to date is not allowed");

        TransactionQueryBuilder builder = new TransactionQueryBuilder();
        builder.fromPublicationDate = from;
        builder.toPublicationDate = to;

        return builder;
    }

    /**
     * Query inside trade transactions that as publications from a given number of days back in time from today's date.
     * Usually transactions are published with 3 days from then the transaction occurred.
     * @param days number of days back in time
     * @return builder object
     */
    public static TransactionQueryBuilder publicationsLastDays(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Past publication days is a negative number");

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        TransactionQueryBuilder builder = new TransactionQueryBuilder();

        builder.fromPublicationDate = from;
        builder.toPublicationDate = to;

        return builder;
    }

    /**
     * Limit the transaction to the this issuer.
     * @param issuer name of the issuer
     * @return builder object
     */
    public TransactionQueryBuilder issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * Limit the transaction to the this person discharging managerial responsibilities (PDMR).
     * @param pdmr name of the PDMR
     * @return builder object
     */
    public TransactionQueryBuilder pdmr(String pdmr) {
        this.pdmr = pdmr;
        return this;
    }

    /**
     * In what language the inside trade transaction should be presented in. Default is Swedish.
     * @param language language to use.
     * @return builder object
     */
    public TransactionQueryBuilder language(Language language) {
        this.language = language;
        return this;
    }

    /**
     * Creates the query object for searching transactions via {@link Insynsregistret} class
     * @return query object
     * @throws UnsupportedEncodingException exception
     */
    public TransactionQuery build() throws UnsupportedEncodingException {
        return new TransactionQuery(fromTransactionDate, toTransactionDate,
                         fromPublicationDate, toPublicationDate,
                         issuer, pdmr,
                         language);
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null)
            return null;

        return LocalDate.ofInstant(date.toInstant(), ZoneId.of("Europe/Stockholm"));
    }

}
