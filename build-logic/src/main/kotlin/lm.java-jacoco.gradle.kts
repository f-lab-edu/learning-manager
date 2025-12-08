plugins {
    jacoco
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

jacoco {
    toolVersion = catalog.findVersion("jacoco").get().toString()
}

tasks.withType<Test>().configureEach {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
