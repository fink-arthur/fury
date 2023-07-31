/*
 * Copyright 2023 The Fury Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fury.serializer;

public enum CompatibleMode {
  /** Class schema must be consistent between serialization peer and deserialization peer. */
  SCHEMA_CONSISTENT,
  /**
   * Class schema can be different between serialization peer and deserialization peer. They can
   * add/delete fields independently.
   */
  COMPATIBLE
}
