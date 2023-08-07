#ifndef XiQ_PROXY_WIDGETS_H
#define XiQ_PROXY_WIDGETS_H

#include <QWidget>

// To permit Xi end to override event methods, we must subclass
// the corresponding C++ classes, with implementations of virtuals
// that dispatch to Xi. The default implementation is then made to loop
// back to C++ again.
template<typename T>
class Proxy: public T
{
public:
    void defaultEvent(QEvent* e);

    virtual void mousePressEvent(QMouseEvent* e);
    virtual void mouseReleaseEvent(QMouseEvent* e);
    virtual void mouseDoubleClickEvent(QMouseEvent* e);
    virtual void mouseMoveEvent(QMouseEvent* e);
    virtual void wheelEvent(QWheelEvent* e);
    virtual void keyPressEvent(QKeyEvent* e);
    virtual void keyReleaseEvent(QKeyEvent* e);
    virtual void enterEvent(QEvent* e);
    virtual void leaveEvent(QEvent* e);
    virtual void paintEvent(QPaintEvent* e);
    virtual void resizeEvent(QResizeEvent* e);
    virtual void closeEvent(QCloseEvent* e);
    virtual void contextMenuEvent(QContextMenuEvent* e);
    virtual void showEvent(QShowEvent* e);
    virtual void hideEvent(QHideEvent* e);
};

#endif
