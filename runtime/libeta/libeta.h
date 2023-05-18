/**
 Some exports to help write Eta bindings
*/
#ifndef LIBETA_H
#define LIBETA_H

#include "multret.h"
#include <stdint.h>
#include <stdio.h>

typedef int64_t etaint;
typedef int64_t etabool;
typedef etaint *etastring;

#define etalength(a) *(etaint *)((a)-1)

#ifdef __cplusplus
#define ETA_EXPORT extern "C"
#else
#define ETA_EXPORT
#endif

#if !defined(__CYGWIN__) && !defined(__APPLE__)
#define ETA(x) _I ## x
#define ETA_BUILTIN(x) _eta_ ## x
#else
#define ETA(x) I ## x
#define ETA_BUILTIN(x) eta_ ## x
/* On Cygwin/Windows (and apparently OS X) the compiler adds _ itself to
   everything; so we don't need one of our own */
#endif

// Main allocation hook
ETA_EXPORT void * ETA_BUILTIN(alloc)(etaint);

// Registers a finalizer for a given object
typedef void Finalizer(void*, void*);
ETA_EXPORT void registerFinalizer(void*, Finalizer*);

extern etaint ETA(parseInt_t2ibai)(etastring);
extern etastring ETA(readln_ai)(void);
extern etastring ETA(unparseInt_aii)(etaint);
extern etabool ETA(eof_b)(void);
extern void ETA(println_pai)(etastring);
extern void ETA(print_pai)(etastring);

// Declare curses methods
extern void ETA(initCurses_p)(void);
extern void ETA(destroyCurses_p)(void);
extern void ETA(showCurser_pb)(etabool);
extern etaint ETA(getWindowSize_t2ii)(void);
extern etaint ETA(getCurserPosition_t2ii)(void);
extern void ETA(moveCurser_pii)(etaint, etaint);
extern void ETA(echoKeys_pb)(etabool);
extern void ETA(clearWindow_p)(void);
extern void ETA(refreshWindow_p)(void);
extern void ETA(inputTimeout_pi)(etaint);
extern etaint ETA(getInputChar_i)(void);
extern etastring ETA(getInputString_aii)(etaint);
extern void ETA(putChar_pi)(etaint);
extern void ETA(putString_pai)(etastring);
extern void ETA(deleteChar_p)(void);
extern etaint ETA(readChar_i)(void);
extern void ETA(putCharAt_piii)(etaint, etaint, etaint);
extern void ETA(putStringAt_paiii)(etastring, etaint, etaint);
extern void ETA(deleteCharAt_pii)(etaint, etaint);
extern etaint ETA(readCharAt_iii)(etaint, etaint);
extern etaint ETA(KEYUP_i)(void);
extern etaint ETA(KEYDOWN_i)(void);
extern etaint ETA(KEYLEFT_i)(void);
extern etaint ETA(KEYRIGHT_i)(void);

/** Helper functions **/

extern void printUcs4char(long int c, FILE *stream);
extern etastring mkString(const char* in);

#endif
// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
