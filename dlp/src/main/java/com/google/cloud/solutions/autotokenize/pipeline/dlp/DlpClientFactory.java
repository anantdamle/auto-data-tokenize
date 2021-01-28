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

package com.google.cloud.solutions.autotokenize.pipeline.dlp;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.dlp.v2.stub.DlpServiceStub;
import java.io.IOException;
import java.io.Serializable;

/**
 * Client factory to be used in
 */
public interface DlpClientFactory extends Serializable {

  DlpServiceClient newClient() throws IOException;


  static DefaultClientFactory defaultFactory() {
    return new DefaultClientFactory();
  }

  static StubbingClientFactory withStub(DlpServiceStub stub) {
    return new StubbingClientFactory(stub);
  }

  final class DefaultClientFactory implements DlpClientFactory {

    @Override
    public DlpServiceClient newClient() throws IOException {
      return DlpServiceClient.create();
    }
  }

  final class StubbingClientFactory implements DlpClientFactory {

    private final DlpServiceStub stub;

    public StubbingClientFactory(DlpServiceStub stub) {
      this.stub = stub;
    }

    @Override
    public DlpServiceClient newClient() throws IOException {
      return DlpServiceClient.create(stub);
    }
  }

}