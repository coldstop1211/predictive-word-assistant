import java.util.*;


//ashu M & shardul

public class Trie {

    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            curr.children.putIfAbsent(c, new TrieNode());
            curr = curr.children.get(c);
            curr.count++;
        }
        curr.endOfWordCount++;
    }

    public boolean contains(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) return false;
        }
        return curr.endOfWordCount > 0;
    }

    public char mostLikelyNextChar(String str) {
        TrieNode curr = root;
        for (char c : str.toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) return '_';
        }

        char maxChar = '_';
        int maxCount = 0;
        for (Map.Entry<Character, TrieNode> entry : curr.children.entrySet()) {
            if (entry.getValue().count > maxCount) {
                maxCount = entry.getValue().count;
                maxChar = entry.getKey();
            }
        }
        return maxChar;
    }

    public Map<Character, Integer> likelyNextCharsFrequency(String str) {
        TrieNode curr = root;
        for (char c : str.toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) return Collections.emptyMap();
        }

        int total = curr.children.values().stream().mapToInt(n -> n.count).sum();
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (Map.Entry<Character, TrieNode> entry : curr.children.entrySet()) {
            frequencyMap.put(entry.getKey(), (int) Math.round((entry.getValue().count / (double) total) * 100));
        }
        return frequencyMap;
    }

    public List<String> getLikelyWords(String prefix) {
        TrieNode node = getNode(prefix);
        if (node == null) return Collections.emptyList();

        Map<String, Integer> wordCounts = new HashMap<>();
        collectWords(node, new StringBuilder(prefix), wordCounts);

        return wordCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getUniqueWords(String prefix) {
        TrieNode node = getNode(prefix);
        if (node == null) return Collections.emptyList();
    
        Map<String, Integer> wordCounts = new HashMap<>();
        collectWords(node, new StringBuilder(prefix), wordCounts);
    
        List<String> uniqueWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueWords.add(entry.getKey());
                if (uniqueWords.size() == 3) 
                break;
            }
        }
        return uniqueWords;
    }

    private TrieNode getNode(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) return null;
        }
        return curr;
    }

    private void collectWords(TrieNode node, StringBuilder currentWord, Map<String, Integer> words) {
        if (node.endOfWordCount > 0) {
            words.put(currentWord.toString(), node.endOfWordCount);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            currentWord.append(entry.getKey());
            collectWords(entry.getValue(), currentWord, words);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }

    public int getWordCount(String word) {
        TrieNode node = getNode(word);
        return node != null ? node.endOfWordCount : 0;
    }

    private static class TrieNode {
        int count;
        int endOfWordCount;
        Map<Character, TrieNode> children;

        TrieNode() {
            count = 0;
            endOfWordCount = 0;
            children = new HashMap<>();
        }
    }

    public static void main(String[] args) {
        Trie trie = new Trie();
        trie.insert("these");
        trie.insert("them");
        trie.insert("those");
        trie.insert("this");
        trie.insert("theme");

        System.out.println(trie.contains("these")); // true
        System.out.println(trie.contains("this"));  // true
        System.out.println(trie.contains("the"));   // false
        System.out.println(trie.contains("therapy")); // false

        System.out.println("most likely next char after 'th': " + trie.mostLikelyNextChar("th")); // 'e'
        System.out.println("next char probabilities for 'th': " + trie.likelyNextCharsFrequency("th"));
    }
}