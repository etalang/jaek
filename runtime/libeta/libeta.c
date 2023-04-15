/**
 An implementation of the Eta standard runtime library.

*/

#include "libeta.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <sys/time.h>
#include <inttypes.h>

#include <gc.h>
#define WORDSIZE 8

/**
 Core runtime
*/

int GC_ready = 0;
void *ETA_BUILTIN(alloc)(etaint size) {
    if (!GC_ready) {
        /* This check unfortunately needs to be here, since
           GC_malloc() could be called from static initialization
           code written in Eta (in other words, we can't rely
           on main() to do the initialization)
        */
        GC_INIT();
        GC_set_all_interior_pointers(1);
        GC_ready = 1;
    }

	return (int64_t *) GC_malloc(size);
}

void registerFinalizer(void* object, Finalizer* fin) {
    GC_register_finalizer_ignore_self(object, fin, 0, 0, 0);
}

void ETA_BUILTIN(out_of_bounds)(void) {
    fprintf(stderr, "Array index out of bounds\n");
    abort();
}

void report_stack_error() {
  fprintf(stderr, "Stack pointer misaligned at call from user code\n");
  abort();
}


#define CHECK_STACK_ALIGNMENT                   \
  asm("pushq %rbp");                              \
  asm("andq $15, %rbp");                           \
  asm("testq %rbp, %rbp");                         \
  asm("jne report_stack_error");                 \
  asm("popq %rbp");


// Internal helper for making arrays
static void* mkArray(int bytes, int cells) {
    etaint *memory = ETA_BUILTIN(alloc)(bytes + sizeof(etaint));
    memory[0] = cells;
    return memory + 1;
}

// Helper: C string to ETA string
etastring mkString(const char* in) {
  //    CHECK_STACK_ALIGNMENT;
    int  c;
    int  len = strlen(in);
    etastring out = mkArray(len * sizeof(etaint), len);

    for (c = 0; c < len; ++c)
        out[c] = in[c];
    return out;
}

extern void ETA(main_paai)(etastring[]);

int main(int argc, char *argv[]) {

    // Create arguments array.
    etastring *args = mkArray(sizeof(etaint *) * argc, argc);
    int c;
    for (c = 0; c < argc; ++c)
        args[c] = mkString(argv[c]);

    // transfer to program's main
    ETA(main_paai)(args);
    return 0;
}


/**
 I/O module
*/

void ETA(print_pai) (etastring str) {
    int c;
    int len = str[-1];
    CHECK_STACK_ALIGNMENT;
    for (c = 0; c < len; ++c)
        printUcs4char(str[c], stdout);
}

void ETA(println_pai) (etastring str) {
    CHECK_STACK_ALIGNMENT;
    ETA(print_pai)(str);
    fputc('\n', stdout);
}

etastring ETA(readln_ai) (void) {
    int len = 0, max = 128;
    etastring res = (etastring)malloc((max+1) * sizeof(etaint));
    CHECK_STACK_ALIGNMENT;
    res = res + 1;
    res[-1] = len;

    for (;;) {
        int c = getchar();
        if (c == -1) break;
        if (c == '\n') break;
        if (len == max) {
            max *= 2;
            etastring nres = (etastring)malloc((max+1) * sizeof(etaint));
            memcpy(nres, &res[-1], (len + 1) * sizeof(etaint));
            free(res);
            res = nres;
        }
        res[len++] = c;
    }

    res[-1] = len;

    return res;
}

etaint ETA(getchar_i) (void) {
    // ### behavior on eof is unspecified
    CHECK_STACK_ALIGNMENT;
    return fgetc(stdin);
}

etaint ETA(eof_b) (void) {
    CHECK_STACK_ALIGNMENT;
    return feof(stdin) ? 1 : 0;
}

/**
 Conv module
*/

etaint ETA(parseInt_t2ibai) (etastring str) {
    // ### should this worry about overflow?
    int c;
    int len = str[-1];
    int neg = 0;
    etaint num = 0;
    etaint ok = 0;
    CHECK_STACK_ALIGNMENT;

    if (!len) {
        SET2NDRESULT(ok);
        return num;
    }

    if (str[0] == '-')
        neg = 1;

    for (c = neg; c < len; ++c) {
        if (str[c] >= '0' && str[c] <= '9') {
            num = 10*num + (str[c] - '0');
        } else {
            num = 0;
            SET2NDRESULT(ok);
            return num; // returning (0, false);
        }
    }

    ok = 1;
    if (neg) num = -num;
    SET2NDRESULT(ok);
    return num;
}

etastring ETA(unparseInt_aii) (etaint in) {
    char buf[32]; // more than enough to represent 64-bit numbers
    CHECK_STACK_ALIGNMENT;

#if defined(WINDOWS) || defined(WIN32)
    sprintf(buf, "%I64d", in);
#else
    sprintf(buf, "%ld", (long) in);
#endif

    return mkString(buf);
}

/**
 Assert module
*/
void ETA(assert_pb) (etaint cond) {
    CHECK_STACK_ALIGNMENT;
    if (!cond) {
        fprintf(stderr, "Assertion failed\n");
        abort();
    }
}

/**
 Timer module. This ab(uses) the long[] type for
 GC'd opaque objects.
*/
struct timeval* ETA(getTimestamp_ai) (void) {
    struct timeval* tStamp = mkArray(sizeof(struct timeval), 0);
    CHECK_STACK_ALIGNMENT;
    gettimeofday(tStamp, 0);
    return tStamp;
}

etaint ETA(timestampDifference_iaiai)(etaint* l, etaint* r) {
    struct timeval* lTime = (struct timeval*)l;
    struct timeval* rTime = (struct timeval*)r;

    etaint secondsDiff = lTime->tv_sec  - rTime->tv_sec;
    etaint usecDiff  = lTime->tv_usec - rTime->tv_usec; // micro is 1e6, so the range is fine.

    long combinedDiff   = usecDiff + secondsDiff * 1000000L;
    return (etaint)(combinedDiff/1000);
}

/* converting UTF-16 to UTF-8 */
#define kUTF8ByteSwapNotAChar    0xFFFE
#define kUTF8NotAChar            0xFFFF
#define kMaxUTF8FromUCS4         0x10FFFF

// 0xFFFD or U+FFFD is the "replacement character", e.g. the question mark symbol
#define kUTF8ReplacementChar     0xFFFD

void printUcs4char(const long int c, FILE *stream) {
    // We can optimize for the common case - e.g. a one byte character - only the overhead of one branch
    if (c <= 0x7F)  /* 0XXX XXXX one byte */
    {
        fputc(c, stream);
    }
    else if (c <= 0x7FF)  /* 110X XXXX  two bytes */
    {
        fputc(( 0xC0 | (c >> 6) ), stream);
        fputc(( 0x80 | (c & 0x3F) ), stream);
    }
    // start checking for weird chars - we are well above 16 bits now, so it doesn't matter how optimal this is
    else if ( c == kUTF8ByteSwapNotAChar || c == kUTF8NotAChar || c > kMaxUTF8FromUCS4)
    {
        printUcs4char(kUTF8ReplacementChar, stream);
    }
    else if (c <= 0xFFFF)  /* 1110 XXXX  three bytes */
    {
        fputc((0xE0 | (c >> 12)), stream);
        fputc((0x80 | ((c >> 6) & 0x3F)), stream);
        fputc((0x80 | (c & 0x3F)), stream);
    }
    else if (c <= kMaxUTF8FromUCS4)  /* 1111 0XXX  four bytes */
    {
        fputc((0xF0 | (c >> 18)), stream);
        fputc((0x80 | ((c >> 12) & 0x3F)), stream);
        fputc((0x80 | ((c >> 6) & 0x3F)), stream);
        fputc((0x80 | (c & 0x3F)), stream);
    }
}

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
// vim: sw=4 ts=4 et
