/**
 Interface to the Eta runtime
*/

#include "libeta.h"

/**
 Test program
*/
etaint ETA(factorial)(etaint i) {
    if (i <= 1)
        return 1;
    else
        return i * ETA(factorial)(i-1);
}

etaint prompt[] = { 7, 'N', 'u', 'm', 'b', 'e', 'r', '?' };
etaint is[]   = { 5, '!', ' ', 'i', 's', ' '};

void ETA(main_paai)(etastring arg[]) {
    etastring input, output;

    while (!ETA(eof_b)()) {
        ETA(print_pai)(prompt + 1);
        input = ETA(readln_ai)();

        // Convert to integer
        etabool ok;
        etaint num = ETA(parseInt_t2ibai)(input);
        GET2NDRESULT(ok);

        if (ok) {
            etaint val = ETA(factorial)(num);

            output  = ETA(unparseInt_aii)(num);
            ETA(print_pai)(is + 1);
            output  = ETA(unparseInt_aii)(val);
            ETA(println_pai)(output);
        }
    }
}
