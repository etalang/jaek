use io
use conv
use kmp

main(args:int[][]) {
    if (length(args) < 3) {
        print("Usage: kmp_test <file> <string>\n")
        return
    }

    p: int[] = args[2]

    in: FileInput = readFile(args[1])

    if (in == null) {
        println("Couldn't open " + args[1])
        return
    }

    s:int[] = ""
    while (!in.feof()) {
        s = s + in.gets()
    }

    precomputed:int[] = begin(s, p)

    i:int = 0
    j:int = 0
    while (i != -1) {
        i = next(s,p,precomputed, i)
        if (i != -1) {
            print("Match found at offset ")
            println (unparseInt(i))
            j = j + 1
        }
    }

    println(unparseInt(j) + " matches found.")
}
