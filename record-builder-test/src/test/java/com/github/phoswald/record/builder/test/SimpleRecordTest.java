package com.github.phoswald.record.builder.test;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimpleRecordTest {

    @Test
    void build_newValid_success() {
        SimpleRecord testee = new SimpleRecordBuilder() //
                .intArg(42) //
                .stringArg("str") //
                .listArg(singletonList("elem")) //
                .build();

        assertEquals(42, testee.intArg());
        assertEquals("str", testee.stringArg());
        assertEquals(singletonList("elem"), testee.listArg());
    }

    @Test
    void build_updateValid_success() {
        SimpleRecord orig = new SimpleRecordBuilder() //
                .intArg(42) //
                .stringArg("str") //
                .listArg(singletonList("elem")) //
                .build();

        SimpleRecord testee = new SimpleRecordBuilder(orig) //
                .intArg(43) //
                .build();

        assertEquals(43, testee.intArg());
        assertEquals("str", testee.stringArg());
        assertEquals(singletonList("elem"), testee.listArg());
    }
}
