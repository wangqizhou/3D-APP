#include <fcntl.h>
#include <assert.h>
#include <sys/ioctl.h>
#include "util/panel_param.h"
#include "com_evistek_utils_evisutil.h"

#define TAG "JNI Panel Control"

//Qs3d
#define QSDEVICE_IO(num)             _IO('H', num)
#define QSDEVICE_GRATING_IOCTL_ENABLE       QSDEVICE_IO(1)
#define QSDEVICE_GRATING_IOCTL_DISABLE      QSDEVICE_IO(2)

//MI3D
#define MI3D_TN_CMD_STATUS      (0x80)
#define MI3D_TN_CMD             (0x40)
#define MI3D_SET_VERTICAL_TN_ON (0x20)
#define MI3D_SET_TN_OFF         (0x10)
#define MI3D_SET_HORIZONTAL_TN_ON (0x40)

#define MI3D_OFF_STATUS         (16)
#define MI3D_VERTICAL_ON_STATUS (32)
#define MI3D_HORIZONTAL_ON_STATUS (64)

typedef void (*ENABLE3D_FUNC)(bool);
typedef bool (*IS3DENABLE_FUNC)();

typedef struct _DevFunPair {
    const char *pDevName;
    ENABLE3D_FUNC pEnable3D;
    IS3DENABLE_FUNC pIs3DEnable;
} DevFunPair;

#ifdef __cplusplus
extern "C" {
#endif

int fd = -1;
const char* m_pDev = NULL;
ENABLE3D_FUNC m_pEnable3DFunc = NULL;
IS3DENABLE_FUNC m_pIs3DEnableFunc = NULL;
bool m_b3DOpened = false;

static void _enable3DProcDrv(bool enable);
static bool _is3DEnabledProcDrv();
static void _enable3DQs3d(bool enable);
static bool _is3DEnabledQs3d();
static void _enable3DMi3d(bool enable);
static bool _is3DEnabledMi3d();

static const DevFunPair m_DevFunsList[] = {
		{"/proc/driver", _enable3DProcDrv, _is3DEnabledProcDrv},
		{"/dev/qs3dgrating", _enable3DQs3d, _is3DEnabledQs3d},
		{"/dev/mi3d_tn_ctrl", _enable3DMi3d, _is3DEnabledMi3d},
		//TODO, add more dev here
	};

static void _enable3DProcDrv(bool enable)
{
    fd = open(m_pDev, O_RDWR, 0);
    if (fd < 0) {
        ALOGE(TAG,"enablePanel3D: failed to open %s", m_pDev);
        return;
    }

    int rc = write(fd, enable ? "1" : "0", 1);
    if (rc != 1) {
        ALOGE(TAG,"enablePanel3D: failed to write %d", enable);
    }

    close(fd);
}

static bool _is3DEnabledProcDrv() {
    fd = open(m_pDev, O_RDWR, 0);
    if (fd < 0) {
        ALOGE(TAG,"isPanel3DEnabled: failed to open %s", m_pDev);
        return false;
    }

    char buf[5] = {'\0'};
    int rc = read(fd, buf, 5);
    if (rc != 5) {
        ALOGE(TAG,"isPanel3DEnabled: failed to read");
    }

    close(fd);
    return buf[0] == '1' ? true : false;
}

static void _enable3DQs3d(bool enable)
{
    fd = open(m_pDev, O_RDWR, 0);
    if (fd < 0) {
        ALOGE(TAG,"enablePanel3D: failed to open %s", m_pDev);
        return;
    }

    if (enable) {
        ioctl(fd, QSDEVICE_GRATING_IOCTL_ENABLE, 1);
        m_b3DOpened = true;
    }
    else  {
        ioctl(fd, QSDEVICE_GRATING_IOCTL_DISABLE, 1);
        m_b3DOpened = false;
    }

    close(fd);
}

static bool _is3DEnabledQs3d() {
    return m_b3DOpened;
}

static void _enable3DMi3d(bool enable)
{
    fd = open(m_pDev, O_RDWR, 0);
    if (fd < 0) {
        ALOGE(TAG,"enablePanel3D: failed to open %s", m_pDev);
        return;
    }

    if (enable) {
        ioctl(fd, MI3D_TN_CMD, MI3D_SET_VERTICAL_TN_ON);
        m_b3DOpened = true;
    }
    else  {
        ioctl(fd, MI3D_TN_CMD, MI3D_SET_TN_OFF);
        m_b3DOpened = false;
    }

    close(fd);
}

static bool _is3DEnabledMi3d() {
    fd = open(m_pDev, O_RDWR, 0);
    if (fd < 0) {
        ALOGE(TAG,"enablePanel3D: failed to open %s", m_pDev);
        return false;
    }

    int prev_status = ioctl(fd, MI3D_TN_CMD_STATUS, MI3D_TN_CMD_STATUS);

	bool status=false;
    if (prev_status < 0 || (prev_status == MI3D_OFF_STATUS) )
        status = false;
    else if (prev_status == MI3D_VERTICAL_ON_STATUS)
        status = true;

    close(fd);
    return status;
}

void enablePanel3D(JNIEnv *env, jobject obj, jboolean enable) {
	if (NULL != m_pEnable3DFunc)
        (*m_pEnable3DFunc)(enable);
	else
        ALOGE(TAG, "enablePanel3D: failed to write %s: %d", m_pDev, enable);
}

bool isPanel3DEnabled(JNIEnv *env, jobject obj) {
	bool res = false;

	if (NULL != m_pIs3DEnableFunc)
		res = (*m_pIs3DEnableFunc)();
	else
        ALOGE(TAG, "isPanel3DEnabled: failed to get function ptr");

    return res;
}


void panelParamSetJNI(JNIEnv *env, jobject obj, jboolean redFirst) {
    panelParamSet(redFirst);
}

void rasterParamSetJNI(JNIEnv *env, jobject obj, jstring dev,
                                jfloat cover, jfloat cot, jfloat offset, jboolean slope) {
    if (m_pDev == NULL) {
        m_pDev = env->GetStringUTFChars(dev, 0);
        ALOGI(TAG, "dev: %s", m_pDev);

        int i, n;
        n = sizeof(m_DevFunsList)/sizeof(DevFunPair);

        for (i=0; i<n; i++)
        {
            int res = strncmp(m_pDev, m_DevFunsList[i].pDevName, strlen(m_DevFunsList[i].pDevName));
            if ( 0 == res )
            {
                ALOGI(TAG, "match dev: %s @ m_DevFunsList[%d]", m_pDev, i);
                m_pEnable3DFunc = m_DevFunsList[i].pEnable3D;
                m_pIs3DEnableFunc = m_DevFunsList[i].pIs3DEnable;
            }
        }
    }

    rasterParamSet(cover, cot, offset, slope);
}

void templeteInitJNI(JNIEnv *env, jobject obj,
                        jbyteArray tmpl, jint w, jint h) {

    jbyte *pBuf;

    pBuf = env->GetByteArrayElements(tmpl, 0);
    templateCreate(pBuf, w, h);
    env->ReleaseByteArrayElements(tmpl, pBuf, 0);
}

static JNINativeMethod methods[] = {
    {"enablePanel3D", "(Z)V", (void*)enablePanel3D},
    {"isPanel3DEnabled", "()Z", (void*)isPanel3DEnabled},
    {"panelParamSet", "(Z)V", (void*)panelParamSetJNI},
    {"rasterParamSet", "(Ljava/lang/String;FFFZ)V", (void*)rasterParamSetJNI},
    {"templeteInit", "([BII)V", (void*)templeteInitJNI},
};

static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods,
    int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL)
        return JNI_FALSE;
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

int register_panel_control_native_methods(JNIEnv* env) {
  if (!registerNativeMethods(env, JNIREG_CLASS, methods, NELEM(methods))) {
    return JNI_FALSE;
  }
  return JNI_TRUE;
}


#ifdef __cplusplus
}
#endif
