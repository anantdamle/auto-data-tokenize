/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.solutions.autotokenize.common;

import static com.google.cloud.solutions.autotokenize.testing.TestGenericRecordGenerator.generateGenericRecors;

import com.google.cloud.solutions.autotokenize.AutoTokenizeMessages;
import com.google.cloud.solutions.autotokenize.AutoTokenizeMessages.SourceType;
import com.google.cloud.solutions.autotokenize.testing.MatchRecordsCountFn;
import com.google.cloud.solutions.autotokenize.testing.TestGenericRecordGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.parquet.ParquetIO;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TransformingFileReaderTest {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Rule public transient TestPipeline writePipeline = TestPipeline.create();

  @Rule public transient TestPipeline readPipeline = TestPipeline.create();

  @Rule public transient TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final SourceType fileSourceType;

  private final int expectedRecordsCount;

  private String testFileFolder;

  @Parameterized.Parameters(name = "{0} file contains {1} records")
  public static Collection<Object[]> differentFileTypes() {
    return ImmutableList.<Object[]>builder()
        .add(new Object[] {SourceType.AVRO, 1000})
        .add(new Object[] {SourceType.PARQUET, 1000})
        .build();
  }

  public TransformingFileReaderTest(SourceType fileSourceType, int expectedRecordsCount) {
    this.fileSourceType = fileSourceType;
    this.expectedRecordsCount = expectedRecordsCount;
  }

  @Before
  public void generateTestRecordsFile() throws IOException {
    Schema testSchema = TestGenericRecordGenerator.SCHEMA;
    File tempFolder = temporaryFolder.newFolder();
    tempFolder.mkdirs();

    testFileFolder = tempFolder.getAbsolutePath();

    logger.atInfo().log("Writing records to: %s", testFileFolder);

    writePipeline
        .apply(
            Create.of(generateGenericRecors(expectedRecordsCount))
                .withCoder(AvroCoder.of(testSchema)))
        .apply(
            FileIO.<GenericRecord>write().via(fileTypeSpecificSink(testSchema)).to(testFileFolder));

    writePipeline.run().waitUntilFinish();
  }

  @Test
  public void expand_providedFile_recordCountMatched() {
    TupleTag<AutoTokenizeMessages.FlatRecord> recordsTag = new TupleTag<>();
    PCollection<AutoTokenizeMessages.FlatRecord> records =
        readPipeline
            .apply(
                TransformingFileReader.forSourceType(fileSourceType)
                    .from(testFileFolder + "/*")
                    .withRecordsTag(recordsTag))
            .get(recordsTag);

    PAssert.that(records).satisfies(new MatchRecordsCountFn<>(expectedRecordsCount));

    readPipeline.run().waitUntilFinish();
  }

  private FileIO.Sink<GenericRecord> fileTypeSpecificSink(Schema schema) {
    switch (fileSourceType) {
      case AVRO:
        return AvroIO.sink(schema);

      case PARQUET:
        return ParquetIO.sink(schema);
    }

    throw new UnsupportedOperationException("not supported for type: " + fileSourceType.name());
  }
}
