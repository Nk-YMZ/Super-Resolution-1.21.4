package multiversion

import groovy.json.JsonSlurper
import java.io.File

class VersionConfig(json: Map<*, *>) {
    val common: CommonConfig = CommonConfig(json["common"] as? Map<*, *> ?: emptyMap<String, Any>())

    lateinit var fabric: FabricPlatformConfig

    init {
        if (json["fabric"] != null && common.enableFabric) {
            fabric = FabricPlatformConfig(json["fabric"] as? Map<*, *> ?: emptyMap<String, Any>())
        }
    }

    companion object {
        fun loadFromFile(file: File): VersionConfig {
            @Suppress("UNCHECKED_CAST")
            val json = JsonSlurper().parse(file) as Map<*, *>
            return VersionConfig(json)
        }
    }
}
