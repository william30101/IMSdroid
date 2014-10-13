#include "example.h"

#define M_PI 3.14159265358979323846

#include <android/log.h>
#define LOG_TAG "hello"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

int Shape::nshapes = 0;
int Ope::beSendSize = 11;
unsigned char Ope::beSendData[] = {'0'};

double Circle::area(void) {
	return M_PI*radius*radius;
}

double Circle::perimeter(void) {
	return 2*M_PI*radius;
}

double Square::area(void) {
	return width*width;
}

double Square::perimeter(void) {
	return 4*width;
}

void Ope::initByteArray() {

	for (int i=0; i < Ope::beSendSize;i++)
		beSendData[i] = '0';
}

int Ope::addToByteArray(unsigned char b, int count ) {

	LOGI("char= %d , count = %c\n",b,count);
	beSendData[count] = b;

	return 0;

}

void Ope::printOpeByteArray() {

	for(int i=0; i < Ope::beSendSize;i++)
	{
		LOGI("beSendData[%d] = %c \n" , i , beSendData[i]);
	}
}

void Ope::printByteArray(unsigned char inByte[] , int len) {

	for(int i=0; i < len;i++)
	{
		LOGI("inByte[%d] = %c \n" , i , inByte[i]);
	}
}


unsigned char* Ope::ByteArrayToString(void)
{
	unsigned char * retData = beSendData;
	return retData;
}
