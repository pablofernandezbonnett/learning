# System Design Exercises

This folder is for interactive system-design practice.

The goal is not to copy a finished architecture. Start with your own mental
model, make its assumptions visible, and refine it through questions about
correctness, scale, failure, and tradeoffs.

## Exercise Loop

For each problem:

1. write what you think the system is and what it must do
2. state the first simple design you would build
3. identify the important requirements and assumptions
4. walk the main flow and the failure cases
5. revise the design, recording what changed and why

Do not jump to databases, queues, or diagrams before the first mental model is
clear. Components are only useful when they solve a named problem.

## Exercises

- [URL Shortener](./url-shortener.md) — start from the basic mapping between a
  short code and a destination URL, then grow the design only when a concrete
  requirement requires it.

## Related Material

- [System Design Guide](../../topics/system-design/system-design-guide.md)
- [System Design Drills](../../topics/system-design/system-design-drills.md)
