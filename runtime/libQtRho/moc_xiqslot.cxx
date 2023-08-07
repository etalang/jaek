/****************************************************************************
** Meta object code from reading C++ file 'xiqslot.h'
**
** Created by: The Qt Meta Object Compiler version 63 (Qt 4.8.7)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "xiqslot.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'xiqslot.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 63
#error "This file was generated using the moc from 4.8.7. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_XiQPushButtonSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      19,   18,   18,   18, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQPushButtonSigs[] = {
    "XiQPushButtonSigs\0\0slotClicked(bool)\0"
};

void XiQPushButtonSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQPushButtonSigs *_t = static_cast<XiQPushButtonSigs *>(_o);
        switch (_id) {
        case 0: _t->slotClicked((*reinterpret_cast< bool(*)>(_a[1]))); break;
        default: ;
        }
    }
}

const QMetaObjectExtraData XiQPushButtonSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQPushButtonSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQPushButtonSigs,
      qt_meta_data_XiQPushButtonSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQPushButtonSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQPushButtonSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQPushButtonSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQPushButtonSigs))
        return static_cast<void*>(const_cast< XiQPushButtonSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQPushButtonSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQTimerSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      14,   13,   13,   13, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQTimerSigs[] = {
    "XiQTimerSigs\0\0slotTimeout()\0"
};

void XiQTimerSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQTimerSigs *_t = static_cast<XiQTimerSigs *>(_o);
        switch (_id) {
        case 0: _t->slotTimeout(); break;
        default: ;
        }
    }
    Q_UNUSED(_a);
}

const QMetaObjectExtraData XiQTimerSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQTimerSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQTimerSigs,
      qt_meta_data_XiQTimerSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQTimerSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQTimerSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQTimerSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQTimerSigs))
        return static_cast<void*>(const_cast< XiQTimerSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQTimerSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQApplicationSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      20,   19,   19,   19, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQApplicationSigs[] = {
    "XiQApplicationSigs\0\0slotAboutToQuit()\0"
};

void XiQApplicationSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQApplicationSigs *_t = static_cast<XiQApplicationSigs *>(_o);
        switch (_id) {
        case 0: _t->slotAboutToQuit(); break;
        default: ;
        }
    }
    Q_UNUSED(_a);
}

const QMetaObjectExtraData XiQApplicationSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQApplicationSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQApplicationSigs,
      qt_meta_data_XiQApplicationSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQApplicationSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQApplicationSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQApplicationSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQApplicationSigs))
        return static_cast<void*>(const_cast< XiQApplicationSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQApplicationSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQActionSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      15,   14,   14,   14, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQActionSigs[] = {
    "XiQActionSigs\0\0slotTriggered()\0"
};

void XiQActionSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQActionSigs *_t = static_cast<XiQActionSigs *>(_o);
        switch (_id) {
        case 0: _t->slotTriggered(); break;
        default: ;
        }
    }
    Q_UNUSED(_a);
}

const QMetaObjectExtraData XiQActionSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQActionSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQActionSigs,
      qt_meta_data_XiQActionSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQActionSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQActionSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQActionSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQActionSigs))
        return static_cast<void*>(const_cast< XiQActionSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQActionSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQCheckBoxSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      17,   16,   16,   16, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQCheckBoxSigs[] = {
    "XiQCheckBoxSigs\0\0slotToggled(bool)\0"
};

void XiQCheckBoxSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQCheckBoxSigs *_t = static_cast<XiQCheckBoxSigs *>(_o);
        switch (_id) {
        case 0: _t->slotToggled((*reinterpret_cast< bool(*)>(_a[1]))); break;
        default: ;
        }
    }
}

const QMetaObjectExtraData XiQCheckBoxSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQCheckBoxSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQCheckBoxSigs,
      qt_meta_data_XiQCheckBoxSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQCheckBoxSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQCheckBoxSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQCheckBoxSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQCheckBoxSigs))
        return static_cast<void*>(const_cast< XiQCheckBoxSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQCheckBoxSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQRadioButtonSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      20,   19,   19,   19, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQRadioButtonSigs[] = {
    "XiQRadioButtonSigs\0\0slotToggled(bool)\0"
};

void XiQRadioButtonSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQRadioButtonSigs *_t = static_cast<XiQRadioButtonSigs *>(_o);
        switch (_id) {
        case 0: _t->slotToggled((*reinterpret_cast< bool(*)>(_a[1]))); break;
        default: ;
        }
    }
}

const QMetaObjectExtraData XiQRadioButtonSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQRadioButtonSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQRadioButtonSigs,
      qt_meta_data_XiQRadioButtonSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQRadioButtonSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQRadioButtonSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQRadioButtonSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQRadioButtonSigs))
        return static_cast<void*>(const_cast< XiQRadioButtonSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQRadioButtonSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
static const uint qt_meta_data_XiQAbstractSliderSigs[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      23,   22,   22,   22, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_XiQAbstractSliderSigs[] = {
    "XiQAbstractSliderSigs\0\0slotSliderMoved(int)\0"
};

void XiQAbstractSliderSigs::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        XiQAbstractSliderSigs *_t = static_cast<XiQAbstractSliderSigs *>(_o);
        switch (_id) {
        case 0: _t->slotSliderMoved((*reinterpret_cast< int(*)>(_a[1]))); break;
        default: ;
        }
    }
}

const QMetaObjectExtraData XiQAbstractSliderSigs::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject XiQAbstractSliderSigs::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_XiQAbstractSliderSigs,
      qt_meta_data_XiQAbstractSliderSigs, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &XiQAbstractSliderSigs::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *XiQAbstractSliderSigs::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *XiQAbstractSliderSigs::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_XiQAbstractSliderSigs))
        return static_cast<void*>(const_cast< XiQAbstractSliderSigs*>(this));
    return QObject::qt_metacast(_clname);
}

int XiQAbstractSliderSigs::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 1)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 1;
    }
    return _id;
}
QT_END_MOC_NAMESPACE
