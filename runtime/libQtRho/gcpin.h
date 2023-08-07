#ifndef GC_PIN_H
#define GC_PIN_H

struct GCPinSet;

/*
 BoehmGC does not provide an easy way of pinning objects, so we do it
 by creating references to them. An object that's to be pinned
 should include a GCPin object as its member, and call
 pin()/unpin() methods as needed.
*/

class GCPin {
private:
    GCPinSet* pinSet; // the block that pins us, or 0.
    int       pinNum; // # in the block
    void*     pinPtr; // link to trace to pinnee
public:
    GCPin(void* pinPtr);
    ~GCPin();

    void pin();
    void unpin();

    bool isPinned() {
        return pinSet;
    }
};

#endif

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
