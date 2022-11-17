/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.rpc;

import java.util.HashMap;
import java.util.Map;

/**
 * When update this class, remember to update the following files
 *
 * <p>docs/UserGuide/API/Status-Codes.md
 *
 * <p>docs/zh/UserGuide/API/Status-Codes.md
 */
public enum TSStatusCode {
  SUCCESS_STATUS(200),
  STILL_EXECUTING_STATUS(201),
  INCOMPATIBLE_VERSION(203),

  NODE_DELETE_ERROR(298),
  ALIAS_ALREADY_EXIST(299),
  PATH_ALREADY_EXIST(300),
  METADATA_ERROR(303),
  PATH_NOT_EXIST(304),
  OUT_OF_TTL(305),
  COMPACTION_ERROR(307),
  SYSTEM_CHECK_ERROR(308),
  SYNC_CONNECTION_ERROR(310),
  DATABASE_PROCESS_ERROR(311),
  STORAGE_ENGINE_ERROR(313),
  TSFILE_PROCESSOR_ERROR(314),
  PATH_ILLEGAL(315),
  LOAD_FILE_ERROR(316),
  DATABASE_NOT_READY(317),
  ILLEGAL_PARAMETER(318),
  ALIGNED_TIMESERIES_ERROR(319),
  DUPLICATED_TEMPLATE(320),
  UNDEFINED_TEMPLATE(321),
  DATABASE_NOT_EXIST(322),
  CONTINUOUS_QUERY_ERROR(323),
  TEMPLATE_NOT_SET(324),
  DIFFERENT_TEMPLATE(325),
  TEMPLATE_IS_IN_USE(326),
  TEMPLATE_INCOMPATIBLE(327),
  SEGMENT_NOT_FOUND(328),
  PAGE_OUT_OF_SPACE(329),
  RECORD_DUPLICATED(330),
  SEGMENT_OUT_OF_SPACE(331),
  SCHEMA_FILE_NOT_EXISTS(332),
  WRITE_AHEAD_LOG_ERROR(333),
  CREATE_PIPE_SINK_ERROR(334),
  PIPE_ERROR(335),
  PIPESERVER_ERROR(336),
  SERIES_OVERFLOW(337),
  TIMESERIES_ALREADY_EXIST(338),
  CREATE_TEMPLATE_ERROR(340),
  SYNC_FILE_REDIRECTION_ERROR(341),
  SYNC_FILE_ERROR(342),
  VERIFY_METADATA_ERROR(343),
  TIMESERIES_IN_BLACK_LIST(344),
  OVERSIZE_RECORD(349),
  SCHEMA_FILE_REDO_LOG_BROKEN(350),
  TRIGGER_FIRE_ERROR(355),
  TRIGGER_LOAD_CLASS_ERROR(360),
  TRIGGER_DOWNLOAD_ERROR(361),
  CREATE_TRIGGER_INSTANCE_ERROR(362),
  ACTIVE_TRIGGER_INSTANCE_ERROR(363),
  DROP_TRIGGER_INSTANCE_ERROR(364),
  UPDATE_TRIGGER_LOCATION_ERROR(365),
  TEMPLATE_NOT_ACTIVATED(366),

  UDF_LOAD_CLASS_ERROR(370),
  UDF_DOWNLOAD_ERROR(371),
  CREATE_FUNCTION_ON_DATANODE_ERROR(372),
  DROP_FUNCTION_ON_DATANODE_ERROR(373),

  EXECUTE_STATEMENT_ERROR(400),
  SQL_PARSE_ERROR(401),
  GENERATE_TIME_ZONE_ERROR(402),
  SET_TIME_ZONE_ERROR(403),
  QUERY_NOT_ALLOWED(405),
  LOGICAL_OPERATOR_ERROR(407),
  LOGICAL_OPTIMIZE_ERROR(408),
  UNSUPPORTED_FILL_TYPE(409),
  QUERY_PROCESS_ERROR(411),
  WRITE_PROCESS_ERROR(412),
  WRITE_PROCESS_REJECT(413),
  SEMANTIC_ERROR(416),
  LOAD_PIECE_OF_TSFILE_ERROR(417),

  UNSUPPORTED_INDEX_TYPE(422),

  MEMORY_NOT_ENOUGH(423),

  INTERNAL_SERVER_ERROR(500),
  CLOSE_OPERATION_ERROR(501),
  READ_ONLY_SYSTEM(502),
  DISK_SPACE_INSUFFICIENT(503),
  START_UP_ERROR(504),
  SHUT_DOWN_ERROR(505),
  MULTIPLE_ERROR(506),
  TSBLOCK_SERIALIZE_ERROR(508),

  WRONG_LOGIN_PASSWORD(600),
  NOT_LOGIN(601),
  NO_PERMISSION(602),
  UNINITIALIZED_AUTH_ERROR(603),
  EXECUTE_PERMISSION_ERROR(604),
  USER_NOT_EXIST(605),
  ROLE_NOT_EXIST(606),
  AUTHENTICATION_FAILED(607),
  CLEAR_PERMISSION_CACHE_ERROR(608),

  // cluster-related errors
  TIME_OUT(701),
  UNSUPPORTED_OPERATION(703),
  NO_CONNECTION(706),
  NEED_REDIRECTION(707),
  ALL_RETRY_ERROR(709),
  MIGRATE_REGION_ERROR(710),
  CREATE_REGION_ERROR(711),
  DELETE_REGION_ERROR(712),
  PARTITION_CACHE_UPDATE_ERROR(713),
  DESERIALIZE_PIECE_OF_TSFILE_ERROR(714),
  CONSENSUS_NOT_INITIALIZED(715),

  // configuration
  CONFIGURATION_ERROR(800),

  // ConfigNode response
  DATANODE_ALREADY_REGISTERED(901),
  DATABASE_ALREADY_EXISTS(903),
  NO_ENOUGH_DATANODE(904),
  ERROR_GLOBAL_CONFIG(905),
  ADD_CONFIGNODE_ERROR(906),
  REMOVE_CONFIGNODE_ERROR(907),
  DATANODE_NOT_EXIST(912),
  DATANODE_STOP_ERROR(917),
  REGION_LEADER_CHANGE_ERROR(918),
  REMOVE_DATANODE_ERROR(919),
  OVERLAP_WITH_EXISTING_TASK(920),
  NOT_AVAILABLE_REGION_GROUP(921),
  CREATE_TRIGGER_ERROR(922),
  DROP_TRIGGER_ERROR(923),
  REGISTER_REMOVED_DATANODE(925),

  NO_SUCH_CQ(930),
  CQ_ALREADY_ACTIVE(931),
  CQ_AlREADY_EXIST(932),
  CQ_UPDATE_LAST_EXEC_TIME_ERROR(933);

  private final int statusCode;

  private static final Map<Integer, TSStatusCode> CODE_MAP = new HashMap<>();

  static {
    for (TSStatusCode value : TSStatusCode.values()) {
      CODE_MAP.put(value.getStatusCode(), value);
    }
  }

  TSStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public static TSStatusCode representOf(int statusCode) {
    return CODE_MAP.get(statusCode);
  }

  @Override
  public String toString() {
    return String.format("%s(%d)", name(), getStatusCode());
  }
}
