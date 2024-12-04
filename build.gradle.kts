// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        // Add the classpath for the Google Services plugin
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}