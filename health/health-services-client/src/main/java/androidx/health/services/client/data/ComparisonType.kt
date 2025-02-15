/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.health.services.client.data

import androidx.health.services.client.proto.DataProto
import androidx.health.services.client.proto.DataProto.ComparisonType.COMPARISON_TYPE_GREATER_THAN
import androidx.health.services.client.proto.DataProto.ComparisonType.COMPARISON_TYPE_GREATER_THAN_OR_EQUAL
import androidx.health.services.client.proto.DataProto.ComparisonType.COMPARISON_TYPE_LESS_THAN
import androidx.health.services.client.proto.DataProto.ComparisonType.COMPARISON_TYPE_LESS_THAN_OR_EQUAL
import androidx.health.services.client.proto.DataProto.ComparisonType.COMPARISON_TYPE_UNKNOWN

/** For determining when a threshold has been met or exceeded in a [MetricCondition]. */
public enum class ComparisonType(public val id: Int) {
    // TODO(b/175064823): investigate adding EQUAL comparison type
    GREATER_THAN(1),
    GREATER_THAN_OR_EQUAL(2),
    LESS_THAN(3),
    LESS_THAN_OR_EQUAL(4);

    /** @hide */
    internal fun toProto(): DataProto.ComparisonType =
        when (this) {
            GREATER_THAN -> COMPARISON_TYPE_GREATER_THAN
            GREATER_THAN_OR_EQUAL -> COMPARISON_TYPE_GREATER_THAN_OR_EQUAL
            LESS_THAN -> COMPARISON_TYPE_LESS_THAN
            LESS_THAN_OR_EQUAL -> COMPARISON_TYPE_LESS_THAN_OR_EQUAL
        }

    public companion object {
        @JvmStatic
        public fun fromId(id: Int): ComparisonType? = values().firstOrNull { it.id == id }

        /** @hide */
        @JvmStatic
        internal fun fromProto(proto: DataProto.ComparisonType): ComparisonType? =
            when (proto) {
                COMPARISON_TYPE_GREATER_THAN -> GREATER_THAN
                COMPARISON_TYPE_GREATER_THAN_OR_EQUAL -> GREATER_THAN_OR_EQUAL
                COMPARISON_TYPE_LESS_THAN -> LESS_THAN
                COMPARISON_TYPE_LESS_THAN_OR_EQUAL -> LESS_THAN_OR_EQUAL
                COMPARISON_TYPE_UNKNOWN -> null
            }
    }
}
