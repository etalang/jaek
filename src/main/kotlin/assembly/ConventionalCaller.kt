package assembly

import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Source
import typechecker.EtaFunc

class ConventionalCaller(private val numArgs: Int, private val numReturns: Int) {
    constructor(func: EtaFunc) : this(func.domain.lst.size, func.codomain.lst.size)

    //value -> MOV into space
    fun putArg(index: Int, value: Register): Instruction {
        TODO()
        // if we are using the first
//        if (numReturns < 3 && numArgs > 0) {
//            Instruction.MOV(
//                Destination.RegisterDest(Register.x86(Register.x86Name.RDI)),
//                Source.RegisterSrc(value)
//            )
//        }
    }

    //value -> MOV into space
    /**
     * [index] starts at 1
     */
    fun putReturn(index: Int, value: Register): Instruction {
        return when (index) {
            1 -> Instruction.MOV(
                Destination.RegisterDest(Register.Abstract("_RV1")),
                Source.RegisterSrc(Register.x86(Register.x86Name.RAX))
            )

            2 -> Instruction.MOV(
                Destination.RegisterDest(Register.Abstract("_RV2")),
                Source.RegisterSrc(Register.x86(Register.x86Name.RDX))
            )

            else -> {
                Instruction.POP(Register.Abstract("_RV$index"))
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