import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DateRangeType, DateRangeSelection } from '../model';

/**
 * @author Peter Szrnka
 */
@Component({
    selector: 'app-date-range-dropdown',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './date-range-dropdown.component.html',
    styleUrls: ['./date-range-dropdown.component.scss']
})
export class DateRangeDropdownComponent {
    DateRangeType = DateRangeType;

    open = false;
    selectedRange: DateRangeType = DateRangeType.LAST_15_MINUTES;
    startDate = '';
    endDate = '';
    @Output()
    dateRangeSelectionEmitter: EventEmitter<DateRangeSelection> = new EventEmitter<DateRangeSelection>();

    @ViewChild('dropdownPanel') dropdownPanel?: ElementRef<HTMLDivElement>;

    constructor(private eRef: ElementRef) { }

    toggle() {
        this.open = !this.open;
        if (this.open) {
            this.onRangeChange();
            setTimeout(() => this.adjustPosition(), 0);
        }
    }

    selectRange(range: DateRangeType) {
        this.selectedRange = range;
        this.onRangeChange();
    }

    onRangeChange() {
        if (this.selectedRange !== DateRangeType.CUSTOM) {
            const now = new Date();
            const start = new Date();

            switch (this.selectedRange) {
                case DateRangeType.LAST_5_MINUTES: start.setMinutes(now.getMinutes() - 5); break;
                case DateRangeType.LAST_15_MINUTES: start.setMinutes(now.getMinutes() - 15); break;
                case DateRangeType.LAST_30_MINUTES: start.setMinutes(now.getMinutes() - 30); break;
                case DateRangeType.LAST_1_HOUR: start.setHours(now.getHours() - 1); break;
                case DateRangeType.LAST_4_HOURS: start.setHours(now.getHours() - 4); break;
                case DateRangeType.LAST_1_DAY: start.setDate(now.getDate() - 1); break;
                case DateRangeType.LAST_7_DAYS: start.setDate(now.getDate() - 7); break;
                case DateRangeType.LAST_1_MONTH: start.setMonth(now.getMonth() - 1); break;
            }

            this.startDate = start.toISOString().slice(0, 16);
            this.endDate = now.toISOString().slice(0, 16);
        }

        this.dateRangeSelectionEmitter.emit({
                dateRangeType: this.selectedRange,
                from: this.startDate,
                to: this.endDate
            });
    }

    onDateChanged() {
        this.dateRangeSelectionEmitter.emit({
            dateRangeType: this.selectedRange,
            from: this.startDate,
            to: this.endDate
        });
    }

    adjustPosition() {
        const panel = this.dropdownPanel?.nativeElement;
        if (!panel) return;

        const rect = panel.getBoundingClientRect();
        const overflowRight = rect.right - window.innerWidth;
        const overflowLeft = rect.left;

        if (overflowRight > 0) {
            panel.style.left = `calc(100% - ${rect.width + overflowRight + 8}px)`;
        }

        if (overflowLeft < 0) {
            panel.style.left = '0';
        }
    }

    @HostListener('document:click', ['$event'])
    clickOutside(event: Event) {
        if (!this.eRef.nativeElement.contains(event.target)) {
            this.open = false;
        }
    }
}
