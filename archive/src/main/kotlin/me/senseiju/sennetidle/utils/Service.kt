package me.senseiju.sennetidle.utils

abstract class Service {
    open fun onDisable() {}
}

class ServiceProvider {
    @PublishedApi
    internal val services = linkedMapOf<Class<out Service>, Service>()

    fun add(service: Service) {
        services[service.javaClass] = service
    }

    inline fun <reified T : Service> get(): T {
        return services[T::class.java] as? T ?: throw IllegalStateException("${T::class.simpleName} has not been registered")
    }

    fun disableAll() {
        services.values.reversed().forEach { it.onDisable() }
        services.clear()
    }
}
