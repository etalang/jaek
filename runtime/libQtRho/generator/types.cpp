#include "types.h"

#include <sstream>
#include <cassert>

using std::string;


string IntType::typeCode() const {
    return "i";
}

string IntType::cType() const {
    return "int";
}

string IntType::xiType() const {
    return ": int";
}

std::string IntType::wrappedType() const
{
    return "int";
}

string BoolType::typeCode() const {
    return "b";
}

string BoolType::cType() const {
    return "bool";
}

string BoolType::xiType() const {
    return ": bool";
}

std::string BoolType::wrappedType() const
{
    return "bool";
}

string VoidType::typeCode() const {
    return "p";
}

string VoidType::cType() const {
    return "void";
}

string VoidType::xiType() const {
    return "";
}

std::string VoidType::wrappedType() const
{
    assert(false);
}

string ObjType::typeCode() const {
    // ### doesn't escape _
    std::ostringstream o;
    o << "r";
    o << name.length();
    o << name;
    return o.str();
}

string ObjType::cType() const {
    return "Xi" + name + "*";
}

string ObjType::xiType() const {
    return ": " + name;
}

std::string ObjType::wrappedType() const
{
    return name;
}

string ArrayType::typeCode() const {
    return "a" + memberType->typeCode();
}

string ArrayType::cType() const {
    // We make arrays complete opaque at C level;
    // requiring custom implementations in all cases.
    return "void*";
}

string ArrayType::xiType() const {
    return memberType->xiType() + "[]";
}

std::string ArrayType::wrappedType() const
{
    return "void*";
}

// For pointer types, we assume they are used only to refer to
// wrapped types defined with 'pointer', not anything more generic.
string PointerType::typeCode() const {
    return baseType->typeCode();
}

string PointerType::cType() const {
    return baseType->cType();
}

string PointerType::xiType() const {
    return baseType->xiType();
}

std::string PointerType::wrappedType() const
{
    return baseType->wrappedType() + "*";
}
