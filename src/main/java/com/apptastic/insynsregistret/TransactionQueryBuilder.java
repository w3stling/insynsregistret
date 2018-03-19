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

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Builder class for creating a query for searching inside trade transactions via the {@link Insynsregistret} class.
 */
public class TransactionQueryBuilder {
    private Date fromTransactionDate;
    private Date toTransactionDate;
    private Date fromPublicationDate;
    private Date toPublicationDate;
    private String issuer;
    private String personDischargingManagerialResponsibilities;
    private Language language;


    private TransactionQueryBuilder() {

    }

    /**
     * Query inside trade transactions between the given dates.
     * @param from from date (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     */
    public static TransactionQueryBuilder transactions(Date from, Date to) {
        if (from == null)
            throw new IllegalArgumentException("From transaction date is null");
        else if (to == null)
            throw new IllegalArgumentException("To transaction date is null");
        else if (TimeUnit.MILLISECONDS.toDays(from.getTime()) > TimeUnit.MILLISECONDS.toDays(to.getTime()))
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
    public static TransactionQueryBuilder transactionsPastXDays(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Past transaction days is a negative number");

        Calendar cal = Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date from = cal.getTime();

        TransactionQueryBuilder builder = new TransactionQueryBuilder();

        builder.fromTransactionDate = from;
        builder.toTransactionDate = to;

        return builder;
    }

    /**
     * Query between the given dates from when inside trade transactions was published.
     * Usually transactions are published with 3 days from then the transaction occurred.
     * @param from from date  (Year, month and day resolution)
     * @param to to date (Year, month and day resolution)
     * @return builder object
     */
    public static TransactionQueryBuilder publications(Date from, Date to) {
        if (from == null)
            throw new IllegalArgumentException("From publication date is null");
        else if (to == null)
            throw new IllegalArgumentException("To publication date is null");
        else if (TimeUnit.MILLISECONDS.toDays(from.getTime()) > TimeUnit.MILLISECONDS.toDays(to.getTime()))
            throw new IllegalArgumentException("From date after to date is not allowed");

        TransactionQueryBuilder builder = new TransactionQueryBuilder();
        builder.fromPublicationDate = from;
        builder.toPublicationDate = to;

        return builder;
    }

    /**
     * Query inside trade transactions publications from a given number of days back in time from today's date.
     * Usually transactions are published with 3 days from then the transaction occurred.
     * @param days number of days back in time
     * @return builder object
     */
    public static TransactionQueryBuilder publicationsPastXDays(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Past publication days is a negative number");

        Calendar cal = Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date from = cal.getTime();

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
    public TransactionQueryBuilder personDischargingManagerialResponsibilities(String pdmr) {
        personDischargingManagerialResponsibilities = pdmr;
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
     */
    public TransactionQuery build() throws UnsupportedEncodingException {
        return new TransactionQuery(fromTransactionDate, toTransactionDate,
                         fromPublicationDate, toPublicationDate,
                         issuer, personDischargingManagerialResponsibilities,
                         language);
    }
}
