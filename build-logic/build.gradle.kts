plugins {
    `kotlin-dsl`
}

dependencies {
    val springBootVersion = libs.versions.spring.boot.get()
    val springDependencyManagementVersion = libs.versions.spring.dependency.management.get()

    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    implementation("io.spring.gradle:dependency-management-plugin:${springDependencyManagementVersion}")
}
