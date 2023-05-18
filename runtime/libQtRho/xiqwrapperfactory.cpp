#include <cassert>

#include <QAction>
#include <QWidget>
#include <QPushButton>
#include <QCheckBox>
#include <QRadioButton>
#include <QMenu>
#include <QToolBar>
#include <QMenuBar>
#include <QLabel>
#include <QGroupBox>
#include <QMainWindow>
#include <QDialog>
#include <QLineEdit>
#include <QTextEdit>
#include <QSlider>
#include <QScrollBar>

#include "xiqwrapperfactory.h"
#include "bindqt.h"

template<typename WrapperType, typename QtType>
WrapperType* makeWrapper(QtType* q)
{
    WrapperType* xi = gcNew<WrapperType>();
    xi->setObject(q);
    return xi;
}

template<typename WrapperType>
WrapperType* tryMakeWrapper(QObject* ptr)
{
    typename WrapperType::wrappedType* cptr;
    if (cptr = qobject_cast<typename WrapperType::wrappedType*>(ptr))
        return makeWrapper<WrapperType, typename WrapperType::wrappedType>(cptr);

    return 0;
}

Xiobj* xiqCreateQObjectWrapper(QObject* ptr)
{
    Xiobj* r = 0;
    if (ptr->isWidgetType()) {
        r = r ? r : tryMakeWrapper<XiQPushButton>(ptr);
        r = r ? r : tryMakeWrapper<XiQCheckBox>(ptr);
        r = r ? r : tryMakeWrapper<XiQRadioButton>(ptr);
        r = r ? r : tryMakeWrapper<XiQMenu>(ptr);
        r = r ? r : tryMakeWrapper<XiQToolBar>(ptr);
        r = r ? r : tryMakeWrapper<XiQMenuBar>(ptr);
        r = r ? r : tryMakeWrapper<XiQLabel>(ptr);
        r = r ? r : tryMakeWrapper<XiQGroupBox>(ptr);
        r = r ? r : tryMakeWrapper<XiQMainWindow>(ptr);
        r = r ? r : tryMakeWrapper<XiQDialog>(ptr);
        r = r ? r : tryMakeWrapper<XiQLineEdit>(ptr);
        r = r ? r : tryMakeWrapper<XiQTextEdit>(ptr);
        r = r ? r : tryMakeWrapper<XiQSlider>(ptr);
        r = r ? r : tryMakeWrapper<XiQScrollBar>(ptr);
        r = r ? r : tryMakeWrapper<XiQAbstractSlider>(ptr); // must be below QSlider, QScrollBar
        r = r ? r : makeWrapper<XiQWidget, QWidget>(static_cast<QWidget*>(ptr));
        return r;
    }

    r = r ? r : tryMakeWrapper<XiQAction>(ptr);
    r = r ? r : tryMakeWrapper<XiQButtonGroup>(ptr);
    r = r ? r : tryMakeWrapper<XiQApplication>(ptr);

    assert(r);
    return r;
}

Xiobj* qobjectToXi(QObject* ptr) {
    if (!ptr)
        return 0;

    Xiobj* o;
    if ((o = xiqGetWrapper(ptr)))
        return o;

    o = xiqCreateQObjectWrapper(ptr);
    assert(xiqGetWrapper(ptr) == o);
    return o;
}

template<typename XiType, typename QType = typename XiType::wrappedType>
struct ToXi_Adjustor
{
    XiType* operator() (QType* orig) {
        return static_cast<XiType*>(qobjectToXi(static_cast<QObject*>(orig)));
    }
};

XiQAction* toXi(QAction* act) {
    return ToXi_Adjustor<XiQAction>()(act);
}

XiQWidget* toXi(QWidget* w) {
    return ToXi_Adjustor<XiQWidget>()(w);
}

XiQPushButton* toXi(QPushButton* b) {
    return ToXi_Adjustor<XiQPushButton>()(b);
}

XiQCheckBox* toXi(QCheckBox* b) {
    return ToXi_Adjustor<XiQCheckBox>()(b);
}

XiQRadioButton* toXi(QRadioButton* b) {
    return ToXi_Adjustor<XiQRadioButton>()(b);
}

XiQButtonGroup* toXi(QButtonGroup* b) {
    return ToXi_Adjustor<XiQButtonGroup>()(b);
}

XiQMenu* toXi(QMenu* b) {
    return ToXi_Adjustor<XiQMenu>()(b);
}

XiQToolBar* toXi(QToolBar* b) {
    return ToXi_Adjustor<XiQToolBar>()(b);
}

XiQApplication* toXi(QApplication* a) {
    return ToXi_Adjustor<XiQApplication>()(a);
}

XiQMenuBar* toXi(QMenuBar* b) {
    return ToXi_Adjustor<XiQMenuBar>()(b);
}

XiQLabel* toXi(QLabel* b) {
    return ToXi_Adjustor<XiQLabel>()(b);
}

XiQGroupBox* toXi(QGroupBox* b) {
    return ToXi_Adjustor<XiQGroupBox>()(b);
}

XiQMainWindow* toXi(QMainWindow* b) {
    return ToXi_Adjustor<XiQMainWindow>()(b);
}

XiQDialog* toXi(QDialog* b) {
    return ToXi_Adjustor<XiQDialog>()(b);
}

XiQLineEdit* toXi(QLineEdit* b) {
    return ToXi_Adjustor<XiQLineEdit>()(b);
}

XiQTextEdit* toXi(QTextEdit* b) {
    return ToXi_Adjustor<XiQTextEdit>()(b);
}

XiQScrollBar* toXi(QScrollBar* b) {
    return ToXi_Adjustor<XiQScrollBar>()(b);
}

XiQSlider* toXi(QSlider* b) {
    return ToXi_Adjustor<XiQSlider>()(b);
}

