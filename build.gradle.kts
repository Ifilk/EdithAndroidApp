// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

subprojects {
    repositories {
        mavenCentral()
        google()

//        maven { setUrl("http://maven.aliyun.com/nexus/content/repositories/google") }
//        maven { setUrl("http://maven.aliyun.com/nexus/content/repositories/jcenter")}
//        maven { setUrl("http://maven.aliyun.com/nexus/content/groups/public/") }
    }
    buildscript {
        dependencies {
            classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
        }
    }
}