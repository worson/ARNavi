//
// Created by 龙 on 2016/11/7.
//

#ifndef HUDLAUNCHER_JNIUTILS_H
#define HUDLAUNCHER_JNIUTILS_H

using namespace std;

/**
 * convert jstring to string
 */
string jstringToStr(JNIEnv* env, jstring jstr);

/**
 * convert jstring to char*
 */
char* jstringToCharArr(JNIEnv* env, jstring jstr);

#endif //HUDLAUNCHER_JNIUTILS_H
