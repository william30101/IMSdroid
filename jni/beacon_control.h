#include <jni.h>

#ifndef _Included_ResetControl
#define _Included_ResetControl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_resetcontrol_MainActivity
 * Method:    getJNIString
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jint JNICALL Native_BeasonReset(JNIEnv *, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif
