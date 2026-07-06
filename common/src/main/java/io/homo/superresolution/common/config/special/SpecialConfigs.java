/*
 * Super Resolution
 * Copyright (c) 2025-2026. 187J3X1-114514
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.homo.superresolution.common.config.special;

import io.homo.superresolution.api.config.ModConfigSpecBuilder;
import io.homo.superresolution.common.upscale.AlgorithmDescriptions;
import io.homo.superresolution.core.impl.Pair;

import java.util.HashMap;
import java.util.Map;

public class SpecialConfigs {
    public DLSSSpecialConfig DLSS;

    public transient Map<String, Pair<SpecialConfig, String>> description = new HashMap<>();

    public SpecialConfigs(ModConfigSpecBuilder builder) {
        builder.comment("special", "Algorithm special configuration");
        DLSS = new DLSSSpecialConfig(builder);
        description.put("dlss", Pair.of(DLSS, AlgorithmDescriptions.DLSS.getDisplayName()));
    }
}
