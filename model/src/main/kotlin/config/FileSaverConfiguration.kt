/*
 * Copyright (C) 2019 HERE Europe B.V.
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

package com.here.ort.model.config

import com.here.ort.utils.storage.FileSaver
import com.here.ort.utils.storage.FileStorage

/**
 * The configuration model for a [FileSaver].
 */
data class FileSaverConfiguration(
    /**
     * A list of glob patterns that define which files will be saved.
     */
    val patterns: List<String>,

    /**
     * Configuration of the [FileStorage] used for saving the files.
     */
    val storage: FileStorageConfiguration
) {
    /**
     * Create a [FileSaver] based on this configuration.
     */
    fun createFileSaver() = FileSaver(patterns, storage.createFileStorage())
}
