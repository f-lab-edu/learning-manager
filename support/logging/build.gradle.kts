plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    api(catalog.findLibrary("logback-classic").get())
    api(catalog.findLibrary("slf4j-api").get())
}