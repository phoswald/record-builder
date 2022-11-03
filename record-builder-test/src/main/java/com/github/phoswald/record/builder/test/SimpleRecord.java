package com.github.phoswald.record.builder.test;

import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record SimpleRecord(int intArg, String stringArg, List<String> listArg) { }
