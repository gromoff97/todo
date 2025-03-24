package utils.general

import java.util.Properties

object EnvironmentConfig {

    private val properties = loadPropsFile()

    val appScheme = properties.propOrThrow("APP_SCHEME")
    val appHost = properties.propOrThrow("APP_HOST")
    val appPort = properties.propOrThrow("APP_PORT").toInt()

    private fun loadPropsFile(propFileName: String = "env.conf"): Properties {
        val props = Properties()
        val input = this::class.java.classLoader.getResourceAsStream(propFileName)
        props.load(input)
        return props
    }

    private fun Properties.propOrThrow(propertyName: String) =
        getProperty(propertyName) ?: throw NoSuchElementException("Couldn't find $propertyName in $this")
}