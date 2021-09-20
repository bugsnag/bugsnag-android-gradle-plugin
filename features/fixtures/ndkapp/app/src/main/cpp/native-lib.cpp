#include <jni.h>
#include <string>
#include <bugsnag.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bugsnag_android_ndkapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    bugsnag_set_user_env(env, "124323", "joe mills", "j@ex.co");
    return env->NewStringUTF(hello.c_str());
}
