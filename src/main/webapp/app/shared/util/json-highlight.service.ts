import { Injectable } from '@angular/core';

/**
 * Service for applying JSON syntax highlighting to pre elements
 *
 * Usage:
 * 1. Add the jhiJsonHighlight directive to any pre element: <pre jhiJsonHighlight>...</pre>
 * 2. The service will automatically detect JSON content and apply highlighting
 * 3. CSS classes are applied for different JSON elements (keys, strings, numbers, etc.)
 *
 * Features:
 * - Automatic JSON detection
 * - Syntax highlighting with color coding
 * - Proper JSON formatting
 * - Fallback for invalid JSON
 */
@Injectable({
  providedIn: 'root',
})
export class JsonHighlightService {
  /**
   * Applies JSON syntax highlighting to pre elements with JSON content
   */
  applyJsonHighlighting(): void {
    // Find all pre elements with wrapped-pre class
    const preElements = document.querySelectorAll('pre.wrapped-pre');

    preElements.forEach(preElement => {
      const text = preElement.textContent || '';

      // Check if content looks like JSON
      if (this.isJsonContent(text)) {
        preElement.classList.add('json-highlight');
        this.highlightJsonContent(preElement, text);
      }
    });
  }

  /**
   * Checks if the given text appears to be JSON
   */
  private isJsonContent(text: string): boolean {
    const trimmed = text.trim();

    // Basic JSON detection patterns
    const jsonPatterns = [
      /^\{.*\}$/s, // Object wrapped in braces
      /^\[.*\]$/s, // Array wrapped in brackets
      /"[^"]*"\s*:/, // Property with colon
      /:\s*"[^"]*"/, // Value with quotes
      /:\s*\d+/, // Numeric value
      /:\s*(true|false|null)/, // Boolean or null
    ];

    return jsonPatterns.some(pattern => pattern.test(trimmed));
  }

  /**
   * Applies syntax highlighting to JSON content
   */
  private highlightJsonContent(element: Element, text: string): void {
    try {
      // First, try to parse and format the JSON
      const parsed = JSON.parse(text);
      const formatted = JSON.stringify(parsed, null, 2);

      // Apply highlighting to the formatted JSON
      const highlighted = this.highlightJsonSyntax(formatted);
      element.innerHTML = highlighted;
    } catch (e) {
      // If parsing fails, try to highlight the original text
      const highlighted = this.highlightJsonSyntax(text);
      element.innerHTML = highlighted;
    }
  }

  /**
   * Highlights JSON syntax by wrapping elements with CSS classes
   */
  private highlightJsonSyntax(jsonText: string): string {
    return (
      jsonText
        // Highlight property names (keys)
        .replace(/"([^"]+)"\s*:/g, '<span class="json-key">"$1"</span><span class="json-punctuation">:</span>')

        // Highlight string values
        .replace(/:\s*"([^"]*)"/g, '<span class="json-punctuation">:</span> <span class="json-string">"$1"</span>')

        // Highlight numbers
        .replace(/:\s*(-?\d+\.?\d*)/g, '<span class="json-punctuation">:</span> <span class="json-number">$1</span>')

        // Highlight booleans
        .replace(/:\s*(true|false)/g, '<span class="json-punctuation">:</span> <span class="json-boolean">$1</span>')

        // Highlight null
        .replace(/:\s*(null)/g, '<span class="json-punctuation">:</span> <span class="json-null">$1</span>')

        // Highlight punctuation (braces, brackets, commas)
        .replace(/([{}[\]])/g, '<span class="json-punctuation">$1</span>')
        .replace(/,/g, '<span class="json-punctuation">,</span>')

        // Handle nested objects and arrays
        .replace(/<span class="json-punctuation">\{<\/span>/g, '<span class="json-punctuation">{</span>')
        .replace(/<span class="json-punctuation">\}<\/span>/g, '<span class="json-punctuation">}</span>')
        .replace(/<span class="json-punctuation">\[<\/span>/g, '<span class="json-punctuation">[</span>')
        .replace(/<span class="json-punctuation">\]<\/span>/g, '<span class="json-punctuation">]</span>')
    );
  }

  /**
   * Manually apply highlighting to a specific element
   */
  highlightElement(element: Element): void {
    const text = element.textContent || '';
    if (this.isJsonContent(text)) {
      element.classList.add('json-highlight');
      this.highlightJsonContent(element, text);
    }
  }
}
