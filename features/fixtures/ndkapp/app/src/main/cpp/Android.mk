LOCAL_PATH   := $(call my-dir)
BUGSNAG_LIBS := $(LOCAL_PATH)/../../../build/intermediates/bugsnag-libs

include $(CLEAR_VARS)
LOCAL_MODULE            := bugsnag-ndk
LOCAL_SRC_FILES         := $(BUGSNAG_LIBS)/jni/$(TARGET_ARCH_ABI)/libbugsnag-ndk.so
LOCAL_EXPORT_C_INCLUDES := $(BUGSNAG_LIBS)/assets/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE     := native-lib
LOCAL_SRC_FILES  := native-lib.cpp

LOCAL_LDLIBS           := -llog -landroid
LOCAL_SHARED_LIBRARIES := bugsnag-ndk

include $(BUILD_SHARED_LIBRARY)
