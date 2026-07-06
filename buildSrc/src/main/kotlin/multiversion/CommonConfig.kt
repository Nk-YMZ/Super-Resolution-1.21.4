package multiversion

class CommonConfig(config: Map<*, *>) {
    val javaVersion: Int = (config["java_version"] as? Number)?.toInt() ?: 0
    val minecraftVersion: String = config["minecraft_version"]?.toString().orEmpty()
    val parchmentVersion: String? = config["parchment_version"]?.toString()
    val neoFormVersion: String? = config["neoform_version"]?.toString()

    val platforms: List<String> = (config["platforms"] as? List<*>)
        ?.mapNotNull { it?.toString() }
        ?: emptyList()

    val lwjglVersion: String = config["lwjgl_version"]?.toString().orEmpty()
    val architecturyApiVersion: String? = config["architectury_api_version"]?.toString()
    val clothConfigVersion: String? = config["cloth_config_version"]?.toString()
    val modArtifactMinecraftVer: String = config["mod_artifact_minecraft_ver"]?.toString().orEmpty()

    var fabricVersionRange: List<String> = emptyList()

    val enableFabric: Boolean = platforms.contains("fabric")

    init {
        val fabric = config["fabric"] as? Map<*, *>
        if (fabric != null && enableFabric) {
            fabricVersionRange = (fabric["minecraft_version_range"] as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?: emptyList()
        }
    }
}
