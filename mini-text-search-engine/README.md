# Mini Text Search Engine (Theory of Formal Computation Project)

A complete, self-contained Python Flask and HTML/CSS/JS web application that demonstrates core **Theory of Formal Computation (TFC)** concepts, including **Tries (Prefix Trees)** for autocompletion, **Regular Expression Pattern Matching**, and **Deterministic Tokenization**.

---

## 🚀 Features

1. **Modern Glassmorphic Dashboard**: A clean dark-mode dashboard styled using modern CSS glassmorphism, responsive grid layout, and floating ambient light elements.
2. **Local File Indexing**: Scans all `.txt` documents located in a local folder (`documents/`) and builds search indexes instantly.
3. **Trie-based Autocomplete**: Demonstrates prefix tree traversal. As you type, the backend traverses a custom Trie in \(O(L)\) time to offer matching autocomplete suggestions.
4. **Keyword & Regular Expression Modes**: 
   - **Literal Search**: Standard substring matching.
   - **RegEx Search**: Harnesses formal language regular expressions using Python's regex matching library, with complete error handling for invalid regex syntax.
5. **Interactive Document Uploader**: Drag and drop new text files into the dashboard. The app processes (tokenizes) the text and updates the Trie autocomplete index dynamically without restarting the server.
6. **Detailed Result Previews**: Shows matching occurrence counts, document cards, and highlighted text snippets.

---

## 🧠 Theory of Formal Computation (TFC) Concepts

This project serves as a practical visualization for the following TFC topics:

* **Finite Automata & Lexical Analysis (Tokenization)**: When indexing files, a lexical scanner (`app.py:tokenize()`) processes raw strings into a stream of valid alphabetical and numerical tokens, mirroring the tokenization process in compilers.
* **Trie (Prefix Tree) Structures**: Autocompletion is modeled using a Trie where nodes represent states and transitions represent characters. Finding words starting with a prefix corresponds to transitioning to the prefix node and finding all reachable terminal states.
* **Regular Expressions and Regular Languages**: In RegEx mode, the input pattern is evaluated using a state machine representation of the regular language, showing the power and limitations of Type-3 languages in the Chomsky hierarchy.

---

## 🛠️ Installation & Execution

### Prerequisites
* Python 3.8+ installed on your system.

### Steps to Run
1. Open your terminal and navigate to the project directory:
   ```bash
   cd mini-text-search-engine
   ```
2. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the Flask server:
   ```bash
   python app.py
   ```
4. Open your web browser and navigate to:
   ```
   http://127.0.0.1:5000
   ```

---

## 📂 Project Structure

```
mini-text-search-engine/
├── app.py                  # Flask application & custom Trie tree structures
├── requirements.txt        # Backend dependencies
├── README.md               # Documentation and execution guide
├── documents/              # Local storage for indexed .txt files
│   ├── tfc_intro.txt
│   ├── regular_expressions.txt
│   └── pushdown_automata.txt
├── templates/
│   └── index.html          # Semantic HTML dashboard template
└── static/
    ├── style.css           # Premium Glassmorphism styling rules
    └── script.js           # UI logic, AJAX communication, & upload handling
```
