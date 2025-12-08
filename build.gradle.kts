plugins {
    java
    jacoco
}

allprojects {
    group = "me.chan99k"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated Jacoco coverage report for all subprojects"

    val jacocoSubprojects = subprojects.filter { subproject ->
        subproject.plugins.hasPlugin("jacoco") &&
                file("${subproject.layout.buildDirectory.get()}/jacoco/test.exec").exists()
    }

    dependsOn(jacocoSubprojects.map { it.tasks.named("test") })

    additionalSourceDirs.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    sourceDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    classDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].output }
    )
    executionData.setFrom(
        jacocoSubprojects.map { file("${it.layout.buildDirectory.get()}/jacoco/test.exec") }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated/html"))
    }
}
