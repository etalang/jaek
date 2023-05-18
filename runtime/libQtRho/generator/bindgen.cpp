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
#include "bindgen.h"
#include <stdlib.h>
#include <algorithm>
#include <iostream>
#include "assert.h"
#include <cctype>
#include <cstdio>
#include "ASCIICType.h"

using namespace std;

static string strReplace(string where, string from, string to) {
    string res = where;
    size_t pos;
    while ((pos = res.find(from)) != string::npos) {
        res = res.substr(0, pos) + to + res.substr(pos + from.length());
    }
    return res;
}

BindGen::BindGen(istream* inStream, ostream* hStream,
                           ostream* cppStream, ostream* xiStream):
    Parser(inStream), out(hStream, cppStream, xiStream), inClass(false)
{}

void BindGen::generateCode()
{
    parse();
}

ostream& BindGen::hInd(int n)
{
    return out.ind(H, n + (inClass ? 4 : 0));
}

ostream& BindGen::cppInd(int n)
{
    return out.ind(Cpp, n);
}

ostream& BindGen::xiInd(int n)
{
    return out.ind(Xi, n + (inClass ? 4 : 0));
}

static std::string vtType(const string& type)
{
    return "Xi" + type + "_vtable";
}

static::string xiType(const string& type)
{
    return "Xi" + type;
}

static string derivedType(const string& type)
{
    return type + "Derived";
}

static std::string szSym(const string& type)
{
    return "ETA(_size_" + type + ")";
}

static std::string vtSym(const string& type)
{
    return "ETA(_vt_" + type + ")";
}

static std::string inSym(const string& type)
{
    return "ETA(_init_" + type + ")";
}

static string escapeUnderscores(const string& in)
{
    string out;
    for (int i = 0; i < in.length(); ++i)
        if (in[i] == '_')
            out += "__";
        else
            out += in[i];
    return out;
}

void BindGen::handleBeginType(TypeKind k, const string& type,
                              const string& superType)
{
    curMethods.clear();
    curEnums.clear();
    curEnumPrefix.clear();

    if ((k == ValueKind || k == PeerlessKind) && !superType.empty())
        issueError("Value/Peerless types cannot be inheritted");

    // Forward decl vtable
    hInd() << "\n\nstruct " << vtType(type) << ";\n";

    // We open up the main type here, since this is where we want the escapes
    // to go to. Info for vtables will be collected in data structures
    // during method action routines
    hInd() << "struct " << xiType(type) << ": public " <<
              (superType.empty() ? std::string("Xiobj") : xiType(superType))
              << "\n";
    hInd() << "{\n";
    hInd(4) << vtType(type) << "* vtable();\n";
    hInd(4) << xiType(type) << "();\n";

    if (k == ValueKind || k == PeerlessKind) {
        // For value types, we can provide the value field here,
        // as well as impl(). Since there is no subclassing,
        // the invokeDtor hook is generated immediately, too

        string valType = (k == ValueKind ? type : string("DummyPeer"));

        hInd(4) << valType << " value;\n";
        hInd(4) << valType << "* impl() {\n";
        hInd(8) << "if (!initialized) \n";
        hInd(12) << "new (this) " << xiType(type) << "();\n";
        hInd(8) << "return &value;\n";
        hInd(4) << "}\n\n";
        hInd(4) << "static void invokeDtor(void* mePtr, void*) {\n";
        hInd(8) << xiType(type) << "* me = static_cast<"
                << xiType(type) << "*>(mePtr);\n";
        hInd(8) << "if (me->initialized)\n"; // don't cleanup if was never created!
        hInd(12) << "me->~" << xiType(type) << "();\n";
        hInd(4) << "}\n\n";
    } else {
        // For pointers, we create an impl method that uses the combination
        // of their getImpl() hook, as well as a static __classOp
        // helper that placement news them and is dispatched to via the vtable.
        // invokeDtor goes via this dispatch hook as well.
        hInd(4) << type << "* impl();\n";
        hInd(4) << "static void invokeDtor(void* mePtr, void*);\n";

        cppInd() << type << "* " << xiType(type) << "::impl()\n";
        cppInd() << "{\n";
        cppInd(4) << "if (!initialized)\n";
        cppInd(8) << "static_cast<" << vtType(type) << "*>(vptr)"
                 << "->__classOp(CallCtor, this);\n";
        cppInd(4) << "return getImpl();\n";
        cppInd() << "}\n\n";

        cppInd() << "void " << xiType(type) << "::invokeDtor(void* mePtr, void*)\n";
        cppInd() << "{\n";
        cppInd(4) << xiType(type) << "* me = static_cast<"
                  << xiType(type) << "*>(mePtr);\n";
        cppInd(4) << "if (me->initialized)\n"; // don't cleanup if was never created!
        cppInd(8) << "me->vtable()->__classOp(CallDtor, me);\n";
        cppInd() << "}\n\n";

        hInd(4) << "static void __classOp(ClassOp op, void* memory);\n";
        cppInd() << "void " << xiType(type) << "::__classOp(ClassOp op, void* memory)\n";
        cppInd() << "{\n";
        cppInd(4) << xiType(type) << "* me = static_cast<"
                  << xiType(type) << "*>(memory);\n";
        cppInd(4) << "if (op == CallCtor) {\n";
        cppInd(8) << "new (memory) " << xiType(type) << "();\n";
        cppInd(8) << "me->createImpl();\n";
        cppInd(4) << "} else {\n";
        cppInd(8) << "me->~" << xiType(type) << "();\n";
        cppInd(4) << "}\n";
        cppInd() << "}\n\n";

        // Also add a typedef to make typechecking of toXi impls a bit easier
        hInd(4) << "typedef " << type << " wrappedType;\n";
    }

    cppInd() << xiType(type) << "::" << xiType(type) << "()\n{\n";
    if (superType.empty())
        cppInd(4) << "if (!vptr)\n"; // don't override Xi one
    else
        cppInd(4) << "if (vptr == &" << vtSym(superType) << ")\n";
    cppInd(8) << "vptr = &" << vtSym(type) << ";\n}\n\n";

    // And now Xi info...
    xiInd() << "record " << type;

    xiInd() << " {\n";

    inClass = true;
    kind    = k;
    this->type      = type;
    this->superType = superType;
    superClasses[type] = superType;
}

void BindGen::handleEndType()
{
    inClass = false;

    // Close class
    xiInd() << "}\n\n";
    hInd() << "};\n\n";

    // Output enum constants. Since we abuse handleMethodImpl,
    // we need to backup curMethods. Yes, it's ugly, but I don't
    // want extra work for this.
    std::vector<Method> oldCurMethods = curMethods;
    for (int e = 0; e < curEnums.size(); ++e) {
        Method m;
        m.name    = curEnums[e];
        m.retType = new ObjType(type);
        // no params.
        handleMethodImpl(m, "static " + xiType(type) + "* c = "
                                "gcNew<" + xiType(type) + ">();\n"
                            "c->value = " + curEnumPrefix + m.name + ";\n"
                            "return c;\n", -1);
    }
    curMethods = oldCurMethods;

    vector<const Method*> allMethods;
    string parentType = superType;

    for (const Method& m : curMethods)
        allMethods.push_back(&m);

    while (!parentType.empty()) {
        const auto moreMethods = classMethods.find(parentType);

        if (moreMethods != classMethods.end()) {
            for (const Method& m : moreMethods->second) {
                if (std::find_if(allMethods.cbegin(), allMethods.cend(),
                            [&m](const Method* method) {
                                return method->name == m.name;
                            })
                        == allMethods.cend()) {
                    allMethods.push_back(&m);
                }
            }
        }

        outputUpcast(type, parentType);
        parentType = superClasses[parentType];
    }

    for (const Method* m : allMethods)
        outputMethodWrapper(*m, type);

    xiInd() << '\n';
    hInd() << '\n';

    outputDerivedType(type);

    xiInd() << '\n';
    hInd() << '\n';

    for (const Method* m : allMethods)
        outputOverride(*m, type);

    xiInd() << '\n';
    hInd() << '\n';

    outputAlloc(type);

    xiInd() << '\n';
    hInd() << '\n';

    if (kind == ValueKind) {
        // For value types, we can autogen toXi, fromXi... though we don't
        hInd() << "inline " << xiType(type) << "* toXi(const " << type << "& orig) {\n";
        // Handle dispatch to enum constants.
        for (int ei = 0; ei < curEnums.size(); ++ei) {
            string e = curEnums[ei];
            hInd(4) << "if (orig == " << curEnumPrefix << e << ")\n";
            hInd(8) << "return ETA(" << e << "_r"
                    << type.length() << escapeUnderscores(type) <<")();\n";
        }

        hInd(4) << xiType(type) << "* r = gcNew<" << xiType(type) << ">();\n";
        hInd(4) << "r->value = orig;\n";
        hInd(4) << "return r;\n";
        hInd()  << "}\n\n";

        hInd() << "inline " << type << " fromXi(" << xiType(type) << "* wrapped) {\n";
        hInd(4) << "return *wrapped->impl();\n";
        hInd() << "}\n\n";
    } else if (kind == PointerKind) {
        // For pointer types, we can only autogen fromXi....
        hInd() << "inline " << type << "* fromXi(" << xiType(type) << "* wrapped) {\n";
        hInd(4) << "return wrapped ? wrapped->impl() : 0;\n";
        hInd() << "}\n\n";
    }

    // peerless types don't need either, since we never go to C++ in the first place

    // Output vtable type decl based on collected stuff
    hInd() << "struct " << vtType(type) << ": public " <<
        (superType.empty() ? std::string("Xivtable") : vtType(superType)) << "\n";

    hInd() << "{\n";

    // We use the reserved slot for only one thing:
    // to get dynamic dispatch of automatic initialization.
    // (so when a pointer class doesn't override a method, its initialization
    //  still happens right)
    if (kind == PointerKind && superType.empty())
        hInd(4) << "void (*__classOp)(ClassOp, void*);\n";
    else
        hInd(4) << "void* reserved;\n";

    for (int mi = 0; mi < curMethods.size(); ++mi) {
        const Method& m = curMethods[mi];

        ostream& h = hInd(4);
        h << m.retType->cType() << " (*" << m.name << ")(";
        h << xiType(type) << "*";
        for (int pi = 0; pi < m.params.size(); ++pi) {
            h << ", ";
            h << m.params[pi].type->cType();
        }
        h << ");\n";
    }

    hInd() << "};\n";

    hInd() << "\n";
    hInd() << "inline " << vtType(type) << "* " << xiType(type) <<
              "::vtable() {\n";
    hInd(4) << "return static_cast<" << vtType(type) << "*>(vptr);\n";
    hInd() << "}\n\n";

    // Declare ABI type info variables & initializer
    hInd() << "extern " << vtType(type) << " " << vtSym(type) << ";\n";
    hInd() << "extern int " << szSym(type) << ";\n";
    hInd() << "ETA_EXPORT void " << inSym(type) << "() __attribute__((constructor));\n";

    hInd() << "\n\n";

    // And define them...
    cppInd() << vtType(type) << " " << vtSym(type) << ";\n";
    cppInd() << "int " << szSym(type) << ";\n";
    cppInd() << "void " << inSym(type) << "()\n";
    cppInd() << "{\n";
    cppInd(4) << "if (" << szSym(type) << ") return;\n";
    // init of superclass not strictly needed, but done for consistency
    if (!superType.empty())
        cppInd(4) << inSym(superType) << "();\n";
    cppInd(4) << szSym(type) << " = sizeof(" << xiType(type) << ");\n";


    classMethods[type] = curMethods;

    // Fill in the vtable entries including those of parent classes.
    string curType = type;
    while (!curType.empty()) {
        const std::vector<Method>& methods = classMethods[curType];
        for (int mi = 0; mi < methods.size(); ++mi) {
            string name = methods[mi].name;

            cppInd(4) << vtSym(type) << "." << name << " = "
                      << xiType(type) << "::" << name << ";\n";
        }

        curType = superClasses[curType];
    }

    // if need be, fill in the init hook as well.
    if (kind == PointerKind)
        cppInd(4) << vtSym(type)  << ".__classOp = "
                  << xiType(type) << "::__classOp;\n";
    cppInd() << "}\n\n";
}

void BindGen::outputEscape(CodeStream stream, const string& code, int lineNum)
{
    int bi = inClass ? 4 : 0;
    out.printCode(out(stream), bi, code, lineNum);
}

void BindGen::handleEscapeXi (const string& code, int lineNum)
{
    outputEscape(Xi, code, lineNum);
}

void BindGen::handleEscapeH  (const string& code, int lineNum)
{
    outputEscape(H, code, lineNum);
}

void BindGen::handleEscapeCpp(const string& code, int lineNum)
{
    outputEscape(Cpp, code, lineNum);
}

void BindGen::handleComment(const string& code)
{
    if (!inClass) {
        out(Xi) << "\n";
        out.printCode(out(Xi), 0, code, 0, "// ");
    }
}

static string dName(const std::string& name) {
    return name + "__decode";
}

bool BindGen::isOverride(const string& method)
{
    return declaredIn(method) != type;
}

string BindGen::declaredIn(const string& method)
{
    string curType = type;
    while (!curType.empty()) {
        const std::vector<Method>& methods = classMethods[curType];
        for (int mi = 0; mi < methods.size(); ++mi) {
            if (methods[mi].name == method)
                return curType;
        }

        curType = superClasses[curType];
    }

    if (curType.empty())
        return type;
    else
        return curType;
}

void BindGen::handleMethodDefault(const Method& m)
{
    if (!inClass)
        issueError("Default methods must be inside classes");

    if (!isOverride(m.name))
        curMethods.push_back(m);
    outputMethodDecl(m.name, m.retType, m.params);

    // Demarshal all the parameters.
    cppInd(4) << xiType(type) << "* __this = static_cast<" << xiType(type)
              << "*>(__thisPtr);\n";
    for (int pi = 0; pi < m.params.size(); ++pi) {
        const Param& p = m.params[pi];
        cppInd(4) << p.type->wrappedType() << " " << dName(p.name)
                  << " = fromXi(" << p.name << ");\n";
    }

    ostream& cpp = cppInd(4);

    // Call the method, and marshal the result if need be
    if (!m.retType->isVoid())
        cpp << "return toXi(";

    cpp << "__this->impl()->" << m.name << "(";
    for (int pi = 0; pi < m.params.size(); ++pi) {
        cpp << dName(m.params[pi].name);
        if (pi != (m.params.size() - 1))
            cpp << ", ";
    }
    cpp << ")";

    if (!m.retType->isVoid())
        cpp << ")"; // close toXi call

    cpp << ";\n";

    cppInd() << "}\n\n";
}

void BindGen::handleMethodImpl(const Method& m,
                               const string& code, int lineNum)
{
    if (!isOverride(m.name))
        curMethods.push_back(m);
    outputMethodDecl(m.name, m.retType, m.params);

    if (!inClass)
        xiInd() << "\n";

    if (inClass) {
        cppInd(4) << xiType(type) << "* __this = static_cast<" << xiType(type)
                << "*>(__thisPtr);\n";
    }

    out.printCode(cppInd(), 4, code, lineNum);

    cppInd() << "}\n\n";
}

void BindGen::handleEnumConst(const string& name)
{
    curEnums.push_back(name);
}

void BindGen::handleEnumPrefix(const string& pfix)
{
    curEnumPrefix = pfix;
}

void BindGen::outputMethodWrapper(const Method& m, const string& thisType)
{
    if (!m.comment.empty())
        out.printCode(out(Xi), 0, m.comment, 0, "// ");

    ObjType thisPtrType(thisType);

    Param thisPtr;
    thisPtr.type = &thisPtrType;
    thisPtr.name = "self";

    vector<Param> params = { thisPtr };
    params.insert(params.end(), m.params.begin(), m.params.end());

    outputMethodDecl(thisType + '_' + m.name, m.retType, params);

    ostream& cpp = cppInd(4);

    cpp << "return self->vtable()->" << m.name << "(self";
    for (int pi = 0; pi < m.params.size(); ++pi) {
        cpp << ", " << m.params[pi].name;
    }
    cpp << ");\n";

    cppInd() << "}\n\n";
}

void BindGen::outputUpcast(const string& child, const string& parent)
{
    ObjType thisPtrType(child);
    ObjType retType(parent);

    Param thisPtr;
    thisPtr.type = &thisPtrType;
    thisPtr.name = "self";

    outputMethodDecl(child + "_to_" + parent, &retType, { thisPtr });

    cppInd(4) << "return self;\n";
    cppInd() << "}\n\n";
}

void BindGen::outputDerivedType(const string& parent)
{
    // Derived type

    string derived = derivedType(parent);

    hInd() << "using " << xiType(derived) << " = " << vtType(parent) << ";\n\n";
    xiInd() << "record " << derived << " {}\n\n";

    // Extend

    ObjType derivedPtrType(derived);

    outputMethodDecl(parent + "_extend", &derivedPtrType, {});

    cppInd(4) << "return copy_vtable(&" << vtSym(parent) << ");\n";
    cppInd() << "}\n\n";

    // Downcast

    ObjType thisPtrType(parent);
    VoidType retType;

    Param thisPtr;
    thisPtr.type = &thisPtrType;
    thisPtr.name = "self";
    Param derivedPtr;
    derivedPtr.type = &derivedPtrType;
    derivedPtr.name = "derived";

    outputMethodDecl(parent + "_downcast", &retType, { thisPtr, derivedPtr });

    cppInd(4) << "if (self->vptr != &" << vtSym(parent) << ")\n";
    cppInd(8) << "downcastError();\n";
    cppInd(4) << "self->vptr = derived;\n";
    cppInd() << "}\n\n";
}

void BindGen::outputOverride(const Method& m, const string& parent)
{
    ObjType parentType(parent);
    ObjType derivedPtrType(derivedType(parent));
    IntType charType;
    ArrayType nameType(&charType);
    VoidType retType;

    Param derivedPtr;
    derivedPtr.type = &derivedPtrType;
    derivedPtr.name = "derived";
    Param methodName;
    methodName.type = &nameType;
    methodName.name = "name";

    outputMethodDecl(parent + "_override_" + m.name, &retType, { derivedPtr, methodName });

    string typeEncoding = m.retType->typeCode() + parentType.typeCode();

    for (const Param& param : m.params)
        typeEncoding += param.type->typeCode();

    cppInd(4) << "etastring name_str = static_cast<etastring>(name);\n";
    cppInd(4) << "void* impl = lookupRhoFunction(name_str, \"" << typeEncoding << "\");\n";
    cppInd(4) << "derived->" << m.name
              << " = reinterpret_cast<decltype(derived->" << m.name << ")>(impl);\n";
    cppInd() << "}\n\n";
}

void BindGen::outputAlloc(const string& thisType)
{
    string lower = thisType;
    std::transform(lower.begin(), lower.end(), lower.begin(),
            [](char c) {
                return std::tolower(c);
            });

    ObjType retType(thisType);

    outputMethodDecl(lower + "_alloc", &retType, {});

    cppInd(4) << retType.cType() << " obj = gcCalloc<" << xiType(thisType) << ">();\n";
    cppInd(4) << "obj->vptr = &" << vtSym(thisType) << ";\n";
    cppInd(4) << "return obj;\n";
    cppInd() << "}\n\n";
}

void BindGen::outputMethodDecl(const string& origName, Type* retType,
                               const vector<Param>& params)
{
    string name   = origName;
    string xiName = origName;
    ostream& h = hInd();
    if (inClass) {
        h << "static ";
    } else {
        h << "ETA_EXPORT ";
        // Replace name with a mangled version...
        name = escapeUnderscores(name);
        name += "_";
        name += retType->typeCode();

        for (int pi = 0; pi < params.size(); ++pi)
            name += params[pi].type->typeCode();

        name = "ETA(" + name + ")";
    }
    h << retType->cType() << " ";
    outputMethodSig(h, name, params);
    h << ";\n";

    ostream& cpp = cppInd();
    cpp << retType->cType() << " ";
    if (inClass)
        cpp << xiType(type) << "::";
    outputMethodSig(cpp, name, params);
    cpp << "\n{\n";

    // Now the Xi decl...
    if (!inClass) {
        ostream& xi = xiInd();
        xi << xiName << "(";
        for (int pi = 0; pi < params.size(); ++pi) {
            xi << params[pi].name << params[pi].type->xiType();
            if (pi != (params.size() - 1))
                xi << ", ";
        }
        xi << ")" << retType->xiType() << "\n";
    }
}

void BindGen::outputMethodSig(ostream& out, const string& name,
                               const vector<Param>& params)
{
    out << name << "(";

    if (inClass) {
        out << xiType(declaredIn(name)) << "* __thisPtr";
        if (params.size())
            out << ", ";
    }

    for (int p = 0; p < params.size(); ++p) {
        out << params[p].type->cType() << " "
            << params[p].name;
        if (p != (params.size() - 1))
            out << ", ";
    }

    out << ")";
}


// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
