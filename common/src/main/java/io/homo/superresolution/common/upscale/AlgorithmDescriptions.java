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

package io.homo.superresolution.common.upscale;

import io.homo.superresolution.api.QualityPreset;
import io.homo.superresolution.api.SuperResolutionAPI;
import io.homo.superresolution.api.event.AlgorithmRegisterEvent;
import io.homo.superresolution.api.platform.OperatingSystem;
import io.homo.superresolution.api.platform.OperatingSystemType;
import io.homo.superresolution.api.platform.SystemArchitecture;
import io.homo.superresolution.api.registry.AlgorithmDescription;
import io.homo.superresolution.api.registry.AlgorithmRegistry;
import io.homo.superresolution.api.utils.Requirement;
import io.homo.superresolution.common.upscale.dlss.DLSS;
import io.homo.superresolution.common.upscale.none.None;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AlgorithmDescriptions {
    private static final List<QualityPreset> DLSS_QUALITY_PRESETS = List.of(
            new QualityPreset()
                    .setName(Component.translatable("superresolution.algo.preset.dlss.ultra_performance"))
                    .setCodeName("dlss_ultra_performance")
                    .setUpscaleRatio(3.0f),
            new QualityPreset()
                    .setName(Component.translatable("superresolution.algo.preset.dlss.performance"))
                    .setCodeName("dlss_performance")
                    .setUpscaleRatio(2.0f),
            new QualityPreset()
                    .setName(Component.translatable("superresolution.algo.preset.dlss.balanced"))
                    .setCodeName("dlss_balanced")
                    .setUpscaleRatio(1.724f),
            new QualityPreset()
                    .setName(Component.translatable("superresolution.algo.preset.dlss.quality"))
                    .setCodeName("dlss_quality")
                    .setUpscaleRatio(1.5f),
            new QualityPreset()
                    .setName(Component.translatable("superresolution.algo.preset.dlss.dlaa"))
                    .setCodeName("dlss_dlaa")
                    .setUpscaleRatio(1.0f)
    );

    public static final AlgorithmDescription<None> NONE = AlgorithmDescription.builder(None.class)
            .briefName("None")
            .codeName("none")
            .displayName("None")
            .requirement(Requirement.nothing())
            .build();

    public static final AlgorithmDescription<DLSS> DLSS = AlgorithmDescription.builder(DLSS.class)
            .briefName("NVIDIA DLSS")
            .codeName("dlss")
            .displayName("NVIDIA DLSS")
            .requirement(
                    Requirement.nothing()
                            .addSupportedOS(new OperatingSystem(SystemArchitecture.X86_64, OperatingSystemType.LINUX))
                            .requiredGlExtension("GL_EXT_memory_object")
                            .requiredGlExtension("GL_EXT_semaphore")
                            .glMajorVersion(4)
                            .glMinorVersion(6)
                            .requireVulkan(true)
            )
            .supportJitter(true)
            .qualityPresets(DLSS_QUALITY_PRESETS)
            .customUpscaleRatio(false)
            .build();

    public static void registryAlgorithms() {
        AlgorithmRegistry.registry(NONE);
        AlgorithmRegistry.registry(DLSS);
        SuperResolutionAPI.EVENT_BUS.post(new AlgorithmRegisterEvent());
    }
}
