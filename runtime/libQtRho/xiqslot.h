#ifndef XiQ_SLOT_H
#define XiQ_SLOT_H

#include <xiobj.h>

#include <QObject>
#include <QCheckBox>
#include <QPushButton>
#include <QRadioButton>
#include <QAbstractSlider>

// An ultra-simple list that's stored in the GC heap,
// so it can be traced accross (as the normal heap can't be)
template<typename T>
struct xiqEventListenerList
{
    struct Cell
    {
        T* listener;
        Cell* next;
    };

    void append(T* listener) {
        Cell* newCell = gcCalloc<Cell>();
        newCell->listener = listener;

        if (head) {
            tail->next = newCell;
            tail = newCell;
        } else {
            head = tail = newCell;
        }
    }

    Cell* head;
    Cell* tail;

    xiqEventListenerList(): head(0), tail(0)
    {}
};

struct XiQPushButton;
class XiQPushButtonSigs: public QObject
{
    Q_OBJECT
public:
    XiQPushButtonSigs(XiQPushButton* xiObj, QPushButton* qObj);
private slots:
    void slotClicked(bool);
private:
    XiQPushButton* xiObj;
    QPushButton*   qObj;
};

struct XiQTimer;
class XiQTimerSigs: public QObject
{
    Q_OBJECT
public:
    XiQTimerSigs(XiQTimer* xiObj, QTimer* qObj);
private slots:
    void slotTimeout();
private:
    XiQTimer* xiObj;
    QTimer*   qObj;
};


struct XiQApplication;
class XiQApplicationSigs: public QObject
{
    Q_OBJECT
public:
    XiQApplicationSigs(XiQApplication* xiObj, QApplication* qObj);
private slots:
    void slotAboutToQuit();
private:
    XiQApplication* xiObj;
    QApplication*   qObj;
};

struct XiQAction;
class XiQActionSigs: public QObject
{
    Q_OBJECT
public:
    XiQActionSigs(XiQAction* xiObj, QAction* qObj);
private slots:
    void slotTriggered();
private:
    XiQAction* xiObj;
    QAction*   qObj;
};

struct XiQCheckBox;
class XiQCheckBoxSigs: public QObject
{
    Q_OBJECT
public:
    XiQCheckBoxSigs(XiQCheckBox* xiObj, QCheckBox* qObj);
private slots:
    void slotToggled(bool);
private:
    XiQCheckBox* xiObj;
    QCheckBox*   qObj;
};

struct XiQRadioButton;
class XiQRadioButtonSigs: public QObject
{
    Q_OBJECT
public:
    XiQRadioButtonSigs(XiQRadioButton* xiObj, QRadioButton* qObj);
private slots:
    void slotToggled(bool);
private:
    XiQRadioButton* xiObj;
    QRadioButton*   qObj;
};

struct XiQAbstractSlider;
class XiQAbstractSliderSigs: public QObject
{
    Q_OBJECT
public:
    XiQAbstractSliderSigs(XiQAbstractSlider* xiObj, QAbstractSlider* qObj);
private slots:
    void slotSliderMoved(int);
private:
    XiQAbstractSlider* xiObj;
    QAbstractSlider*   qObj;
};


#endif
