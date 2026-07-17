// Frontend Javascript for Mini Text Search Engine
document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const searchInput = document.getElementById('search-input');
    const clearBtn = document.getElementById('clear-btn');
    const autocompleteDropdown = document.getElementById('autocomplete-dropdown');
    const modeNormalBtn = document.getElementById('mode-normal');
    const modeRegexBtn = document.getElementById('mode-regex');
    const searchBtn = document.getElementById('search-btn');
    const resultsCountText = document.getElementById('results-count-text');
    const searchMetadata = document.getElementById('search-metadata');
    const resultsContainer = document.getElementById('results-container');
    
    // File Upload Elements
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const browseBtn = document.getElementById('browse-btn');
    const uploadStatus = document.getElementById('upload-status');

    // App State
    let searchMode = 'normal'; // 'normal' or 'regex'
    let debounceTimer = null;
    let activeSuggestionIndex = -1;

    // --- Search Mode Toggles ---
    modeNormalBtn.addEventListener('click', () => {
        setSearchMode('normal');
    });

    modeRegexBtn.addEventListener('click', () => {
        setSearchMode('regex');
    });

    function setSearchMode(mode) {
        searchMode = mode;
        if (mode === 'normal') {
            modeNormalBtn.classList.add('active');
            modeRegexBtn.classList.remove('active');
            searchInput.placeholder = "Type to search or test prefixes...";
        } else {
            modeNormalBtn.classList.remove('active');
            modeRegexBtn.classList.add('active');
            searchInput.placeholder = "Enter regular expression (e.g. [0-9]+ or P[D|F]A)...";
            // Autocomplete doesn't make sense for regex, so hide it
            closeAutocomplete();
        }
    }

    // --- Input Interactions ---
    searchInput.addEventListener('input', () => {
        // Toggle Clear button visibility
        if (searchInput.value.trim().length > 0) {
            clearBtn.style.display = 'block';
        } else {
            clearBtn.style.display = 'none';
        }

        // Trigger autocomplete (Trie) in Normal mode only
        if (searchMode === 'normal') {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                fetchAutocompleteSuggestions();
            }, 200); // 200ms debounce
        } else {
            closeAutocomplete();
        }
    });

    clearBtn.addEventListener('click', () => {
        searchInput.value = '';
        clearBtn.style.display = 'none';
        closeAutocomplete();
        searchInput.focus();
    });

    // Close dropdown on click outside
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.input-wrapper')) {
            closeAutocomplete();
        }
    });

    // --- Keyboard Navigation in Autocomplete ---
    searchInput.addEventListener('keydown', (e) => {
        const items = autocompleteDropdown.getElementsByClassName('autocomplete-item');
        
        if (autocompleteDropdown.style.display === 'block' && items.length > 0) {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                activeSuggestionIndex++;
                if (activeSuggestionIndex >= items.length) activeSuggestionIndex = 0;
                highlightSuggestion(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                activeSuggestionIndex--;
                if (activeSuggestionIndex < 0) activeSuggestionIndex = items.length - 1;
                highlightSuggestion(items);
            } else if (e.key === 'Enter') {
                if (activeSuggestionIndex > -1) {
                    e.preventDefault();
                    selectSuggestion(items[activeSuggestionIndex].dataset.word);
                }
            } else if (e.key === 'Escape') {
                closeAutocomplete();
            }
        } else {
            // Standard enter press executes search
            if (e.key === 'Enter') {
                e.preventDefault();
                executeSearch();
            }
        }
    });

    // --- Trie Autocomplete Requests ---
    function fetchAutocompleteSuggestions() {
        const query = searchInput.value.trim();
        if (query.length === 0) {
            closeAutocomplete();
            return;
        }

        fetch(`/autocomplete?q=${encodeURIComponent(query)}`)
            .then(res => res.json())
            .then(suggestions => {
                renderAutocompleteDropdown(suggestions);
            })
            .catch(err => {
                console.error("Autocomplete fetch error: ", err);
                closeAutocomplete();
            });
    }

    function renderAutocompleteDropdown(suggestions) {
        if (suggestions.length === 0) {
            closeAutocomplete();
            return;
        }

        autocompleteDropdown.innerHTML = '';
        activeSuggestionIndex = -1;

        suggestions.forEach((word) => {
            const li = document.createElement('li');
            li.className = 'autocomplete-item';
            li.role = 'option';
            li.dataset.word = word;
            
            // Bold matching prefix portion
            const prefix = searchInput.value.trim().toLowerCase();
            if (word.startsWith(prefix)) {
                const boldPart = word.substring(0, prefix.length);
                const normalPart = word.substring(prefix.length);
                li.innerHTML = `<i class="fa-solid fa-clock-rotate-left"></i> <strong>${boldPart}</strong>${normalPart}`;
            } else {
                li.innerHTML = `<i class="fa-solid fa-clock-rotate-left"></i> ${word}`;
            }

            li.addEventListener('click', () => {
                selectSuggestion(word);
            });

            autocompleteDropdown.appendChild(li);
        });

        autocompleteDropdown.style.display = 'block';
    }

    function highlightSuggestion(items) {
        // Remove active class from all
        for (let item of items) {
            item.classList.remove('active-item');
        }
        
        if (activeSuggestionIndex > -1 && items[activeSuggestionIndex]) {
            const activeItem = items[activeSuggestionIndex];
            activeItem.classList.add('active-item');
            
            // Scroll element into view if needed
            activeItem.scrollIntoView({ block: 'nearest' });
        }
    }

    function selectSuggestion(word) {
        searchInput.value = word;
        closeAutocomplete();
        executeSearch();
    }

    function closeAutocomplete() {
        autocompleteDropdown.style.display = 'none';
        autocompleteDropdown.innerHTML = '';
        activeSuggestionIndex = -1;
    }

    // --- Search Requests ---
    searchBtn.addEventListener('click', executeSearch);

    function executeSearch() {
        const query = searchInput.value.trim();
        closeAutocomplete();

        if (query.length === 0) {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fa-solid fa-magnifying-glass empty-icon"></i>
                    <p>Please enter a keyword or expression to search.</p>
                </div>
            `;
            resultsCountText.innerText = "Search Results";
            searchMetadata.innerText = "Query was empty";
            return;
        }

        // Loading state
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-spinner fa-spin empty-icon" style="color: var(--color-indigo);"></i>
                <p>Analyzing text repositories using ${searchMode === 'regex' ? 'Regular Expression' : 'Trie Search'}...</p>
            </div>
        `;

        const startTime = performance.now();
        fetch(`/search?q=${encodeURIComponent(query)}&mode=${searchMode}`)
            .then(res => res.json())
            .then(data => {
                const endTime = performance.now();
                const duration = ((endTime - startTime) / 1000).toFixed(4);
                renderSearchResults(data, duration);
            })
            .catch(err => {
                console.error("Search error: ", err);
                resultsContainer.innerHTML = `
                    <div class="search-error-card">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <div>
                            <h4>API Connection Failed</h4>
                            <p>Could not connect to the backend server. Please verify Flask is running.</p>
                        </div>
                    </div>
                `;
            });
    }

    function renderSearchResults(data, durationSeconds) {
        // Handle compile errors (specifically in Regex mode)
        if (data.error) {
            resultsContainer.innerHTML = `
                <div class="search-error-card">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <div>
                        <h4>Formal Pattern Processing Error</h4>
                        <p>${data.error}</p>
                    </div>
                </div>
            `;
            resultsCountText.innerText = "Error in Pattern Grammar";
            searchMetadata.innerText = `Failed in ${durationSeconds}s`;
            return;
        }

        const results = data.results;
        resultsCountText.innerText = `Found ${results.length} Matching Document${results.length !== 1 ? 's' : ''}`;
        searchMetadata.innerText = `Checked index in ${durationSeconds}s`;

        if (results.length === 0) {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fa-solid fa-face-frown-open empty-icon"></i>
                    <p>No matches found for "${escapeHTML(data.query)}".</p>
                    <p class="empty-hint">Verify syntax rules, letters, and token structures in your search word.</p>
                </div>
            `;
            return;
        }

        resultsContainer.innerHTML = '';
        results.forEach(fileResult => {
            const card = document.createElement('article');
            card.className = 'result-card';
            
            // Build Snippet List
            let snippetListHTML = '';
            fileResult.snippets.forEach(snippet => {
                snippetListHTML += `<blockquote class="snippet-block">${snippet}</blockquote>`;
            });

            card.innerHTML = `
                <div class="result-card-header">
                    <div class="file-name"><i class="fa-regular fa-file-lines"></i> ${escapeHTML(fileResult.filename)}</div>
                    <span class="occurrence-badge">${fileResult.count} match${fileResult.count !== 1 ? 'es' : ''}</span>
                </div>
                <div class="snippets-container">
                    ${snippetListHTML}
                </div>
            `;
            resultsContainer.appendChild(card);
        });
    }

    // Helper utility to escape HTML
    function escapeHTML(str) {
        return str.replace(/[&<>'"]/g, 
            tag => ({
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                "'": '&#39;',
                '"': '&quot;'
            }[tag] || tag)
        );
    }

    // --- Dynamic Drag & Drop File Upload Actions ---
    
    // Highlight drop area when item is dragged over it
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add('dragover');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove('dragover');
        }, false);
    });

    // Handle dropped files
    dropZone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files.length > 0) {
            handleFileUpload(files[0]);
        }
    });

    // Browse button interactions
    browseBtn.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            handleFileUpload(fileInput.files[0]);
        }
    });

    function handleFileUpload(file) {
        if (!file.name.endsWith('.txt')) {
            showUploadStatus("Only .txt files are accepted for lexical indexing.", "error");
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        showUploadStatus(`Uploading & tokenizing ${file.name}...`, "info");

        fetch('/upload', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                showUploadStatus(data.message, "success");
                fileInput.value = ''; // Reset input
            } else {
                showUploadStatus(data.message || "Failed to index file.", "error");
            }
        })
        .catch(err => {
            console.error("Upload failure: ", err);
            showUploadStatus("Network error occurred while uploading.", "error");
        });
    }

    function showUploadStatus(message, type) {
        uploadStatus.className = 'status-msg'; // reset classes
        uploadStatus.innerText = message;

        if (type === 'success') {
            uploadStatus.classList.add('success');
        } else if (type === 'error') {
            uploadStatus.classList.add('error');
        } else {
            // Information message (no special styling except showing block)
            uploadStatus.style.display = 'block';
            uploadStatus.style.background = 'rgba(255, 255, 255, 0.05)';
            uploadStatus.style.color = 'var(--text-secondary)';
            uploadStatus.style.border = '1px dashed rgba(255, 255, 255, 0.1)';
        }

        // Hide notice after 4 seconds if it is a success/error message
        if (type === 'success' || type === 'error') {
            setTimeout(() => {
                uploadStatus.style.display = 'none';
                uploadStatus.innerText = '';
            }, 4000);
        }
    }
});
