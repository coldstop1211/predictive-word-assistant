import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

//MAKE SURE TO CHANGE THE PATH NAMES (this is due to my computer beign a mac)

@SuppressWarnings("unused")
public class TrieDisplay extends JPanel implements KeyListener {
    private JFrame frame;
    private JPanel mainPanel, selectionPanel, typingPanel;
    private CardLayout cardLayout;
    private int size = 30, width = 1000, height = 600;
    private Trie trie;
    private String word;            // word you are trying to spell printed in large font
    private List<WordColor> wordList; // list of words with colors
    private char likelyChar;        // used for single most likely character
    private boolean wordsLoaded;    // use this to make sure words are all loaded before you start typing
    private Map<Character, Integer> likelyCharFrequencies; // store likelihood percentages
    private List<String> topLikelyWords;
    private List<String> uniqueWords;
    private Image backgroundTitleImage;  // Background image for selection panel
    private Image backgroundSecondaryImage; // Background image for typing panel

    public TrieDisplay() {
        frame = new JFrame("Trie Next");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Load the background images
        backgroundTitleImage = new ImageIcon("intro.jpeg").getImage();
        backgroundSecondaryImage = new ImageIcon("secondarybackground.jpeg").getImage();

        createSelectionPanel();
        createTypingPanel();

        mainPanel.add(selectionPanel, "Selection");
        mainPanel.add(typingPanel, "Typing");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

     private void createSelectionPanel() {
        selectionPanel = new BackgroundPanel(backgroundTitleImage);
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setBorder(new EmptyBorder(200, 10, 10, 10));

        JLabel label = new JLabel("Select a text file to load:");
        label.setFont(new Font("Impact", Font.BOLD, 40)); 
        label.setForeground(Color.WHITE); 
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        selectionPanel.add(label);

        JButton button1 = new JButton("The Art of War by Sun Tzu");
        button1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button1.setPreferredSize(new Dimension(300, 50));
        button1.setMaximumSize(new Dimension(300, 50));
        button1.addActionListener(e -> loadFile("artwar.1b.txt"));
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        selectionPanel.add(button1);

        JButton button2 = new JButton("The Similarion by J.R.R. Tolkien");
        button2.setAlignmentX(Component.CENTER_ALIGNMENT);
        button2.setPreferredSize(new Dimension(300, 50));
        button2.setMaximumSize(new Dimension(300, 50));
        button2.addActionListener(e -> loadFile("TheSimilarion.txt"));
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        selectionPanel.add(button2);
    }

    private void createTypingPanel() {
        typingPanel = new BackgroundPanel(backgroundSecondaryImage);
        typingPanel.setLayout(new BorderLayout());
    
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false); // Make the content panel transparent
        contentPanel.add(this, BorderLayout.CENTER);
    
        JButton backButton = new JButton("Back to File Selection");
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Selection");
            this.removeKeyListener(this);
        });
        contentPanel.add(backButton, BorderLayout.SOUTH);
    
        typingPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void loadFile(String filePath) {
        trie = new Trie();
        readFileToTrie(filePath);

        wordsLoaded = true;
        word = "";
        wordList = new ArrayList<>();
        likelyChar = ' ';
        likelyCharFrequencies = new HashMap<>();
        topLikelyWords = new ArrayList<>();
        uniqueWords = new ArrayList<>();

        cardLayout.show(mainPanel, "Typing");
        typingPanel.repaint();  // Force repaint when switching
        this.addKeyListener(this);
        this.setOpaque(false);
        this.requestFocusInWindow();
    }

    // All Graphics handled in this method. Don't do calculations here
    public void paintComponent(Graphics g) {
        super.paintComponent(g);                // Setup and Background
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(new Font("Courier New", Font.BOLD, 40));       // Header
        g2.setColor(Color.WHITE);
        if (wordsLoaded)
            g2.drawString("Start Typing:", 40, 100);
        else
            g2.drawString("Loading... please wait", 40, 100);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Courier New", Font.BOLD, 24));       // Typed text:  White == valid partial word
        if (trie.contains(word))                        // Red == invalid
            g2.setColor(Color.GREEN);                       // Green == full word
        else if (likelyChar == '_')
            g2.setColor(Color.RED);
        else
            g2.setColor(Color.WHITE);

        // Display wordList in one line
        int cursorPosition = 40;
        for (WordColor wc : wordList) {
            g2.setColor(wc.color);
            g2.drawString(wc.word + " ", cursorPosition, 160);
            cursorPosition += wc.word.length() * 15 + 10; // Adjust spacing as needed
        }

        // Display current word being typed
        g2.setColor(Color.WHITE);
        g2.drawString(word, cursorPosition, 160);

        // Next likely char
        g2.setColor(Color.MAGENTA);
        g2.setFont(new Font("Courier New", Font.BOLD, 20));
        g2.drawString("Next likely char -> " + likelyChar, 40, 200);

        Map<Character, Integer> freqMap = trie.likelyNextCharsFrequency(word);

        List<Map.Entry<Character, Integer>> sortedFreq = new ArrayList<>(freqMap.entrySet());
        sortedFreq.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int topN = Math.min(6, sortedFreq.size());
        sortedFreq = sortedFreq.subList(0, topN);

        StringBuilder freqDisplay = new StringBuilder("Next char frequencies: ");
        for (Map.Entry<Character, Integer> entry : sortedFreq) {
            freqDisplay.append(entry.getKey()).append(":").append(entry.getValue()).append("%  ");
        }

        // Freq char
        g2.setColor(Color.CYAN);
        g2.drawString(freqDisplay.toString(), 40, 240);

        // Likely words
        g2.setColor(Color.GREEN);
        StringBuilder likelyWordsDisplay = new StringBuilder("Likely words -> ");
        for (String likelyWord : topLikelyWords) {
            likelyWordsDisplay.append(likelyWord).append(" = ").append(trie.getWordCount(likelyWord)).append(",  ");
        }
        g2.drawString(likelyWordsDisplay.toString(), 40, 280);

        // Unique words
        g2.setColor(Color.YELLOW);
        StringBuilder uniqueWordsDisplay = new StringBuilder("Unique words -> ");
        for (String uniqueWord : uniqueWords) {
            uniqueWordsDisplay.append(uniqueWord).append("=").append(trie.getWordCount(uniqueWord)).append(",  ");
        }
        g2.drawString(uniqueWordsDisplay.toString(), 40, 320);
    }

    public void keyPressed(KeyEvent e) {              // This handles key press
        int keyCode = e.getKeyCode();
        if (keyCode == 8) { // Backspace -> remove last letter
            if (!word.isEmpty()) {
                word = word.substring(0, word.length() - 1);
            } else if (!wordList.isEmpty()) {
                WordColor lastWord = wordList.remove(wordList.size() - 1);
                word = lastWord.word;
            }
        } else if (keyCode == KeyEvent.VK_SPACE) { // Space -> verify word
            if (trie.contains(word)) {
                wordList.add(new WordColor(word, Color.GREEN));
            } else {
                wordList.add(new WordColor(word, Color.RED));
            }
            word = "";
        } else if (keyCode == KeyEvent.VK_ENTER) { // enter -> add most likely word
            if (!topLikelyWords.isEmpty()) {
                word = topLikelyWords.get(0);
                wordList.add(new WordColor(word, Color.GREEN));
                word = "";
            }
        } else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) { // Alphabetic key
            if (word.length() < 140) { // Check if the word length is less than 140 characters
                word += KeyEvent.getKeyText(keyCode).toLowerCase();
            }
        }

        likelyChar = trie.mostLikelyNextChar(word);
        likelyCharFrequencies = trie.likelyNextCharsFrequency(word);
        topLikelyWords = trie.getLikelyWords(word);
        uniqueWords = trie.getUniqueWords(word);

        // Debug statements
        System.out.println("Current word: " + word);
        System.out.println("Top likely words: " + topLikelyWords);

        repaint();
    }

    public void loadFilesToTrie(List<String> fileNames) {
        for (String fileName : fileNames) {
            readFileToTrie(fileName);
        }
    }

    public void readFileToTrie(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
            br.close();

            String text = content.toString().toLowerCase();
            // Remove all characters that are not letters, apostrophes, or whitespace
            text = text.replaceAll("[^a-zA-Z'\\s]", "");

            String[] words = text.split("\\s+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /*** empty methods needed for interfaces ***/
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void actionPerformed(ActionEvent e) {}

    public static void main(String[] args) {
        TrieDisplay app = new TrieDisplay();
    }

    // Helper class to store words with their colors
    private static class WordColor {
        String word;
        Color color;

        WordColor(String word, Color color) {
            this.word = word;
            this.color = color;
        }
    }

    // Custom JPanel to draw the background image
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
