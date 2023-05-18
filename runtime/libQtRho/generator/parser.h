/*
 *  A utilitity for building various tables and specializations for the
 *  KJS Frostbyte bytecode
 *
 *  Copyright (C) 2007, 2008 Maks Orlovich (maksim@kde.org)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this library; see the file COPYING.LIB.  If not, write to
 *  the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA 02110-1301, USA.
 *
 */

#ifndef PARSER_H
#define PARSER_H

#include "lexer.h"
#include "types.h"

#include <vector>
using std::vector;

struct Param {
    Type*       type;
    std::string name;
};

struct Method {
    string name;
    Type*  retType;
    vector<Param> params;
    string comment;
};

class Parser
{
public:
    Parser(istream* stream);

    void parse();

    enum TypeKind {
        ValueKind,
        PeerlessKind, // no C++ peer, use a dummy one.
        PointerKind
    };
private:
    // Interface to action routines

    // superType is empty if none
    virtual void handleBeginType(TypeKind k, const string& type,
                                 const string& superType) = 0;
    virtual void handleEndType() = 0;

    // All of these may be called both inside a type and at top-level.
    virtual void handleEscapeXi (const string& code, int lineNum) = 0;
    virtual void handleEscapeH  (const string& code, int lineNum) = 0;
    virtual void handleEscapeCpp(const string& code, int lineNum) = 0;
    virtual void handleComment  (const string& text) = 0;

    virtual void handleMethodDefault(const Method& m) = 0;
    virtual void handleMethodImpl(const Method& m,
                                  const string& code, int lineNum) = 0;
    virtual void handleEnumConst(const string& name) = 0;
    virtual void handleEnumPrefix(const string& pfix) = 0;

    struct Flag {
        const char* name;
        unsigned    value;
    };

    void parseTypeDecl();
    void parseMethod();
    void parseEscape();
    Type* parseType();

    // Matches flags specified as a zero-terminated pair array above,
    // and returns their or. In syntax, they look like
    // [flag1, flag2, flag3] and are optional.
    unsigned matchFlags(const Flag* permittedFlags);

    // These unconditionally parse items of given type.
    string matchIdentifier();
    void   matchCode(std::string* stringOut, int* lineOut);
    int    matchNumber();
    void   match(Lexer::TokenType t);

    // These conditionally consume given token if it's there,
    // and return true if so.
    bool check(Lexer::TokenType t);

    bool tokenLoaded;
    Lexer::Token nextToken;

    bool   hadError;
    std::string lastComment;

    Lexer* lexer;

    Lexer::Token peekNext();
    Lexer::Token getNext();
protected:
    void issueError(const string& msg);
};

#endif
// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;

