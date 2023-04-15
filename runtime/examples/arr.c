#include "libeta.h"

void ETA(main_paai)(etastring arg[]) {
    etaint argc = etalength(arg);
    etaint a;
    for (a = 0; a < argc; a++) {
        ETA(println_pai)(arg[a]);
    }
}
