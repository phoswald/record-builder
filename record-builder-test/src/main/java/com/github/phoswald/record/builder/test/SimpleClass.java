package com.github.phoswald.record.builder.test;

import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

class SimpleClass {

    @RecordBuilder
    record SimmpleInnerRecord(int intArg, String stringArg, List<String> listArg) { }
}
