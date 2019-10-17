/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.ort.model.licenses

import com.here.ort.model.yamlMapper

data class LicenseConfiguration(
    val licenseSets: List<LicenseSet> = emptyList(),
    val licenses: List<License> = emptyList()
) {
    init {
        require(licenseSets.groupBy{ it.id }.values.all { it.size == 1 } )
        require(licenses.groupBy{ it.id }.values.all { it.size == 1 } )

        // TODO: check that license class has entry in classes
    }

    private val licensesBySetId by lazy {
        val result = mutableMapOf<String, MutableSet<License>>()

        licenseSets.forEach { set ->
            result.put(set.id, mutableSetOf())
        }

        licenses.forEach { license ->
            license.sets.forEach { setId ->
                result.getOrPut(setId) { mutableSetOf() }.add(license)
            }
        }

        result
    }

    fun getLicensesForSet(setId: String): Set<License> {
        require(licensesBySetId.containsKey(setId)) { "Unknown license set ID: $setId." }

        return licensesBySetId[setId]!!
    }

    fun writeAsYaml(): String = yamlMapper.writeValueAsString(this)
}

fun LicenseConfiguration?.orEmpty(): LicenseConfiguration = if (this != null) this else LicenseConfiguration()
