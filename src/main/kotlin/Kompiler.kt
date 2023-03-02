import ast.Interface
import ast.Node
import ast.Program
import typechecker.Context
import typechecker.EtaType
import java.io.File

class Kompiler {
    var libraries : MutableMap<String, Node> = HashMap()
    fun createTopLevelContext(ast: Node, libpath: String, currFile: Etac.CurrFile) : Context {
        val returnGamma = Context()
        if (ast is Program) {
            for (import in ast.imports){
                val filepath = File(libpath, import.lib + ".eti") // needs to use library path
                if (filepath.exists()) {
                    val prevFile = currFile.file
                    currFile.file = filepath
                    val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(filepath)
                    if (interfaceAST is Interface) {
                        for (method in interfaceAST.methodHeaders) {
                            var domainList = ArrayList<EtaType.OrdinaryType>()
                            for (decl in method.args) {
                                domainList.add(EtaType.translateType(decl.type))
                            }
                            var codomainList = ArrayList<EtaType.OrdinaryType>()
                            for (t in method.returnTypes) {
                                codomainList.add(EtaType.translateType(t))
                            }
                            val currFunType = EtaType.ContextType.FunType(
                                EtaType.ExpandedType(domainList),
                                EtaType.ExpandedType(codomainList),
                                true
                            )
                            if (returnGamma.contains(method.id)) {
                                if (returnGamma.lookup(method.id) != currFunType) {
                                    throw SemanticError(
                                        0,
                                        0,
                                        "Mismatch in type of function ${method.id} among interfaces"
                                    )
                                }
                            } else {
                                returnGamma.bind(method.id, currFunType)
                            }
                        }
                        libraries[import.lib] = interfaceAST
                    }
                    else {
                        currFile.file = prevFile
                        throw SemanticError(0, 0, "Could not import interface ${import.lib} AST")
                    }
                    currFile.file = prevFile
                } else {
                    throw SemanticError(0, 0, "Could not find interface ${import.lib} file")
                }
            }
        } else {
            throw Exception()
        }
        return returnGamma
    }
}