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
#include "example.h"
#include "MyClient.h"

#undef	TCSAFLUSH
#define	TCSAFLUSH	TCSETSF
#ifndef	_TERMIOS_H_
#define	_TERMIOS_H_
#endif


static int debugData = false;

static int fd = 0 , nanoFd , driveFd;
static int encoderDataSize = 8;

struct termios newtio, oldtio;

static const char *classPathName = "org/doubango/imsdroid/UartCmd";
#define LOG_TAG "hello"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)


using namespace android;

extern "C"
{

	JNIEXPORT jbyteArray JNICALL Native_Combine(JNIEnv *env,jobject mc ,
			jobject nanoq, jobject encodq)
	{



		jclass nanoClazz = env->GetObjectClass(nanoq);
		jmethodID nanoGetMethodID = env->GetMethodID(nanoClazz, "get", "(I)Ljava/lang/Object;");
		jmethodID nanoSizeMethodID = env->GetMethodID(nanoClazz, "size", "()I");
		int nanoSize = env->CallIntMethod(nanoq, nanoSizeMethodID);


		jclass encoClazz = env->GetObjectClass(encodq);
		//ArrayList_class       = env->FindClass( "java/util/ArrayList" );
		jmethodID encoGetMethodID = env->GetMethodID(encoClazz, "get", "(I)Ljava/lang/Object;");
		jmethodID encoSizeMethodID = env->GetMethodID(encoClazz, "size", "()I");
		int encoderSize = env->CallIntMethod(encodq, encoSizeMethodID);

		LOGE("nanoQueue's size is : %d", nanoSize);
		LOGE("encoderQueue's size is : %d", encoderSize);


		//nanoFloatArray = env->NewFloatArray(nanoSize);


		jfloat nanoFloatArray[nanoSize];
		//nanoPan data save in this array.

		jfloat encoderFloatArray[encoderSize];
		//encoder data save in this array.

		//jbyte * encoderByteArr[encoderDataSize];


		//encoderFloatArray = env->NewFloatArray(encoderSize);

		//jfloat nanoTempArray[nanoSize];
		//jfloat encoderTempArray[encoderSize];

		 for (int i = 0; i < nanoSize; i++)
		 {
			jfloatArray nanoTemp = (jfloatArray)env->CallObjectMethod(nanoq, nanoGetMethodID, i);
			jfloat* flt1 = env->GetFloatArrayElements( nanoTemp,0);
			LOGI("jni nanobyte = %.2f i = %d",flt1[0],i);
			nanoFloatArray[i] = flt1[0];

			env->ReleaseFloatArrayElements(nanoTemp, flt1, 0);
		 }


		 //LOGI("jni nanobyte = %.2f",nanoFloatArray[0]);

		 for (int i = 0; i < encoderSize; i++)
		 {
				jbyteArray encoByte = (jbyteArray)env->CallObjectMethod(encodq, nanoGetMethodID, i);

				//jint byteLeng = env-> GetArrayLength(encoByte);

				//jbyte *arr   =   env-> GetByteArrayElements(encoByte, 0);

				jbyte * encoderByteArr = env-> GetByteArrayElements(encoByte, 0);


				LOGI("encobyte data = %s" , encoderByteArr);
				//c=(unsigned char*)arr;

				//LOGI("encobyte = %s",c);
		 }

		 Ope *op = new Ope();

		 //op->printByteArray(c , encoderSize);

		 op->initByteArray();


		 op->addToByteArray('G',2);
		 op->printOpeByteArray();

		 //Output Data format  0x53 0x09 X4 X3 X2 X1 Y4 Y3 Y2 Y1 CRC2 CRC1 0x45
		 //Save to byte array beSendMsg[13]

		 unsigned char * beSendMsgchar = op->ByteArrayToString();

		 jbyteArray beSendMsg = env->NewByteArray (13);
		 env->SetByteArrayRegion (beSendMsg, 0, 13, reinterpret_cast<jbyte*>(beSendMsgchar));

		 return  beSendMsg;
	}


	JNIEXPORT jint JNICALL Native_StartCal(JNIEnv *env,jobject mc)
	{

		//int *str = (char*)env->GetStringUTFChars(data, NULL);

		//Circle* cir = new Circle(5);
		//cir->area();
		//LOGI("radius = %lf area=%lf",cir->radius,cir->area());
		//cir.


		  return 0;
	}

	JNIEXPORT jint JNICALL Native_WriteDemoData(JNIEnv *env,jobject mc, jintArray data, jint size)
	{

		//int *str = (char*)env->GetStringUTFChars(data, NULL);

		jsize len = env->GetArrayLength(data);
		jint *body = env->GetIntArrayElements(data, 0);
		for (int i=0; i < size; i++)
			LOGI("Hello from JNI - element: %d\n", body[i]);


		Circle* cir = new Circle(10);
		LOGI("radius = %lf ",cir->radius);
		//cir.

		  env->ReleaseIntArrayElements(data, body, 0);
		  return 0;
	}

	JNIEXPORT jint JNICALL Native_OpenUart(JNIEnv *env,jobject mc, jstring s , jint fdnum)
	{

		const char *str1 = "/dev/";
		char *str2 = (char*)env->GetStringUTFChars(s, NULL);
		char *sall = (char*) malloc(strlen(str1) + strlen(str2) + 1);

		strcpy(sall, str1);
		strcat(sall, str2);

		LOGI("open uart port device node = %s , fdnum=%d \n",sall,fdnum);

		if (fdnum == 1)
		{
			driveFd = open(sall, O_RDWR | O_NOCTTY | O_NDELAY);
			if (driveFd > 0)
				fd = driveFd;
		}
		else if (fdnum == 2)
		{
			nanoFd = open(sall, O_RDWR | O_NOCTTY | O_NDELAY);
			if (nanoFd > 0)
				fd = nanoFd;
		}
		else
		{
			fd = 0;
		}

		env->ReleaseStringUTFChars(s, str2);

		free(sall);

		return fd;
	}

	JNIEXPORT jint JNICALL Native_CloseUart(JNIEnv *env,jobject mc, jint fdnum)
	{

		close(fdnum);
	}

	JNIEXPORT jint JNICALL Native_SetUart(JNIEnv *env,jobject mc, jint i,jint fdnum)
	{
		int Baud_rate[] = { B19200, B115200};
		LOGI("Native_SetUart %d", i);

		if (fdnum == 1)
		{

		tcgetattr(driveFd, &oldtio);
		tcgetattr(driveFd, &newtio);
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

			if (tcsetattr(driveFd, TCSANOW, &newtio) < 0)
			{
				LOGE("tcsetattr2 fail !\n");
				exit(1);
			}

			return driveFd;
		}
		else if (fdnum == 2)
		{
			tcgetattr(nanoFd, &oldtio);
			tcgetattr(nanoFd, &newtio);
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

				if (tcsetattr(nanoFd, TCSANOW, &newtio) < 0)
				{
					LOGE("tcsetattr2 fail !\n");
					exit(1);
				}

				return nanoFd;
		}
			return -1;
	}

	JNIEXPORT jint JNICALL Native_SendMsgUart(JNIEnv *env,jobject mc, jstring str, jint fdnum , jbyteArray inByte)
	{
		int len;
		jboolean isCopy;
		const char *strBuf;

		jbyte* a = env->GetByteArrayElements(inByte,&isCopy);
		char *buf = (char*)a;

		strBuf = env->GetStringUTFChars(str, NULL);
		len = env->GetStringLength(str);
		if (fdnum == 1)
		{

			write(driveFd, buf, len);
		}
		else if (fdnum == 2)
		{
			write(nanoFd, buf, len);
		}
		//LOGI("len = %d",len);
		LOGI(" 1=dri 2=nano  write to %d  driveFd=%d", fdnum,driveFd);

		LOGI("Write data 3 = %x",buf[3]);
		LOGI("Write data 4 = %x",buf[4]);
		LOGI("Write data 5 = %x",buf[5]);

		env->ReleaseStringUTFChars(str, strBuf);
		env->ReleaseByteArrayElements(inByte, a, 0);
	}

	JNIEXPORT jstring JNICALL Native_ReceiveMsgUart(JNIEnv *env,jobject mc, jint fdnum)
	{
		char buffer[255];
		char buf[255];
		char buffertest[255] = {'a','b','c','d','\0'};
		int len, i = 0, k = 0 , count = 0;
		jfloatArray result;
		memset(buffer, 0, sizeof(buffer));
		memset(buf, 0, sizeof(buf));


		if (fdnum == 1)
			len = read(driveFd, buffer, 255);
		else if (fdnum == 2)
			len = read(nanoFd, buffer, 255);



		LOGI("read on native function driveFd = %d leng = %d" ,driveFd,len);
		if (debugData)
		{
			for (i =0;i< 255 ; i++)
			{

				if (buffertest[i] != '\0')
					count++;

			}

			LOGI("read on native function leng = %d" ,count);
			if(count <= 0)
			{
				return NULL;
			}
			//jbyteArray arr = env->NewByteArray(count);
			//env->SetByteArrayRegion(arr,0,count, (jbyte*)buffertest);

			return env->NewStringUTF(buffer);
		}
		else if (len > 0)
		{

			LOGI("read on fd = %d native function buf = %s" ,driveFd,buffer);

			return env->NewStringUTF(buffer);
		}

		//env->ReleaseByteArrayElements(arr, 0 );

		return NULL;
		/////////////////

	}


	JNIEXPORT jbyteArray JNICALL Native_ReceiveByteMsgUart(JNIEnv *env,jobject mc, jint fdnum)
			{
				char buffer[255];
				char buf[255];
				char buffertest[255] = {'a','b','c','d','\0'};
				jbyte nodatabyte[5] = {0x01,0x01,0x01,0x01,0x01};
				int len, i = 0, k = 0 , count = 0;
				jfloatArray result;
				memset(buffer, 0, sizeof(buffer));
				memset(buf, 0, sizeof(buf));

				if (fdnum == 1)
					len = read(driveFd, buffer, 255);
				else if (fdnum == 2)
					len = read(nanoFd, buffer, 255);



				LOGI("rec leng = %d" ,len);

				if (len > 0)
				{
					LOGI("read on native function buf[0] = %x  buf[1]= %x buf[2]= %x buf[3]= %x buf[4]= %x buf[5]= %x"
							,buffer[0],buffer[1],buffer[2],buffer[3],buffer[4],buffer[5]);

					jbyteArray array = env->NewByteArray(len);

					env->SetByteArrayRegion (array, 0, len, (jbyte*)(buffer));

					return array;
					//buffer[len]='\0';
					//return env->NewStringUTF(buffer);
				}

				jbyteArray array = env->NewByteArray(5);
				env->SetByteArrayRegion (array, 0, 5, (jbyte*)(nodatabyte));
				//env->ReleaseByteArrayElements(arr, 0 );

				return array;
				/////////////////

			}


	static JNINativeMethod gMethods[] = {
		//Java Name			(Input Arg) return arg   JNI Name
		{"ReceiveMsgUart",   "(I)Ljava/lang/String;",(void *)Native_ReceiveMsgUart},
		{"ReceiveByteMsgUart",   "(I)[B",(void *)Native_ReceiveByteMsgUart},
		{"SendMsgUart",   "(Ljava/lang/String;I[B)I",  (void *)Native_SendMsgUart},
		{"SetUart",   "(II)I",   					(void *)Native_SetUart},
		{"OpenUart",   "(Ljava/lang/String;I)I",   	(void *)Native_OpenUart},
		{"WriteDemoData",   "([II)I",   	(void *)Native_WriteDemoData},
		{"StartCal",   "()I",   	(void *)Native_StartCal},
		{"CloseUart",   "(I)I",   	(void *)Native_CloseUart},
		{"Combine",   "(Ljava/util/ArrayList;Ljava/util/ArrayList;)[B",   	(void *)Native_Combine},



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



