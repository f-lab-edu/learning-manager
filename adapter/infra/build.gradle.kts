plugins {
    id("lm.java-library")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))

    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api")
}
