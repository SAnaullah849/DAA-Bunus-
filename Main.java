import java.util.*;

public class Main {
    static class Node {
        Map<Character, Node> next = new HashMap<>();
        Node fail;
        Node outputLink;
        List<Integer> patternIds = new ArrayList<>();
    }

    static class AhoCorasick {
        Node root = new Node();
        List<String> patterns;

        AhoCorasick(List<String> patterns) {
            this.patterns = patterns;
            for (int i = 0; i < patterns.size(); i++) {
                Node current = root;
                for (char c : patterns.get(i).toCharArray()) {
                    current = current.next.computeIfAbsent(c, key -> new Node());
                }
                current.patternIds.add(i);
            }
            buildFailureLinks();
        }

        void buildFailureLinks() {
            Queue<Node> queue = new ArrayDeque<>();
            root.fail = root;
            for (Node child : root.next.values()) {
                child.fail = root;
                queue.add(child);
            }

            while (!queue.isEmpty()) {
                Node current = queue.remove();
                for (Map.Entry<Character, Node> entry : current.next.entrySet()) {
                    char c = entry.getKey();
                    Node child = entry.getValue();
                    Node fallback = current.fail;
                    while (fallback != root && !fallback.next.containsKey(c)) {
                        fallback = fallback.fail;
                    }
                    child.fail = fallback.next.getOrDefault(c, root);
                    child.outputLink = !child.fail.patternIds.isEmpty()
                            ? child.fail : child.fail.outputLink;
                    queue.add(child);
                }
            }
        }

        // Counts all matches, including overlapping matches.
        long search(String text) {
            return search(text, false);
        }

        long search(String text, boolean printMatches) {
            Node current = root;
            long matches = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                while (current != root && !current.next.containsKey(c)) {
                    current = current.fail;
                }
                current = current.next.getOrDefault(c, root);

                Node found = current;
                while (found != null) {
                    for (int id : found.patternIds) {
                        matches++;
                        if (printMatches) {
                            System.out.println(patterns.get(id) + " found at index "
                                    + (i - patterns.get(id).length() + 1));
                        }
                    }
                    found = found.outputLink;
                }
            }
            return matches;
        }
    }

    static int[] prefixTable(String pattern) {
        int[] prefix = new int[pattern.length()];
        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = prefix[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            prefix[i] = j;
        }
        return prefix;
    }

    static long kmpSearch(String text, List<String> patterns) {
        long matches = 0;
        for (String pattern : patterns) {
            int[] prefix = prefixTable(pattern);
            int j = 0;
            for (int i = 0; i < text.length(); i++) {
                while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                    j = prefix[j - 1];
                }
                if (text.charAt(i) == pattern.charAt(j)) j++;
                if (j == pattern.length()) {
                    matches++;
                    j = prefix[j - 1];
                }
            }
        }
        return matches;
    }

    static String randomWord(Random random, int length) {
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < length; i++) {
            word.append((char) ('a' + random.nextInt(26)));
        }
        return word.toString();
    }

    static void benchmark(int patternCount, int textLength) {
        Random random = new Random(42 + patternCount + textLength);
        List<String> patterns = new ArrayList<>();
        for (int i = 0; i < patternCount; i++) {
            patterns.add(randomWord(random, 4 + random.nextInt(5)));
        }
        StringBuilder textBuilder = new StringBuilder(randomWord(random, textLength));
        // Put a few known patterns in the text, so there are actual matches.
        for (int i = 0; i < Math.min(patternCount, 10); i++) {
            String word = patterns.get(i);
            int position = random.nextInt(textLength - word.length() + 1);
            textBuilder.replace(position, position + word.length(), word);
        }
        String text = textBuilder.toString();

        // Warm up the JVM before measuring.
        for (int i = 0; i < 3; i++) {
            new AhoCorasick(patterns).search(text);
            kmpSearch(text, patterns);
        }

        long ahoTime = 0;
        long kmpTime = 0;
        long ahoMatches = 0;
        long kmpMatches = 0;
        for (int run = 0; run < 5; run++) {
            long start = System.nanoTime();
            AhoCorasick aho = new AhoCorasick(patterns);
            ahoMatches = aho.search(text);
            ahoTime += System.nanoTime() - start;

            start = System.nanoTime();
            kmpMatches = kmpSearch(text, patterns);
            kmpTime += System.nanoTime() - start;
        }
        if (ahoMatches != kmpMatches) {
            throw new IllegalStateException("Aho-Corasick and KMP match counts differ!");
        }
        System.out.printf(Locale.US, "%8d | %11d | %12.3f | %10.3f | %7d%n",
                patternCount, textLength, ahoTime / 5_000_000.0,
                kmpTime / 5_000_000.0, ahoMatches);
    }

    public static void main(String[] args) {
        System.out.println("Small example (zero-based positions):");
        List<String> example = Arrays.asList("he", "she", "his", "hers");
        new AhoCorasick(example).search("ahishers", true);

        System.out.println("\nAverage time over 5 runs (build + search, milliseconds):");
        System.out.println("Patterns | Text length | Aho-Corasick |        KMP | Matches");
        System.out.println("---------+-------------+--------------+------------+--------");
        int[] counts = {10, 100, 1000};
        int[] lengths = {1000, 10000, 100000};
        for (int count : counts) {
            for (int length : lengths) {
                benchmark(count, length);
            }
        }
    }
}
