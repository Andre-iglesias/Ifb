package Example.Crack;

import java.util.Scanner;

        public class CryptoAnalysis {

            private static final double[] EN_FREQUENCIES = {
                    0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015,
                    0.06094, 0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749,
                    0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758,
                    0.00978, 0.02360, 0.00150, 0.01974, 0.00074
            };

            private static final double[] PT_FREQUENCIES = {
                    0.1463, 0.0104, 0.0388, 0.0499, 0.1257, 0.0102, 0.0130, 0.0128,
                    0.0618, 0.0040, 0.0002, 0.0278, 0.0474, 0.0505, 0.1073, 0.0252,
                    0.012, 0.0653, 0.0781, 0.0434, 0.0463, 0.0167, 0.0001, 0.0021,
                    0.0001, 0.0047
            };

            private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

            private static String filterString(String input) {
                return input.toLowerCase().replaceAll("[^a-z]", "");
            }

            public static void showCoincidences(String cipherText) {
                int cipherLength = cipherText.length();
                for (int offset = 1; offset <= cipherLength / 2; offset++) {
                    int counter = 0;
                    for (int i = 0; i < cipherLength - offset; i++) {
                        if (cipherText.charAt(i) == cipherText.charAt(i + offset)) {
                            counter++;
                        }
                    }
                    System.out.println(offset + ": " + counter);
                }
            }

            private static double getC(String sequence) {
                double N = sequence.length();
                double frequencySum = 0.0;

                for (char letter : LETTERS.toCharArray()) {
                    long count = sequence.chars().filter(ch -> ch == letter).count();
                    frequencySum += count * (count - 1);
                }

                return (N * (N - 1)) > 0 ? (frequencySum / (N * (N - 1))) * 26 : 0;
            }

            private static void probableKeyLength(String cipherText, boolean isEnglish) {


                for (int guess = 1; guess <= 20; guess++) {
                    double coincidenceSum = 0.0;

                    for (int i = 0; i < guess; i++) {
                        StringBuilder sequence = new StringBuilder();
                        for (int j = i; j < cipherText.length(); j += guess) {
                            sequence.append(cipherText.charAt(j));
                        }
                        coincidenceSum += getC(sequence.toString());
                    }

                    System.out.println(guess + ": " + coincidenceSum);
                }
            }

            private static char frequencies(String seq, boolean isEnglish) {
                double[] letterFrequencies = isEnglish ? EN_FREQUENCIES : PT_FREQUENCIES;
                double[] chiSquaredArray = new double[26];

                for (int i = 0; i < 26; i++) {
                    double sumSq = 0.0;
                    int[] counts = new int[26];

                    for (char ch : seq.toCharArray()) {
                        int shifted = ((ch - 'a' - i + 26) % 26);
                        counts[shifted]++;
                    }

                    for (int j = 0; j < 26; j++) {
                        double observed = counts[j] / (double) seq.length();
                        double expected = letterFrequencies[j];
                        sumSq += Math.pow(observed - expected, 2) / expected;
                    }

                    chiSquaredArray[i] = sumSq;
                }

                int shift = 0;
                double minChi = chiSquaredArray[0];
                for (int i = 1; i < 26; i++) {
                    if (chiSquaredArray[i] < minChi) {
                        minChi = chiSquaredArray[i];
                        shift = i;
                    }
                }

                return (char) (shift + 'a');
            }

            private static String getKey(String cipherText, int keyLength, boolean isEnglish) {
                StringBuilder key = new StringBuilder();

                for (int i = 0; i < keyLength; i++) {
                    StringBuilder sequence = new StringBuilder();
                    for (int j = i; j < cipherText.length(); j += keyLength) {
                        sequence.append(cipherText.charAt(j));
                    }
                    key.append(frequencies(sequence.toString(), isEnglish));
                }

                return key.toString();
            }

            public static void main(String[] args) {
                Scanner scanner = new Scanner(System.in);

                System.out.print("Digite 'en' para inglês e 'pt' para português: ");
                String language = scanner.nextLine();
                boolean isEnglish = language.equalsIgnoreCase("en");

                System.out.print("Digite o texto cifrado: ");
                String cipherText = filterString(scanner.nextLine());

                probableKeyLength(cipherText, isEnglish);

                System.out.print("Digite o tamanho da chave: ");
                int keyLength = scanner.nextInt();

                System.out.println("Possível chave: " + getKey(cipherText, keyLength, isEnglish));
            }
        }
