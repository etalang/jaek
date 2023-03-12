
import ast.Interface
import ast.Node
import ast.Program
import errors.CompilerError
import errors.LexicalError
import errors.ParseError
import errors.SemanticError
import typechecker.Context
import typechecker.EtaType
import java.io.File

class Kompiler {
    var libraries : MutableMap<String, Node> = HashMap()
    fun createTopLevelContext(inFile: File, ast : Node, libpath: String, typedFile: File?) : Context {
        var returnGamma = Context()
        if (ast is Program) {
            for (import in ast.imports){
                val filepath = File(libpath, import.lib + ".eti") // needs to use library path
                if (filepath.exists()) {
                    try {
                        val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(filepath)
                        if (interfaceAST is Interface) {
                            returnGamma = bindInterfaceMethods(filepath, interfaceAST, returnGamma)
                            libraries[import.lib] = interfaceAST
                        }
                        else {
                            throw SemanticError(import.terminal.line,import.terminal.column, "Could not import interface ${import.lib} AST", inFile)
                        }
                    } catch (e : CompilerError) {
                        typedFile?.appendText("${import.terminal.line}:${import.terminal.column} error:Error in interface file ${import.lib} preventing 'use'")
                        throw e
                    }
                } else {
                    throw SemanticError(import.terminal.line,import.terminal.column, "Could not find interface ${import.lib} file", inFile)
                }
            }
        } else {
            if (ast is Interface) {
                returnGamma = bindInterfaceMethods(inFile, ast, returnGamma)
                libraries[inFile.name] = ast
            }
        }
        return returnGamma
    }

    fun bindInterfaceMethods(inFile: File, interfaceAST : Interface, returnGamma : Context) : Context {
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
                        method.terminal.line, method.terminal.column,
                        "Mismatch in type of function ${method.id} among interfaces", inFile
                    )
                }
            } else {
                method.etaType = currFunType
                returnGamma.bind(method.id, currFunType)
            }
        }
        return returnGamma
    }
}