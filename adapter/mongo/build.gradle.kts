plugins {
    id("lm.java-mongodb")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))
}