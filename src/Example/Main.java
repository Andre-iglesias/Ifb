package Example;

import Example.Cipher.Vigenere;
import Example.Crack.CryptoAnalysis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Vigenère Cipher & Crack Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        // Tabs for different functionalities
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Encrypt/Decrypt Panel ---
        JPanel edPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField keyField = new JTextField();
        JTextArea messageArea = new JTextArea(3, 20);
        JTextArea resultArea = new JTextArea(3, 20);
        resultArea.setEditable(false);

        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");

        encryptButton.addActionListener(e -> {
            String key = keyField.getText();
            String message = messageArea.getText();
            String result = Vigenere.cifra(message, key);
            resultArea.setText(result);
        });

        decryptButton.addActionListener(e -> {
            String key = keyField.getText();
            String message = messageArea.getText();
            String result = Vigenere.decifra(message, key);
            resultArea.setText(result);
        });

        edPanel.add(new JLabel("Key:"));
        edPanel.add(keyField);
        edPanel.add(new JLabel("Message:"));
        edPanel.add(new JScrollPane(messageArea));
        edPanel.add(encryptButton);
        edPanel.add(decryptButton);
        edPanel.add(new JLabel("Result:"));
        edPanel.add(new JScrollPane(resultArea));

        tabbedPane.add("Encrypt/Decrypt", edPanel);

        // --- Crack Panel (simplified, as CryptoAnalysis is mostly console-based) ---
        JPanel crackPanel = new JPanel(new BorderLayout());
        JTextArea crackInput = new JTextArea(3, 20);
        JTextArea crackOutput = new JTextArea(8, 30);
        crackOutput.setEditable(false);
        JButton coincidencesButton = new JButton("Show Coincidences");

        coincidencesButton.addActionListener((ActionEvent e) -> {
            // For demonstration, just filter string and show coincidences
            String cipherText = crackInput.getText();
            // CryptoAnalysis.showCoincidences(cipherText); // If method is static & public
            // For now, inform the user to check console or implement output redirection
            crackOutput.setText("Results shown in console.\n(You may adapt CryptoAnalysis methods to return strings.)");

        });

        crackPanel.add(new JLabel("Cipher Text:"), BorderLayout.NORTH);
        crackPanel.add(new JScrollPane(crackInput), BorderLayout.CENTER);
        JPanel cpButtonPanel = new JPanel();
        cpButtonPanel.add(coincidencesButton);
        crackPanel.add(cpButtonPanel, BorderLayout.SOUTH);
        crackPanel.add(new JScrollPane(crackOutput), BorderLayout.EAST);

        tabbedPane.add("Crack", crackPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}