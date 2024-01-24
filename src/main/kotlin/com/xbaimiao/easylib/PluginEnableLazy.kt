package com.xbaimiao.easylib

class PluginEnableLazy<out T>(private val initializer: () -> T) : Lazy<T> {

    private var typedValue: T? = null

    /**
     * Gets the lazily initialized value of the current Lazy instance.
     * Once the value was initialized it must not change during the rest of lifetime of this Lazy instance.
     */
    override val value: T
        get() {
            val t = typedValue
            if (t != null) {
                return t
            }

            val typedValue = initializer()
            this.typedValue = typedValue
            return typedValue
        }

    /**
     * Returns `true` if a value for this Lazy instance has been already initialized, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this Lazy instance.
     */
    override fun isInitialized(): Boolean {
        return typedValue != null
    }

    fun init() {
        val typedValue = initializer()
        this.typedValue = typedValue
    }

}
