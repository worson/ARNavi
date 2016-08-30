LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
OPENCV_LIB_TYPE:=SHARED#STATIC #
include /Developer/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := HaloECP.cpp algorithm.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += /Developer/OpenCV-android-sdk/sdk/native/jni/include
LOCAL_LDLIBS     += -llog -ldl -LlibPath

LOCAL_MODULE     := HaloECP

include $(BUILD_SHARED_LIBRARY)
