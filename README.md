Insynsregistret
===============

[![Build Status](https://travis-ci.org/w3stling/insynsregistret.svg?branch=master)](https://travis-ci.org/w3stling/insynsregistret)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://apptastic-software.mit-license.org)


[Insynsregistret](https://www.fi.se/sv/vara-register/insynsregistret) is a Swedish financial registry maintained by
the [Swedish Finansinspektionen](http://www.fi.se) (FI). It contains information regarding insider trading on
Nasdaq Stockholm and Nordic Growth Market (NGM) and other trading venues.

This registry publishes information about the trading activities that have taken place during the day performed by
the insiders and people close to them. The registry includes information concerning
the position the insider involved in a certain trading activity has, what kind of activity it is (sell, buy or
gift etc.), what kind of security that is traded and the quantity. 

All insider trading is reported to FI, which publishes the data on a daily basis to this public database.

This Java library makes it easier to automate data extraction from Insynsregistret.

Examples
--------
Get all insider trades published in the last 30 days.

```java
Insynsregistret registry = new Insynsregistret();

Query query = QueryBuilder.publicationsPastXDays(30).build();

List<Transaction> transactions = registry.search(query)
        .collect(Collectors.toList());
```
Get all insider trades published in the last 30 days in Swedish Match (ISIN SE0000310336)
and that is part of a share option programme.

```java
Insynsregistret registry = new Insynsregistret();

Query query = QueryBuilder.publicationsPastXDays(30).build();

List<Transaction> transactions = registry.search(query)
        .filter(t -> t.getIsin().equals("SE0000310336"))
        .filter(Transaction::isLinkedToShareOptionProgramme)
        .collect(Collectors.toList());
```

Get the number of inside trades in Hexagon between given dates.

```java
Insynsregistret registry = new Insynsregistret();

Query query = QueryBuilder.instance()
        .fromPublicationDate(getFrom())
        .toPublicationDate(getTo())
        .issuer("Hexagon AB")
        .build();

long nofTransactions = registry.search(query).count();
```

Total value of all inside trades in company Loomis the last month.

```java
Insynsregistret registry = new Insynsregistret();

Query query = QueryBuilder.trasactionsPastXDays(30)
        .issuer("Loomis AB")
        .build();

double total = registry.search(query)
        .mapToDouble(t -> t.getQuantity() * t.getPrice())
        .sum();
```

License
-------

    MIT License
    
    Copyright (c) 2018, Apptastic Software
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
