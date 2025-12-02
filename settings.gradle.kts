pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "learning-manager"

include(
    ":app:api"
)

include(
    ":core:domain",
    ":core:provides",
    ":core:requires",
    ":core:service"
)

include(
    ":adapter:persistence",
    ":adapter:mongo",
    ":adapter:infra"
)

include(
    ":support:logging",
    ":support:monitoring"
)
