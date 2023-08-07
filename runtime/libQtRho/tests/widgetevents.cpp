#include <bindqt.h>
#include <libeta.h>

#include <iostream>

XI_EXPORT void XI(println_pai)(int*);

static XiQApplication* qapp;

static void keyPressEvent(XiQWidget* __thisPtr, XiQKeyEvent* e)
{
    XiQString* s = e->vtable()->text(e);
    XI(println_pai)((int*)s->vtable()->XiString(s));

    // Forward it to parent class.
    __thisPtr->vtable()->defaultEvent(__thisPtr, e);
}

static void mouseMoveEvent(XiQWidget* __thisPtr, XiQMouseEvent* me)
{
    XiQPoint* p = me->vtable()->pos(me);
    std::cout << p->vtable()->x(p) << "," << p->vtable()->y(p) << "\n";
}

static void clicked(XiQPushButton* __thisPtr)
{
    // Ask whether to quit or not.
    int64_t quit[] = {5, 'Q', 'u', 'i', 't', '?' };
    XiQString* quitS = XI(qs_o7QStringai)(quit + 1);

    int res = XI(qmessageBoxQuestion_io7QWidgeto7QStringo7QStringi)
                (__thisPtr, quitS, quitS,  XI(ButtonYes_i)() + XI(ButtonNo_i)());
    if (res == XI(ButtonYes_i)())
        qapp->vtable()->quit(qapp);
}

XiQPushButton_vtable ourTable;

XI_EXPORT
void XI(main_paai)(int64_t** args) {
    XiQAppCtorReturn ret;
    XI(qapplication_t2o12QApplicationaaiaai)(&ret, args);
    qapp = ret.app;

    // Create a QWidget "subclass", though we copy the
    // vtable here and not in init function.
    XiQPushButton* w = (XiQPushButton*)_xi_alloc(XI(_size_QPushButton));
    ourTable = XI(_vt_QPushButton);

    // override the events
    ourTable.keyPressEvent  = keyPressEvent;
    ourTable.mouseMoveEvent = mouseMoveEvent;

    // and even clicked handler
    ourTable.clicked = clicked;

    w->vptr = &ourTable;

    XiQIcon* ok = XI(qiconStandard_o5QIcono12StandardIcon)(XI(DialogOkButton_o12StandardIcon)());
    w->vtable()->setIcon(w, ok);

    w->vtable()->setFocusPolicy(w, XI(ClickFocus_o11FocusPolicy)());
    w->vtable()->setMouseTracking(w, true);
    w->vtable()->show(w);
    qapp->vtable()->exec(qapp);
}
