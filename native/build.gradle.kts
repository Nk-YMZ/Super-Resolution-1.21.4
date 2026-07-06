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
        include("libSuperResolution+*+*.so")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    from("$projectDir/cpp/output/bin") {
        include("libSuperResolution+*+*.so")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")
}

tasks.register<Copy>("copyNativeLibAll") {
    from("$projectDir/cpp/output/lib") {
        include("libSuperResolution*+*+*.so")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    from("$projectDir/cpp/output/bin") {
        include("libSuperResolution*+*+*.so")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")

    // NVIDIA NGX DLSS 运行时 snippet（来自 DLSS submodule lib/Linux_x86_64/rel）
    // 由 DLSS.java 设置的 NGX_FEATURE_DLL_PATH 在 NATIVE_LIBRARIES_DIR 中按文件名查找
    from("$projectDir/cpp/SRNativeDLSS/third_party/DLSS/lib/Linux_x86_64/rel") {
        include("libnvidia-ngx-dlss.so.310.7.0")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into("$projectDir/../common/src/main/resources/lib/")
}

tasks.register<Exec>("buildNativeCppLinux") {
    group = "build"
    description = "Build native C/C++ .so Libraries for Linux"
    workingDir = file("$projectDir/cpp")
    commandLine("bash", "build_linux.sh")
}

tasks.register("buildNativeCpp") {
    group = "build"
    description = "Build native C/C++ libraries for Linux"
    dependsOn("buildNativeCppLinux")
}

tasks.named("copyNativeLibAll") {
    mustRunAfter("buildNativeCpp")
    outputs.upToDateWhen { false }
}

tasks.register("buildNative") {
    dependsOn("buildNativeCpp")
    dependsOn("copyNativeLibAll")
}
