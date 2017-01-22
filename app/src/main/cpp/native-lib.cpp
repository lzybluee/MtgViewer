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

LUALIB_API int android_print(lua_State *L) {
    const char *s = lua_tostring(L, -1);
    __android_log_print(ANDROID_LOG_ERROR, "LUA", "%s", s);
    stream << s << endl;
    return 0;
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushString(JNIEnv *env, jobject thiz, jstring key, jstring value)
{
    if (value)
        lua_pushstring(state, env->GetStringUTFChars(value, 0));
    else
        lua_pushnil(state);

    lua_setglobal(state, env->GetStringUTFChars(key, 0));
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushInteger(JNIEnv *env, jobject thiz, jstring key, jint value)
{
    lua_pushinteger(state, (int)value);
    lua_setglobal(state, env->GetStringUTFChars(key, 0));
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushBoolean(JNIEnv *env, jobject thiz, jstring key, jboolean value)
{
    lua_pushboolean(state, (bool)value);
    lua_setglobal(state, env->GetStringUTFChars(key, 0));
}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_luaPushFloat(JNIEnv *env, jobject thiz, jstring key, jfloat value)
{
    lua_pushnumber(state, (float)value);
    lua_setglobal(state, env->GetStringUTFChars(key, 0));
}

extern "C"
void luaPushStringArray(JNIEnv *env, jobject thiz, jstring key, jstring value) {

}

extern "C"
void Java_lu_cifer_mtgviewer_LuaScript_initLua(JNIEnv *env, jobject thiz) {
    state = luaL_newstate();
    luaL_openlibs(state);
    lua_pushcfunction(state, android_print);
    lua_setglobal(state, "print");
}

extern "C"
jstring Java_lu_cifer_mtgviewer_LuaScript_runScript(JNIEnv *env, jobject thiz, jstring code) {
    stream.str("");

    if (luaL_loadstring(state, env->GetStringUTFChars(code, 0)) || lua_pcall(state, 0, 0, 0))
    {
        const char *e = lua_tostring(state, -1);
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Error : %s", e);
        stream << "Error: " << e << endl;
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "LUA", "Finished");
        stream << "Finished" << endl;
    }

    std::string hello = stream.str();
    return env->NewStringUTF(hello.c_str());
}
