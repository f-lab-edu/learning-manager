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

    // Configuration Phase: jacoco 플러그인이 있는 모든 서브프로젝트 선택
    // (파일 존재 여부는 체크하지 않음)
    val jacocoSubprojects = subprojects.filter { it.plugins.hasPlugin("jacoco") }

    // 모든 test 태스크에 의존
    dependsOn(jacocoSubprojects.mapNotNull { it.tasks.findByName("test") })

    // 소스/클래스는 Configuration Phase에서 설정 가능
    additionalSourceDirs.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    sourceDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    classDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].output }
    )

    // Execution Phase: 실제 존재하는 .exec 파일만 수집
    executionData.setFrom(
        jacocoSubprojects
            .map { file("${it.layout.buildDirectory.get()}/jacoco/test.exec") }
            .filter { it.exists() }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated/html"))
    }
}
