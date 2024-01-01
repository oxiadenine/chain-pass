package io.github.oxiadenine.chainpass

actual typealias DesktopIgnore = org.junit.jupiter.api.Disabled

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class AndroidIgnore