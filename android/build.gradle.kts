plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

allprojects {
    layout.buildDirectory.set(file("/tmp/emma-build/${project.name}"))
}
