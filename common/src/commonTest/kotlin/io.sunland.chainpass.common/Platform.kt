package io.sunland.chainpass.common

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class DesktopIgnore()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class AndroidIgnore()