#include <xiqslot.h>
#include <bindqt.h>

XiQPushButtonSigs::XiQPushButtonSigs(XiQPushButton* xiObj, QPushButton* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(clicked(bool)),
            this, SLOT(slotClicked(bool)));
}

void XiQPushButtonSigs::slotClicked(bool)
{
    // Dispatch to the peer, and all its listeners.
    xiObj->vtable()->clicked(xiObj);

    xiqEventListenerList<XiClickListener>::Cell* cur;
    for (cur = xiObj->clickListeners.head; cur; cur = cur->next)
        cur->listener->vtable()->clicked(cur->listener, xiObj);
}

///////////////////////////////////////////////////////////////////////////////
XiQTimerSigs::XiQTimerSigs(XiQTimer* xiObj, QTimer* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(timeout()),
            this, SLOT(slotTimeout()));
}

void XiQTimerSigs::slotTimeout()
{
    // Dispatch to the peer, and all its listeners.
    xiObj->vtable()->timeout(xiObj);

    xiqEventListenerList<XiTimerListener>::Cell* cur;
    for (cur = xiObj->timerListeners.head; cur; cur = cur->next)
        cur->listener->vtable()->timeout(cur->listener, xiObj);
}


///////////////////////////////////////////////////////////////////////////////
XiQApplicationSigs::XiQApplicationSigs(XiQApplication* xiObj, QApplication* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(aboutToQuit()),
            this, SLOT(slotAboutToQuit()));
}

void XiQApplicationSigs::slotAboutToQuit()
{
    // Dispatch to the peer, and all its listeners.
    xiqEventListenerList<XiAboutToQuitListener>::Cell* cur;
    for (cur = xiObj->quitListeners.head; cur; cur = cur->next)
        cur->listener->vtable()->aboutToQuit(cur->listener);
}

///////////////////////////////////////////////////////////////////////////////
XiQActionSigs::XiQActionSigs(XiQAction* xiObj, QAction* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(triggered(bool)),
            this, SLOT(slotTriggered()));
}

void XiQActionSigs::slotTriggered()
{
    xiObj->vtable()->triggered(xiObj);
    xiqEventListenerList<XiActionListener>::Cell* cur;
    for (cur = xiObj->actionListeners.head; cur; cur = cur->next)
        cur->listener->vtable()->triggered(cur->listener, xiObj);
}

///////////////////////////////////////////////////////////////////////////////
XiQCheckBoxSigs::XiQCheckBoxSigs(XiQCheckBox* xiObj, QCheckBox* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(toggled(bool)),
            this, SLOT(slotToggled(bool)));
}

void XiQCheckBoxSigs::slotToggled(bool v)
{
    xiObj->vtable()->toggled(xiObj, v);
    xiqEventListenerList<XiToggleListener>::Cell* cur;
    for (cur = xiObj->listeners.head; cur; cur = cur->next)
        cur->listener->vtable()->toggled(cur->listener, xiObj, v);
}

///////////////////////////////////////////////////////////////////////////////
XiQRadioButtonSigs::XiQRadioButtonSigs(XiQRadioButton* xiObj, QRadioButton* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(toggled(bool)),
            this, SLOT(slotToggled(bool)));
}

void XiQRadioButtonSigs::slotToggled(bool v)
{
    xiObj->vtable()->toggled(xiObj, v);
    xiqEventListenerList<XiToggleListener>::Cell* cur;
    for (cur = xiObj->listeners.head; cur; cur = cur->next)
        cur->listener->vtable()->toggled(cur->listener, xiObj, v);
}

///////////////////////////////////////////////////////////////////////////////
XiQAbstractSliderSigs::XiQAbstractSliderSigs(XiQAbstractSlider* xiObj, QAbstractSlider* qObj):
    xiObj(xiObj), qObj(qObj)
{
    connect(qObj, SIGNAL(sliderMoved(int)),
            this, SLOT(slotSliderMoved(int)));
}

void XiQAbstractSliderSigs::slotSliderMoved(int v)
{
    xiObj->vtable()->sliderMoved(xiObj, v);
    xiqEventListenerList<XiSliderListener>::Cell* cur;
    for (cur = xiObj->sliderListeners.head; cur; cur = cur->next)
        cur->listener->vtable()->sliderMoved(cur->listener, xiObj, v);
}

