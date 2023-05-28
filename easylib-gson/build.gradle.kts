plugins {
    id("java")
}

group = "com.xbaimiao"
version = "2.0.4"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}