package com.bugsnag.android.gradle

import static com.bugsnag.android.gradle.BugsnagReleasesTask.isValidVcsProvider
import static com.bugsnag.android.gradle.BugsnagReleasesTask.parseProviderUrl
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

import groovy.transform.CompileStatic

import org.junit.Test

@CompileStatic
class BugsnagReleasesTest {

    @Test
    void ensureProviderValidation() {
        assertTrue(isValidVcsProvider(null))
        Collection<String> valid = Arrays.asList("github", "github-enterprise",
            "bitbucket", "bitbucket-server", "gitlab", "gitlab-onpremise")

        for (String provider : valid) {
            assertTrue(isValidVcsProvider(provider))
        }
    }

    @Test
    void ensureInvalidProviderException() throws Exception {
        assertFalse(isValidVcsProvider("foo"))
    }

    @Test
    void testVcsProviderParse() throws Exception {
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
