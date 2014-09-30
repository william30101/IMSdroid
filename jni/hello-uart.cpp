#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <sys/ioctl.h>


#undef	TCSAFLUSH
#define	TCSAFLUSH	TCSETSF
#ifndef	_TERMIOS_H_
#define	_TERMIOS_H_
#endif

static int fd;
struct termios newtio, oldtio;

static const char *classPathName = "org/doubango/imsdroid/UartCmd";
#define LOG_TAG "hello-uart"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)




extern "C"
{

	JNIEXPORT jlong JNICALL Native_ReadService(JNIEnv *env,jobject mc, jstring s)
	{
		//unsigned long long test = 55544112365156165544;
		//MyClient client;
		//char *str = (char*)env->GetStringUTFChars(s, NULL);
		//int ret = client.setN(2012);
		//LOGI("setN return: %d\n", ret);

		//env->ReleaseStringUTFChars(s, str);
		return 0;
	}

	JNIEXPORT jint JNICALL Native_OpenUart(JNIEnv *env,jobject mc, jstring s)
	{

		const char *str1 = "/dev/";
		char *str2 = (char*)env->GetStringUTFChars(s, NULL);
		char *sall = (char*) malloc(strlen(str1) + strlen(str2) + 1);

		strcpy(sall, str1);
		strcat(sall, str2);

		LOGI("open uart port device node = %s ",sall);
		fd = open(sall, O_RDWR | O_NOCTTY | O_NDELAY);

		env->ReleaseStringUTFChars(s, str2);

		free(sall);
		return fd;
	}

	JNIEXPORT void JNICALL Java_idv_android_hellouart_Uart2C_closeUart(JNIEnv *env,jobject mc, jint i)
	{

		close(i);
	}

	JNIEXPORT jint JNICALL Native_SetUart(JNIEnv *env,jobject mc, jint i)
	{
		int Baud_rate[] = { B9600, B115200};
		LOGI("Native_SetUart %d", i);

		tcgetattr(fd, &oldtio);
		tcgetattr(fd, &newtio);
		cfsetispeed(&newtio, Baud_rate[i]);
		cfsetospeed(&newtio, Baud_rate[i]);

		newtio.c_lflag = 0;
		newtio.c_cflag = Baud_rate[i] | CS8 | CREAD | CLOCAL;
		newtio.c_iflag = BRKINT | IGNPAR | IXON | IXOFF | IXANY;
		newtio.c_oflag = 02;
		newtio.c_line = 0;
		newtio.c_cc[7] = 255;
		newtio.c_cc[4] = 0;
		newtio.c_cc[5] = 0;

		if (tcsetattr(fd, TCSANOW, &newtio) < 0)
		{
			LOGE("tcsetattr2 fail !\n");
			exit(1);
		}
		return fd;
	}

	JNIEXPORT jint JNICALL Native_SendMsgUart(JNIEnv *env,jobject mc, jbyteArray inByte)
	{
		int len,i;

		jbyte* dataByteArray = env->GetByteArrayElements(inByte,NULL);
		jsize byteArrayLength = env->GetArrayLength(inByte);
		//const char *buf;
		//buf = env->GetStringUTFChars(str, NULL);
		//len = env->GetStringLength(str);
		LOGI("len = %d\n",byteArrayLength);
		for(i = 0; i< byteArrayLength ; i++)
		{
			LOGI("data = 0x%x\n",dataByteArray[i]);
		}
		write(fd, dataByteArray, byteArrayLength);
		//env->ReleaseStringUTFChars(str, buf);
		env->ReleaseByteArrayElements(inByte, dataByteArray, 0);
	}

	JNIEXPORT jstring JNICALL Native_ReceiveMsgUart(JNIEnv *env,jobject mc)
	{
		char buffer[255];
		char buf[255];
		int len, i = 0, k = 0;
		memset(buffer, 0, sizeof(buffer));
		memset(buf, 0, sizeof(buf));
		len = read(fd, buffer, 255);
		if (len > 0)
		{
			buf[len]='\0';
			return env->NewStringUTF(buffer);
		} else
			return NULL;
	}


	static JNINativeMethod gMethods[] = {
		//Java Name			(Input Arg) return arg   JNI Name
		{"ReceiveMsgUart",   "()Ljava/lang/String;",(void *)Native_ReceiveMsgUart},
		{"SendMsgUart",   "([B)I",  (void *)Native_SendMsgUart},
		{"SetUart",   "(I)I",   					(void *)Native_SetUart},
		{"OpenUart",   "(Ljava/lang/String;)I",   	(void *)Native_OpenUart},
		{"ReadService",   "(Ljava/lang/String;)I",   	(void *)Native_ReadService},

	};

	static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods)
	{
		jclass clazz;
		clazz = env->FindClass(className);
		if (clazz == NULL)
		{
			LOGI("can't find className=%s  \n",className);
			return JNI_FALSE;
		}

		if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
		{
		LOGE("register nativers error");
			return JNI_FALSE;
		}

		return JNI_TRUE;
	}

	static int register_android_native_uart(JNIEnv *env){

		 if (!registerNativeMethods(env, classPathName,
				 gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
			return JNI_FALSE;
		  }
		  return JNI_TRUE;
	}


	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
		JNIEnv* env = NULL;
		jint result = -1;

		LOGI("Entering JNI_OnLoad \n");

		if (vm->GetEnv((void**)&env,JNI_VERSION_1_4) != JNI_OK)
			goto bail;

		if (!register_android_native_uart(env))
			goto bail;

		/* success -- return valid version number */
		result = JNI_VERSION_1_4;

		bail:
			LOGI("Leaving JNI_OnLoad (result=0x%x)\n", result);
			return result;
	}
}
