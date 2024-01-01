package io.github.oxiadenine.chainpass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class DesktopIgnore()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class AndroidIgnore()