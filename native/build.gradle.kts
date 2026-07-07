plugins {
    java
}

group = "io.homo.SuperResolution"
version = "0.0.1-alpha.1"

repositories {
    mavenCentral()
}

tasks.named("clean") {
    doFirst {
        delete("$projectDir/cpp/build")
        delete("$projectDir/cpp/output")
    }
}

tasks.register<Copy>("copyNativeLib") {
    from("$projectDir/cpp/output/lib") {
        include("libSuperResolution+*+*.dll")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    from("$projectDir/cpp/output/bin") {
        include("libSuperResolution+*+*.dll")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")
}

tasks.register<Copy>("copyNativeLibAll") {
    from("$projectDir/cpp/output/lib") {
        include("libSuperResolution*+*+*.dll")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    from("$projectDir/cpp/output/bin") {
        include("libSuperResolution*+*+*.dll")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    // NVIDIA NGX DLSS 运行时 redistributable（来自 DLSS submodule lib/Windows_x86_64/rel）
    // 由 DLSS.java 设置的 NGX_FEATURE_DLL_PATH 在 NATIVE_LIBRARIES_DIR 中按文件名查找
    from("$projectDir/cpp/SRNativeDLSS/third_party/DLSS/lib/Windows_x86_64/rel") {
        include("nvngx_dlss.dll")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")
}

tasks.register<Exec>("buildNativeCppWindows") {
    group = "build"
    description = "Build native C/C++ .dll Libraries for Windows"
    workingDir = file("$projectDir/cpp")
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        commandLine("powershell", "-ExecutionPolicy", "Bypass", "-File", "build_windows.ps1")
    } else {
        commandLine("bash", "build_windows_docker.sh")
    }
}

tasks.register("buildNativeCpp") {
    group = "build"
    description = "Build native C/C++ libraries for Windows"
    dependsOn("buildNativeCppWindows")
}

tasks.named("copyNativeLibAll") {
    mustRunAfter("buildNativeCpp")
    outputs.upToDateWhen { false }
}

tasks.register("buildNative") {
    dependsOn("buildNativeCpp")
    dependsOn("copyNativeLibAll")
}
