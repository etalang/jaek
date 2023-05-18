#include "xiobj.h"

#include <cstdio>
#include <cstdlib>
#include <string>
#include <dlfcn.h>

#define STRINGIFY(x) STRINGIFY_HELPER(x)
#define STRINGIFY_HELPER(x) #x

void downcastError()
{
    std::fputs("Attempted to downcast an object of invalid type\n", stderr);
    std::exit(EXIT_FAILURE);
}

static void lookupError [[noreturn]] ()
{
    const char* const msg = dlerror();

    if (msg) {
        std::fputs(msg, stderr);
        std::fputc('\n', stderr);
    }

    std::exit(EXIT_FAILURE);
}

static void* lookup(const char* symbol)
{
    static void* handle;
    void* ptr;

    if (!handle && !(handle = dlopen(nullptr, RTLD_LAZY)))
        lookupError();

    if (!(ptr = dlsym(handle, symbol)))
        lookupError();

    return ptr;
}

static std::string mangledName(etastring name, const char* typeEncoding)
{
    const etaint nameLen = name[-1];
    std::string out = STRINGIFY(ETA());

    for (etaint i = 0; i < nameLen; i++) {
        if (name[i] == '_')
            out += "__";
        else
            out += static_cast<char>(name[i]);
    }

    out += '_';
    out += typeEncoding;

    return out;
}

void* lookupRhoFunction(etastring name, const char* typeEncoding)
{
    return lookup(mangledName(name, typeEncoding).c_str());
}
