import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer.ExposedPort
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.image.DockerLoadImage
import com.bmuschko.gradle.docker.tasks.image.DockerRemoveImage
import org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import org.apache.tools.ant.taskdefs.condition.Os.isFamily

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.qameta.allure") version "2.11.2"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

group = "indi.gromov"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }
kotlin { jvmToolchain(17) }
tasks.wrapper { gradleVersion = "8.13" }
tasks.test {
    useJUnitPlatform()
}

allure {
    report {
        version.set("2.24.0")
        reportDir = file("build/allure-results")
    }
    adapter { aspectjWeaver.set(true) }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.junit.jupiter:junit-jupiter-params:5.12.1")

    // Http - client
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    // (de-)serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")

    // Assertions
    implementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")

    // Synchronous wait for asynchronous stuff
    implementation("org.awaitility:awaitility-kotlin:4.3.0")

    // Reporting
    implementation(platform("io.qameta.allure:allure-bom:2.29.1"))
    implementation("io.qameta.allure:allure-junit5")

    // load testing
    implementation("us.abstracta.jmeter:jmeter-java-dsl:1.29.1") {
        exclude("org.apache.jmeter", "bom")
    }
}

val defaultAppPort = "8080"
val defaultTarFilePath = "docker/todo-app.tar"
val defaultImageName = "todo-app"
val defaultContainerName = "todo-app-container"

val removeTodoImage by tasks.registering(DockerRemoveImage::class) {
    group = "docker"
    force.set(true)
    targetImageId(defaultImageName)
    onError {
        if (!this.message!!.contains("No such image")) throw this
    }
}

val removeTodoContainer by tasks.registering(DockerRemoveContainer::class) {
    group = "docker"
    force.set(true)
    targetContainerId(defaultContainerName)
    onError {
        if (!this.message!!.contains("No such container")) throw this
    }
}

val loadTodoImage by tasks.registering(DockerLoadImage::class) {
    group = "docker"
    imageFile.set(file(defaultTarFilePath))
}

val createTodoContainer by tasks.registering(DockerCreateContainer::class) {
    group = "docker"
    targetImageId("$defaultImageName:latest")
    containerName.set(defaultContainerName)
    exposedPorts.add(ExposedPort("tcp", listOf(4242)))
    hostConfig.portBindings.set(listOf("$defaultAppPort:4242"))
    envVars.set(mapOf("VERBOSE" to "1"))
}

val startTodoContainer by tasks.registering(DockerStartContainer::class) {
    group = "docker"
    targetContainerId(defaultContainerName)
}

val resetAndDeployApp by tasks.registering(Exec::class) {
    group = "docker"
    workingDir = file(".")
    executable = if (isFamily(FAMILY_WINDOWS)) { "gradlew.bat" } else { "./gradlew" }

    val tasks = listOf(removeTodoContainer, removeTodoImage, loadTodoImage, createTodoContainer, startTodoContainer)
    args(*tasks.map { it.name }.toTypedArray())
}