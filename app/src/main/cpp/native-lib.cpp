#include <jni.h>
#include <android/log.h>
#include <string>
#include <iostream>
#include <sstream>

using namespace std;

extern "C" {
#include <lua.h>
#include <lauxlib.h>
#include <lualib.h>
}

static stringstream stream;
static lua_State *state;
static bool error;

LUALIB_API int android_print(lua_State *L) {
    const char *s = lua_tostring(L, -1);
    __android_log_print(ANDROID_LOG_ERROR, "LUA", "%s", s);
    stream << s << endl;
    return 0;
}

static void loadFile(const char* path) {
    FILE* file = fopen(path, "r");
    if(file) {
        fclose(file);
    } else {
        stream << "Warning: file not found. " << path << endl;
        return;
    }

    if (luaL_loadfile(state, path)) {
        error = true;
        const char *e = lua_tostring(state, -1);
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Error : %s %s", path, e);
        stream << "Error: " << path << " " << e << endl;
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Finished");
        stream << "Finished" << endl;
    }
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushString(JNIEnv *env, jobject thiz, jstring k, jstring v) {
    const char *key = env->GetStringUTFChars(k, 0);
    if (v) {
        const char *value = env->GetStringUTFChars(v, 0);
        lua_pushstring(state, value);
        env->ReleaseStringUTFChars(v, value);
    }
    else {
        lua_pushnil(state);
    }
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushInteger(JNIEnv *env, jobject thiz, jstring k, jint v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushinteger(state, (int)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushBoolean(JNIEnv *env, jobject thiz, jstring k, jboolean v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushboolean(state, (bool)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushFloat(JNIEnv *env, jobject thiz, jstring k, jfloat v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushnumber(state, (float)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushStringArray(JNIEnv *env, jobject thiz, jstring k, jobjectArray v) {
    const char *key = env->GetStringUTFChars(k, 0);
    lua_newtable(state);
    int count = env->GetArrayLength(v);
    for (int i = 0; i < count; i++) {
        lua_pushinteger(state, i + 1);
        jstring jstr = (jstring) (env->GetObjectArrayElement(v, i));
        const char* cstr = env->GetStringUTFChars(jstr, 0);
        lua_pushstring(state, cstr);
        env->ReleaseStringUTFChars(jstr, cstr);
        lua_settable(state, -3);
    }
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_initLua(JNIEnv *env, jobject thiz, jstring jfile) {
    state = luaL_newstate();
    luaL_openlibs(state);
    lua_pushcfunction(state, android_print);
    lua_setglobal(state, "print");

    const char *file = env->GetStringUTFChars(jfile, 0);
    loadFile(file);
    env->ReleaseStringUTFChars(jfile, file);
}

extern "C"
jint Java_lu_cifer_mtgviewer_LuaScript_getResult(JNIEnv *env, jobject thiz) {
    if (error)
        return 2;

    lua_getglobal(state, "result");
    bool result = (bool)lua_toboolean(state, -1);
    return result ? 1 : 0;
}

extern "C"
jstring Java_lu_cifer_mtgviewer_LuaScript_runScript(JNIEnv *env, jobject thiz, jstring jcode, jstring jfile) {
    const char *code = env->GetStringUTFChars(jcode, 0);
    const char *file = env->GetStringUTFChars(jfile, 0);

    stream.str("");

    loadFile(file);

    if (luaL_loadstring(state, code) || lua_pcall(state, 0, 0, 0))
    {
        error = true;
        const char *e = lua_tostring(state, -1);
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Error : %s", e);
        stream << "Error: " << e << endl;
    } else {
        error = false;
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Finished");
        stream << "Finished" << endl;
    }

    env->ReleaseStringUTFChars(jcode, code);
    env->ReleaseStringUTFChars(jfile, file);

    std::string ret = stream.str();
    return env->NewStringUTF(ret.c_str());
}
