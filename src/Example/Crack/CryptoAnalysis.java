package Example.Crack;

import java.util.*;

public class CryptoAnalysis {
    private static final String PORTUGUESE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // Portuguese letter frequencies (A-Z, in order, as percentages)
    private static final double[] PORTUGUESE_FREQ = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40,
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    // Cleans input, keeps only uppercase letters
    public static String clean(String input) {
        return input.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    // Kasiski Examination to guess key length
    public static int kasiskiExamination(String ciphertext) {
        Map<String, List<Integer>> trigramPositions = new HashMap<>();
        for (int i = 0; i < ciphertext.length() - 2; i++) {
            String trigram = ciphertext.substring(i, i+3);
            trigramPositions.computeIfAbsent(trigram, k -> new ArrayList<>()).add(i);
        }
        List<Integer> distances = new ArrayList<>();
        for (List<Integer> positions : trigramPositions.values()) {
            if (positions.size() > 1) {
                for (int i = 1; i < positions.size(); i++) {
                    distances.add(positions.get(i) - positions.get(i-1));
                }
            }
        }
        Map<Integer, Integer> gcdCounts = new HashMap<>();
        for (int i = 0; i < distances.size(); i++) {
            for (int j = i+1; j < distances.size(); j++) {
                int gcd = gcd(distances.get(i), distances.get(j));
                if (gcd > 1 && gcd < 21) { // likely key lengths
                    gcdCounts.put(gcd, gcdCounts.getOrDefault(gcd, 0) + 1);
                }
            }
        }
        return gcdCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1); // Default to 1 if nothing found
    }

    // Greatest Common Divisor
    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    // Index of Coincidence for a given key length
    public static double indexOfCoincidence(String text, int keyLen) {
        double sum = 0.0;
        for (int i = 0; i < keyLen; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < text.length(); j += keyLen) {
                sb.append(text.charAt(j));
            }
            sum += singleIC(sb.toString());
        }
        return sum / keyLen;
    }

    private static double singleIC(String s) {
        int[] count = new int[26];
        for (char c : s.toCharArray()) {
            count[c - 'A']++;
        }
        int N = s.length();
        double ic = 0.0;
        for (int n : count) {
            ic += n * (n - 1);
        }
        return ic / (N * (N - 1.0));
    }

    // Finds the key using frequency analysis for a given key length
    public static String findKey(String ciphertext, int keyLen) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < keyLen; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < ciphertext.length(); j += keyLen) {
                sb.append(ciphertext.charAt(j));
            }
            key.append(mostLikelyShift(sb.toString()));
        }
        return key.toString();
    }

    // Returns the most likely letter (A-Z) for this segment using Portuguese frequencies
    private static char mostLikelyShift(String segment) {
        int bestShift = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int shift = 0; shift < 26; shift++) {
            int[] freq = new int[26];
            for (char c : segment.toCharArray()) {
                int shifted = (c - 'A' - shift + 26) % 26;
                freq[shifted]++;
            }
            double score = 0.0, total = segment.length();
            for (int i = 0; i < 26; i++) {
                double observed = freq[i] / total;
                score += observed * Math.log(PORTUGUESE_FREQ[i] + 1e-6); // avoid log(0)
            }
            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }
        return (char) ('A' + bestShift);
    }

    // Decrypts ciphertext with the found key
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
    // Returns the minimal period of the found key (e.g., CHAVE in CHAVECHAVE)
    public static String minimalPeriod(String key) {
        int n = key.length();
        for (int p = 1; p <= n; p++) {
            if (n % p == 0) {
                String period = key.substring(0, p);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n / p; i++) sb.append(period);
                if (sb.toString().equals(key)) return period;
            }
        }
        return key;
    }

    // Full pipeline
    public static void main(String[] args) {
        String ciphertext = "PHQPINLFDQFLTVVFLEIUWHNOSQZOGQGYGPPJHVVRQOOMMBVNOIFVUMEPKOJWVLLCEFVSYERLQPIPHVDPCVSHSTHDJVGZCJQGÇHRVQCZEMIWUIMRCWRVÇEELNOVCSOIHGVVZPJVAIGKÃVCJRVHRDEOHINYOHDZWWHSCMUAÓRDEUSEIHÁTPANWQIRZXGTPJWCUTDKQZHZVÓKZENUWLCDHQZEWEVHLCEUXUZQQSDVVCTOYIUAIISFVMPRFVTJHQZENGWAAQEOLMNMNÊUCDSEVMJPJVSVVTLGVPCKONIEVRVÇÕIUHCZPGYAYSUJOHSULCVHCWAGEXYAOMXLSNIQWOYITKEOVCGEMÀZKKAJWGJONHQWANWCKO";
        ciphertext = clean(ciphertext);

        int keyLen = kasiskiExamination(ciphertext);
        String key = findKey(ciphertext, keyLen);
        String minKey = minimalPeriod(key);
        String plaintext = decrypt(ciphertext, minKey);

        System.out.println("Estimated key length: " + keyLen);
        System.out.println("Estimated key: " + key);
        System.out.println("Minimal period key: " + minKey);
        System.out.println("Plaintext: " + plaintext);
    }
}