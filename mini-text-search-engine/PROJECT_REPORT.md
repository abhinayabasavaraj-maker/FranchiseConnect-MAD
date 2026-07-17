# Mini Text Search Engine

## Project Report

### 1. Abstract
This project builds a Mini Text Search Engine using Python Flask and a modern web dashboard. It demonstrates several Theory of Formal Computation concepts through deterministic lexical tokenization, Trie-based autocomplete, and regular expression pattern matching. The system supports document upload, real-time indexing, search across local text files, and highlighted result preview.

### 2. Problem Statement
Students often struggle to connect formal computation theory with practical applications. This project addresses that gap by implementing a text search system that uses formal TFC concepts to solve an everyday problem: finding keywords and patterns in documents.

Key requirements:
- Index a local corpus of `.txt` documents.
- Provide responsive search and autocomplete behavior.
- Support both literal keyword search and regex-based pattern matching.
- Visualize the relationship between input text, tokenization, and search state.

### 3. Theory / Background
The project integrates the following formal computation concepts:

#### 3.1 Tokenization
Tokenization is the process of breaking raw text into meaningful symbols called tokens. In formal computation, this is the first step of lexical analysis for compilers and interpreters.
- Implemented using a deterministic regular expression.
- Extracts alphabetic and numeric sequences as tokens.
- Ensures consistent indexing across documents.

#### 3.2 Trie (Prefix Tree)
A Trie is a tree-based data structure used to store a dynamic set of strings.
- Each node corresponds to a character in a token.
- The path from root to a terminal node spells a full token.
- Enables efficient prefix search and autocomplete in `O(L)` time where `L` is prefix length.

#### 3.3 Regular Expressions
Regular expressions describe patterns in formal languages and can be evaluated by finite automata.
- Used here to allow advanced text search patterns.
- Supports character classes, quantifiers, alternation, and grouping.
- Demonstrates how regex engines match strings according to formal grammar rules.

#### 3.4 Finite Automata Concepts
Although the system is not a complete automaton simulation, it demonstrates core automata ideas.
- Trie traversal acts like walking through deterministic states.
- Each character read from the query moves the search state along child transitions.
- Accepting states correspond to valid complete tokens.

### 4. System Design (Automata Model)

#### 4.1 Architecture
The system uses a three-layer design:
- **Backend**: `app.py` handles data ingestion, indexing, and API endpoints.
- **Frontend**: `templates/index.html`, `static/script.js`, and `static/style.css` present the user interface and client-side logic.
- **Storage**: `documents/` contains the indexed `.txt` files.

#### 4.2 Logical Modules
- `app.py`
  - `tokenize(text)`: Converts file content into tokens.
  - `Trie` and `TrieNode`: Implements prefix tree insertion and query logic.
  - `index_all_files()`: Rebuilds the Trie from all documents.
  - `search_in_file()`: Performs literal or regex search and prepares result snippets.
  - Flask routes: `/`, `/autocomplete`, `/search`, `/upload`.

- `script.js`
  - Manages input events and UI state.
  - Calls autocomplete and search endpoints.
  - Displays results and handles file uploads.

- `style.css`
  - Defines the dashboard layout and color system.
  - Implements glassmorphism styling and responsive design.

#### 4.3 Automata Model Explanation
The Trie structure behaves like a deterministic automaton:
- **States**: Each `TrieNode` is a state.
- **Alphabet**: Characters allowed in tokens (letters, digits, underscore, dash).
- **Transition function**: Each child link from a node is a transition for one character.
- **Start state**: The Trie root.
- **Accept states**: Nodes where `is_end_of_word` is `True`.

In regex search:
- The regex compiler constructs an internal automaton for the query.
- The matcher scans document text to find strings accepted by that automaton.

#### 4.4 Data Flow
1. User opens dashboard at `http://127.0.0.1:5001`.
2. The backend indexes all text files in `documents/` on startup.
3. User types in the search box.
4. In normal mode, the frontend requests `/autocomplete?q=...`.
5. The backend traverses the Trie and returns prefix suggestions.
6. User executes a search.
7. Frontend calls `/search?q=...&mode=...`.
8. Backend performs literal or regex search across all documents.
9. Matching files and highlighted snippets are returned.
10. User can upload a new `.txt` file to `/upload`, triggering re-indexing.

### 5. Derivations

#### 5.1 Tokenization Derivation
The tokenization method is derived from a deterministic finite recognizer for word-like strings:
- Input alphabet: letters, digits, underscore, and dash.
- Regular expression: `\b[a-zA-Z0-9_-]+\b`.
- This rule accepts tokens such as `automata`, `token_value`, `PDA`, and `regex-101`.
- It rejects punctuation, whitespace, and empty tokens.
- Each matched token becomes a symbol in the Trie.

#### 5.2 Trie Derivation
The Trie is built using the following rules:
- Start from the root state for each token.
- For each character in the token:
  - If a transition for that character exists, follow it.
  - Otherwise, create a new state and transition.
- After processing the final character, mark the state as accepting and increment frequency.
- For autocomplete, any prefix path leads to a subtree of accepted tokens.

This derivation ensures that common prefixes are shared and retrieval is fast.

#### 5.3 Regex Search Derivation
Literal search is implemented by escaping the query and compiling it as a fixed string. Regex search is implemented by compiling the query with flags:
- `re.IGNORECASE`: Case-insensitive matching.
- `re.MULTILINE`: Allows `^` and `$` to match line boundaries.

The matcher iterates over all non-empty matches and extracts surrounding snippets. This approach demonstrates how formal patterns can be evaluated against text input.

### 6. JFLAP Simulation
This project does not include a JFLAP file. Instead, the automata concepts are represented in code:
- The Trie simulates a deterministic finite automaton for prefix recognition.
- The regex search shows how regular languages are processed using pattern automata.

If required, the Trie could be converted into a JFLAP DFA by mapping nodes to states and character links to transitions.

### 7. Conclusion
The Mini Text Search Engine connects formal computation theory with usable software. It shows how deterministic tokenization, Trie-based prefix search, and regex matching can work together in a real application. The result is a project that is both educational and functional.

### 8. Future Enhancements
- Add a full document list view with file metadata.
- Implement ranking by token frequency and relevance.
- Support phrase search and boolean operators.
- Add syntax validation for regex before submission.
- Include a visual Trie diagram for classroom presentation.
