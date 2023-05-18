/*
 * Generator utility to help make QtXi
 *
 * This code is based on IceMaker:
 *
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
#include "parser.h"

#include <cstdlib>
#include <sstream>


/**
 This is a fairly straightforward affair. It's written in a
 recursive descent style, but the language is actually regular.

 The error recovert is even simpler: we just exit. That's it.
*/

Parser::Parser(istream* stream): tokenLoaded(false), hadError(false), lexer(new Lexer(stream))
{}

string Parser::matchIdentifier()
{
    Lexer::Token tok = getNext();
    if (tok.type == Lexer::Ident)
        return tok.value;
    issueError("Expected identifier, got:" + tok.toString(lexer));
    return "";
}

void Parser::matchCode(std::string* strOut, int* lineOut)
{
    Lexer::Token tok = getNext();
    if (tok.type == Lexer::Code) {
        *lineOut = tok.lineNum;
        *strOut  = tok.value;
        return;
    }
    issueError("Expected code, got:" + tok.toString(lexer));
}

int Parser::matchNumber()
{
    Lexer::Token tok = getNext();
    if (tok.type == Lexer::Number)
        return std::atol(tok.value.c_str());
    issueError("Expected number, got:" + tok.toString(lexer));
    return 0;
}

void Parser::match(Lexer::TokenType t)
{
    Lexer::Token tok = getNext();
    if (tok.type != t)
        issueError("Expected " + Lexer::Token(t).toString(lexer) + " got:" + tok.toString(lexer));
}

bool Parser::check(Lexer::TokenType t)
{
    if (peekNext().type == t) {
        getNext(); // tasty!
        return true;
    } else {
        return false;
    }
}

unsigned Parser::matchFlags(const Flag* permittedFlags)
{
    unsigned flagsVal = 0;
    if (check(Lexer::LBracket)) {
        while (true) {
            std::string flag;

            // We permit keywords to double as flags.
            if (peekNext().isKeyword())
                flag = getNext().toString(lexer);
            else
                flag = matchIdentifier();

            // Lookup the name.
            bool found = false;
            for (int pos = 0; permittedFlags[pos].name; ++pos) {
                if (flag == std::string(permittedFlags[pos].name)) {
                    found = true;
                    flagsVal |= permittedFlags[pos].value;
                }
            }

            if (!found)
                issueError("invalid flag:" + flag);

            // Done or more?
            if (check(Lexer::RBracket))
                return flagsVal;
            else
                match(Lexer::Comma);
        }
    }
    return 0;
}

void Parser::issueError(const string& msg)
{
    std::cerr << "Parse error:" << msg << " at about line:" << lexer->lineNumber() << "\n";
    std::exit(-1);
}

Lexer::Token Parser::peekNext()
{
    if (!tokenLoaded) {
        nextToken   = lexer->nextToken();
        tokenLoaded = true;
    }

    return nextToken;
}

Lexer::Token Parser::getNext()
{
    if (tokenLoaded) {
        tokenLoaded = false;
        return nextToken;
    }

    return lexer->nextToken();
}

void Parser::parse()
{
    Lexer::Token tok = peekNext();

    // The types are first..
    while (tok.type != Lexer::EndOfFile) {
        if (tok.type == Lexer::Value || tok.type == Lexer::Pointer ||
                                        tok.type == Lexer::NoPeer)
            parseTypeDecl();
        else
            parseMethod();
        tok = peekNext();
    }

    match(Lexer::EndOfFile);
}

void Parser::parseTypeDecl() {
    TypeKind kind;
    if (check(Lexer::Value)) {
        kind = ValueKind;
    } else if (check(Lexer::NoPeer)) {
        kind = PeerlessKind;
    } else {
        match(Lexer::Pointer);
        kind = PointerKind;
    }
    // [value | ptr | nopeer ] name [: parent ] { methods }

    std::string name = matchIdentifier();
    std::string super;

    if (peekNext().type == Lexer::Colon) {
        getNext();
        super = matchIdentifier();
    }

    handleBeginType(kind, name, super);

    match(Lexer::LBrace);
    lastComment.clear();

    while (peekNext().type != Lexer::RBrace)
        parseMethod();

    handleEndType();

    match(Lexer::RBrace);
    lastComment.clear();
}

void Parser::parseMethod() {
    std::string code;
    int         line;

    // This handles both true method declarations and escapes
    if (check(Lexer::Xi)) {
        matchCode(&code, &line);
        handleEscapeXi(code, line);
        return;
    }

    if (check(Lexer::BindH)) {
        matchCode(&code, &line);
        handleEscapeH(code, line);
        return;
    }

    if (check(Lexer::BindCpp)) {
        matchCode(&code, &line);
        handleEscapeCpp(code, line);
        return;
    }

    if (peekNext().type == Lexer::Comment) {
        lastComment = getNext().value;
        handleComment(lastComment);
        return;
    }

    if (check(Lexer::EnumConst)) {
        string name = matchIdentifier();
        match(Lexer::SemiColon);

        handleEnumConst(name);
        return;
    }

    if (check(Lexer::EnumPrefix)) {
        matchCode(&code, &line);
        handleEnumPrefix(code);
        return;
    }

    // Now the method. This uses C++'ish syntax, except the type names
    // are xi names (and void for return)
    Method m;
    m.retType = parseType();
    m.name = matchIdentifier();
    m.comment = lastComment;

    match(Lexer::LParen);
    while (peekNext().type != Lexer::RParen) {
        Param p;
        p.type = parseType();

        if (peekNext().type == Lexer::Ident) {
            p.name = matchIdentifier();
        } else {
            std::ostringstream o;
            o << "param" << m.params.size();
            p.name = o.str();
        }

        m.params.push_back(p);

        if (peekNext().type != Lexer::RParen)
            match(Lexer::Comma);
    }

    match(Lexer::RParen);

    // Do we have an impl or not?
    if (check(Lexer::SemiColon)) {
        handleMethodDefault(m);
    } else {
        matchCode(&code, &line);
        handleMethodImpl(m, code, line);
    }

    lastComment.clear();
}

Type* Parser::parseType() {
    std::string typeName = matchIdentifier();
    Type* type = 0;

    if (typeName == "int")
        type = new IntType();
    else if (typeName == "bool")
        type = new BoolType();
    else if (typeName == "void")
        type = new VoidType();
    else
        type = new ObjType(typeName);

    // Parse any arrays and pointers, if applicable.
    // note that we only support one, and only pointers to ObjTypes

    // neither applies to void (as we can't bind void*)
    if (type->isVoid())
        return type;

    if (peekNext().type == Lexer::LBracket) {
        while (peekNext().type == Lexer::LBracket) {
            match(Lexer::LBracket);
            match(Lexer::RBracket);

            type = new ArrayType(type);
        }
        return type;
    }

    if (check(Lexer::Star)) {
        // ### this isn't quite right as it doesn't check for being
        // pointer type and not value type
        if (!type->isObject())
            issueError("Can only use pointers to wrapped pointer-like types");

        type = new PointerType(static_cast<ObjType*>(type));
    }

    return type;
}

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
