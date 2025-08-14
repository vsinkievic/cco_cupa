import { Directive, ElementRef, OnInit, inject } from '@angular/core';
import { JsonHighlightService } from '../util/json-highlight.service';

@Directive({
  selector: '[jhiJsonHighlight]',
  standalone: true,
})
export class JsonHighlightDirective implements OnInit {
  private elementRef = inject(ElementRef);
  private jsonHighlightService = inject(JsonHighlightService);

  ngOnInit(): void {
    // Apply highlighting after the element is initialized
    setTimeout(() => {
      this.jsonHighlightService.highlightElement(this.elementRef.nativeElement);
    }, 50);
  }
}
