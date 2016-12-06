
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE:=STATIC #SHARED
include C:\files\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_SRC_FILES  := HaloECP.cpp algorithm.cpp CrossRoad.cpp FileDataEnginePlaceholder.cpp LinkFileInfo.cpp MergeMapData.cpp NaviFile.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += C:\files\OpenCV-android-sdk\sdk\native\jni\include\
LOCAL_LDLIBS     := -llog -ldl -llibPath

LOCAL_MODULE     := HaloECP
LOCAL_PROGUARD_ENABLED:= disabled
include $(BUILD_SHARED_LIBRARY)
