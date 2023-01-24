import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
  java
  checkstyle
  application
  id("org.graalvm.buildtools.native") version "0.9.13"
  id("org.cadixdev.licenser")
  id("com.github.johnrengelman.shadow")
}

val nettyVersion: String by project
val slf4jVersion: String by project
val logbackVersion: String by project
val adventureVersion: String by project
val junitVersion: String by project

apply(from = "../gradle/checkstyle.gradle")

application {
    mainClass.set("com.velocitypowered.proxy.Velocity")
}

license {
  header.set(rootProject.resources.text.fromFile("HEADER.txt"))
}

tasks {

  jar {
    manifest {
      // val buildNumber = System.getenv("BUILD_NUMBER") ?: "unknown"
      // val version: String
      // if (project.version.toString().endsWith("-SNAPSHOT")) {
      //   version = "${project.version} (git-${project.ext.get("CurrentShortRevision")}-b${buildNumber})"
      // } else {
      //   version = "${project.version}"
      // }

      attributes(
        "Main-Class" to "com.velocitypowered.proxy.Velocity",
        "Implementation-Title" to "Velocity",
        "Implementation-Version" to project.version.toString(),
        "Implementation-Vendor" to "Velocity Contributors",
        "Multi-Release" to "true"
      )
    }
  }

  shadowJar {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
  }

  withType<Checkstyle> {
    exclude("**/com/velocitypowered/proxy/protocol/packet/*.java")
  }
}

dependencies {
    // Note: we depend on the API twice, first the main sourceset, and then the annotation processor.
    implementation(project(":velocity-api"))
    implementation(project.project(":velocity-api").sourceSets["ap"].output)
    implementation(project(":velocity-native"))

    implementation("io.netty:netty-codec:${nettyVersion}")
    implementation("io.netty:netty-codec-haproxy:${nettyVersion}")
    implementation("io.netty:netty-codec-http:${nettyVersion}")
    implementation("io.netty:netty-handler:${nettyVersion}")
    implementation("io.netty:netty-transport-native-epoll:${nettyVersion}")
    implementation("io.netty:netty-transport-native-epoll:${nettyVersion}:linux-x86_64")
    implementation("io.netty:netty-transport-native-epoll:${nettyVersion}:linux-aarch_64")

    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:jul-to-slf4j:${slf4jVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")

    implementation ("net.sf.jopt-simple:jopt-simple:5.0.4")
  // command-line options
    implementation ("gay.ampflower:terminalconsoleappender:2.0.0-SNAPSHOT")
    runtimeOnly ("org.jline:jline-terminal-jansi:3.21.0")
  // Needed for JLine
    runtimeOnly ("com.lmax:disruptor:3.4.4")
  // Async loggers

    implementation("it.unimi.dsi:fastutil-core:8.5.4")

    implementation(platform("net.kyori:adventure-bom:${adventureVersion}"))
    implementation("net.kyori:adventure-nbt")
    implementation("net.kyori:adventure-platform-facet:4.0.0")

    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("com.spotify:completable-futures:0.3.5")

    implementation("com.electronwill.night-config:toml:3.6.4")

    implementation("org.bstats:bstats-base:2.2.1")
    implementation("org.lanternpowered:lmbda:2.0.0")

    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")

    implementation("space.vectrix.flare:flare:2.0.0")
    implementation("space.vectrix.flare:flare-fastutil:2.0.0")

    compileOnly("com.github.spotbugs:spotbugs-annotations:4.4.0")

    testImplementation ("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation ("org.mockito:mockito-core:3.+")
}

tasks {
  test {
    useJUnitPlatform()
  }

  shadowJar {
    // Exclude all the collection types we don't intend to use
    exclude("it/unimi/dsi/fastutil/booleans/**")
    exclude("it/unimi/dsi/fastutil/bytes/**")
    exclude("it/unimi/dsi/fastutil/chars/**")
    exclude("it/unimi/dsi/fastutil/doubles/**")
    exclude("it/unimi/dsi/fastutil/floats/**")
    exclude("it/unimi/dsi/fastutil/longs/**")
    exclude("it/unimi/dsi/fastutil/shorts/**")

    // Exclude the fastutil IO utilities - we don't use them.
    exclude("it/unimi/dsi/fastutil/io/**")

    // Exclude most of the int types - Object2IntMap have a values() method that returns an
    // IntCollection, and we need Int2ObjectMap
    exclude("it/unimi/dsi/fastutil/ints/*Int2Boolean*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Byte*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Char*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Double*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Float*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Int*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Long*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Short*")
    exclude("it/unimi/dsi/fastutil/ints/*Int2Reference*")
    exclude("it/unimi/dsi/fastutil/ints/IntAVL*")
    exclude("it/unimi/dsi/fastutil/ints/IntArray*")
    exclude("it/unimi/dsi/fastutil/ints/*IntBi*")
    exclude("it/unimi/dsi/fastutil/ints/Int*Pair")
    exclude("it/unimi/dsi/fastutil/ints/IntLinked*")
    exclude("it/unimi/dsi/fastutil/ints/IntList*")
    exclude("it/unimi/dsi/fastutil/ints/IntHeap*")
    exclude("it/unimi/dsi/fastutil/ints/IntOpen*")
    exclude("it/unimi/dsi/fastutil/ints/IntRB*")
    exclude("it/unimi/dsi/fastutil/ints/IntSorted*")
    exclude("it/unimi/dsi/fastutil/ints/*Priority*")
    exclude("it/unimi/dsi/fastutil/ints/*BigList*")

    // Try to exclude everything BUT Object2Int{LinkedOpen,Open,CustomOpen}HashMap
    exclude("it/unimi/dsi/fastutil/objects/*ObjectArray*")
    exclude("it/unimi/dsi/fastutil/objects/*ObjectAVL*")
    exclude("it/unimi/dsi/fastutil/objects/*Object*Big*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Boolean*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Byte*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Char*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Double*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Float*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2IntArray*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2IntAVL*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2IntRB*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Long*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Object*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Reference*")
    exclude("it/unimi/dsi/fastutil/objects/*Object2Short*")
    exclude("it/unimi/dsi/fastutil/objects/*ObjectRB*")
    exclude("it/unimi/dsi/fastutil/objects/*Reference*")

    // Exclude Checker Framework annotations
    exclude("org/checkerframework/checker/**")

    relocate("org.bstats", "com.velocitypowered.proxy.bstats")
  }
}

artifacts {
    archives(tasks.shadowJar)
}

graalvmNative {
  binaries {
    named("main") {
      // TODO: https://stackoverflow.com/a/63835469
      buildArgs.add("--initialize-at-build-time=ch.qos.logback,net.minecrell.terminalconsole,org.slf4j,org.jline,org.fusesource.jansi,java.net.InetAddress,java.net.Inet4Address,java.net.Inet6Address")
      buildArgs.add("--initialize-at-run-time=io.netty.channel.epoll.Epoll,io.netty.channel.epoll.Native,io.netty.channel.epoll.EpollEventLoop,io.netty.channel.epoll.EpollEventArray,io.netty.channel.DefaultFileRegion,io.netty.channel.kqueue.KQueueEventArray,io.netty.channel.kqueue.KQueueEventLoop,io.netty.channel.kqueue.Native,io.netty.channel.unix.Errors,io.netty.channel.unix.IovArray,io.netty.channel.unix.Limits,io.netty.util.internal.logging.Log4JLogger,io.netty.channel.kqueue.KQueue")
      buildArgs.add("--trace-class-initialization=java.net.Inet4Address,java.net.Inet6Address")
      buildArgs.add("--trace-object-instantiation=java.net.Inet4Address,java.net.Inet6Address")
      //buildArgs.add("--initialize-at-run-time=ch.qos.logback.core.recovery.ResilientFileOutputStream,ch.qos.logback.core.FileAppender")
      buildArgs.add("-H:+ReportExceptionStackTraces")
    }
  }
}
