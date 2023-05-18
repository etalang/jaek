#ifndef XiQ_WRAPPER_FACTORY_H
#define XiQ_WRAPPER_FACTORY_H

#include <QObject>
#include <xiobj.h>
#include <xiqobjectholder.h>

struct XiQAction;
struct XiQWidget;
struct XiQPushButton;
struct XiQApplication;
struct XiQCheckBox;
struct XiQRadioButton;
struct XiQButtonGroup;
struct XiQMenu;
struct XiQToolBar;
struct XiQMenuBar;
struct XiQLabel;
struct XiQGroupBox;
struct XiQMainWindow;
struct XiQDialog;
struct XiQLineEdit;
struct XiQTextEdit;
struct XiQSlider;
struct XiQScrollBar;

// This method creates the wrapper of an appropriate type;
// e.g. if given a pure QWidget it will return a XiQWidget,
// while if it's given a QPushButton, it will return a XiQPushButton, etc.
Xiobj* xiqCreateQObjectWrapper(QObject* ptr);

XiQAction*      toXi(QAction* act);
XiQPushButton*  toXi(QPushButton* b);
XiQCheckBox*    toXi(QCheckBox* b);
XiQRadioButton* toXi(QRadioButton* b);
XiQWidget*      toXi(QWidget* w);
XiQApplication* toXi(QApplication* a);
XiQButtonGroup* toXi(QButtonGroup* a);
XiQMenu*        toXi(QMenu* m);
XiQToolBar*     toXi(QToolBar* t);
XiQMenuBar*     toXi(QMenuBar* m);
XiQLabel*       toXi(QLabel* m);
XiQMainWindow*  toXi(QMainWindow* m);
XiQGroupBox*    toXi(QGroupBox* m);
XiQDialog*      toXi(QDialog* m);
XiQLineEdit*    toXi(QLineEdit* m);
XiQTextEdit*    toXi(QTextEdit* m);
XiQScrollBar*   toXi(QScrollBar* m);
XiQSlider*      toXi(QSlider* m);
#endif
