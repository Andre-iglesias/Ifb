package Example.Crack;

import java.util.*;

public class Bruteforce {

    // Frequências relativas das letras (A-Z) em textos em português, em porcentagem.
    private static final double[] PORTUGUESE_FREQ = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40,
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    // Remove caracteres não-alfabéticos e converte tudo para maiúsculo
    public static String clean(String input) {
        return input.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    // Função que decifra um texto cifrado com a cifra de Vigenère usando uma chave
    public static String decrypt(String ciphertext, String key) {
        StringBuilder sb = new StringBuilder();
        int keyLen = key.length();
        for (int i = 0; i < ciphertext.length(); i++) {
            char c = ciphertext.charAt(i);      // Letra cifrada
            char k = key.charAt(i % keyLen);    // Letra correspondente da chave (repetida)
            int p = (c - k + 26) % 26;          // Cálculo da letra original (mod 26 para manter no alfabeto)
            sb.append((char) ('A' + p));        // Converte de volta para caractere
        }
        return sb.toString(); // Retorna o texto plano decifrado
    }

    // Calcula um "score" de similaridade com o português baseado na frequência de letras
    private static double scorePortuguese(String text) {
        int[] count = new int[26]; // Contador de cada letra
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') count[c - 'A']++;
        }
        double score = 0.0, total = text.length();
        for (int i = 0; i < 26; i++) {
            double observed = count[i] / total; // Frequência observada
            // Penaliza a diferença absoluta entre a frequência observada e a esperada
            score += -Math.abs(observed - (PORTUGUESE_FREQ[i] / 100.0));
        }
        return score;
    }

    // Gera todas as combinações possíveis de chaves de tamanho keyLen e avalia cada uma
    private static void bruteForce(String ciphertext, int keyLen, char[] key, int pos, List<Result> results, int limit) {
        // Quando a chave está completa
        if (pos == keyLen) {
            String k = new String(key);               // Converte chave para string
            String pt = decrypt(ciphertext, k);       // Decifra texto com essa chave
            double s = scorePortuguese(pt);           // Avalia o texto
            results.add(new Result(k, s, pt));        // Armazena resultado

            // Mantém apenas os 'limit' melhores resultados
            if (results.size() > limit) {
                results.sort((a, b) -> Double.compare(b.score, a.score));
                results.subList(limit, results.size()).clear();
            }
            return;
        }

        // Para cada letra do alfabeto, continua recursivamente a construção da chave
        for (char c = 'A'; c <= 'Z'; c++) {
            key[pos] = c;
            bruteForce(ciphertext, keyLen, key, pos + 1, results, limit);
        }
    }

    // Roda o brute force para todos os tamanhos de chave no intervalo [minLen, maxLen]
    public static void bruteForceAll(String ciphertext, int minLen, int maxLen, int resultLimit) {
        List<Result> allResults = new ArrayList<>();
        for (int keyLen = minLen; keyLen <= maxLen; keyLen++) {
            System.out.println("Trying key length: " + keyLen);
            List<Result> results = new ArrayList<>();
            bruteForce(ciphertext, keyLen, new char[keyLen], 0, results, resultLimit);
            allResults.addAll(results);
        }

        // Ordena todos os resultados pelo score (melhores primeiro)
        allResults.sort((a, b) -> Double.compare(b.score, a.score));

        // Exibe os melhores resultados encontrados
        for (int i = 0; i < Math.min(resultLimit, allResults.size()); i++) {
            Result r = allResults.get(i);
            System.out.println("Key: " + r.key + " | Score: " + r.score);
            System.out.println("Plaintext: " + r.plaintext.substring(0, Math.min(100, r.plaintext.length())) + (r.plaintext.length() > 100 ? "..." : ""));
            System.out.println();
        }
    }

    // Classe auxiliar que representa um resultado (chave, pontuação, texto claro)
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

    // Função principal: limpa o texto e executa o brute force
    public static void main(String[] args) {
        String ciphertext = "GMUKCUDCHNQQCVOTWNP";
        ciphertext = clean(ciphertext);

        // Tenta todas as chaves de tamanho 2 (pode aumentar, mas vai ficar muito lento)
        bruteForceAll(ciphertext, 2, 2, 675); // Mostra até 675 melhores resultados
    }
}
