package com.bugsnag.android.gradle

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings(["GetterMethodCouldBeProperty", "UnnecessaryReturnKeyword", "ReturnsNullInsteadOfEmptyCollection"])
class FakeVariantOutputImpl extends BaseVariantOutputImpl {

    private final String name

    protected FakeVariantOutputImpl(String name) {
        super(null, null)
        this.name = name
    }

    @Override
    int getVersionCode() {
        return 1
    }

    @Override
    String getName() {
        return name
    }
}
