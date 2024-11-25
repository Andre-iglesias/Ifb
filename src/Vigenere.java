import java.util.Scanner;

public class Vigenere {

    public static String limpa(String text)
    {
        String res = "";
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                res += c;
            }
        }
        return res;
    }

    public static String cifra(String text,  String key)
    {
        String res = "";
        int j = 0;
        text = limpa(text);
        key = limpa(key);
        text = text.toUpperCase();

        key = key.toUpperCase();

        int tamanhoInicialChave = key.length();
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                if (j < tamanhoInicialChave) {
                    if (c >= (char)128){
                        res += c;


                    }else{
                        res += (char) ((((c + key.charAt(j)) - 2  * 65) % 26) + 65);
                        j++;
                    }
                }else {
                    j=0;
                    i--;
                }

            }
        }
        return res;
    }

    public static String decifra(String text, String key)
    {
        String res = "";        text = limpa(text);
        key = limpa(key);
        text = text.toUpperCase();

        key = key.toUpperCase();
        int j = 0;
        int tamanhoInicialChave = key.length();
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (j < tamanhoInicialChave) {
                if (c >= (char)128){
                    res += c;

                }else{res += (char) ((c - key.charAt(j) + 26) % 26 + 65);

                    j++;
                }
            }else {
                j=0;
                i--;
            }


        }
        return res;
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite 1 para cifrar ou 2 para decifrar:");
        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Digite a chave:");
        String key = scanner.nextLine();
        System.out.println("Digite a mensagem:");
        String message = scanner.nextLine();

        switch (choice) {
            case 1:
                String encrypted = cifra(message, key);
                System.out.println("Mensagem criptografada: " + encrypted);
                break;
            case 2:
                String decrypted = decifra(message, key);
                System.out.println("Mensagem descriptografada: " + decrypted);
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

}
