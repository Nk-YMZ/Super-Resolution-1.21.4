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

package io.homo.superresolution.core.graphics.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.EXTMemoryObjectWin32;
import org.lwjgl.opengl.EXTSemaphoreWin32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.*;

import static io.homo.superresolution.core.graphics.vulkan.VulkanUtils.VK_CHECK;
import static org.lwjgl.vulkan.VK11.*;

public class VulkanInterop {
    public static final VulkanInteropExt IMPL = new WindowsVulkanInteropExtImpl();

    public interface VulkanInteropExt {
        void glImportSemaphoreHandleEXT(
                MemoryStack stack,
                int semaphore,
                long handle
        );

        long vkGetSemaphoreHandleKHR(
                MemoryStack stack,
                VkDevice device,
                Struct pGetHandleInfo
        );


        Struct createVkSemaphoreGetHandleInfoKHR(MemoryStack stack, long pVkSemaphore);

        Struct createVkExportSemaphoreCreateInfo(MemoryStack stack);

        Struct createVkExportMemoryAllocateInfo(MemoryStack stack);

        Struct createVkImportMemoryInfo(MemoryStack stack, long handle);

        long vkGetMemoryHandle(MemoryStack stack, VkDevice device, long memory);

        void glImportMemoryEXT(int memoryObject, long size, long handle);
    }

    private static class WindowsVulkanInteropExtImpl implements VulkanInteropExt {

        @Override
        public void glImportSemaphoreHandleEXT(MemoryStack stack, int semaphore, long handle) {
            EXTSemaphoreWin32.glImportSemaphoreWin32HandleEXT(
                    semaphore,
                    EXTSemaphoreWin32.GL_HANDLE_TYPE_OPAQUE_WIN32_EXT,
                    handle
            );
        }

        @Override
        public long vkGetSemaphoreHandleKHR(MemoryStack stack, VkDevice device, Struct pGetHandleInfo) {
            PointerBuffer pHandle = stack.callocPointer(1);
            VK_CHECK(
                    KHRExternalSemaphoreWin32.vkGetSemaphoreWin32HandleKHR(
                            device,
                            (VkSemaphoreGetWin32HandleInfoKHR) pGetHandleInfo,
                            pHandle
                    ),
                    "Failed to export Windows semaphore handle"
            );
            return pHandle.get(0);
        }

        @Override
        public Struct createVkSemaphoreGetHandleInfoKHR(MemoryStack stack, long pVkSemaphore) {
            VkSemaphoreGetWin32HandleInfoKHR semaphoreGetInfo = VkSemaphoreGetWin32HandleInfoKHR.calloc(stack);
            semaphoreGetInfo.sType(KHRExternalSemaphoreWin32.VK_STRUCTURE_TYPE_SEMAPHORE_GET_WIN32_HANDLE_INFO_KHR);
            semaphoreGetInfo.handleType(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_WIN32_BIT);
            semaphoreGetInfo.semaphore(pVkSemaphore);
            return semaphoreGetInfo;
        }

        @Override
        public Struct createVkExportSemaphoreCreateInfo(MemoryStack stack) {
            VkExportSemaphoreCreateInfo exportSemaphoreCreateInfo = VkExportSemaphoreCreateInfo.calloc(stack);
            exportSemaphoreCreateInfo.sType(VK_STRUCTURE_TYPE_EXPORT_SEMAPHORE_CREATE_INFO);
            exportSemaphoreCreateInfo.handleTypes(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_WIN32_BIT);
            return exportSemaphoreCreateInfo;
        }

        @Override
        public Struct createVkExportMemoryAllocateInfo(MemoryStack stack) {
            return VkExportMemoryAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO)
                    .handleTypes(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT);
        }

        @Override
        public Struct createVkImportMemoryInfo(MemoryStack stack, long handle) {
            return VkImportMemoryWin32HandleInfoKHR.calloc(stack)
                    .sType(KHRExternalMemoryWin32.VK_STRUCTURE_TYPE_IMPORT_MEMORY_WIN32_HANDLE_INFO_KHR)
                    .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT)
                    .handle(handle);
        }

        @Override
        public long vkGetMemoryHandle(MemoryStack stack, VkDevice device, long memory) {
            VkMemoryGetWin32HandleInfoKHR getInfo = VkMemoryGetWin32HandleInfoKHR.calloc(stack)
                    .sType(KHRExternalMemoryWin32.VK_STRUCTURE_TYPE_MEMORY_GET_WIN32_HANDLE_INFO_KHR)
                    .memory(memory)
                    .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT);

            PointerBuffer pHandle = stack.mallocPointer(1);
            VK_CHECK(KHRExternalMemoryWin32.vkGetMemoryWin32HandleKHR(device, getInfo, pHandle),
                    "Failed to export Windows memory handle");
            return pHandle.get(0);
        }

        @Override
        public void glImportMemoryEXT(int memoryObject, long size, long handle) {
            EXTMemoryObjectWin32.glImportMemoryWin32HandleEXT(
                    memoryObject,
                    size,
                    EXTMemoryObjectWin32.GL_HANDLE_TYPE_OPAQUE_WIN32_EXT,
                    handle
            );
        }
    }
}
