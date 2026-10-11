package gg.idlemob.idlemobcore.framework.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess

@ConfigSerializable
class DatabaseConfig {
    var host = "localhost"
    var port = 3306
    var database = "idlemob"
    var username = "idlemob"
    var password = "change-me"
    var pool = PoolConfig()
    var properties: Map<String, String> = emptyMap()

    @PostProcess
    private fun validate() {
        require(host.isNotBlank()) { "host must be set" }
        require(port in 1..65535) { "port must be between 1 and 65535" }
        require(database.matches(Regex("[A-Za-z0-9_$]+"))) { "database may only contain letters, digits, underscores and dollar signs" }
        require(username.isNotBlank()) { "username must be set" }
    }
}

@ConfigSerializable
class PoolConfig {
    var maximumPoolSize = 10
    var minimumIdle = 10

    @PostProcess
    private fun validate() {
        require(maximumPoolSize >= 1) { "maximum-pool-size must be at least 1" }
        require(minimumIdle in 0..maximumPoolSize) { "minimum-idle must be between 0 and maximum-pool-size" }
    }
}
