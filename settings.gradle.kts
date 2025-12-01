pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "learning-manager"

// Core 모듈들
include(
    ":core:api",
    ":core:domain",
    ":core:provides",
    ":core:requires",
    ":core:service"
)

// Adapter 모듈들
include(
    ":adapter:mysql",
    ":adapter:mongo"
)
