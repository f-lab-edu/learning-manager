plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation("org.springframework:spring-core")
    implementation("org.slf4j:slf4j-api")
}