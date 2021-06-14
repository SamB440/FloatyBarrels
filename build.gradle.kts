plugins {
    id("com.github.johnrengelman.shadow") version("7.0.0")
    id("java")
}

var group = "com.convallyria"
val version = "1.0.0"

java.sourceCompatibility = JavaVersion.VERSION_16
java.targetCompatibility = JavaVersion.VERSION_16

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.gitlab.samb440:languagy:2.0.3-RELEASE") {
        exclude("commons-io")
        exclude("commons-codec")
    } // languagy
    implementation("org.bstats:bstats-bukkit:2.2.1") // plugin stats

    compileOnly("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:21.0.1")
    compileOnly("me.clip:placeholderapi:2.10.9")
}

tasks.shadowJar {
    dependencies {
        exclude("commons-io")
        exclude("commons-codec")
    }
    relocate("net.islandearth.languagy", "com.convallyria.floatybarrels.libs.languagy")
    relocate("org.bstats", "com.convallyria.floatybarrels.libs.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(16)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to version)
    }
}
