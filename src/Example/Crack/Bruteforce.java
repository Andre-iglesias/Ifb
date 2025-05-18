package Example.Crack;

import java.util.*;

public class Bruteforce {
    // Portuguese letter frequencies (A-Z, in order, as percentages)
    private static final double[] PORTUGUESE_FREQ = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40,
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    public static String clean(String input) {
        return input.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    // Decrypts ciphertext with the given key
    public static String decrypt(String ciphertext, String key) {
        StringBuilder sb = new StringBuilder();
        int keyLen = key.length();
        for (int i = 0; i < ciphertext.length(); i++) {
            char c = ciphertext.charAt(i);
            char k = key.charAt(i % keyLen);
            int p = (c - k + 26) % 26;
            sb.append((char) ('A' + p));
        }
        return sb.toString();
    }

    // Scores the text by comparing its letter frequency to Portuguese
    private static double scorePortuguese(String text) {
        int[] count = new int[26];
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') count[c - 'A']++;
        }
        double score = 0.0, total = text.length();
        for (int i = 0; i < 26; i++) {
            double observed = count[i] / total;
            score += -Math.abs(observed - (PORTUGUESE_FREQ[i] / 100.0));
        }
        return score;
    }

    // Recursively generates all keys of a given length
    private static void bruteForce(String ciphertext, int keyLen, char[] key, int pos, List<Result> results, int limit) {
        if (pos == keyLen) {
            String k = new String(key);
            String pt = decrypt(ciphertext, k);
            double s = scorePortuguese(pt);
            results.add(new Result(k, s, pt));
            if (results.size() > limit) {
                // Keep only the best 'limit' results
                results.sort((a, b) -> Double.compare(b.score, a.score));
                results.subList(limit, results.size()).clear();
            }
            return;
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            key[pos] = c;
            bruteForce(ciphertext, keyLen, key, pos + 1, results, limit);
        }
    }

    public static void bruteForceAll(String ciphertext, int minLen, int maxLen, int resultLimit) {
        List<Result> allResults = new ArrayList<>();
        for (int keyLen = minLen; keyLen <= maxLen; keyLen++) {
            System.out.println("Trying key length: " + keyLen);
            List<Result> results = new ArrayList<>();
            bruteForce(ciphertext, keyLen, new char[keyLen], 0, results, resultLimit);
            allResults.addAll(results);
        }
        // Show best results
        allResults.sort((a, b) -> Double.compare(b.score, a.score));
        for (int i = 0; i < Math.min(resultLimit, allResults.size()); i++) {
            Result r = allResults.get(i);
            System.out.println("Key: " + r.key + " | Score: " + r.score);
            System.out.println("Plaintext: " + r.plaintext.substring(0, Math.min(100, r.plaintext.length())) + (r.plaintext.length() > 100 ? "..." : ""));
            System.out.println();
        }
    }

    static class Result {
        String key;
        double score;
        String plaintext;

        Result(String key, double score, String plaintext) {
            this.key = key;
            this.score = score;
            this.plaintext = plaintext;
        }
    }

    public static void main(String[] args) {
        String ciphertext = "GMUKCUDCHNQQCVOTWNP";
        ciphertext = clean(ciphertext);

        // Try all keys of length 2 to 3 (higher lengths are VERY slow)
        bruteForceAll(ciphertext, 2, 2, 675); // Show top 10 results per length
    }
}