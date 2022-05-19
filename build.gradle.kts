plugins {
    id("scala")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repository.apache.org/content/groups/snapshots")
}

object Versions {
    const val spark = "3.4.0-SNAPSHOT"
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.1.1")

    // Spark
    implementation("org.apache.spark:spark-sql_2.13:${Versions.spark}")

    // H2 DB
    implementation("com.h2database:h2:2.1.210")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
}

tasks.withType<ScalaCompile>().configureEach {
    options.compilerArgs.add("--add-opens=java.base/java.util=ALL-UNNAMED")
    options.compilerArgs.add("--add-opens=java.base/java.lang=ALL-UNNAMED")
    options.compilerArgs.add("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
    options.compilerArgs.add("--add-opens=java.base/java.nio=ALL-UNNAMED")
    options.compilerArgs.add("--add-opens=java.base/sun.nio.ch=ALL-UNNAMED")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
    jvmArgs("--add-opens=java.base/java.nio=ALL-UNNAMED")
}

tasks.withType<Exec>().configureEach {
    args("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    args("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
    args("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
    args("--add-opens", "java.base/java.nio=ALL-UNNAMED")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
