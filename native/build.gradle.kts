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
