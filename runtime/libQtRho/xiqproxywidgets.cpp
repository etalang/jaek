#include <xiqproxywidgets.h>
#include <xiqobjectholder.h> // for getWrapper
#include <bindqt.h>

template<typename XiType, typename QType = typename XiType::wrappedType>
struct ToXi_Event
{
    XiType* operator() (QType* orig) {
        XiType* o = gcNew<XiType>();
        o->e = orig;
        return o;
    }
};

template<typename T>
void Proxy<T>::paintEvent(QPaintEvent* pe)
{
    // Find our wrapper. It must exist, since we were created by one.
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->paintEvent(wrapper, ToXi_Event<XiQPaintEvent>()(pe));
}

template<typename T>
void Proxy<T>::mousePressEvent(QMouseEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->mousePressEvent(wrapper, ToXi_Event<XiQMouseEvent>()(e));
}

template<typename T>
void Proxy<T>::mouseReleaseEvent(QMouseEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->mouseReleaseEvent(wrapper, ToXi_Event<XiQMouseEvent>()(e));
}

template<typename T>
void Proxy<T>::mouseDoubleClickEvent(QMouseEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->mouseDoubleClickEvent(wrapper, ToXi_Event<XiQMouseEvent>()(e));
}

template<typename T>
void Proxy<T>::mouseMoveEvent(QMouseEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->mouseMoveEvent(wrapper, ToXi_Event<XiQMouseEvent>()(e));
}

template<typename T>
void Proxy<T>::wheelEvent(QWheelEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->wheelEvent(wrapper, ToXi_Event<XiQWheelEvent>()(e));
}

template<typename T>
void Proxy<T>::keyPressEvent(QKeyEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->keyPressEvent(wrapper, ToXi_Event<XiQKeyEvent>()(e));
}

template<typename T>
void Proxy<T>::keyReleaseEvent(QKeyEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->keyReleaseEvent(wrapper, ToXi_Event<XiQKeyEvent>()(e));
}

template<typename T>
void Proxy<T>::enterEvent(QEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->enterEvent(wrapper, ToXi_Event<XiQEvent>()(e));
}

template<typename T>
void Proxy<T>::leaveEvent(QEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->leaveEvent(wrapper, ToXi_Event<XiQEvent>()(e));
}

template<typename T>
void Proxy<T>::resizeEvent(QResizeEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->resizeEvent(wrapper, ToXi_Event<XiQResizeEvent>()(e));
}

template<typename T>
void Proxy<T>::closeEvent(QCloseEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->closeEvent(wrapper, ToXi_Event<XiQEvent>()(e));
}

template<typename T>
void Proxy<T>::contextMenuEvent(QContextMenuEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->contextMenuEvent(wrapper, ToXi_Event<XiQContextMenuEvent>()(e));
}

template<typename T>
void Proxy<T>::showEvent(QShowEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->showEvent(wrapper, ToXi_Event<XiQEvent>()(e));
}

template<typename T>
void Proxy<T>::hideEvent(QHideEvent* e)
{
    XiQWidget* wrapper = static_cast<XiQWidget*>(xiqGetWrapper(this));
    wrapper->vtable()->hideEvent(wrapper, ToXi_Event<XiQEvent>()(e));
}

template<typename T>
void Proxy<T>::defaultEvent(QEvent* e)
{
    // Matches the code in QWidget::event, for the types we support.
    switch (e->type()) {
    case QEvent::MouseMove:
        T::mouseMoveEvent(static_cast<QMouseEvent*>(e));
        break;
    case QEvent::MouseButtonPress:
        T::mousePressEvent(static_cast<QMouseEvent*>(e));
        break;
    case QEvent::MouseButtonRelease:
        T::mouseReleaseEvent(static_cast<QMouseEvent*>(e));
        break;
    case QEvent::MouseButtonDblClick:
        T::mouseDoubleClickEvent(static_cast<QMouseEvent*>(e));
        break;
    case QEvent::ContextMenu:
        T::contextMenuEvent(static_cast<QContextMenuEvent*>(e));
        break;
    case QEvent::Wheel:
        T::wheelEvent(static_cast<QWheelEvent*>(e));
        break;
    case QEvent::KeyPress:
        T::keyPressEvent(static_cast<QKeyEvent*>(e));
        break;
    case QEvent::KeyRelease:
        T::keyReleaseEvent(static_cast<QKeyEvent*>(e));
        break;
    case QEvent::Enter:
        T::enterEvent(e);
        break;
    case QEvent::Leave:
        T::leaveEvent(e);
        break;
    case QEvent::Resize:
        T::resizeEvent(static_cast<QResizeEvent*>(e));
        break;
    case QEvent::Close:
        T::closeEvent(static_cast<QCloseEvent*>(e));
        break;
    case QEvent::Show:
        T::showEvent(static_cast<QShowEvent*>(e));
        break;
    case QEvent::Hide:
        T::hideEvent(static_cast<QHideEvent*>(e));
        break;
    case QEvent::Paint:
        T::paintEvent(static_cast<QPaintEvent*>(e));
        break;
    default:
        assert(0);
    }
}

// Force instanciation.
template class Proxy<QWidget>;
template class Proxy<QPushButton>;
template class Proxy<QCheckBox>;
template class Proxy<QRadioButton>;
template class Proxy<QMenu>;
template class Proxy<QToolBar>;
template class Proxy<QMenuBar>;
template class Proxy<QLabel>;
template class Proxy<QGroupBox>;
template class Proxy<QMainWindow>;
template class Proxy<QLineEdit>;
template class Proxy<QDialog>;
template class Proxy<QTextEdit>;
template class Proxy<QAbstractSlider>;
template class Proxy<QSlider>;
template class Proxy<QScrollBar>;
