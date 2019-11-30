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
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * Class that represents the query for inside trade transaction. It is used when searching for transactions.
 * Object of this class is created via {@link TransactionQueryBuilder} class.
 */
public class TransactionQuery {
    private static final String INSYNSREGISTERET_URL = "https://marknadssok.fi.se/publiceringsklient/%1$s/Search/Search?SearchFunctionType=Insyn&Utgivare=%2$s&PersonILedandeStällningNamn=%3$s&Transaktionsdatum.From=%4$s&Transaktionsdatum.To=%5$s&Publiceringsdatum.From=%6$s&Publiceringsdatum.To=%7$s&button=export";
    private static final String URL_ENCODING = "UTF-8";
    private final DateTimeFormatter dateFormatter;
    private final String url;
    private final Language language;


    /**
     * Constructor for inside trade transaction query.
     * Query either transactions or publication between dates. Optional parameters issue, PDMR and language parameters.
     * @param fromTransactionDate from transaction date
     * @param toTransactionDate to transaction date
     * @param fromPublicationDate from publication date
     * @param toPublicationDate to publication date
     * @param issuer - issuer name
     * @param pdmr person discharging managerial responsibilities (PDMR) name
     * @param language - language
     * @throws UnsupportedEncodingException exception
     */
    @Deprecated
    TransactionQuery(Date fromTransactionDate, Date toTransactionDate, Date fromPublicationDate, Date toPublicationDate,
                     String issuer, String pdmr, Language language) throws UnsupportedEncodingException {
        
        String issuerName = URLEncoder.encode(orDefault(issuer, ""), URL_ENCODING);
        String pdmrName = URLEncoder.encode(orDefault(pdmr, ""), URL_ENCODING);
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.language = orDefault(language, Language.SWEDISH);
        String languageName = this.language.getName();

        url = String.format(INSYNSREGISTERET_URL, languageName, issuerName, pdmrName,
                toDateString(toLocalDate(fromTransactionDate)), toDateString(toLocalDate(toTransactionDate)),
                toDateString(toLocalDate(fromPublicationDate)), toDateString(toLocalDate(toPublicationDate)));
    }

    TransactionQuery(LocalDate fromTransactionDate, LocalDate toTransactionDate,
                     LocalDate fromPublicationDate, LocalDate toPublicationDate,
                     String issuer, String pdmr, Language language) throws UnsupportedEncodingException {

        String issuerName = URLEncoder.encode(orDefault(issuer, ""), URL_ENCODING);
        String pdmrName = URLEncoder.encode(orDefault(pdmr, ""), URL_ENCODING);
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.language = orDefault(language, Language.SWEDISH);
        String languageName = this.language.getName();

        url = String.format(INSYNSREGISTERET_URL, languageName, issuerName, pdmrName,
                toDateString(fromTransactionDate), toDateString(toTransactionDate),
                toDateString(fromPublicationDate), toDateString(toPublicationDate));
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null)
            return null;

        return LocalDate.ofInstant(date.toInstant(), ZoneId.of("Europe/Stockholm"));
    }

    private String toDateString(LocalDate date) {
        if (date == null)
            return "";

        return date.format(dateFormatter);
    }

    private static <T> T orDefault(T value, T defaultValue) {
        return (value != null) ? value : defaultValue;
    }

    /**
     * Get the URL for the inside trade transaction query.
     * @return URL
     */
    public String getUrl() {
        return url;
    }
}
