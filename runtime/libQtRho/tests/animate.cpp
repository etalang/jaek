// A simple bouncing ball demo..

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

XI_EXPORT
void XI(main_paai)(int64_t** args) {
    XiQAppCtorReturn ret;
    XI(qapplication_t2o12QApplicationaaiaai)(&ret, args);

    // Create a QWidget "subclass", with custom paintEvent
    // vtable here and not in init function.
    mainWidget = (XiQWidget*)_xi_alloc(XI(_size_QWidget));
    mainWidgetTable = XI(_vt_QWidget);

    // override the events
    mainWidgetTable.paintEvent = handlePaintEvent;
    mainWidget->vptr = &mainWidgetTable;

    // create the back buffer pixmap
    backBuffer = XI(qpixmap_o7QPixmapii)(DIM, DIM);

    // create animation timer.
    XiQTimer* timer = XI(qtimer_o6QTimer)();

    // make our listener.
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

    mainWidget->setFixedSize(mainWidget, XI(qsize_o5QSizeii)(DIM, DIM));
    mainWidget->vtable()->show(mainWidget);
    ret.app->vtable()->exec(ret.app);
}
