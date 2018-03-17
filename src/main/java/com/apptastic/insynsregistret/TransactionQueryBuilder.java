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

import java.util.Calendar;
import java.util.Date;


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

    public static TransactionQueryBuilder transactions(Date from, Date to) {
        if (from == null)
            throw new IllegalArgumentException("From transaction date is null");

        if (to == null)
            throw new IllegalArgumentException("To transaction date is null");

        TransactionQueryBuilder builder = new TransactionQueryBuilder();
        builder.fromTransactionDate = from;
        builder.toTransactionDate = to;

        return builder;
    }

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

    public static TransactionQueryBuilder publications(Date from, Date to) {
        if (from == null)
            throw new IllegalArgumentException("From publication date is null");

        if (to == null)
            throw new IllegalArgumentException("To publication date is null");

        TransactionQueryBuilder builder = new TransactionQueryBuilder();
        builder.fromPublicationDate = from;
        builder.toPublicationDate = to;

        return builder;
    }

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

    public TransactionQueryBuilder issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public TransactionQueryBuilder personDischargingManagerialResponsibilities(String person) {
        personDischargingManagerialResponsibilities = person;
        return this;
    }

    public TransactionQueryBuilder language(Language language) {
        this.language = language;
        return this;
    }

    public Query build() {
        return new Query(fromTransactionDate, toTransactionDate,
                         fromPublicationDate, toPublicationDate,
                         issuer, personDischargingManagerialResponsibilities,
                         language);
    }
}
