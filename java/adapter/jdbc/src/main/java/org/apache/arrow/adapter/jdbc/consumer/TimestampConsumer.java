/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.arrow.adapter.jdbc.consumer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.arrow.vector.TimeStampMilliTZVector;

/**
 * Consumer which consume timestamp type values from {@link ResultSet}.
 * Write the data to {@link TimeStampMilliTZVector}.
 */
public abstract class TimestampConsumer implements JdbcConsumer<TimeStampMilliTZVector> {

  /**
   * Creates a consumer for {@link TimeStampMilliTZVector}.
   */
  public static TimestampConsumer createConsumer(
          TimeStampMilliTZVector vector, int index, boolean nullable, Calendar calendar) {
    if (nullable) {
      return new NullableTimestampConsumer(vector, index, calendar);
    } else {
      return new NonNullableTimestampConsumer(vector, index, calendar);
    }
  }

  protected TimeStampMilliTZVector vector;
  protected final int columnIndexInResultSet;
  protected final Calendar calendar;

  protected int currentIndex;

  /**
   * Instantiate a TimestampConsumer.
   */
  public TimestampConsumer(TimeStampMilliTZVector vector, int index) {
    this(vector, index, null);
  }

  /**
   * Instantiate a TimestampConsumer.
   */
  public TimestampConsumer(TimeStampMilliTZVector vector, int index, Calendar calendar) {
    this.vector = vector;
    this.columnIndexInResultSet = index;
    this.calendar = calendar;
  }

  @Override
  public void close() throws Exception {
    this.vector.close();
  }

  @Override
  public void resetValueVector(TimeStampMilliTZVector vector) {
    this.vector = vector;
    this.currentIndex = 0;
  }

  /**
   * Nullable consumer for timestamp.
   */
  static class NullableTimestampConsumer extends TimestampConsumer {

    /**
     * Instantiate a TimestampConsumer.
     */
    public NullableTimestampConsumer(TimeStampMilliTZVector vector, int index) {
      super(vector, index);
    }

    /**
     * Instantiate a TimestampConsumer.
     */
    public NullableTimestampConsumer(TimeStampMilliTZVector vector, int index, Calendar calendar) {
      super(vector, index, calendar);
    }

    @Override
    public void consume(ResultSet resultSet) throws SQLException {
      Timestamp timestamp = calendar == null ? resultSet.getTimestamp(columnIndexInResultSet) :
          resultSet.getTimestamp(columnIndexInResultSet, calendar);
      if (!resultSet.wasNull()) {
        vector.setSafe(currentIndex, timestamp.getTime());
      }
      currentIndex++;
    }
  }

  /**
   * Non-nullable consumer for timestamp.
   */
  static class NonNullableTimestampConsumer extends TimestampConsumer {

    /**
     * Instantiate a TimestampConsumer.
     */
    public NonNullableTimestampConsumer(TimeStampMilliTZVector vector, int index) {
      super(vector, index);
    }

    /**
     * Instantiate a TimestampConsumer.
     */
    public NonNullableTimestampConsumer(TimeStampMilliTZVector vector, int index, Calendar calendar) {
      super(vector, index, calendar);
    }

    @Override
    public void consume(ResultSet resultSet) throws SQLException {
      Timestamp timestamp = calendar == null ? resultSet.getTimestamp(columnIndexInResultSet) :
          resultSet.getTimestamp(columnIndexInResultSet, calendar);
      vector.setSafe(currentIndex, timestamp.getTime());
      currentIndex++;
    }
  }
}
