#ifndef Xi_QOBJECT_HOLDER_H
#define Xi_QOBJECT_HOLDER_H

#include <cassert>
#include <QObject>
#include <QPointer>

#include <xiobj.h>
#include <gcpin.h>

// Since Qt methods will return QObjects and not the wrappers,
// we keep a table that lets us do reverse lookup.
Xiobj* xiqGetWrapper(QObject* object);
void   xiqPutWrapper(QObject* object, Xiobj* wrapper);
void   xiqForgetWrapper(QObject* object);

// This is a helper to manage pinning/unpinning/deletion of QObjects
// wrapped by Xi objects. The basic invariant is that QObjects
// with parents are managed by C++ and are pinned; those without
// parents are managed by the Xi GC.
//
// Because of pinning, this should be included by value in the
// wrapper object
template<typename T> class XiQObjectHolder
{
private:
    QPointer<T> handle; // the use of QPointer here isn't needed for
                        // operation, but is rather a precaution
                        // to make crashes more predictable in case of
                        // mistakes

    GCPin pin;
public:
    XiQObjectHolder(): pin(this)
    {}

    T* get() {
        return handle.data();
    }

    bool isNull() const {
        return handle.isNull();
    }

    void setObject(T* object, Xiobj* wrapper) {
        assert (handle.isNull());
        handle = object;
        if (object->parent())
            pin.pin();
        xiqPutWrapper(object, wrapper);
    }

    void parentChange() {
        if (handle->parent())
            pin.pin();
        else
            pin.unpin();
    }

    ~XiQObjectHolder() {
        // if we are not pinned, we own the object, so we should delete it.
        // (and if we're pinned, we should not be here!)
        assert (!pin.isPinned());

        xiqForgetWrapper(handle.data());
        delete handle.data();
    }
};

#endif
// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
