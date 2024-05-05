import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TextEditor extends JFrame {
    private JTextArea textArea;
    private String filePath = "C:\\temp\\myfile.txt"; // Default file path
    private TreeMap<Integer, String> insertedLines;
    private TreeSet<Integer> deletedLines;

    public TextEditor() {
        setTitle("Simple Text Editor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        textArea.setEditable(true); // Allow user input

        // Add key listener to handle user input
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleCommand();
                }
            }
        });

        // Initialize insertedLines and deletedLines
        insertedLines = new TreeMap<>();
        deletedLines = new TreeSet<>();

        // Display file content with line numbers
        listLines();
        textArea.append(">> ");
        setVisible(true);
    }

    private void handleCommand() {
        String fullText = textArea.getText().trim();
        String[] lines = fullText.split("\\n");
        String input = lines[lines.length - 1];
        input.trim();
        String[] tokens = input.split("\\s+");
        if (tokens.length == 0) {
            return; // No command entered
        }
        String command = tokens[0];
        switch (command) {
            case "list":
                listLines();
                break;
            case "del":
                if (tokens.length < 2) {
                    appendToTextArea("Invalid command. Please specify line number to delete.\n");
                    break;
                }
                int delLineNumber = Integer.parseInt(tokens[1]);
                deleteLine(delLineNumber);
                break;
            case "ins":
                if (tokens.length < 3) {
                    appendToTextArea("Invalid command. Please specify line number and text to insert.\n");
                    break;
                }
                int insLineNumber = Integer.parseInt(tokens[1]);
                String insText = input.substring(command.length() + tokens[1].length() + 2);
                insertLine(insLineNumber, insText);
                break;
            case "save":
                saveToFile();
                break;
            case "quit":
                dispose();
                break;
            default:
                appendToTextArea("Invalid command. Please try again.\n");
                break;
        }
        appendToTextArea("\n");
    }

    private void appendToTextArea(String text) {
        textArea.append(text);
        textArea.setCaretPosition(textArea.getDocument().getLength()); // Auto-scroll to bottom
    }

    private void listLines() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder text = new StringBuilder();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (!deletedLines.contains(lineNumber)) {
                    text.append(lineNumber).append(": ").append(line).append("\n");
                    lineNumber++;
                }
            }
            for (Map.Entry<Integer, String> entry : insertedLines.entrySet()) {
                text.insert(getOffset(entry.getKey()), entry.getValue() + "\n");
            }
            textArea.setText(text.toString());
            textArea.repaint();
            setVisible(true);
        } catch (IOException e) {
            appendToTextArea("Error: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void deleteLine(int lineNumber) {
        deletedLines.add(lineNumber);
        listLines();
    }

    private void insertLine(int lineNumber, String text) {
        insertedLines.put(lineNumber, text);
        listLines();
    }

    private void saveToFile() {
        try {
            StringBuilder newText = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    if (!deletedLines.contains(lineNumber)) {
                        newText.append(line).append("\n");
                    }
                    lineNumber++;
                }
            }
            for (Map.Entry<Integer, String> entry : insertedLines.entrySet()) {
                newText.insert(getOffset(entry.getKey()), entry.getValue() + "\n");
            }
            saveTextToFile(filePath, newText.toString());
            appendToTextArea("Changes saved to file " + filePath + " successfully.\n");
        } catch (IOException e) {
            appendToTextArea("Error: " + e.getMessage() + "\n");
        } finally {
            // Clear insertedLines and deletedLines after saving
            insertedLines.clear();
            deletedLines.clear();
        }
    }

    private int getOffset(int lineNumber) {
        int offset = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLineNumber = 1;
            while ((line = reader.readLine()) != null && currentLineNumber < lineNumber) {
                offset += line.length() + 1; // Add 1 for newline character
                currentLineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return offset;
    }

    private void saveTextToFile(String fileName, String text) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(text);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditor::new);
    }
}
