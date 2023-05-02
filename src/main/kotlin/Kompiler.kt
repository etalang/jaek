
import ast.Interface
import ast.Node
import ast.Program
import ast.Method
import ast.RhoRecord
import errors.CompilerError
import errors.SemanticError
import typechecker.Context
import typechecker.EtaFunc
import typechecker.EtaType
import java.io.File

class Kompiler {
    var libraries : MutableMap<String, Node> = HashMap()
    fun createTopLevelContext(inFile: File, ast : Node, libpath: String, typedFile: File?) : Context {
        var returnGamma = Context()
        if (ast is Program) {
            when (ast) {
                is Program.EtaProgram -> {
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
                }
                is Program.RhoModule -> {

                }
            }

        } else {
            if (ast is Interface) {
                when (ast) {
                    is Interface.EtaInterface -> {
                    returnGamma = bindInterfaceMethods(inFile, ast, returnGamma)
                    libraries[inFile.name] = ast
                    }
                    is Interface.RhoInterface -> {

                    }
                }
            }
        }
        return returnGamma
    }

    fun bindInterfaceMethods(inFile: File, interfaceAST : Interface, returnGamma : Context) : Context {
        //deal with interfaces allowing uses and also having interface headers
        for (header in interfaceAST.headers) {
            if (header is Method) {
                var domainList = ArrayList<EtaType.OrdinaryType>()
                for (decl in header.args) {
                    domainList.add(EtaType.translateType(decl.type))
                }
                var codomainList = ArrayList<EtaType.OrdinaryType>()
                for (t in header.returnTypes) {
                    codomainList.add(EtaType.translateType(t))
                }
                val currFunType = EtaFunc(
                    EtaType.ExpandedType(domainList),
                    EtaType.ExpandedType(codomainList),
                    true
                )
                if (returnGamma.contains(header.id)) {
                    if (returnGamma.lookup(header.id) != currFunType) {
                        throw SemanticError(
                            header.terminal.line, header.terminal.column,
                            "Mismatch in type of function ${header.id} among interfaces", inFile
                        )
                    }
                } else {
                    header.etaType = currFunType
                    returnGamma.bind(header.id, currFunType)
                }
            } else if (header is RhoRecord){
                TODO()
            }
        }
        return returnGamma
    }
}