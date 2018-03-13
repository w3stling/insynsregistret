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


public class Transaction {
    private String publicationDate;
    private String issuer;
    private String leiCode;
    private String notifier;
    private String personDischargingManagerialResponsibilities;
    private String position;
    private boolean isCloselyAssociated;
    private boolean isAmendment;
    private String detailsOfAmendment;
    private boolean isInitialNotification;
    private boolean isLinkedToShareOptionProgramme;
    private String natureOfTransaction;
    private String instrument;
    private String isin;
    private String transactionDate;
    private long quantity;
    private String unit;
    private double price;
    private String currency;
    private String tradingVenue;
    private String status;

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getLeiCode() {
        return leiCode;
    }

    public void setLeiCode(String leiCode) {
        this.leiCode = leiCode;
    }

    public String getNotifier() {
        return notifier;
    }

    public void setNotifier(String notifier) {
        this.notifier = notifier;
    }

    public String getPersonDischargingManagerialResponsibilities() {
        return personDischargingManagerialResponsibilities;
    }

    public void setPersonDischargingManagerialResponsibilities(String personDischargingManagerialResponsibilities) {
        this.personDischargingManagerialResponsibilities = personDischargingManagerialResponsibilities;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isCloselyAssociated() {
        return isCloselyAssociated;
    }

    public void setCloselyAssociated(boolean closelyAssociated) {
        isCloselyAssociated = closelyAssociated;
    }

    public boolean isAmendment() {
        return isAmendment;
    }

    public void setAmendment(boolean amendment) {
        isAmendment = amendment;
    }

    public String getDetailsOfAmendment() {
        return detailsOfAmendment;
    }

    public void setDetailsOfAmendment(String detailsOfAmendment) {
        this.detailsOfAmendment = detailsOfAmendment;
    }

    public boolean isInitialNotification() {
        return isInitialNotification;
    }

    public void setInitialNotification(boolean initialNotification) {
        isInitialNotification = initialNotification;
    }

    public boolean isLinkedToShareOptionProgramme() {
        return isLinkedToShareOptionProgramme;
    }

    public void setLinkedToShareOptionProgramme(boolean linkedToShareOptionProgramme) {
        isLinkedToShareOptionProgramme = linkedToShareOptionProgramme;
    }

    public String getNatureOfTransaction() {
        return natureOfTransaction;
    }

    public void setNatureOfTransaction(String natureOfTransaction) {
        this.natureOfTransaction = natureOfTransaction;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTradingVenue() {
        return tradingVenue;
    }

    public void setTradingVenue(String tradingVenue) {
        this.tradingVenue = tradingVenue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
