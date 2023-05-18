/**
 Curses module.
*/

#include "libeta.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <sys/time.h>
#include <inttypes.h>
#include <curses.h>

#include <gc.h>
#define WORDSIZE 8

void ETA(initCurses_p)(void) {
    initscr();
    keypad(stdscr, TRUE);
}

void ETA(destroyCurses_p)(void) {
    delwin(stdscr);
    endwin();
}

void ETA(showCursor_pb)(etabool enabled) {
    curs_set(enabled ? 1 : 0);
}

etaint ETA(getWindowSize_t2ii)(void) {
    int x, y;
    etaint x_, y_;
    getmaxyx(stdscr, y, x);
    x_ = x; y_ = y;
    SET2NDRESULT(y_);
    return x_;
}

etaint ETA(getCursorPosition_t2ii)(void) {
    int x, y; etaint x_, y_;
    getyx(stdscr, y, x);
    x_ = x; y_ = y;
    SET2NDRESULT(y_);
    return x_;
}

void ETA(moveCursor_pii)(etaint x, etaint y) {
    move(y, x);
}

void ETA(echoKeys_pb)(etabool enable) {
    if (enable) {
        echo();
    } else {
        noecho();
    }
}

void ETA(clearWindow_p)(void) {
    clear();
}

void ETA(refreshWindow_p)(void) {
    refresh();
}

void ETA(inputTimeout_pi)(etaint delay) {
    timeout(delay);
}

etaint ETA(getInputChar_i)(void) {
    return getch();
}

etastring ETA(getInputString_aii)(etaint maxlength) {
    char str[maxlength + 1];
    getnstr(str, maxlength);
    return mkString(str);
}

void ETA(putChar_pi)(etaint c) {
    addch(c);
}

void ETA(putString_pai)(etastring str) {
    int c;
    int len = str[-1];
    char string[len];
    for (c = 0; c < len; ++c)
        string[c] = str[c];
    addnstr(string, len);
}

void ETA(deleteChar_p)(void) {
    delch(); 
}

etaint ETA(readChar_i)(void) {
    return inch();
}

void ETA(putCharAt_piii)(etaint c, etaint x, etaint y) {
    mvaddch(y, x, c);
}

void ETA(putStringAt_paiii)(etastring str, etaint x, etaint y) {
    int c;
    int len = str[-1];
    char string[len];
    for (c = 0; c < len; ++c)
        string[c] = str[c];
    mvaddnstr(y, x, string, len);
}

void ETA(deleteCharAt_pii)(etaint x, etaint y) {
    mvdelch(y, x);
}

etaint ETA(readCharAt_iii)(etaint x, etaint y) {
    return mvinch(y, x);
} 

etaint ETA(KEYUP_i)(void) { return KEY_UP; }
etaint ETA(KEYDOWN_i)(void) { return KEY_DOWN; }
etaint ETA(KEYLEFT_i)(void) { return KEY_LEFT; }
etaint ETA(KEYRIGHT_i)(void) { return KEY_RIGHT; }

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
// vim: sw=4 ts=4 et
