# Verification Script for Mini Text Search Engine backend components
import sys
import os

# Insert project path into sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

try:
    from app import Trie, tokenize, search_in_file
    print("SUCCESS: Successfully imported Trie, tokenize, and search_in_file from app.py")
except Exception as e:
    print(f"FAILED: Import error: {e}")
    sys.exit(1)

# 1. Test Tokenization
print("\n--- Testing Tokenization ---")
sample_text = "Automata theory is the study of abstract machines: DFA, NFA, and PDA!"
tokens = tokenize(sample_text)
expected_tokens = ["automata", "theory", "is", "the", "study", "of", "abstract", "machines", "dfa", "nfa", "and", "pda"]
print(f"Tokens generated: {tokens}")
if tokens == expected_tokens:
    print("SUCCESS: Tokenization matches expected output!")
else:
    print("FAILED: Tokenization mismatch.")

# 2. Test Trie Prefix Matching
print("\n--- Testing Trie Autocomplete ---")
trie = Trie()
for t in tokens:
    trie.insert(t)
# Insert duplicate to check frequency
trie.insert("automata")

suggestions = trie.get_suggestions("aut")
print(f"Suggestions for 'aut': {suggestions}")
if "automata" in suggestions:
    print("SUCCESS: Trie prefix suggestions work correctly!")
else:
    print("FAILED: Trie could not suggest 'automata' for prefix 'aut'")

# 3. Test Search In File (Normal Mode)
print("\n--- Testing Substring Search ---")
filepath = os.path.join(os.path.dirname(os.path.abspath(__file__)), "documents", "tfc_intro.txt")
if not os.path.exists(filepath):
    print(f"WARNING: tfc_intro.txt not found at {filepath}, skipping search test.")
else:
    try:
        count, snippets = search_in_file(filepath, "automata")
        print(f"Matches for 'automata': {count}")
        print(f"Snippets: {snippets}")
        if count > 0 and len(snippets) > 0 and "<mark class='highlight'>automata</mark>" in snippets[0].lower():
            print("SUCCESS: Literal search and highlighting works!")
        else:
            print("FAILED: Literal search highlighting did not match.")
    except Exception as e:
        print(f"FAILED: Search error: {e}")

# 4. Test Search In File (Regex Mode)
print("\n--- Testing Regex Search ---")
if os.path.exists(filepath):
    try:
        # Match 'DFA' or 'NFA' or 'PDA'
        count, snippets = search_in_file(filepath, "DFA|NFA|PDA", is_regex=True)
        print(f"Regex Matches for 'DFA|NFA|PDA': {count}")
        print(f"Snippets: {snippets}")
        if count > 0:
            print("SUCCESS: Regex search matching works!")
        else:
            print("FAILED: Regex search failed to match DFA/NFA/PDA in text.")
    except Exception as e:
        print(f"FAILED: Regex search error: {e}")

    # Test Invalid Regex Handling
    print("\n--- Testing Invalid Regex Error Handling ---")
    try:
        search_in_file(filepath, "[A-Z+", is_regex=True)
        print("FAILED: Invalid regex did not raise an exception.")
    except ValueError as ve:
        print(f"SUCCESS: Invalid regex raised correct ValueError: {ve}")
    except Exception as e:
        print(f"FAILED: Invalid regex raised unexpected error type: {e}")

print("\nVerification process completed!")
