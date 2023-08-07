#include "xiqobjectholder.h"

#include <QHash>

static QHash<QObject*, Xiobj*>* s_wrappers;

static QHash<QObject*, Xiobj*>& wrappers() {
    if (!s_wrappers)
        s_wrappers = new QHash<QObject*, Xiobj*>;
    return *s_wrappers;
}

Xiobj* xiqGetWrapper(QObject* object)
{
    return wrappers().value(object);
}

void xiqPutWrapper(QObject* object, Xiobj* wrapper)
{
    assert (!wrappers().contains(object));
    wrappers().insert(object, wrapper);
}

void xiqForgetWrapper(QObject* object)
{
    assert (wrappers().contains(object));
    wrappers().remove(object);
}

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
