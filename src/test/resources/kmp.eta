match(t:int[], p:int[]) : int {
    return matcher(t, p, prefix(p), 1)
}

begin(t:int[], p:int[]) : int[] {
    return prefix(p)
}

next(t:int[], p:int[], pi:int[], i:int):int {
    return matcher(t, p, pi, i+1)
}

matcher(t:int[], p:int[], pi:int[], i_:int) : int {
    i:int = i_
    n:int = length(t)
    m:int = length(p)

    q:int = 0

    while i <= n {
        while (q > 0 & p[q] != t[i-1]) q = pi[q-1]
        if (p[q] == t[i-1]) q = q + 1
        if q == m { return i-m+1 }

        i = i+1
    }
    return -1
}

prefix(p:int[]) : int[] {
    m:int = length(p)
    pi:int[m]
    k:int = 0
    q:int = 2
    while q <= m {
        while (k > 0 & p[k] != p[q-1]) k = pi[k-1]
        if (p[k] == p[q-1]) k = k + 1
        pi[q-1] = k
        q = q + 1
    }
    return pi
}
