package io.sunland.chainpass.common

actual typealias DesktopIgnore = org.junit.Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class AndroidIgnore