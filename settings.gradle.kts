import groovy.json.JsonSlurper

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.shedaniel.me/")
        maven(url = "https://libraries.minecraft.net")
        maven(url = "https://maven.parchmentmc.org/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "superresolution"

val requestedTasks = gradle.startParameter.taskNames.orEmpty()
val nativeOnlyMode = requestedTasks.isNotEmpty() && requestedTasks.all { taskName ->
    val normalized = taskName.lowercase()
    normalized.startsWith(":native:") || normalized == ":native" || normalized == "native" || normalized.startsWith("native:")
}

include("native")
if (!nativeOnlyMode) {
    include("common")
    include("fabric")
}

val minecraftVersionConfig = providers.gradleProperty("minecraft_version_config").orNull
    ?: throw GradleException("缺少属性 minecraft_version_config")

val versionConfigSrc = JsonSlurper().parse(
    File("$rootDir/configs/$minecraftVersionConfig.json")
) as Map<*, *>

gradle.extensions.extraProperties["versionConfigSrc"] = versionConfigSrc

val commonConfig = versionConfigSrc["common"] as? Map<*, *>
    ?: throw GradleException("版本配置缺少 common 节点")

val minecraftVersion = commonConfig["minecraft_version"]?.toString()
    ?: throw GradleException("版本配置缺少 common.minecraft_version")

gradle.extensions.extraProperties["minecraft_version"] = minecraftVersion

gradle.extensions.extraProperties["isVulkan"] = providers.gradleProperty("is_vulkan").orNull?.toBoolean() ?: false
gradle.extensions.extraProperties["isDev"] = providers.gradleProperty("is_dev").orNull?.toBoolean() ?: false
gradle.extensions.extraProperties["isEnableAutoDownload"] = providers.gradleProperty("enable_auto_download").orNull?.toBoolean() ?: false
gradle.extensions.extraProperties["isUseDebugLib"] = providers.gradleProperty("use_debug_lib").orNull?.toBoolean() ?: false

if (!nativeOnlyMode) {
    val isVulkan = gradle.extensions.extraProperties["isVulkan"] as Boolean
    println("❇️ 图形API " + if (isVulkan) "Vulkan" else "OpenGL")
    println("❇️ 当前Minecraft版本 $minecraftVersion")
}
