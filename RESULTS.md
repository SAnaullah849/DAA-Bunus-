# Experimental Results

Measured using Java 21 (`System.nanoTime()`), average of 5 timed runs for each combination. Each timed run includes algorithm preprocessing and search. Data generation and 3 JVM warm-up runs are excluded. Time is in milliseconds (ms).

| Patterns | Text characters | Aho–Corasick average (ms) | KMP average (ms) | Matches |
|---:|---:|---:|---:|---:|
| 10 | 1,000 | 0.479 | 0.830 | 9 |
| 10 | 10,000 | 0.832 | 0.738 | 10 |
| 10 | 100,000 | 1.475 | 3.899 | 10 |
| 100 | 1,000 | 0.573 | 0.494 | 10 |
| 100 | 10,000 | 0.599 | 4.010 | 10 |
| 100 | 100,000 | 2.211 | 41.902 | 13 |
| 1,000 | 1,000 | 1.222 | 4.691 | 8 |
| 1,000 | 10,000 | 0.946 | 43.509 | 12 |
| 1,000 | 100,000 | 5.161 | 412.468 | 50 |

## Observations

Aho–Corasick is especially effective when searching many patterns in a long text. For 1,000 patterns and 100,000 text characters, it took about 5.161 ms compared with 412.468 ms for separate KMP searches in this experiment. Small cases can favor KMP because building a trie has an initial cost. Short timing results can vary because of JVM optimization, garbage collection, and system load.

The number of matches may be less than the number of patterns because the inserted pattern strings can overwrite each other in the generated text.
