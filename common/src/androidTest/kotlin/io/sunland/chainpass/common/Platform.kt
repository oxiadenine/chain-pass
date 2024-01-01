package io.sunland.chainpass.common

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class DesktopIgnore

actual typealias AndroidIgnore = org.junit.Ignore