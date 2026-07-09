import "@testing-library/jest-dom/vitest";
import { vi } from "vitest";

// @testing-library/dom's waitFor() (used by findBy*) auto-drives fake timers, but
// only detects a global `jest` object, not Vitest's `vi.useFakeTimers()`. Shim a
// minimal `jest` so that detection kicks in. See:
// https://github.com/testing-library/dom-testing-library/blob/main/src/helpers.ts
(globalThis as Record<string, unknown>).jest = {
  advanceTimersByTime: (ms: number) => vi.advanceTimersByTime(ms),
};
