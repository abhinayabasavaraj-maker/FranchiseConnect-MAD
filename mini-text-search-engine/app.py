import os
import re
import html
from flask import Flask, request, jsonify, render_template
from werkzeug.utils import secure_filename

app = Flask(__name__)
UPLOAD_FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'documents')
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16 MB upload limit

# Ensure documents folder exists
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Trie Data Structure for Autocomplete (TFC Prefix Matching Demonstration)
class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_end_of_word = False
        self.frequency = 0
        self.word = ""

class Trie:
    def __init__(self):
        self.root = TrieNode()

    def insert(self, word):
        if not word:
            return
        node = self.root
        for char in word.lower():
            if char not in node.children:
                node.children[char] = TrieNode()
            node = node.children[char]
        node.is_end_of_word = True
        node.frequency += 1
        node.word = word.lower()

    def get_suggestions(self, prefix, limit=5):
        prefix = prefix.lower()
        node = self.root
        for char in prefix:
            if char not in node.children:
                return []
            node = node.children[char]
        
        suggestions = []
        self._collect_words(node, suggestions)
        # Sort suggestions by frequency (descending) then alphabetically
        suggestions.sort(key=lambda x: (-x[1], x[0]))
        return [word for word, freq in suggestions[:limit]]

    def _collect_words(self, node, suggestions):
        if node.is_end_of_word:
            suggestions.append((node.word, node.frequency))
        for child_node in node.children.values():
            self._collect_words(child_node, suggestions)

# Global Trie Instance
trie = Trie()

def tokenize(text):
    """
    Splits text into alphanumeric word tokens (lexical analysis).
    Demonstrates tokenization, a core concept in Theory of Formal Computation.
    """
    tokens = re.findall(r'\b[a-zA-Z0-9_-]+\b', text)
    return [t.lower() for t in tokens]

def index_all_files():
    """
    Scans the local documents directory, tokenizes each text file, 
    and inserts all tokens into the Trie to update autocompletion index.
    """
    global trie
    trie = Trie()  # Rebuild the Trie from scratch to avoid stale index
    if not os.path.exists(UPLOAD_FOLDER):
        return
    for filename in os.listdir(UPLOAD_FOLDER):
        if filename.endswith('.txt'):
            filepath = os.path.join(UPLOAD_FOLDER, filename)
            try:
                with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    tokens = tokenize(content)
                    for token in tokens:
                        trie.insert(token)
            except Exception as e:
                print(f"Error indexing {filename}: {e}")

# Initial indexing on server start
index_all_files()

def search_in_file(filepath, query, is_regex=False):
    """
    Searches for a keyword or regex pattern in a file.
    Returns the count of matches and highlighted snippet previews.
    """
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
    except Exception:
        return 0, []

    snippets = []
    
    if is_regex:
        try:
            # Compile regex pattern (case-insensitive & multiline matching)
            pattern = re.compile(query, re.IGNORECASE | re.MULTILINE)
        except re.error as e:
            raise ValueError(f"Invalid Regular Expression: {str(e)}")
    else:
        # Standard search - escape the query to treat it literally
        pattern = re.compile(re.escape(query), re.IGNORECASE)

    # Find matches and exclude zero-width matches (like empty strings) to prevent infinite loops
    matches = [m for m in pattern.finditer(content) if m.start() != m.end()]

    count = len(matches)
    if count == 0:
        return 0, []

    # Extract up to 3 snippets around matching occurrences
    max_snippets = 3
    for match in matches[:max_snippets]:
        start_idx = max(0, match.start() - 60)
        end_idx = min(len(content), match.end() + 60)

        # Align snippet to word boundaries for readability
        while start_idx > 0 and not content[start_idx].isspace():
            start_idx -= 1
        while end_idx < len(content) and not content[end_idx].isspace():
            end_idx += 1

        raw_snippet = content[start_idx:end_idx]
        
        # Adjust start_idx and end_idx by removing leading/trailing spaces
        stripped_leading = len(raw_snippet) - len(raw_snippet.lstrip())
        stripped_trailing = len(raw_snippet) - len(raw_snippet.rstrip())
        
        start_idx += stripped_leading
        end_idx -= stripped_trailing
        snippet_text = content[start_idx:end_idx]
        
        # Match boundaries relative to snippet start
        m_start = match.start() - start_idx
        m_end = match.end() - start_idx

        # Prevent HTML injection while rendering matching highlight tags
        before_match = html.escape(snippet_text[:m_start])
        match_val = html.escape(snippet_text[m_start:m_end])
        after_match = html.escape(snippet_text[m_end:])

        highlighted = f"{before_match}<mark class='highlight'>{match_val}</mark>{after_match}"
        prefix = "... " if start_idx > 0 else ""
        suffix = " ..." if end_idx < len(content) else ""
        snippets.append(prefix + highlighted + suffix)

    return count, snippets

@app.route('/')
def home():
    """Serves the front-end dashboard."""
    return render_template('index.html')

@app.route('/autocomplete', methods=['GET'])
def autocomplete():
    """API endpoint providing prefix-matching suggestions from the Trie."""
    q = request.args.get('q', '').strip()
    if not q:
        return jsonify([])
    suggestions = trie.get_suggestions(q, limit=5)
    return jsonify(suggestions)

@app.route('/search', methods=['GET'])
def search():
    """API endpoint for querying text files."""
    q = request.args.get('q', '')
    mode = request.args.get('mode', 'normal')
    is_regex = (mode == 'regex')

    if not q:
        return jsonify({"results": [], "query": q, "mode": mode, "error": None})

    results = []
    error_msg = None

    try:
        for filename in os.listdir(UPLOAD_FOLDER):
            if filename.endswith('.txt'):
                filepath = os.path.join(UPLOAD_FOLDER, filename)
                match_count, snippets = search_in_file(filepath, q, is_regex=is_regex)
                if match_count > 0:
                    results.append({
                        "filename": filename,
                        "count": match_count,
                        "snippets": snippets
                    })
        # Sort results: files with the highest matches first
        results.sort(key=lambda x: x['count'], reverse=True)
    except ValueError as ve:
        error_msg = str(ve)
    except Exception as e:
        error_msg = f"An unexpected error occurred: {str(e)}"

    return jsonify({
        "results": results,
        "query": q,
        "mode": mode,
        "error": error_msg
    })

@app.route('/upload', methods=['POST'])
def upload_file():
    """API endpoint handling .txt file uploads and dynamic indexing."""
    if 'file' not in request.files:
        return jsonify({"success": False, "message": "No file part in the request"}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({"success": False, "message": "No selected file"}), 400

    if file and file.filename.endswith('.txt'):
        filename = secure_filename(file.filename)
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        try:
            file.save(filepath)
            # Re-index dynamically to feed new tokens to the Trie autocomplete tree
            index_all_files()
            return jsonify({"success": True, "message": f"Successfully uploaded and indexed '{filename}'!"})
        except Exception as e:
            return jsonify({"success": False, "message": f"Error saving file: {str(e)}"}), 500
    else:
        return jsonify({"success": False, "message": "Only .txt files are allowed"}), 400

if __name__ == '__main__':
    app.run(debug=False, port=5001)
