#include <jni.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <android/log.h>
//#include "beacon_control.h"

static const char *classPathName = "org/doubango/imsdroid/BeaconUtils";
#define TAG "beacon"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)

JNIEXPORT jint JNICALL Native_BeasonReset(JNIEnv *env, jobject obj, jstring str){
	char *dir = (char*)env->GetStringUTFChars(str, JNI_FALSE);
	int fd, ret, i;
	char buf[3] = {'1', '0', '1'};

	LOGD("Interface: %s", dir);
	fd = open(dir, O_WRONLY);
	if(fd < 0) {
		LOGD("Need to change sysfs mode to 777");
		return -1;
	}

	for(i = 0; i < 3; i++) {
		ret = write(fd, &buf[i], 1);
		if (ret < 0) {
			LOGD("Write fail");
			close(fd);
			return -1;
		}
		usleep(200000);
	}

	LOGD("Reset success");
	close(fd);

	return 0;
}

static JNINativeMethod methods[] = {
	//Java Name		(Input Arg) return arg		JNI Name
	{"BeasonReset",	"(Ljava/lang/String;)I",	(void *)Native_BeasonReset},
};

static int registerNativeMethods(JNIEnv* env, const char* className,
	JNINativeMethod* methods, int numMethods)
{
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL)
	{
		LOGI("can't find className=%s  \n",className);
		return JNI_FALSE;
	}

	if (env->RegisterNatives(clazz, methods, numMethods) < 0)
	{
	LOGE("register nativers error");
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

static int register_android_natives(JNIEnv *env){

	 if (!registerNativeMethods(env, classPathName,
			 methods, sizeof(methods) / sizeof(methods[0]))) {
		return JNI_FALSE;
	  }
	  return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	LOGI("Entering JNI_OnLoad");

	if (vm->GetEnv((void**)&env,JNI_VERSION_1_4) != JNI_OK)
		goto bail;

	if (!register_android_natives(env))
		goto bail;

	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	bail:
		LOGI("Leaving JNI_OnLoad (result=0x%x)\n", result);
		return result;
}
