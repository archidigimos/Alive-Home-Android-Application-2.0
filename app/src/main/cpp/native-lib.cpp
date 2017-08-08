#include <jni.h>
#include <string>

extern "C"
jstring Java_com_example_archismansarkar_login_1signup_AESHelper_getSecretKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "";
    return env->NewStringUTF(key.c_str());
}
extern "C"
jstring Java_com_example_archismansarkar_login_1signup_MainActivity_getMessage(JNIEnv* env, jobject) {
    std::string message = "";
    return env->NewStringUTF(message.c_str());
}