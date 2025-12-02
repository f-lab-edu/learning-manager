plugins {
    id("lm.java-infra")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))
}
