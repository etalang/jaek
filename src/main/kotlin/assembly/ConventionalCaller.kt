package assembly

import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Instruction.*
import assembly.x86.Register
import assembly.x86.Register.x86Name.*
import assembly.x86.Source
import typechecker.EtaFunc

class ConventionalCaller(private val numArgs: Int, private val numReturns: Int) {
    constructor(func: EtaFunc) : this(func.domain.lst.size, func.codomain.lst.size)

    //value -> MOV into space]
    /**
     * creates instruction for moving argument into register/stack
     * [index] starts at 1
     */
    fun putArg(index: Int, value: Register): Instruction {
        val adjIdx = if (numReturns > 2) index + 1 else index // bump everything down one to keep space for ret ptr
        return if (adjIdx <= 6) {
            val reg = when (adjIdx) {
                1 -> RDI // ind = 1 and args >2, can't get here, bumps to RSI
                2 -> RSI
                3 -> RDX
                4 -> RCX
                5 -> R8
                6 -> R9
                else -> throw Exception("charles how did we get here")
            }
            MOV(
                Destination.RegisterDest(Register.x86(reg)),
                Source.RegisterSrc(value)
            )
        } else {
            PUSH(value)
        }
    }

    //value -> MOV into space
    /**
     * [index] starts at 1
     */
    fun putReturn(index: Int, value: Register): Instruction {
        return when (index) {
            1 -> MOV(
                Destination.RegisterDest(Register.Abstract("_RV1")),
                Source.RegisterSrc(Register.x86(Register.x86Name.RAX))
            )

            2 -> MOV(
                Destination.RegisterDest(Register.Abstract("_RV2")),
                Source.RegisterSrc(Register.x86(Register.x86Name.RDX))
            )

            else -> {
                POP(Register.Abstract("_RV$index"))
            }
        }
    }


    /** takes in _RV{[index]} and outputs where it is **/
    fun getReturn(index: Int): Source {
        TODO()
    }

    /** takes in _ARG{[index]} and outputs where it is **/
    fun getArg(index: Int): Source {
        TODO()
    }

}