
# record-builder

Builders for Java records, dead simple!

## Quick Start

Maven `pom.xml`:

~~~
    <dependency>
      <groupId>com.github.phoswald</groupId>
      <artifactId>record-builder</artifactId>
      <version>0.1.0</version>
      <scope>provided</scope>
    </dependency>
~~~

Java `record`:

~~~
@RecordBuilder
public record SimpleRecord(int intArg, String stringArg, List<String> listArg) { }
~~~

Usage:

~~~
        // Create a new instance from a builder:
        SimpleRecord rec1 = new SimpleRecordBuilder()
                .intArg(42)
                .stringArg("str")
                .listArg(singletonList("elem"))
                .build();

        // Create a builder from an existing instance:
        SimpleRecord rec2 = new SimpleRecordBuilder(rec1)
                .intArg(43)
                .build();
~~~
