plugins {
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("uk.ac.ox.poseidon.gui.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("uk.ac.ox.oxfish:POSEIDON")
    implementation("com.esotericsoftware:minlog:1.3.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.metawidget.modules:metawidget-all:4.1")
    implementation("commons-beanutils:commons-beanutils:1.8.3")
    implementation("org.yaml:snakeyaml:1.29")
    implementation("si.uom:si-quantity:2.1")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<JavaCompile> {
    options.isDeprecation = true
    options.compilerArgs.add("-Xlint:unchecked")
}

tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}