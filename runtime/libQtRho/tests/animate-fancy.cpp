// A fancier bouncing ball demo, with menus and such

#include <bindqt.h>
#include <libeta.h>

// Dimension of the arena, and radius of the object.
const int DIM = 256;
const int R   = 16;

// position of the object.
int x = 100, y = 50;
// directions
int dx = 2, dy = 1;

static XiQWidget* mainWidget;
XiQWidget_vtable mainWidgetTable;

// we use a pixmap for double-buffering. Actually, Qt will do it
// for us, but I test more things this way!
static XiQPixmap* backBuffer;

static XiQRect* ballRect()
{
    return XI(qrect_o5QRectiiii)(x - R, y - R, R*2, R*2);
}

static void handlePaintEvent(XiQWidget* __thisPtr, XiQPaintEvent* pe)
{
    // draw scene on backBuffer
    backBuffer->vtable()->fill(backBuffer, XI(qcolor_o6QColoriii)(255, 255, 192));
    XiQPainter* p = XI(qpainter_o8QPaintero12QPaintDevice)(backBuffer);
    p->vtable()->setHighQuality(p, true);

    XiQPen* pen = XI(qpen_o4QPeno6QColor)(XI(qcolor_o6QColoriii)(0, 0, 255));
    pen->vtable()->setWidth(pen, 5);
    p->vtable()->setPen(p, pen);
    p->vtable()->setBrush(p, XI(qbrush_o6QBrusho6QColor)(XI(qcolor_o6QColoriii)(255, 0, 0)));
    p->vtable()->drawEllipse(p, ballRect());
    p->vtable()->end(p);

    // paint the backbuffer
    XiQRect* dirty = pe->vtable()->rect(pe);
    XiQPainter* pw = XI(qpainter_o8QPaintero12QPaintDevice)(__thisPtr);
    pw->vtable()->drawPixmapPortion(pw, dirty->vtable()->topLeft(dirty),
                                    backBuffer, dirty);
    pw->vtable()->end(pw);
}

// Timer event listener
XiTimerListener_vtable listenerTable;

XiQTimer* timer;

static void handleTimeout(XiTimerListener* __thisPtr, XiQTimer* timer)
{
    XiQRect* oldRect = ballRect();

    x += dx;
    y += dy;

    if (x + R >= DIM || (x - R)<= 0)
        dx = -dx;

    if (y + R >= DIM || (y - R) <= 0)
        dy = -dy;

    XiQRect* newRect = ballRect();
    XiQRect* both    = newRect->vtable()->united(newRect, oldRect);

    mainWidget->vtable()->repaint(mainWidget,
                                  both->vtable()->adjusted(both, -5, -5, 5, 5));
}

XiQAction *play, *stop;

// Our action listener
static void handleTriggered(XiActionListener* __this, XiQAction* a)
{
    if (a == play) {
        play->vtable()->setEnabled(play, false);
        stop->vtable()->setEnabled(stop, true);
        timer->vtable()->start(timer);
    } else {
        play->vtable()->setEnabled(play, true);
        stop->vtable()->setEnabled(stop, false);
        timer->vtable()->stop(timer);
    }
}

XiActionListener_vtable actionListTable;

XI_EXPORT
void XI(main_paai)(int64_t** args) {
    XiQAppCtorReturn ret;
    XI(qapplication_t2o12QApplicationaaiaai)(&ret, args);

    // Create the main window
    XiQMainWindow* mw = XI(qmainwindow_o11QMainWindow)();

    // Create toolbar..
    XiQToolBar* tb = XI(qtoolbar_o8QToolBar)();
    tb->vtable()->setToolButtonStyle(tb, XI(ToolButtonTextBesideIcon_o15ToolButtonStyle)());

    mw->vtable()->addToolBar(mw, tb);

    // Create menu, add it.
    int64_t pbStr[] = {8 , 'P', 'l', 'a', 'y', 'b', 'a', 'c', 'k'};
    XiQMenu* menu = XI(qmenu_o5QMenuo7QString)(XI(qs_o7QStringai)(pbStr+1));
    XiQMenuBar* mbar = mw->vtable()->menuBar(mw);
    mbar->vtable()->addMenu(mbar, menu);

    // Actions for animation..
    int64_t playStr[] = { 4, 'P', 'l', 'a', 'y' };
    play = XI(qaction_o7QActiono7QString)(XI(qs_o7QStringai)(playStr+1));
    play->vtable()->setIcon(play, XI(qiconStandard_o5QIcono12StandardIcon)(XI(MediaPlay_o12StandardIcon)()));
    play->vtable()->setEnabled(play, false);

    int64_t stopStr[] = { 4, 'S', 't', 'o', 'p' };
    stop = XI(qaction_o7QActiono7QString)(XI(qs_o7QStringai)(stopStr+1));
    stop->vtable()->setIcon(stop, XI(qiconStandard_o5QIcono12StandardIcon)(XI(MediaStop_o12StandardIcon)()));

    // add action listener.
    XiActionListener* al = (XiActionListener*)_xi_alloc(XI(_size_ActionListener));
    actionListTable = XI(_vt_ActionListener);
    actionListTable.triggered = handleTriggered;
    al->vptr = &actionListTable;

    play->vtable()->addActionListener(play, al);
    stop->vtable()->addActionListener(stop, al);

    // Add them to toolbar, menu
    tb->vtable()->addAction(tb, play);
    tb->vtable()->addAction(tb, stop);

    menu->vtable()->addAction(menu, play);
    menu->vtable()->addAction(menu, stop);

    // Create a QWidget "subclass", with custom paintEvent
    // vtable here and not in init function.
    mainWidget = (XiQWidget*)_xi_alloc(XI(_size_QWidget));
    mainWidgetTable = XI(_vt_QWidget);

    // override the events
    mainWidgetTable.paintEvent = handlePaintEvent;
    mainWidget->vptr = &mainWidgetTable;

    mainWidget->vtable()->setFixedSize(mainWidget, XI(qsize_o5QSizeii)(DIM, DIM));

    // create the back buffer pixmap
    backBuffer = XI(qpixmap_o7QPixmapii)(DIM, DIM);

    // create animation timer.
    timer = XI(qtimer_o6QTimer)();

    // make our timer listener.
    XiTimerListener* l = (XiTimerListener*)_xi_alloc(XI(_size_TimerListener));
    listenerTable = XI(_vt_TimerListener);
    listenerTable.timeout = handleTimeout;
    l->vptr = &listenerTable;
    // and add it.
    timer->vtable()->addTimerListener(timer, l);

    // start the timer..
    timer->vtable()->setSingleShot(timer, false);
    timer->vtable()->setInterval(timer, 40);
    timer->vtable()->start(timer);

    // put it into the main window
    mw->vtable()->setCentralWidget(mw, mainWidget);
    mw->vtable()->show(mw);
    ret.app->vtable()->exec(ret.app);
}
