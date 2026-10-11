package gg.idlemob.idlemobcore.framework.database

object SqlResources {

    fun load(path: String): String {
        return find(path) ?: throw IllegalStateException("Missing SQL resource $path")
    }

    fun find(path: String): String? {
        return SqlResources::class.java.classLoader.getResourceAsStream(path)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
    }
}
