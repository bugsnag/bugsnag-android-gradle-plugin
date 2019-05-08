package com.bugsnag.android.gradle

import org.gradle.api.Project

abstract class Call {

    static final int MAX_RETRY_COUNT = 5
    static final int TIMEOUT_MILLIS = 60000

    private final Project project // 60 seconds

    protected Call(Project project) {
        this.project = project
    }

    /**
     * Attempts to upload to the server, using automatic retries if unsuccessful
     */
    void execute() {
        boolean uploadSuccessful = makeApiCall()

        int maxRetryCount = retryCount
        int retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            project.logger.warn(String.format("Retrying Bugsnag upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount))
            uploadSuccessful = makeApiCall()
            retryCount--
        }
    }

    /**
     * Makes a request to an API. If the call is not successful, clients should return false for
     * automatic retry logic.
     *
     * @return true if successful, otherwise false.
     */
    abstract boolean makeApiCall()

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    int getRetryCount() {
        project.bugsnag.retryCount >= MAX_RETRY_COUNT ? MAX_RETRY_COUNT : project.bugsnag.retryCount
    }

}
