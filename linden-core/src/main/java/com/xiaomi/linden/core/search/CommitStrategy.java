// Copyright 2016 Xiaomi, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.xiaomi.linden.core.search;

import com.google.common.base.Throwables;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommitStrategy extends Thread {
  private static Logger LOGGER = LoggerFactory.getLogger(CommitStrategy.class);

  private final IndexWriter indexWriter;
  private final DirectoryTaxonomyWriter taxoWriter;
  private long maxInterval = 60000;
  private int maxDocs = 50000;

  public CommitStrategy(IndexWriter indexWriter, DirectoryTaxonomyWriter taxoWriter) {
    this.indexWriter = indexWriter;
    this.taxoWriter = taxoWriter;
  }

  public CommitStrategy(IndexWriter indexWriter, DirectoryTaxonomyWriter taxoWriter,
      int maxInterval, int maxDocs) {
    this.indexWriter = indexWriter;
    this.taxoWriter = taxoWriter;
    this.maxInterval = maxInterval;
    this.maxDocs = maxDocs;
  }

  @Override
  public void run() {
    int lastDocsNum = indexWriter.numDocs();
    long lastTime = System.currentTimeMillis();
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(1000);
        if (lastDocsNum != indexWriter.numDocs() &&
            ((indexWriter.numDocs() - lastDocsNum > maxDocs) ||
                (System.currentTimeMillis() - lastTime > maxInterval))) {
          indexWriter.commit();
          if (taxoWriter != null) {
            taxoWriter.commit();
          }
          lastDocsNum = indexWriter.numDocs();
          lastTime = System.currentTimeMillis();
        }
      } catch (InterruptedException e) {
        break;
      } catch (IOException e) {
        LOGGER.error("{}", Throwables.getStackTraceAsString(e));
      }
    }
    try {
      indexWriter.commit();
      if (taxoWriter != null) {
        taxoWriter.commit();
      }
    } catch (IOException e) {
      LOGGER.error("{}", Throwables.getStackTraceAsString(e));
    }
    LOGGER.info("Commit strategy exit");
  }

  public void close() {
    interrupt();
    try {
      join();
    } catch (InterruptedException e) {
      // do nothing
    }
  }
}
