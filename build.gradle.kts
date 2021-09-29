plugins {
    kotlin("jvm") version "1.5.31"
    java

    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version ("1.1.0")
}

apply(from = "${rootDir}/lib-publish.gradle")
apply(from = "${rootDir}/scripts/publish-root.gradle")
apply(from = "${rootDir}/scripts/publish-module.gradle")

group = "org.cuongnv.bytearraypool"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}