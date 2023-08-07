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

#ifndef BIND_GEN_H
#define BIND_GEN_H

#include "filetemplate.h"
#include "codeprinter.h"
#include "types.h"
#include "parser.h"

#include <map>
using namespace std;


class BindGen: public Parser
{
public:
    BindGen(istream* inStream, ostream* hStream, ostream* cppStream,
            ostream* xiStream);

    void generateCode();
private:
    virtual void handleBeginType(TypeKind k, const string& type,
                                 const string& superType);
    virtual void handleEndType();

    virtual void handleEscapeXi (const string& code, int lineNum);
    virtual void handleEscapeH  (const string& code, int lineNum);
    virtual void handleEscapeCpp(const string& code, int lineNum);
    virtual void handleComment  (const string& text);

    virtual void handleEnumConst(const string& name);
    virtual void handleEnumPrefix(const string& pfix);
    virtual void handleMethodDefault(const Method& m);
    virtual void handleMethodImpl(const Method& m,
                                  const string& code, int lineNum);

    void outputEscape(CodeStream stream, const string& code, int lineNum);

    void outputMethodWrapper(const Method& m, const string& thisType);
    void outputUpcast(const string& child, const string& parent);

    void outputDerivedType(const string& parent);
    void outputOverride(const Method& m, const string& parent);

    void outputAlloc(const string& thisType);

    // both declares in h and xi and opens in cpp
    void outputMethodDecl(const string& name, Type* retType,
                          const vector<Param>& params);

    // minus return type..
    void outputMethodSig(ostream& out, const string& name,
                         const vector<Param>& params);


    // indented versions of streams, adjusted for inClass as well.
    ostream& hInd(int n = 0);
    ostream& cppInd(int n = 0);
    ostream& xiInd(int n = 0);

    bool inClass;
    TypeKind    kind;
    std::string type; // current type, if inClass = true
    std::string superType;
    CodePrinter  out;
    std::vector<Method> curMethods; // methods in current class
    std::vector<string> curEnums;   // enum constants in current class
    std::string curEnumPrefix; // string to prepend to get values of
                               // enumerator constants.

    map<string, string> superClasses;
    map<string, vector<Method> > classMethods; // methods in previously
                                               // declared classes

    bool isOverride(const string& method);
    string declaredIn(const string& method);
};

#endif
// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
