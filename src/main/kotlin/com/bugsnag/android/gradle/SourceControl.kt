package com.bugsnag.android.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class SourceControl @Inject constructor(objects: ObjectFactory) {
    val provider: Property<String> = objects.property(String::class.java)
    val repository: Property<String> = objects.property(String::class.java)
    val revision: Property<String> = objects.property(String::class.java)
}
