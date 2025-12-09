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

    // jacoco 플러그인이 적용된 서브프로젝트만 필터링
    val jacocoSubprojects = subprojects.filter { it.plugins.hasPlugin("jacoco") }

    // 모든 서브프로젝트의 test 태스크에 의존
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

    // fileTree는 Execution Phase에서 평가되며, 존재하지 않는 디렉토리는 무시됨
    jacocoSubprojects.forEach { subproject ->
        executionData.from(
            fileTree(subproject.layout.buildDirectory) {
                include("jacoco/test.exec")
            }
        )
    }

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated/html"))
    }
}
