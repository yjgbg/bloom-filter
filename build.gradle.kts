plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://nexus3.hypers.cc/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("io.lettuce:lettuce-core:6.1.5.RELEASE")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("junit:junit-dep:4.11")
    compileOnly("org.projectlombok:lombok:+")
}

group = "com.hypers"
version = "1.0.0-SNAPSHOT"
description = "distributed-redis-bloom-filter"
java.sourceCompatibility = JavaVersion.VERSION_11

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
