package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariant
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MappingFileProviderKtTest {

    @Mock
    lateinit var proj: Project

    @Mock
    lateinit var variant: ApkVariant

    @Mock
    lateinit var fileCollectionProvider: Provider<FileCollection>

    @Test
    fun findMappingFileAgp() {
        `when`(variant.mappingFileProvider).thenReturn(fileCollectionProvider)
        assertEquals(fileCollectionProvider, findMappingFileAgp(variant, proj))
    }
}
