package ir.lowered

import assembly.Tile
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRReturn

/** IRReturn represents returning 0 or more values in [valList] from the current function **/
class LIRReturn(val valList: List<LIRExpr>) : LIRStmt.EndBlock() {
    override val java: IRReturn = factory.IRReturn(valList.map { it.java })

    override val defaultTile: Tile.Regular
        get() {
            val insns = mutableListOf<Instruction>()
//            val reglst = mutableListOf<Register>()
            if (valList.isNotEmpty()) { // single return
                val firstReturnTile = valList.first().optimalTile()
                insns.addAll(firstReturnTile.instructions)
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RAX)),
                        Source.RegisterSrc(firstReturnTile.outputRegister)
                    )
                )
            }
            if (valList.size >= 2) { // multireturn
                val secondReturnTile = valList.get(1).optimalTile()
                insns.addAll(secondReturnTile.instructions)
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RDX)),
                        Source.RegisterSrc(secondReturnTile.outputRegister)
                    )
                )
            }
            if (valList.size > 2) { // begin da push
                for (i in valList.size - 1 downTo 3) {
                    val returnTile = valList[i].optimalTile()
                    insns.addAll(returnTile.instructions)
                    insns.add(
                        Instruction.MOV(
                            Destination.MemoryDest(
                                Memory.RegisterMem(
                                    Register.x86(Register.x86Name.RDI), null,
                                    offset = 8L * (i - 3L)
                                )
                            ),
                            Source.RegisterSrc(returnTile.outputRegister)
                        )
                    )
                }
            }
            // TODO: test whether this works/ensure that the invariants are preserved so that this works
            insns.add(Instruction.LEAVE())
            insns.add(Instruction.RET())

            return Tile.Regular(insns, 1)
        }

    override fun findBestTile() {}
}