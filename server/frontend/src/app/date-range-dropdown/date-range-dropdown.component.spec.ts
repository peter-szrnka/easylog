import { ComponentFixture, TestBed } from "@angular/core/testing";
import { beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { DateRangeDropdownComponent } from "./date-range-dropdown.component";
import { ElementRef, provideZonelessChangeDetection } from "@angular/core";
import { BrowserTestingModule, platformBrowserTesting } from "@angular/platform-browser/testing";
import { ActivatedRoute } from "@angular/router";
import { of } from "rxjs";
import { DateRangeType } from "../model";

/**
 * @author Peter Szrnka
 */
describe("DateRangeDropdownComponent", () => {
    let component: DateRangeDropdownComponent;
    let fixture: ComponentFixture<DateRangeDropdownComponent>;

    beforeAll(() => {
    TestBed.initTestEnvironment(BrowserTestingModule, platformBrowserTesting());
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
        imports: [DateRangeDropdownComponent],
        providers: [
            {
            provide: ActivatedRoute,
            useValue: { queryParams: of({}) }
            },
            provideZonelessChangeDetection()
        ],
        }).compileComponents();

        fixture = TestBed.createComponent(DateRangeDropdownComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        fixture.detectChanges();

        component.dateRangeSelectionEmitter.subscribe(selection => {
            expect(selection).toBeDefined();
        });

        expect(component).toBeTruthy();
    });

     it("should toggle open state", () => {
        expect(component.open).toBe(false);
        component.toggle();
        expect(component.open).toBe(true);
        component.toggle();
        expect(component.open).toBe(false);
    });

    it.each([
        DateRangeType.LAST_5_MINUTES,
        DateRangeType.LAST_15_MINUTES,
        DateRangeType.LAST_30_MINUTES,
        DateRangeType.LAST_1_HOUR,
        DateRangeType.LAST_4_HOURS,
        DateRangeType.LAST_1_DAY,
        DateRangeType.LAST_7_DAYS,
        DateRangeType.LAST_1_MONTH
    ])('should check daterange', (dateRangeType: DateRangeType) => {
        const emitSpy = vi.spyOn(component.dateRangeSelectionEmitter, "emit");
        component.selectRange(dateRangeType);
        expect(emitSpy).toHaveBeenCalled();
    });

    it("should select a range and emit event", () => {
         const emitSpy = vi.spyOn(component.dateRangeSelectionEmitter, "emit");

        component.selectRange(DateRangeType.LAST_1_HOUR);

        expect(component.selectedRange).toBe(DateRangeType.LAST_1_HOUR);
        expect(emitSpy).toHaveBeenCalled();
        expect(component.open).toBe(false);
    });

    it("should handle CUSTOM range without closing dropdown", () => {
        component.toggle();
        component.selectRange(DateRangeType.CUSTOM);
        component.onDateChanged();

        expect(component.selectedRange).toBe(DateRangeType.CUSTOM);
        expect(component.open).toBe(true);
    });

    it("should adjust panel position when dropdown is open", () => {
        component.dropdownPanel = {
            nativeElement: {
                getBoundingClientRect: () => ({ left: -5, width: 100 }),
                style: { left: '' }
            }
        } as ElementRef<HTMLDivElement>;

        component.adjustPosition();
        expect(component.dropdownPanel.nativeElement.style.left).toBe('0');

        component.dropdownPanel.nativeElement.getBoundingClientRect = () => ({ bottom: 50, top: 50, height: 50, right: 50, x:0, y: 0, left: 50, width: 100, toJSON: () => {} });
        component.adjustPosition();
        expect(component.dropdownPanel.nativeElement.style.left).toContain('calc(');
    });

    it("should close dropdown when clicking outside", () => {
        const clickEvent = new Event('click');
        const div = document.createElement('div');
        vi.spyOn(component['eRef'].nativeElement, 'contains').mockReturnValue(false);

        component.open = true;
        component.clickOutside(clickEvent);

        expect(component.open).toBe(false);
    });
});