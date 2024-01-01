package io.sunland.chainpass.common

actual typealias DesktopIgnore = org.junit.jupiter.api.Disabled

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class AndroidIgnore