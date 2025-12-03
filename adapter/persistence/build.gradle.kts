plugins {
    id("lm.java-jpa")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))
}