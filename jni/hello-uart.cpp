//#include <jni.h>
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
#include "hello-uart.h"
#include <math.h>

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

#define DegToRad 3.141592653/180
#define D 11.836
#define pi 3.14
#define piD (11.83*3.14)/60
#define dt 0.3

#define LOWER   -0.5
#define UPPER	0.5

int X1=-2,Y1=0,Z1=0,X2=-2,Y2=0,Z2=0,X3=-2,Y3=0,Z3=0;
int I[3][3]={{1,0,0},{0,1,0},{0,0,1}};
static float Tx=1,Ty=1,Tz=0;
static float z1,z2,z3,a,b,c,d1,d2,d3;
static float HX1,HY1,HX2,HY2,HX3,HY3,HXA1,HYA1,HXB1,HYB1,HXC1,HYC1,HXA2,HYA2,HXB2,HYB2,HXC2,HYC2,HXA3,HYA3,HXB3,HYB3,HXC3,HYC3;
static float Z01[3][1],Z0[3][1],dZ[3][1];
static float H[3][8],HT[8][3];

static float Kk[8][3],Pk_HkT[8][3],HkPk_HkT[3][3],HkPk_HkTR[3][3],InvHkPk_HkTR[3][6],AnsInvHkPk_HkTR[3][3];
static float KkdZ[8][1],Xk[8][1];
static float KkHk[8][8],KkHkPk_[8][8],Pk[8][8];

//static float Xk_[8][1]={{0},{0},{-0.6},{0.4},{5.0},{0},{4.63},{4.1}};//modify the coordinate here(robot,anchor1,anchor2,anchor3)
//static float Xk_[8][1]={{0},{0},{-0.6},{0.4},{4.9},{4.63},{5.0},{0}};//modify the coordinate here(robot,anchor1,anchor2,anchor3)
static float Xk_[8][1]={{0},{0},{2.29},{-1.18},{2.3},{6.18},{6.18},{6.18}};
static float Pk_[8][8]={{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,1,0,0,0,0,0},{0,0,0,1,0,0,0,0},{0,0,0,0,1,0,0,0},{0,0,0,0,0,1,0,0},{0,0,0,0,0,0,1,0},{0,0,0,0,0,0,0,1}};

//Encoder6

static float Q[8][8]={{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0}};//modify the rate of state equation
//static float Q[8][8]={{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0}};//modify the rate of state equation
//DW1000
static float R[3][3]={{35,0,0},{0,35,0},{0,0,35}};//modify the rate of measurement
//static float R[3][3]={{0.1,0,0},{0,0.1,0},{0,0,0.1}};//modify the rate of measurement

static float X,Y,dX,dY;
static float cosine,sine,VL,VR,V,W;
static float d_theta;
static int initial,d,e,f,theta1;
static int C=0;


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

	JNIEXPORT jint JNICALL Native_OpenUart(JNIEnv *env,jobject mc, jstring s )
	{

		const char *str1 = "/dev/";
		char *str2 = (char*)env->GetStringUTFChars(s, NULL);
		char *sall = (char*) malloc(strlen(str1) + strlen(str2) + 1);

		strcpy(sall, str1);
		strcat(sall, str2);

		//scrcmp if true , return 0.
		bool ismxc3 = !strcmp(str2, "ttymxc3");
		bool ismxc2 = !strcmp(str2, "ttymxc2");

		LOGI("open uart port device node = %s , ismxc2=%d ismxc3=%d\n",sall,ismxc2,ismxc3);

		if (ismxc3)
		{
			driveFd = open(sall, O_RDWR | O_NOCTTY | O_NDELAY);
			if (driveFd > 0)
			{
				// 0 => 19200
				Native_SetUart(env,mc,driveFd,0);
				fd = driveFd;
			}
		}
		else if (ismxc2)
		{
			nanoFd = open(sall, O_RDWR | O_NOCTTY | O_NDELAY);
			if (nanoFd > 0)
			{
				// 1 => 115200
				Native_SetUart(env,mc,nanoFd,1);
				fd = nanoFd;
			}
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

	JNIEXPORT jint JNICALL Native_SetUart(JNIEnv *env,jobject mc, jint fdnum, jint baudrate)
	{
		int Baud_rate[] = { B19200, B115200};
		LOGI("Native_SetUart %d", baudrate);

		tcgetattr(fdnum, &oldtio);
		tcgetattr(fdnum, &newtio);
		cfsetispeed(&newtio, Baud_rate[baudrate]);
		cfsetospeed(&newtio, Baud_rate[baudrate]);

		newtio.c_lflag = 0;
		newtio.c_cflag = Baud_rate[baudrate] | CS8 | CREAD | CLOCAL;
		newtio.c_iflag = BRKINT | IGNPAR | IXON | IXOFF | IXANY;
		newtio.c_oflag = 02;
		newtio.c_line = 0;
		newtio.c_cc[7] = 255;
		newtio.c_cc[4] = 0;
		newtio.c_cc[5] = 0;

		if (tcsetattr(fdnum, TCSANOW, &newtio) < 0)
		{
			LOGE("tcsetattr2 fail !\n");
			exit(1);
		}

		return fdnum;
	}

	JNIEXPORT jint JNICALL Native_SendMsgUart(JNIEnv *env,jobject mc,  jint fdnum , jbyteArray inByte)
	{
			int len;
			jboolean isCopy;

			jbyte* a = env->GetByteArrayElements(inByte,&isCopy);
			len = env->GetArrayLength(inByte);
			char *buf = (char*)a;


			if (fdnum == 1)
			{

				write(driveFd, buf, len);
			}
			else if (fdnum == 2)
			{
				write(nanoFd, buf, len);
			}
			LOGI("len = %d",len);
			//LOGI("Write data 0 = %x",buf[0]);
			//LOGI("Write data 1 = %x",buf[1]);
			//LOGI("Write data 2 = %x",buf[2]);
			//LOGI("Write data 3 = %x",buf[3]);
			//LOGI("Write data 4 = %x",buf[4]);
			//LOGI("Write data 5 = %x",buf[5]);
			//LOGI("Write data 6 = %x",buf[6]);

			env->ReleaseByteArrayElements(inByte, a, 0);
	}

	JNIEXPORT jstring JNICALL Native_ReceiveDW1000Uart(JNIEnv *env,jobject mc, jint fdnum)
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

	JNIEXPORT jint JNICALL Native_WeightSet(JNIEnv *env,jobject mc, jfloat dwWeight , jfloat encoderWeight)
	{
		//jfloatArray result;
		//float RobotLocation[] = { 1.0 , 2.0};
		//result = env->NewFloatArray(2); // Store X , Y data

		LOGI("DW weight = %f encoder weight = %f",dwWeight,encoderWeight);

		Q[0][0] = encoderWeight;
		Q[1][1] = encoderWeight;

		R[0][0] = dwWeight;
		R[1][1] = dwWeight;
		R[2][2] = dwWeight;

		//env->SetFloatArrayRegion(result, 0, 2, RobotLocation);

		return 0;
	}


	JNIEXPORT jint JNICALL Native_AnchorSet(JNIEnv *env,jobject mc, jfloat anchor1X , jfloat anchor1Y , jfloat anchor2X , jfloat anchor2Y , jfloat anchor3X , jfloat anchor3Y)
	{
		//jfloatArray result;
		//float RobotLocation[] = { 1.0 , 2.0};
		//result = env->NewFloatArray(2); // Store X , Y data

		LOGI("DW anchor1X = %f anchor1Y = %f anchor2X = %f anchor2Y = %f anchor3X = %f anchor3Y = %f"
				,anchor1X,anchor1Y,anchor2X,anchor2Y,anchor3X,anchor3Y);


		Xk_[2][1] = anchor1X;
		Xk_[3][1] = anchor1Y;

		Xk_[4][1] = anchor2X;
		Xk_[5][1] = anchor2Y;

		Xk_[6][1] = anchor3X;
		Xk_[7][1] = anchor3Y;



		//env->SetFloatArrayRegion(result, 0, 2, RobotLocation);

		return 0;
	}


	///------EKF calculation--------------------------------------------------------------------------
	JNIEXPORT jfloatArray JNICALL Native_EKF(JNIEnv *env,jobject mc,jfloat a,jfloat b,jfloat c,jint left,jint right,jint degree)
	{
		FILE *fp = NULL;
		jfloatArray result;
		// [0] mesure X [1] mesure Y
		// [2] State X  [3] state Y
		//byte[] ReByteEnco = new byte[11];
		float RobotLocation[4];
		result = env->NewFloatArray(4); // Store X , Y data

		LOGI("dwWeight=%f encoderWeight=%f",Q[0][0],R[0][0]);

		int i,j,k,l;
				if (C==0){
				initial=degree;
				}

				memset(Pk_HkT,0, sizeof(int)*24);
				memset(HkPk_HkT,0, sizeof(int)*9);
				memset(Kk,0, sizeof(int)*24);
				memset(KkdZ,0, sizeof(int)*8);
				memset(KkHk,0, sizeof(int)*64);
				memset(KkHkPk_,0, sizeof(int)*64);

				d1=sqrt(pow(a,2)- pow((Tz-Z1),2));
				d2=sqrt(pow(b,2)- pow((Tz-Z2),2));
				d3=sqrt(pow(c,2)- pow((Tz-Z3),2));

				z1=sqrt(pow((Xk_[0][0]-Xk_[2][0]),2)+pow((Xk_[1][0]-Xk_[3][0]),2));// + get_rand(LOWER, UPPER);
				z2=sqrt(pow((Xk_[0][0]-Xk_[4][0]),2)+pow((Xk_[1][0]-Xk_[5][0]),2));// + get_rand(LOWER, UPPER);
				z3=sqrt(pow((Xk_[0][0]-Xk_[6][0]),2)+pow((Xk_[1][0]-Xk_[7][0]),2));// + get_rand(LOWER, UPPER);

				Z01[0][0]=z1;
				Z01[1][0]=z2;
				Z01[2][0]=z3;

				Z0[0][0]=d1;
				Z0[1][0]=d2;
				Z0[2][0]=d3;

				for (j=0;j<3;j++){
					dZ[j][0] = Z0[j][0] - Z01[j][0];
				}

				HX1=(Xk_[0][0]-Xk_[2][0])/z1,HY1=(Xk_[1][0]-Xk_[3][0])/z1,HXA1=-(Xk_[0][0]-Xk_[2][0])/z1,HYA1=-(Xk_[1][0]-Xk_[3][0])/z1,HXB1=0,HYB1=0,HXC1=0,HYC1=0;
				HX2=(Xk_[0][0]-Xk_[4][0])/z2,HY2=(Xk_[1][0]-Xk_[5][0])/z2,HXA2=0,HYA2=0,HXB2=-(Xk_[0][0]-Xk_[4][0])/z2,HYB2=-(Xk_[1][0]-Xk_[5][0])/z2,HXC2=0,HYC2=0;
				HX3=(Xk_[0][0]-Xk_[6][0])/z3,HY3=(Xk_[1][0]-Xk_[7][0])/z3,HXA3=0,HYA3=0,HXB3=0,HYB3=0,HXC3=-(Xk_[0][0]-Xk_[6][0])/z3,HYC3=-(Xk_[1][0]-Xk_[7][0])/z3;

				H[0][0]=HX1,H[0][1]=HY1,H[0][2]=HXA1,H[0][3]=HYA1,H[0][4]=HXB1,H[0][5]=HYB1,H[0][6]=HXC1,H[0][7]=HYC1;
				H[1][0]=HX2,H[1][1]=HY2,H[1][2]=HXA2,H[1][3]=HYA2,H[1][4]=HXB2,H[1][5]=HYB2,H[1][6]=HXC2,H[1][7]=HYC2;
				H[2][0]=HX3,H[2][1]=HY3,H[2][2]=HXA3,H[2][3]=HYA3,H[2][4]=HXB3,H[2][5]=HYB3,H[2][6]=HXC3,H[2][7]=HYC3;

				for(i=0;i<8;i++){
							for(j=0;j<3;j++){
								HT[i][j]=H[j][i];
							}
				}
		///-----Compute the Kalman Gain----------------------------------------------------
				for(i=0;i<8;i++){
							for(j=0;j<3;j++){
								for(k=0;k<8;k++){
								Pk_HkT[i][j]=Pk_HkT[i][j]+(Pk_[i][k]*HT[k][j]);
								}
							}
				}

				for(i=0;i<3;i++){
							for(j=0;j<3;j++){
								for(k=0;k<8;k++){
									HkPk_HkT[i][j]=HkPk_HkT[i][j]+(H[i][k]*Pk_HkT[k][j]);
								}
							}
				}

				for(i=0;i<3;i++){
							for(j=0;j<3;j++){
								HkPk_HkTR[i][j]=HkPk_HkT[i][j]+R[i][j];
							}
				}
		///----�X�W�x�}----------------------------------------------------------------------------------------
				for(i=0;i<3;i++){
							for(j=0;j<3;j++){
								InvHkPk_HkTR[i][j]=HkPk_HkTR[i][j];
								InvHkPk_HkTR[i][j+3]=I[i][j];
							}
						}
		///----Inverse Matrix Calculation------------------------------------------------------------------
				for(i=0;i<3;i++){
							X=InvHkPk_HkTR[i][i];
							for(j=0;j<6;j++){
							InvHkPk_HkTR[i][j]=InvHkPk_HkTR[i][j]/X;
							}

							for(k=0;k<3;k++){
								if(k!=i){
									Y=InvHkPk_HkTR[k][i];
									for(l=0;l<6;l++){
									InvHkPk_HkTR[k][l]=-(Y*InvHkPk_HkTR[i][l])+InvHkPk_HkTR[k][l];
									}
								}
							}
						}
		///-----Get Inverse Matrix and Calculate the Kalman Gain--------------------------------------------------------------------------------------
				for(i=0;i<3;i++){
							for(j=3;j<6;j++){
							AnsInvHkPk_HkTR[i][j-3]=InvHkPk_HkTR[i][j];
							}
				}

				for(i=0;i<8;i++){
							for(j=0;j<3;j++){
								for(k=0;k<3;k++){
									Kk[i][j]=Kk[i][j]+(Pk_HkT[i][k]*AnsInvHkPk_HkTR[k][j]);
								}
							}
				}
		///-----Update estimate with measurement zk--------------------------------------------------------
				for(i=0;i<8;i++){
							for(j=0;j<3;j++){
								KkdZ[i][0]=KkdZ[i][0]+(Kk[i][j]*dZ[j][0]);
							}
						}
				for(i=0;i<8;i++){
							Xk[i][0]=Xk_[i][0]+KkdZ[i][0];
				}
				LOGD("Xk0=%.3f",Xk[0][0]);
				LOGE("Xk1=%.3f",Xk[1][0]);


				RobotLocation[0] = Xk[0][0] ;
				RobotLocation[1] = Xk[1][0] ;

				//fp = fopen("/sdcard/data2.txt","a");
				//fprintf(fp,"Xk=%.4f,Yk=%.4f\n",Xk[0][0],Xk[1][0]);
				//fprintf(fp,"left=%d,right=%d,compass=%d\n",left,right,degree);
				//fclose(fp);
				//fp = NULL;

		///-----Update Error Covariance----------------------------------------------------------------------
				for(i=0;i<8;i++){
							for(j=0;j<3;j++){
								for(k=0;k<8;k++){
									KkHk[i][k]=KkHk[i][k]+(Kk[i][j]*H[j][k]);
								}
							}
						}

				for(i=0;i<8;i++){
							for(j=0;j<8;j++){
								KkHkPk_[i][j]=KkHkPk_[i][j]+(KkHk[i][j]*Pk_[i][j]);
							}
						}

				for(i=0;i<8;i++){
							for(j=0;j<8;j++){
								Pk[i][j]=Pk_[i][j]-KkHkPk_[i][j];
							}
				}

		///------State equation------------------------------------------------------------------------------
				VL=((((float)left/6)*piD)/dt);
				VR=((((float)right/6)*piD)/dt);

				V=(VL+VR)/2;

				if (degree-initial<0){
					if(initial-degree>180){
						theta1=(degree-initial)+360;
					}
					else{
						theta1=degree-initial;
					}
				}
				else{
					if(degree-initial>180){
						theta1=(degree-initial)-360;
					}
					else
						theta1=degree-initial;
				}
				//theta1=-(theta1);
				//theta1=degree-initial;

				cosine = cos(theta1*DegToRad);
				sine = sin(theta1*DegToRad);

				dX = (V*dt)*(cosine);
				dY = (V*dt)*(sine);
				//LOGD("dX = %.2f",dX);
				//LOGE("dY = %.2f",dY);
				Xk_[0][0] = Xk[0][0]+(dX/100);//�N�첾�q���⦨����
				Xk_[1][0] = Xk[1][0]+(dY/100);
				Xk_[2][0] = Xk[2][0];
				Xk_[3][0] = Xk[3][0];
				Xk_[4][0] = Xk[4][0];
				Xk_[5][0] = Xk[5][0];
				Xk_[6][0] = Xk[6][0];
				Xk_[7][0] = Xk[7][0];
				//fprintf(fp,"%d,%d,%d\n",theta1,degree,initial);
				//fprintf(fp,"dX=%.4f,dY=%.4f\n",dX,dY);
				//fprintf(fp,"%.4f,%.4f\n",Xk_[0][0],Xk_[1][0]);
				//fclose(fp);
				//fp = NULL;
				LOGD("Xk_0=%.3f",Xk_[0][0]);
				LOGE("Xk_1=%.3f",Xk_[1][0]);

				RobotLocation[2] = Xk_[0][0];
				RobotLocation[3] = Xk_[1][0];

				env->SetFloatArrayRegion(result, 0, 4, RobotLocation);

		///-----Error covarinace----------------------------------------------------------------------------
				for(i=0;i<8;i++){
							for(j=0;j<8;j++){
								Pk_[i][j]=Pk[i][j]+Q[i][j];
							}
				}
				C=1;

				return result;
	}

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

	static JNINativeMethod gMethods[] = {
		//Java Name			(Input Arg) return arg   JNI Name
		{"ReceiveDW1000Uart",   "(I)Ljava/lang/String;",(void *)Native_ReceiveDW1000Uart},
		{"ReceiveByteMsgUart",   "(I)[B",(void *)Native_ReceiveByteMsgUart},
		{"SendMsgUart",   "(I[B)I",  (void *)Native_SendMsgUart},
		{"SetUart",   "(II)I",   					(void *)Native_SetUart},
		{"OpenUart",   "(Ljava/lang/String;)I",   	(void *)Native_OpenUart},
		{"WriteDemoData",   "([II)I",   	(void *)Native_WriteDemoData},
		{"StartCal",   "()I",   	(void *)Native_StartCal},
		{"CloseUart",   "(I)I",   	(void *)Native_CloseUart},
		{"Combine",   "(Ljava/util/ArrayList;Ljava/util/ArrayList;)[B",   	(void *)Native_Combine},
		{"EKF", "(FFFIII)[F"	,(void *)Native_EKF},
		{"WeightSet", "(FF)I"	,(void *)Native_WeightSet},
		//{"AnchorSet", "(FFFFFF)I"	,(void *)Native_AnchorSet},
		{"BeasonReset",	"(Ljava/lang/String;)I",	(void *)Native_BeasonReset},

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



