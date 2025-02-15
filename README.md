Insynsregistret
===============

[![Build](https://github.com/w3stling/insynsregistret/actions/workflows/build.yml/badge.svg)](https://github.com/w3stling/insynsregistret/actions/workflows/build.yml)
[![Download](https://img.shields.io/badge/download-4.3.1-brightgreen.svg)](https://central.sonatype.com/artifact/com.apptasticsoftware/insynsregistret/4.3.1/overview)
[![Javadoc](https://img.shields.io/badge/javadoc-4.3.1-blue.svg)](https://w3stling.github.io/insynsregistret/javadoc/4.3.1)
[![License](http://img.shields.io/:license-MIT-blue.svg?style=flat-round)](http://apptastic-software.mit-license.org)   
[![CodeQL](https://github.com/w3stling/insynsregistret/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/w3stling/insynsregistret/actions/workflows/codeql-analysis.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=w3stling_insynsregistret&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=w3stling_insynsregistret)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=w3stling_insynsregistret&metric=coverage)](https://sonarcloud.io/summary/new_code?id=w3stling_insynsregistret)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=w3stling_insynsregistret&metric=bugs)](https://sonarcloud.io/summary/new_code?id=w3stling_insynsregistret)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=w3stling_insynsregistret&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=w3stling_insynsregistret)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=w3stling_insynsregistret&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=w3stling_insynsregistret)

> **Note** - from version 4.0.0:
> * New Java package name
> * New group ID in Maven / Gradle dependency declaration
> * Moved repository from `JCenter` to `Maven Central Repository`

[Insynsregistret][1] is a Swedish financial registry maintained by
the [Finansinspektionen][2] (FI). It contains information regarding insider trading on
Nasdaq Stockholm and Nordic Growth Market (NGM) and other trading venues.

This registry publishes information about the trading activities that have taken place during the day performed by
the insiders and people close to them. The registry includes information concerning
the position the insider involved in a certain trading activity has, what kind of activity it is (sell, buy or
gift etc.), what kind of security that is traded and the quantity.

All insider trading is reported to FI, which publishes the data on a daily basis to this public database.

This Java library makes it easier to automate data extraction from Insynsregistret.

Examples
--------
### Search issuer
Find all issuer names that contains the word "fing" and prints it.
Issuer can be used to refine the transaction search, see example below.
```java
Insynsregistret registry = new Insynsregistret();

FreeTextQuery query = FreeTextQueryBuilder.issuer("fing").build();
registry.search(query).forEach(System.out::println);
```

### Search persons discharging managerial responsibilities (PDMR)
Find the first PDMR name that contains the word "Carl".
PDMR can be used to refine the transaction search.
```java
Insynsregistret registry = new Insynsregistret();

FreeTextQuery query = FreeTextQueryBuilder.pdmr("Carl").build();
Optional<String> pdmr = registry.search(query).findFirst();
```

### Search transactions
Get all insider trades published in the last 30 days, presented in English. Default language is Swedish.

```java
Insynsregistret registry = new Insynsregistret();

TransactionQuery query = TransactionQueryBuilder.publicationsLastDays(30)
        .language(Language.ENGLISH)
        .build();

List<Transaction> transactions = registry.search(query)
        .collect(Collectors.toList());
```

Get all insider trades published in the last 30 days in Swedish Match (ISIN SE0000310336)
and that is part of a share option programme.

```java
Insynsregistret registry = new Insynsregistret();

TransactionQuery query = TransactionQueryBuilder.publicationsLastDays(30).build();

List<Transaction> transactions = registry.search(query)
        .filter(Transaction::isLinkedToShareOptionProgramme)
        .filter(t -> t.getIsin().equals("SE0000310336"))
        .collect(Collectors.toList());
```

A better way to achieve the same result as in the example above is to refine the search
with an issuer query parameter to limit the search instead of filtering the search result.
See next example with Hexagon AB.

Get the number of inside trades in Hexagon between given dates.

```java
Insynsregistret registry = new Insynsregistret();

TransactionQuery query = TransactionQueryBuilder.publications(getFromDate(), getToDate())
        .issuer("Hexagon AB")
        .build();

long nofTransactions = registry.search(query).count();
```

Total value of all inside trades in company Loomis the last 30 days.

```java
Insynsregistret registry = new Insynsregistret();

TransactionQuery query = TransactionQueryBuilder.transactionsLastDays(30)
        .issuer("Loomis AB")
        .build();

double total = registry.search(query)
        .mapToDouble(t -> t.getQuantity() * t.getPrice())
        .sum();
```

Java System Properties
----------------------
| Key | Description | Default |
| :--- | :--- | :--- |
| https.proxyHost | The host name of the proxy server. |   |
| https.proxyPort | The port number of the proxy server. |   |

Download
--------

Download [the latest JAR][3] or grab via [Maven][4] or [Gradle][5].

### Maven setup
Add dependency declaration:
```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.apptasticsoftware</groupId>
            <artifactId>insynsregistret</artifactId>
            <version>4.3.1</version>
        </dependency>
    </dependencies>
    ...
</project>
```

### Gradle setup
Add dependency declaration:
```groovy
dependencies {
    implementation 'com.apptasticsoftware:insynsregistret:4.3.1'
}
```

Insynsregistret library requires at minimum Java 11.

License
-------

    MIT License
    
    Copyright (c) 2025, Apptastic Software
    
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


[1]: https://www.fi.se/en/our-registers/pdmr-transactions
[2]: https://www.fi.se/en
[3]: https://central.sonatype.com/artifact/com.apptasticsoftware/insynsregistret/4.3.1/overview
[4]: https://maven.apache.org
[5]: https://gradle.org