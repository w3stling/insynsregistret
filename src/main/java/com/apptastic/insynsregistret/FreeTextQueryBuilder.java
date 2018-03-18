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


/**
 * Builder class for creating a query for searching issuer names and person discharging managerial responsibilities (PDMR) names.
 */
public class FreeTextQueryBuilder {
    private String issuer;
    private String personDischargingManagerialResponsibilities;

    private FreeTextQueryBuilder() {

    }

    /**
     * Creates an instance of {@link FreeTextQueryBuilder} that is used for building a query for issuer names.
     * @param freeText text to search for that is part of the issuer name
     * @return Builder object
     */
    public static FreeTextQueryBuilder issuer(String freeText) {
        if (freeText == null)
            throw new IllegalArgumentException("Issuer free text is null");

        FreeTextQueryBuilder builder = new FreeTextQueryBuilder();
        builder.issuer = freeText;
        return builder;
    }

    /**
     * Creates an instance of {@link FreeTextQueryBuilder} that is used for building a query for person discharging managerial responsibilities (PDMR) names.
     * @param freeText text to search for that is part of the PDMR name
     * @return Builder object
     */
    public static FreeTextQueryBuilder personDischargingManagerialResponsibilities(String freeText) {
        if (freeText == null)
            throw new IllegalArgumentException("Issuer free text is null");

        FreeTextQueryBuilder builder = new FreeTextQueryBuilder();
        builder.personDischargingManagerialResponsibilities = freeText;
        return builder;
    }

    /**
     * Creates the query object for searching issuer names and PDMR names via {@link Insynsregistret} class
     * @return query object
     * @throws UnsupportedEncodingException exception
     */
    public FreeTextQuery build() throws UnsupportedEncodingException {
        return new FreeTextQuery(issuer, personDischargingManagerialResponsibilities);
    }
}
