import { TestBed } from '@angular/core/testing';
import { JsonHighlightService } from './json-highlight.service';

describe('JsonHighlightService', () => {
  let service: JsonHighlightService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JsonHighlightService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should detect JSON content correctly', () => {
    const validJson = '{"name": "test", "value": 123}';
    const invalidJson = 'This is not JSON';

    // Test with a mock element
    const mockElement = {
      textContent: validJson,
      classList: { add: jest.fn() },
      innerHTML: '',
    } as any;

    service.highlightElement(mockElement);
    expect(mockElement.classList.add).toHaveBeenCalledWith('json-highlight');
  });

  it('should format and highlight JSON properly', () => {
    const jsonText = '{"name":"test","value":123,"active":true}';
    const mockElement = {
      textContent: jsonText,
      classList: { add: jest.fn() },
      innerHTML: '',
    } as any;

    service.highlightElement(mockElement);

    // Check that the element was marked as JSON
    expect(mockElement.classList.add).toHaveBeenCalledWith('json-highlight');

    // Check that innerHTML was updated with highlighted content
    expect(mockElement.innerHTML).toContain('json-key');
    expect(mockElement.innerHTML).toContain('json-string');
    expect(mockElement.innerHTML).toContain('json-number');
    expect(mockElement.innerHTML).toContain('json-boolean');
  });
});
