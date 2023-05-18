#include <gcpin.h>
#include <xiobj.h> // for gcNew
#include <cassert>

const int PINS_PER_BLOCK = 256;

struct GCPinSet
{
    GCPin* pins[PINS_PER_BLOCK];
    int       used;
    GCPinSet* next;

    static void invokeDtor(void*, void*)
    {} // nothing to do.
};

static int totalPinSlots;
static int totalPinsUsed;
static GCPinSet*  pinList;

GCPin::GCPin(void* pinPtr): pinSet(0), pinNum(-1), pinPtr(pinPtr)
{}

GCPin::~GCPin()
{
    assert (!pinSet);
}

void GCPin::pin()
{
    if (pinSet)
        return;

    // Allocate a new pinblock if needed
    if (totalPinsUsed == totalPinSlots) {
        GCPinSet* newSet = gcNew<GCPinSet>();
        newSet->next = pinList;
        pinList      = newSet;
        totalPinSlots += PINS_PER_BLOCK;
    }

    // Find a non-full pin block... somewhat inefficient, yes.
    for (pinSet = pinList; pinSet; pinSet = pinSet->next) {
        if (pinSet->used < PINS_PER_BLOCK)
            break;
    }

    assert(pinSet);

    // and even more inefficiency: find an empty slot.
    for (pinNum = 0; pinNum < PINS_PER_BLOCK; ++pinNum) {
        if (pinSet->pins[pinNum] == 0)
            break;
    }

    assert (pinNum < PINS_PER_BLOCK);

    pinSet->pins[pinNum] = this;
    ++pinSet->used;
    ++totalPinsUsed;
}

void GCPin::unpin()
{
    if (!pinSet)
        return;

    pinSet->pins[pinNum] = 0;
    --pinSet->used;
    --totalPinsUsed;
    pinSet = 0;
}


// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
