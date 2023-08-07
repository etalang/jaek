#include <bindqt.h>
#include <libeta.h>

XI_EXPORT void XI(println_pai)(int*);

XiQButtonGroup* bg; // global to prevent GC

XI_EXPORT
void XI(main_paai)(int64_t** args) {
    XiQAppCtorReturn ret;
    XI(qapplication_t2o12QApplicationaaiaai)(&ret, args);
    XiQApplication* app = ret.app;

    // We use it modeless here
    XiQDialog* d = XI(qdialog_o7QDialog)();
    XiQVBoxLayout* v = XI(qvboxLayout_o11QVBoxLayout)();
    d->vtable()->setLayout(d, v);

    // Add a slider and a buddy label
    int64_t val[] = {5, 'V', 'a', 'l', 'u', 'e'};
    XiQLabel*  l1 = XI(qlabel_o6QLabelo7QString)(XI(qs_o7QStringai)(&val[1]));
    XiQSlider* sl = XI(qslider_o7QSlider)();
    l1->vtable()->setBuddy(l1, sl);
    sl->vtable()->setOrientation(sl, XI(Horizontal_o11Orientation)());
    sl->vtable()->setRange(sl, 0, 100);
    sl->vtable()->setTickInterval(sl, 10);
    sl->vtable()->setTickPosition(sl, XI(TicksAbove_o18SliderTickPosition)());
    XiQHBoxLayout* hrow1 = XI(qhboxLayout_o11QHBoxLayout)();
    hrow1->vtable()->addWidget(hrow1, l1);
    hrow1->vtable()->addWidget(hrow1, sl);
    v->vtable()->addLayout(v, hrow1);

    // A a checkbox.
    int64_t cbox[] = {8, 'C', 'h', 'e', 'c', 'k', 'b', 'o', 'x'};
    XiQCheckBox* cb = XI(qcheckbox_o9QCheckBoxo7QString)(XI(qs_o7QStringai)(&val[1]));
    v->vtable()->addWidget(v, cb);

    // Some radios... We add them to a QButtonGroup, though we don't have to -
    // they'd be grouped automatically due to same parent.
    int64_t a[] = {1, 'A'};
    bg = XI(qbuttongroup_o12QButtonGroup)();
    XiQString* aStr = XI(qs_o7QStringai)(&a[1]);
    for (int i = 0; i < 5; ++i) {
        XiQString* label = aStr->vtable()->plus(aStr, XI(qsNum_o7QStringi)(i + 1));
        XiQRadioButton* r = XI(qradio_o12QRadioButtono7QString)(label);
        v->vtable()->addWidget(v, r);
        bg->vtable()->addButton(bg, r);
    }

    // Some more, inside a QGroupBox
    int64_t gr[] = {5, 'G', 'r', 'o', 'u', 'p'};
    XiQGroupBox* group = XI(qgroupbox_o9QGroupBoxo7QString)(XI(qs_o7QStringai)(&gr[1]));
    v->vtable()->addWidget(v, group);

    XiQVBoxLayout* vg = XI(qvboxLayout_o11QVBoxLayout)();
    group->vtable()->setLayout(group, vg);
    group->vtable()->setFlat(group, true);

    // And a secound group, which we add to a QButtonGroup
    int64_t b[] = {1, 'B'};
    XiQString* bStr = XI(qs_o7QStringai)(&b[1]);
    for (int i = 0; i < 5; ++i) {
        XiQString* label = bStr->vtable()->plus(bStr, XI(qsNum_o7QStringi)(i + 1));
        XiQRadioButton* r = XI(qradio_o12QRadioButtono7QString)(label);
        vg->vtable()->addWidget(vg, r);
    }

    // QLineEdit + a buddy.
    XiQLabel*    l2 = XI(qlabel_o6QLabelo7QString)(XI(qs_o7QStringai)(&val[1]));
    XiQLineEdit* le = XI(qlineedit_o9QLineEdit)();;
    l2->vtable()->setBuddy(l2, le);
    XiQHBoxLayout* hrow2 = XI(qhboxLayout_o11QHBoxLayout)();
    hrow2->vtable()->addWidget(hrow2, l2);
    hrow2->vtable()->addWidget(hrow2, le);
    v->vtable()->addLayout(v, hrow2);

    // A QTextEdit
    XiQTextEdit* t = XI(qtextedit_o9QTextEdit)();
    v->vtable()->addWidget(v, t);

    // A couple of buttons
    XiQHBoxLayout* hrow3 = XI(qhboxLayout_o11QHBoxLayout)();
    hrow3->vtable()->addStretch(hrow3);

    int64_t okStr[3] = {2, 'O', 'k' };
    XiQPushButton* ok = XI(qpushbutton_o11QPushButtono7QString)(XI(qs_o7QStringai)(&okStr[1]));
    ok->vtable()->setIcon(ok, XI(qiconStandard_o5QIcono12StandardIcon)(XI(DialogOkButton_o12StandardIcon)()));
    hrow3->vtable()->addWidget(hrow3, ok);

    int64_t cancelStr[7] = {6, 'C', 'a', 'n', 'c', 'e', 'l'};
    XiQPushButton* cancel = XI(qpushbutton_o11QPushButtono7QString)(XI(qs_o7QStringai)(&cancelStr[1]));
    cancel->vtable()->setIcon(cancel, XI(qiconStandard_o5QIcono12StandardIcon)(XI(DialogCancelButton_o12StandardIcon)()));
    hrow3->vtable()->addWidget(hrow3, cancel);
    v->vtable()->addLayout(v, hrow3);

    d->vtable()->show(d);
    app->vtable()->exec(app);
}

