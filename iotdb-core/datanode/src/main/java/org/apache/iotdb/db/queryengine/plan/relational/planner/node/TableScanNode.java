/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.queryengine.plan.relational.planner.node;

import org.apache.iotdb.common.rpc.thrift.TRegionReplicaSet;
import org.apache.iotdb.commons.schema.table.column.TsTableColumnCategory;
import org.apache.iotdb.db.queryengine.common.SessionInfo;
import org.apache.iotdb.db.queryengine.plan.planner.plan.node.PlanNode;
import org.apache.iotdb.db.queryengine.plan.planner.plan.node.PlanNodeId;
import org.apache.iotdb.db.queryengine.plan.planner.plan.node.PlanVisitor;
import org.apache.iotdb.db.queryengine.plan.planner.plan.node.source.SourceNode;
import org.apache.iotdb.db.queryengine.plan.relational.metadata.ColumnSchema;
import org.apache.iotdb.db.queryengine.plan.relational.metadata.Metadata;
import org.apache.iotdb.db.queryengine.plan.relational.metadata.QualifiedObjectName;
import org.apache.iotdb.db.queryengine.plan.relational.planner.Symbol;
import org.apache.iotdb.db.queryengine.plan.relational.sql.ast.Expression;

import com.google.common.collect.ImmutableList;
import org.apache.tsfile.utils.ReadWriteIOUtils;

import javax.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.iotdb.commons.schema.table.column.TsTableColumnCategory.FIELD;
import static org.apache.iotdb.commons.schema.table.column.TsTableColumnCategory.TIME;

public abstract class TableScanNode extends SourceNode {

  protected QualifiedObjectName qualifiedObjectName;
  // Indicate the column this node need to output
  protected List<Symbol> outputSymbols;
  // Indicate the column this node need to fetch from StorageEngine,
  // the number of fetched columns may be more than output columns when there are predicates push
  // down.
  protected Map<Symbol, ColumnSchema> assignments;

  // push down predicate for current series, could be null if it doesn't exist
  @Nullable protected Expression pushDownPredicate;

  // push down limit for result set. The default value is -1, which means no limit
  protected long pushDownLimit;

  // push down offset for result set. The default value is 0
  protected long pushDownOffset;

  // The id of DataRegion where the node will run
  // For query of schemaInfo, we only need the list of DataNodeLocation
  protected TRegionReplicaSet regionReplicaSet;

  public TableScanNode(
      PlanNodeId id,
      QualifiedObjectName qualifiedObjectName,
      List<Symbol> outputSymbols,
      Map<Symbol, ColumnSchema> assignments) {
    super(id);
    this.qualifiedObjectName = qualifiedObjectName;
    this.outputSymbols = outputSymbols;
    this.assignments = assignments;
  }

  public TableScanNode(
      PlanNodeId id,
      QualifiedObjectName qualifiedObjectName,
      List<Symbol> outputSymbols,
      Map<Symbol, ColumnSchema> assignments,
      Expression pushDownPredicate,
      long pushDownLimit,
      long pushDownOffset) {
    super(id);
    this.qualifiedObjectName = qualifiedObjectName;
    this.outputSymbols = outputSymbols;
    this.assignments = assignments;
    this.pushDownPredicate = pushDownPredicate;
    this.pushDownLimit = pushDownLimit;
    this.pushDownOffset = pushDownOffset;
  }

  protected TableScanNode() {}

  @Override
  public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
    return visitor.visitTableScan(this, context);
  }

  @Override
  public List<PlanNode> getChildren() {
    return ImmutableList.of();
  }

  @Override
  public void addChild(PlanNode child) {}

  @Override
  public int allowedChildCount() {
    return 0;
  }

  @Override
  public List<String> getOutputColumnNames() {
    return outputSymbols.stream().map(Symbol::getName).collect(Collectors.toList());
  }

  public List<Symbol> getIdColumnsInTableStore(Metadata metadata, SessionInfo session) {
    return Objects.requireNonNull(
            metadata.getTableSchema(session, qualifiedObjectName).orElse(null))
        .getColumns()
        .stream()
        .filter(columnSchema -> columnSchema.getColumnCategory() == TsTableColumnCategory.TAG)
        .map(columnSchema -> Symbol.of(columnSchema.getName()))
        .collect(Collectors.toList());
  }

  public boolean isMeasurementOrTimeColumn(Symbol symbol) {
    ColumnSchema columnSchema = assignments.get(symbol);
    return columnSchema != null
        && (columnSchema.getColumnCategory() == FIELD || columnSchema.getColumnCategory() == TIME);
  }

  public boolean isTimeColumn(Symbol symbol) {
    return isTimeColumn(symbol, assignments);
  }

  public Optional<Symbol> getTimeColumn() {
    return getTimeColumn(assignments);
  }

  public static Optional<Symbol> getTimeColumn(Map<Symbol, ColumnSchema> assignments) {
    for (Map.Entry<Symbol, ColumnSchema> entry : assignments.entrySet()) {
      if (entry.getValue().getColumnCategory() == TIME) {
        return Optional.of(entry.getKey());
      }
    }
    return Optional.empty();
  }

  public static boolean isTimeColumn(Symbol symbol, Map<Symbol, ColumnSchema> columnSchemaMap) {
    ColumnSchema columnSchema = columnSchemaMap.get(symbol);
    return columnSchema != null && columnSchema.getColumnCategory() == TIME;
  }

  @Override
  public List<Symbol> getOutputSymbols() {
    return outputSymbols;
  }

  @Override
  public void open() throws Exception {}

  @Override
  public void close() throws Exception {}

  public QualifiedObjectName getQualifiedObjectName() {
    return this.qualifiedObjectName;
  }

  public void setOutputSymbols(List<Symbol> outputSymbols) {
    this.outputSymbols = outputSymbols;
  }

  public void setAssignments(Map<Symbol, ColumnSchema> assignments) {
    this.assignments = assignments;
  }

  public Map<Symbol, ColumnSchema> getAssignments() {
    return this.assignments;
  }

  public long getPushDownLimit() {
    return this.pushDownLimit;
  }

  public void setPushDownLimit(long pushDownLimit) {
    this.pushDownLimit = pushDownLimit;
  }

  public long getPushDownOffset() {
    return this.pushDownOffset;
  }

  public void setPushDownOffset(long pushDownOffset) {
    this.pushDownOffset = pushDownOffset;
  }

  public Expression getPushDownPredicate() {
    return this.pushDownPredicate;
  }

  public void setPushDownPredicate(@Nullable Expression pushDownPredicate) {
    this.pushDownPredicate = pushDownPredicate;
  }

  public TRegionReplicaSet getRegionReplicaSet() {
    return this.regionReplicaSet;
  }

  public void setRegionReplicaSet(TRegionReplicaSet regionReplicaSet) {
    this.regionReplicaSet = regionReplicaSet;
  }

  protected static void serializeMemberVariables(
      TableScanNode node, ByteBuffer byteBuffer, boolean serializeOutputSymbols) {
    if (node.qualifiedObjectName.getDatabaseName() != null) {
      ReadWriteIOUtils.write(true, byteBuffer);
      ReadWriteIOUtils.write(node.qualifiedObjectName.getDatabaseName(), byteBuffer);
    } else {
      ReadWriteIOUtils.write(false, byteBuffer);
    }
    ReadWriteIOUtils.write(node.qualifiedObjectName.getObjectName(), byteBuffer);

    if (serializeOutputSymbols) {
      ReadWriteIOUtils.write(node.outputSymbols.size(), byteBuffer);
      node.outputSymbols.forEach(symbol -> ReadWriteIOUtils.write(symbol.getName(), byteBuffer));
    }

    ReadWriteIOUtils.write(node.assignments.size(), byteBuffer);
    for (Map.Entry<Symbol, ColumnSchema> entry : node.assignments.entrySet()) {
      Symbol.serialize(entry.getKey(), byteBuffer);
      ColumnSchema.serialize(entry.getValue(), byteBuffer);
    }

    if (node.pushDownPredicate != null) {
      ReadWriteIOUtils.write(true, byteBuffer);
      Expression.serialize(node.pushDownPredicate, byteBuffer);
    } else {
      ReadWriteIOUtils.write(false, byteBuffer);
    }

    ReadWriteIOUtils.write(node.pushDownLimit, byteBuffer);
    ReadWriteIOUtils.write(node.pushDownOffset, byteBuffer);
  }

  protected static void serializeMemberVariables(
      TableScanNode node, DataOutputStream stream, boolean serializeOutputSymbols)
      throws IOException {
    if (node.qualifiedObjectName.getDatabaseName() != null) {
      ReadWriteIOUtils.write(true, stream);
      ReadWriteIOUtils.write(node.qualifiedObjectName.getDatabaseName(), stream);
    } else {
      ReadWriteIOUtils.write(false, stream);
    }
    ReadWriteIOUtils.write(node.qualifiedObjectName.getObjectName(), stream);

    if (serializeOutputSymbols) {
      ReadWriteIOUtils.write(node.outputSymbols.size(), stream);
      for (Symbol symbol : node.outputSymbols) {
        ReadWriteIOUtils.write(symbol.getName(), stream);
      }
    }

    ReadWriteIOUtils.write(node.assignments.size(), stream);
    for (Map.Entry<Symbol, ColumnSchema> entry : node.assignments.entrySet()) {
      Symbol.serialize(entry.getKey(), stream);
      ColumnSchema.serialize(entry.getValue(), stream);
    }

    if (node.pushDownPredicate != null) {
      ReadWriteIOUtils.write(true, stream);
      Expression.serialize(node.pushDownPredicate, stream);
    } else {
      ReadWriteIOUtils.write(false, stream);
    }

    ReadWriteIOUtils.write(node.pushDownLimit, stream);
    ReadWriteIOUtils.write(node.pushDownOffset, stream);
  }

  protected static void deserializeMemberVariables(
      ByteBuffer byteBuffer, TableScanNode node, boolean deserializeOutputSymbols) {
    boolean hasDatabaseName = ReadWriteIOUtils.readBool(byteBuffer);
    String databaseName = null;
    if (hasDatabaseName) {
      databaseName = ReadWriteIOUtils.readString(byteBuffer);
    }
    String tableName = ReadWriteIOUtils.readString(byteBuffer);
    node.qualifiedObjectName = new QualifiedObjectName(databaseName, tableName);

    int size;

    if (deserializeOutputSymbols) {
      size = ReadWriteIOUtils.readInt(byteBuffer);
      List<Symbol> outputSymbols = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        outputSymbols.add(Symbol.deserialize(byteBuffer));
      }
      node.outputSymbols = outputSymbols;
    }

    size = ReadWriteIOUtils.readInt(byteBuffer);
    Map<Symbol, ColumnSchema> assignments = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      assignments.put(Symbol.deserialize(byteBuffer), ColumnSchema.deserialize(byteBuffer));
    }
    node.assignments = assignments;

    boolean hasPushDownPredicate = ReadWriteIOUtils.readBool(byteBuffer);
    if (hasPushDownPredicate) {
      node.pushDownPredicate = Expression.deserialize(byteBuffer);
    }

    node.pushDownLimit = ReadWriteIOUtils.readLong(byteBuffer);
    node.pushDownOffset = ReadWriteIOUtils.readLong(byteBuffer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TableScanNode that = (TableScanNode) o;
    return Objects.equals(qualifiedObjectName, that.qualifiedObjectName)
        && Objects.equals(outputSymbols, that.outputSymbols)
        && Objects.equals(regionReplicaSet, that.regionReplicaSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), qualifiedObjectName, outputSymbols, regionReplicaSet);
  }

  @Override
  public String toString() {
    return "TableScanNode-" + this.getPlanNodeId();
  }

  @Override
  public PlanNode replaceChildren(List<PlanNode> newChildren) {
    checkArgument(newChildren.isEmpty(), "newChildren is not empty");
    return this;
  }
}
