#include <assert.h>
#include <stdlib.h>
#include "com_evistek_utils_evisutil.h"

/*
 * JNI Initialization
 */

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = JNI_ERR;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    assert(env != NULL);

    if (!register_panel_control_native_methods(env)) {
        return result;
    }

    if (!register_frame_process_native_methods(env)) {
        return result;
    }

    if (!register_shader_script_native_methods(env)) {
        return result;
    }

    result = JNI_VERSION_1_4;
    return result;
}
