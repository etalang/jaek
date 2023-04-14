package ir.lowered

import assembly.Tile
import assembly.TileBuilder
import assembly.x86.*
import assembly.x86.Instruction.*
import assembly.x86.Destination.*
import assembly.x86.Source.*
import edu.cornell.cs.cs4120.etac.ir.IRReturn

/** IRReturn represents returning 0 or more values in [valList] from the current function **/
class LIRReturn(val valList: List<LIRExpr>) : LIRStmt.EndBlock() {
    override val java: IRReturn = factory.IRReturn(valList.map { it.java })

    override val defaultTile: Tile.Regular
        get() {
            val builder = TileBuilder.Regular(1, this)
            if (valList.isNotEmpty()) { // single return
                val firstReturnTile = valList.first().optimalTile()
                builder.consume(firstReturnTile)
                builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RAX)),
                        RegisterSrc(firstReturnTile.outputRegister)))
            }
            if (valList.size >= 2) { // multireturn
                val secondReturnTile = valList.get(1).optimalTile()
                builder.consume(secondReturnTile)
                builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RDX)),
                    RegisterSrc(secondReturnTile.outputRegister)))
            }
            if (valList.size > 2) { // begin da push
                for (i in valList.size - 1 downTo 3) {
                    val returnTile = valList[i].optimalTile()
                    builder.consume(returnTile)
                    builder.add(MOV(MemoryDest(Memory.RegisterMem(
                        Register.x86(Register.x86Name.RDI), null,
                                    offset = 8L * (i - 3L))),
                            RegisterSrc(returnTile.outputRegister)))
                }
            }
            // TODO: test whether this works/ensure that the invariants are preserved so that this works
            builder.add(LEAVE())
            builder.add(RET())

            return builder.build() 
        }

    override fun findBestTile() {}
}