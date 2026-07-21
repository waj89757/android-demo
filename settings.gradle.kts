pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ★ React Native 0.73 的 Android AAR 打包在 node_modules 里的本地 maven 仓库
        maven {
            url = uri("$rootDir/krn/hello-krn/node_modules/react-native/android")
        }
        // ★ JSC（JavaScript Core 引擎）
        maven {
            url = uri("$rootDir/krn/hello-krn/node_modules/jsc-android/dist")
        }
    }
}

rootProject.name = "demo01"
include(":app")
