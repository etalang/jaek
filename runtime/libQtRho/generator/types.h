#ifndef TYPES_H
#define TYPES_H

#include <string>
#include <iostream>

using std::string;
using std::ostream;

class Type {
public:
    // mangling code for the type name
    virtual std::string typeCode() const = 0;

    // C type representation of the Xi type
    virtual std::string cType() const  = 0;

    // C++ type being wrapped.
    virtual std::string wrappedType() const = 0;

    // Xi type representation, including the colon
    virtual std::string xiType() const  = 0;

    virtual bool isVoid() const {
        return false;
    }

    virtual bool isObject() const {
        return false;
    }
};

class IntType: public Type {
public:
    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;
};

class BoolType: public Type {
public:
    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;
};

class VoidType: public Type {
public:
    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;

    virtual bool isVoid() const {
        return true;
    }
};

class ObjType: public Type {
public:
    ObjType(const std::string& name): name(name) {}

    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;

    virtual bool isObject() const {
        return true;
    }
private:
    std::string name;
};

class ArrayType: public Type {
public:
    ArrayType(Type* memberType): memberType(memberType) {}

    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;
private:
    Type* memberType;
};

class PointerType: public Type {
public:
    PointerType(ObjType* baseType): baseType(baseType) {}

    std::string typeCode() const;
    std::string cType() const;
    std::string xiType() const;
    std::string wrappedType() const;
private:
    ObjType* baseType;
};

#endif
