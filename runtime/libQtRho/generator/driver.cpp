/*
 * Generator utility to help make QtXi
 *
 * This code is based on IceMaker:
 *
 *  A utilitity for building various tables and specializations for the
 *  KJS Frostbyte bytecode
 *
 *  Copyright (C) 2007, 2008 Maks Orlovich (maksim@kde.org)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this library; see the file COPYING.LIB.  If not, write to
 *  the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA 02110-1301, USA.
 *
 */

#include "filetemplate.h"
#include "bindgen.h"

int main(int argc, char* argv[])
{
    if (argc < 2 || argc > 3) {
        std::cerr << "Usage: makebindings <path> [<idl file>]\n";
	return 1;
    }

    std::string path = std::string(argv[1]) + "/";
    std::string proj = (argc == 3) ? std::string(argv[2]) : "qt";

    ifstream def;
    std::string idlFile = path + proj + ".idl";
    def.open(idlFile.c_str());
    if (def.fail()) {
        std::cerr << "Unable to open " << idlFile << "\n";
        return -1;
    }

    FileTemplate qtH  (proj, path + "bind" + proj + ".h.in",   "bind" + proj + ".h");
    FileTemplate qtCpp(proj, path + "bind" + proj + ".cpp.in", "bind" + proj + ".cpp");
    FileTemplate xi   (proj, path + proj + ".ri.in",  proj + ".ri");

    if (!qtH.ok() || !qtCpp.ok() || !xi.ok())
        return -1;

    BindGen build(&def, &qtH.out, &qtCpp.out, &xi.out);
    build.generateCode();
    return 0;
}

// kate: indent-width 4; replace-tabs on; tab-width 4; space-indent on;
