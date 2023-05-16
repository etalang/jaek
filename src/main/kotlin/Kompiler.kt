
import ast.*
import ast.Interface.*
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
                    for (import in ast.imports) {
                        val importPath = File(libpath, import.lib + ".eti") // needs to use library path
                        if (importPath.exists()) {
                            val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(importPath)
                            returnGamma = checkAndLoadInterface(inFile, importPath, interfaceAST, import, returnGamma, typedFile)
                        } else {
                            throw SemanticError(
                                import.terminal.line,
                                import.terminal.column,
                                "Could not find interface ${import.lib} file",
                                inFile
                            )
                        }
                    }
                }

                is Program.RhoModule -> {
                    // NEED CHECK TO MAKE SURE IF A FILE HAS CORRESPONDING INTERFACE, EVERYTHING IN INTERFACE IS DEFINED

                    // We will load the records in our module here first
                    ast.definitions.forEach{ definition ->
                        if (definition is RhoRecord) {
                            if (definition.name in returnGamma.recordTypes()) {
                                throw SemanticError(definition.terminal.line, definition.terminal.column,"Redeclared record type ${definition.name}", inFile)
                            }
                            // when iterating through this in the future, preserves order of adding elts to map
                            val fieldTypes = linkedMapOf<String, EtaType.OrdinaryType>()
                            for (f in definition.fields) {
                                for (identifier in f.ids) {
                                    fieldTypes[identifier.name] = EtaType.translateType(f.type)
                                }
                            }
                            val recordType = EtaType.ContextType.RecordType(definition.name, fieldTypes)
                            definition.etaType = recordType
                            returnGamma.bind(definition.name, recordType)
                        }
                    }

                    val thisFilePath = File(libpath,inFile.nameWithoutExtension + ".ri") // need to check this file interface
                    if (thisFilePath.exists()) {
                        val interfaceAST = libraries[inFile.nameWithoutExtension] ?: ASTUtil.getAST(thisFilePath)
                        if (interfaceAST is RhoInterface) {
                            // add the imports that are used in the interface into the module's use list
                            ast.imports.addAll(interfaceAST.imports)

                            // check that all headers are defined in the Rho Module and that their types/subtypes match
                            interfaceAST.headers.forEach{ header ->
                                when (header){
                                    is Method -> {
                                        var foundDef = false
                                        ast.definitions.forEach{ definition ->
                                            if (definition is Method && definition.id == header.id) {
                                                foundDef = true
                                                bindInterfaceMethod(inFile, header, returnGamma)
                                            }
                                        }
                                        if (!foundDef) {
                                            throw SemanticError(header.terminal.line,
                                                header.terminal.column,"Definition ${header.id} not found in Rho Module", inFile)
                                        }
                                    }
                                    is RhoRecord -> {
                                        val recordDefinition = returnGamma.lookup(header.name)
                                        if (returnGamma.contains(header.name)){
                                            if (recordDefinition !is EtaType.ContextType.RecordType) {
                                                throw SemanticError(header.terminal.line,
                                                    header.terminal.column,"Definition ${header.name} is not a record type in Rho Module", inFile)
                                            } else {
                                                checkRecordSubtyping(header, recordDefinition, inFile)
                                                //TODO: not exactly correct
                                                header.etaType = recordDefinition
                                            }
                                        } else {
                                            throw SemanticError(header.terminal.line,
                                                header.terminal.column,"Definition ${header.name} in Interface but not found in Rho Module", inFile)
                                        }
                                    }
                                    else -> throw SemanticError(header.terminal.line,
                                        header.terminal.column,"Invalid header type", inFile)
                                }
                            }
                        } else {
                            throw SemanticError(
                                ast.terminal.line,
                                ast.terminal.column,
                                "${thisFilePath} is not a rho interface",
                                inFile
                            )
                        }
                    }
                    for (import in ast.imports) {
                        val etaPath = File(libpath, import.lib + ".eti") // needs to use library path
                        val rhoPath = File(libpath, import.lib + ".ri")
                        if (etaPath.exists()) {
                            val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(etaPath)
                            returnGamma = checkAndLoadInterface(inFile, etaPath, interfaceAST, import, returnGamma, typedFile)
                        }
                        else if (rhoPath.exists()) {
                            val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(rhoPath)
                            returnGamma = checkAndLoadInterface(inFile, rhoPath, interfaceAST, import, returnGamma, typedFile)
                            returnGamma = loadRhoInterfaceDependencies(inFile, libpath, ast.imports, returnGamma, typedFile)
                        }
                        else {
                            throw SemanticError(
                                import.terminal.line,
                                import.terminal.column,
                                "Could not find interface ${import.lib} as .eti or .ri file",
                                inFile
                            )
                        }
                    }
                }
            }
            } else {
                if (ast is Interface) {
                    returnGamma = bindInterfaceDefinitions(inFile, ast, returnGamma)
                    libraries[inFile.name] = ast
                    if (ast is RhoInterface) {
                        returnGamma = loadRhoInterfaceDependencies(inFile, libpath, ast.imports, returnGamma, typedFile)
                    }
                }
            }
            return returnGamma
    }

    fun checkRecordSubtyping(header : RhoRecord, recordtype : EtaType.ContextType.RecordType, inFile: File) {
       header.fields.forEach{ field ->
          for (identifier in field.ids) {
              if (recordtype.fields[identifier.name] == null) {
                  throw SemanticError(identifier.terminal.line, identifier.terminal.column,"${identifier.name} is not defined in record ${recordtype.name} in Rho Module", inFile)
              } else {
                  val headerFieldType = EtaType.translateType(field.type)
                  val recordFieldType = recordtype.fields[identifier.name]
                  if (headerFieldType != recordFieldType) {
                      throw SemanticError(
                          identifier.terminal.line,
                          identifier.terminal.column,
                          "${identifier.name} is defined as ${headerFieldType} in header but ${recordFieldType} in record ${recordtype.name} in Rho Module",
                          inFile)
                  }
              }
          }
       }
    }

    fun loadRhoInterfaceDependencies(inFile : File, importPath : String, imports : MutableList<Use>, returnGamma : Context, typedFile : File?) : Context {
        val seenImports = mutableSetOf<String>()
        // we have already imported the original ri file, so we don't need to do it again
        seenImports.add(inFile.nameWithoutExtension + ".ri")
        val remainingUses = imports.toMutableList()
        var finalGamma = returnGamma
        while (remainingUses.isNotEmpty()) {
            val import = remainingUses.removeAt(0)
            if (seenImports.contains(import.lib + ".eti") || seenImports.contains(import.lib + ".ri")) {
                continue
            }
            val etapath = File(importPath, import.lib + ".eti")
            val rhopath = File(importPath, import.lib + ".ri")
            if (etapath.exists()) {
                seenImports.add(import.lib + ".eti")
                val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(etapath)
                finalGamma = checkAndLoadInterface(inFile, etapath, interfaceAST, import, returnGamma, typedFile)
            }
            if (rhopath.exists()) {
                seenImports.add(import.lib + ".ri")
                val interfaceAST = libraries[import.lib] ?: ASTUtil.getAST(rhopath)
                if (interfaceAST is RhoInterface) {
                    remainingUses.addAll(interfaceAST.imports)
                }
                finalGamma = checkAndLoadInterface(inFile, rhopath, interfaceAST, import, returnGamma, typedFile)
            }
        }
        return finalGamma
    }

    /** checkAndLoadInterface checks if interfaceAST is an Interface AST node, and if so, adds all the bindings
     * into something i don't fucking know */
    fun checkAndLoadInterface(inFile : File, importPath : File, interfaceAST: Node, import : Use, returnGamma : Context, typedFile : File?) : Context {
        try {
            if (interfaceAST is Interface) {
                if (inFile.extension == "eta" && interfaceAST is RhoInterface){
                    throw SemanticError(import.terminal.line, import.terminal.column, "Rho interface used by eta file", inFile)
                }
                libraries[import.lib] = interfaceAST
                return bindInterfaceDefinitions(importPath, interfaceAST, returnGamma)
            } else {
                throw SemanticError(
                    import.terminal.line,
                    import.terminal.column,
                    "Could not import interface ${import.lib} AST",
                    inFile
                )
            }
        } catch (e: CompilerError) {
            typedFile?.appendText("${import.terminal.line}:${import.terminal.column} error:Error in interface file ${import.lib} preventing 'use'")
            throw e
        }
    }


    fun bindInterfaceMethod(inFile:File, header: Method, returnGamma: Context) {
        val domainList = ArrayList<EtaType.OrdinaryType>()
        for (decl in header.args) {
            domainList.add(EtaType.translateType(decl.type))
        }
        val codomainList = ArrayList<EtaType.OrdinaryType>()
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
    }

    fun bindInterfaceRecord(inFile:File, header: RhoRecord, returnGamma: Context) {
        val fieldMap = linkedMapOf<String, EtaType.OrdinaryType>()
        for (decl in header.fields) {
            for (id in decl.ids) {
                fieldMap[id.name] = EtaType.translateType(decl.type)
            }
        }
        val currRecordType = EtaType.ContextType.RecordType(header.name, fieldMap)

        if (returnGamma.contains(header.name)) { // has to obey subtyping relation
            val existingRecordType = returnGamma.lookup(header.name)
            if (existingRecordType !is EtaType.ContextType.RecordType) {
                throw SemanticError(header.terminal.line, header.terminal.column, "Conflicting record definition in Interface", inFile)
            }
            else {
                checkRecordSubtyping(header, existingRecordType, inFile)
            }
        }
        else {
            header.etaType = currRecordType
            returnGamma.bind(header.name, currRecordType)
        }
    }

    fun bindInterfaceDefinitions(inFile: File, interfaceAST: Interface, returnGamma: Context) : Context {
        //deal with interfaces allowing uses and also having interface headers
        for (header in interfaceAST.headers) {
            if (header is Method) {
                bindInterfaceMethod(inFile, header, returnGamma)
            } else if (header is RhoRecord){
                bindInterfaceRecord(inFile, header, returnGamma)
            }
        }
        return returnGamma
    }
}