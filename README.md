# Aho–Corasick Multiple Pattern Searching (Java)

## What this project does
This project finds all occurrences of several patterns in one text using Aho–Corasick. It also compares the running time with KMP, which searches for each pattern separately. Overlapping matches are counted.

## Files
- `Main.java` — trie, failure links, output links, Aho–Corasick search, KMP, and benchmark.
- `RESULTS.md` — measured experiment results.
- `Notes 1` — Handwritten Notes.
- - `Notes 2` — Handwritten Notes.


## How to run
Install Java 8 or newer, then run:

```bash
javac Main.java
java Main
```

The program first prints a small correctness example, then runs all 9 benchmark cases.

## How it works
1. **Trie:** Insert every pattern character by character. Shared prefixes use the same nodes. Each ending node stores its pattern ID.
2. **Failure links:** Build these using breadth-first search (a queue). If the current character cannot be followed, a failure link takes us to the longest useful suffix, without starting from the beginning.
3. **Output links:** Each node can point to the nearest failure ancestor that ends a pattern. This also finds shorter patterns that finish at the same text position.
4. **Search:** Read the text once, follow trie edges or failure links, and report matches at the current node and along output links. The reported index is zero-based.

## Complexity
Let `P` be the sum of pattern lengths, `N` the text length, and `Z` the number of matches.

- Trie construction: **O(P)** expected time.
- Failure/output link construction: **O(P × L)** worst case for this simple implementation, where `L` is the maximum pattern length, because it may walk through several failure links for each edge. With a fixed alphabet and a fully completed transition table, preprocessing can be **O(P × alphabet size)**.
- Searching: **O(N + Z)** amortized time for the failure-link traversal and reporting all matches.
- Space: **O(P)** for trie nodes, edges, failure links, output links, and stored pattern IDs (using hash maps and a fixed-size character representation).

For the common fixed-alphabet formulation, Aho–Corasick is often summarized as **O(P + N + Z)** overall, with linear preprocessing using an appropriate transition representation. The code here uses hash maps for simplicity and can take longer to build links in the worst case.

**KMP:** Searching each of `K` patterns separately takes **O(K × N + P)** time, with **O(maximum pattern length)** extra space if each prefix table is processed one at a time.

## Experiment method
- Pattern counts: **10, 100, 1,000**.
- Text lengths: **1,000, 10,000, 100,000** characters.
- Each of the 9 combinations is run **5 times**; the average is measured with `System.nanoTime()`.
- Both timings include preprocessing/building and searching, but exclude test-data generation and JVM warm-up.
- Random lowercase letters are generated with a fixed seed for repeatable input. Patterns are 4–8 characters long. Up to 10 patterns are inserted into the text to ensure matches.
- Aho–Corasick and KMP match counts are checked to be equal.
- Three warm-up runs per case are not included in the average.

 for measured times from one run on the testing machine. Results may differ by computer and Java runtime.

## Discussion
Aho–Corasick usually becomes much faster than separate KMP searches when the number of patterns and the text are large, because it scans the text only once. For small tests, setup costs and JVM timing noise can make KMP competitive or even faster. Aho–Corasick needs extra memory for the trie and links; KMP is simpler and a good choice for one pattern or a few patterns. For a large pattern set, Aho–Corasick is usually the better fit.



