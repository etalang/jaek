#include <bindqt.h>
#include <libeta.h>

XI_EXPORT void XI(println_pai)(int64_t*);

XI_EXPORT
void XI(main_paai)(int64_t** args) {
    XiQAppCtorReturn ret;
    XI(qapplication_t2o12QApplicationaaiaai)(&ret, args);
    XiQApplication* app = ret.app;
    args = ret.modifiedArgs;

    // Print out args.
    int argc = ((int*)args)[-1];
    for (int a = 0; a < argc; ++a) {
        XI(println_pai)(args[a]);
    }

    int64_t hi[] = {2, 'h', 'i'};
    XiQString* qhi = XI(qs_o7QStringai)(&hi[1]);
    XiQPushButton* b = XI(qpushbutton_o11QPushButtono7QString)(qhi);
    b->vtable()->show(b);
    app->vtable()->exec(app);
}

