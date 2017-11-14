package com.bugsnag.android.gradle

import org.junit.Assert
import org.junit.Test

class BugsnagReleasesTest {

    @Test
    void ensureProviderValidation() {
        Assert.assertTrue(BugsnagReleasesTask.isValidVcsProvider(null))
        Collection<String> valid = Arrays.asList("github", "github-enterprise", "bitbucket", "bitbucket-server", "gitlab", "gitlab-onpremise")

        for (String provider : valid) {
            Assert.assertTrue(BugsnagReleasesTask.isValidVcsProvider(provider))
        }
    }

    @Test
    void ensureInvalidProviderException() throws Exception {
        Assert.assertFalse(BugsnagReleasesTask.isValidVcsProvider("foo"))
    }

    @Test
    void ensureValidPayload() throws Exception {
        Assert.assertTrue(BugsnagReleasesTask.isValidPayload("foo", "bar"))
    }

    @Test
    void ensureInvalidPayload() throws Exception {
        Assert.assertFalse(BugsnagReleasesTask.isValidPayload(null, null))
    }

}
