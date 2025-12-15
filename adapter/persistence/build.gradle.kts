plugins {
    id("lm.java-jpa")
    id("lm.java-jdbc")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))
}
