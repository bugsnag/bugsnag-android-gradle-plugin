package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.BugsnagReleasesTask.Companion.isValidVcsProvider
import com.bugsnag.android.gradle.BugsnagReleasesTask.Companion.parseProviderUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BugsnagReleasesTest {

    @Test
    fun ensureProviderValidation() {
        assertTrue(isValidVcsProvider(null))
        val valid: Collection<String> = listOf(
            "github",
            "github-enterprise",
            "bitbucket",
            "bitbucket-server",
            "gitlab",
            "gitlab-onpremise"
        )
        valid.forEach { provider ->
            assertTrue(isValidVcsProvider(provider))
        }
    }

    @Test
    fun ensureInvalidProviderException() {
        assertFalse(isValidVcsProvider("foo"))
    }

    @Test
    fun testVcsProviderParse() {
        assertNull(parseProviderUrl(null))
        assertNull(parseProviderUrl(""))
        assertNull(parseProviderUrl("foo"))
        assertEquals("github", parseProviderUrl("http://github.com/a/a.git"))
        assertEquals("github-enterprise", parseProviderUrl("http://github-enterprise.com/a/a.git"))
        assertEquals("bitbucket", parseProviderUrl("http://bitbucket.com/a/a.git"))
        assertEquals("bitbucket-server", parseProviderUrl("http://bitbucket-server.com/a/a.git"))
        assertEquals("gitlab", parseProviderUrl("http://gitlab.com/a/a.git"))
        assertEquals("gitlab-onpremise", parseProviderUrl("http://gitlab-onpremise.com/a/a.git"))
    }
}
