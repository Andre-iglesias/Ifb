package Example.Crack;

import java.util.*;

public class CryptoAnalysis {

    // Alfabeto em português sem acentos (somente letras maiúsculas)
    private static final String PORTUGUESE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Frequência percentual das letras do alfabeto em português (A-Z)
    private static final double[] PORTUGUESE_FREQ = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40,
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    // Remove tudo que não for letra e converte para maiúsculo
    public static String clean(String input) {
        return input.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    // Exame de Kasiski: tenta descobrir o comprimento provável da chave
    public static int kasiskiExamination(String ciphertext) {
        Map<String, List<Integer>> trigramPositions = new HashMap<>();

        // Percorre o texto procurando trigramas (sequências de 3 letras)
        for (int i = 0; i < ciphertext.length() - 2; i++) {
            String trigram = ciphertext.substring(i, i + 3);
            trigramPositions.computeIfAbsent(trigram, k -> new ArrayList<>()).add(i);
        }

        // Calcula distâncias entre ocorrências dos mesmos trigramas
        List<Integer> distances = new ArrayList<>();
        for (List<Integer> positions : trigramPositions.values()) {
            if (positions.size() > 1) {
                for (int i = 1; i < positions.size(); i++) {
                    distances.add(positions.get(i) - positions.get(i - 1));
                }
            }
        }

        // Conta os divisores comuns (MDC) das distâncias encontradas
        Map<Integer, Integer> gcdCounts = new HashMap<>();
        for (int i = 0; i < distances.size(); i++) {
            for (int j = i + 1; j < distances.size(); j++) {
                int gcd = gcd(distances.get(i), distances.get(j));
                if (gcd > 1 && gcd < 21) { // valores prováveis para comprimento da chave
                    gcdCounts.put(gcd, gcdCounts.getOrDefault(gcd, 0) + 1);
                }
            }
        }

        // Retorna o comprimento com mais ocorrências como estimativa
        return gcdCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1); // Se nada for encontrado, assume 1
    }

    // Calcula o Máximo Divisor Comum (MDC)
    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    // Calcula o índice de coincidência médio para um dado comprimento de chave
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

    // Calcula o índice de coincidência para uma sequência de letras
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

    // Descobre a chave usando análise de frequência com base no português
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

    // Descobre o melhor deslocamento (letra da chave) para um segmento
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
                score += observed * Math.log(PORTUGUESE_FREQ[i] + 1e-6); // evita log(0)
            }

            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }

        return (char) ('A' + bestShift);
    }

    // Decifra o texto usando a chave encontrada
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

    // Identifica o menor período que se repete na chave (ex: CHAVE em CHAVECHAVE)
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

    // Execução completa do ataque
    public static void main(String[] args) {
        // Texto cifrado (criptografado com cifra de Vigenère)
        String ciphertext = "PHQPINLFDQFLTVVFLEIUWHNOSQZOGQGYGPPJHVVRQOOMMBVNOIFVUMEPKOJWVLLCEFVSYERLQPIPHVDPCVSHSTHDJVGZCJQGÇHRVQCZEMIWUIMRCWRVÇEELNOVCSOIHGVVZPJVAIGKÃVCJRVHRDEOHINYOHDZWWHSCMUAÓRDEUSEIHÁTPANWQIRZXGTPJWCUTDKQZHZVÓKZENUWLCDHQZEWEVHLCEUXUZQQSDVVCTOYIUAIISFVMPRFVTJHQZENGWAAQEOLMNMNÊUCDSEVMJPJVSVVTLGVPCKONIEVRVÇÕIUHCZPGYAYSUJOHSULCVHCWAGEXYAOMXLSNIQWOYITKEOVCGEMÀZKKAJWGJONHQWANWCKO";

        ciphertext = clean(ciphertext); // Limpa o texto: só letras maiúsculas

        int keyLen = kasiskiExamination(ciphertext); // Estima o tamanho da chave
        String key = findKey(ciphertext, keyLen);    // Tenta descobrir a chave
        String minKey = minimalPeriod(key);          // Reduz chave à menor repetição
        String plaintext = decrypt(ciphertext, minKey); // Decifra com a chave

        // Exibe os resultados
        System.out.println("Estimated key length: " + keyLen);
        System.out.println("Estimated key: " + key);
        System.out.println("Minimal period key: " + minKey);
        System.out.println("Plaintext: " + plaintext);
    }
}
