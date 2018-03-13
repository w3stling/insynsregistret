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


public class QueryBuilder {
    private Date fromTransactionDate;
    private Date toTransactionDate;
    private Date fromPublicationDate;
    private Date toPublicationDate;
    private String issuer;
    private String personDischargingManagerialResponsibilities;
    private Language language;


    private QueryBuilder() {

    }

    static QueryBuilder instance() {
        return new QueryBuilder();
    }

    static QueryBuilder trasactionsPastXDays(int days) {
        Calendar cal = Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date from = cal.getTime();

        QueryBuilder builder = new QueryBuilder();

        builder.fromTransactionDate(from)
                .toTransactionDate(to);

        return builder;
    }

    static QueryBuilder publicationsPastXDays(int days) {
        Calendar cal = Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date from = cal.getTime();

        QueryBuilder builder = new QueryBuilder();

        builder.fromPublicationDate(from)
                .toPublicationDate(to);

        return builder;
    }

    public QueryBuilder fromTransactionDate(Date from) {
        fromTransactionDate = from;
        return this;
    }

    public QueryBuilder toTransactionDate(Date to) {
        toTransactionDate = to;
        return this;
    }

    public QueryBuilder fromPublicationDate(Date from) {
        fromPublicationDate = from;
        return this;
    }

    public QueryBuilder toPublicationDate(Date to) {
        toPublicationDate = to;
        return this;
    }

    public QueryBuilder issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public QueryBuilder personDischargingManagerialResponsibilities(String person) {
        personDischargingManagerialResponsibilities = person;
        return this;
    }

    public QueryBuilder language(Language language) {
        this.language = language;
        return this;
    }

    public Query build() {
        if ((fromTransactionDate == null || toTransactionDate == null) &&
            (fromPublicationDate == null || toPublicationDate == null)) {

            throw new IllegalArgumentException("Not both from and to transaction dates or from and to publication dates can be null");
        }

        return new Query(fromTransactionDate, toTransactionDate,
                         fromPublicationDate, toPublicationDate,
                         issuer, personDischargingManagerialResponsibilities,
                         language);
    }
}
