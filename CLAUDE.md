# Claude Code Behavioral Guidelines

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

Tradeoff: These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding
Don't assume. Don't hide confusion. Surface tradeoffs.

Before implementing:
* State your assumptions explicitly. If uncertain, ask.
* If multiple interpretations exist, present them - don't pick silently.
* If a simpler approach exists, say so. Push back when warranted.
* If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First
Minimum code that solves the problem. Nothing speculative.

* No features beyond what was asked.
* No abstractions for single-use code.
* No "flexibility" or "configurability" that wasn't requested.
* No error handling for impossible scenarios.
* If you write 200 lines and it could be 50, rewrite it.
* Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes
Touch only what you must. Clean up only your own mess.

When editing existing code:
* Don't "improve" adjacent code, comments, or formatting.
* Don't refactor things that aren't broken.
* Match existing style, even if you'd do it differently.
* If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
* Remove imports/variables/functions that YOUR changes made unused.
* Don't remove pre-existing dead code unless asked.
* The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution
Define success criteria. Loop until verified.

Transform tasks into verifiable goals:
* "Add validation" → "Write tests for invalid inputs, then make them pass"
* "Fix the bug" → "Write a test that reproduces it, then make it pass"
* "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## 5. Data & Persistence Integrity
Prioritize data correctness, transaction boundaries, and query efficiency when interacting with the database layer.

* **Prevent N+1 Queries:** When fetching relational data, explicitly analyze the query behavior. Use eager loading, join fetches, or batch sizing instead of relying on default lazy loading to prevent cascade queries.
* **Explicit Transaction Scoping:** Define transaction boundaries clearly for any state-changing business logic. Mark read-only queries explicitly (e.g., read-only transactions) to optimize database connection usage and reduce locking overhead.
* **Never Expose Entities:** Do not expose database entities directly to the API or presentation layer. Always map them to dedicated Data Transfer Objects (DTOs) to avoid accidental modifications via persistence context tracking (dirty checking).

## 6. Defensive API & Error Contract
Assume failures, network latency, and invalid input at system boundaries.

* **Strict Input Validation:** Enforce strict validation rules at all API entry points. Reject invalid requests early, returning a standard error structure with specific validation failures rather than generic internal errors.
* **Never Swallow Exceptions:** Do not catch exceptions without logging or rethrowing them. Use a centralized global exception handler to map unhandled runtime errors into clean, predictable API payloads.
* **Hide Internal Traces:** Never expose stack traces, database schemas, or internal server paths in client responses. Log detailed context internally for debugging, but sanitize public-facing error messages.

## 7. Concurrency & Stateless Design
Always design code under the assumption that it will execute in a multi-threaded, horizontally scaled environment.

* **Keep Components Stateless:** Ensure application components (services, controllers, components) are completely stateless. Do not store request-scoped or mutable data in class fields of singleton instances.
* **Thread-Safe Shared State:** If state must be shared or cached in-memory, use thread-safe data structures (e.g., concurrent collections, atomic wrappers) or explicit synchronization primitives. Avoid plain static variables for global state.

> **Backend Exception to "Simplicity First"**
> While rule #2 mandates no error handling for impossible scenarios, backend boundary conditions (Database I/O, external API integration, concurrency, and client inputs) are inherently unpredictable. For infrastructure, external integration, and network boundaries, explicit defensive handling and resilience override absolute code minimization.

## 8. Meta-Context & Execution Boundaries (Optimized for Claude Code)
Strictly separate conceptual/architectural discussion from actual tool execution and code generation. Do not let agent loops misinterpret domain logic as chat commands.

* **Business Logic over Meta-Commands:** Always interpret user scenarios through the lens of application behavior and system requirements, never as chat meta-instructions. For example, if the user describes a user action (e.g., "toggling a button," "canceling and re-clicking"), they are asking about database state, status updates, or business logic—NOT telling you to pause, undo, or change your code-generation state.
* **No Premature Tool Calls:** Do not invoke file-modifying tools (`write_file`, `edit_file`, etc.) during a conceptual or design discussion. Keep your actions restricted to `view_file` or purely textual responses until an explicit implementation trigger (e.g., "Implement this," "Write the code") is provided.
* **Prevent Token Bleed on Ambiguity:** If a user statement is brief, conversational, or ambiguous, do not assume it is a green light to output large blocks of code or execute multi-step tools. Default to a concise textual response or a clarifying question. Never burn tokens writing speculative code based on an unconfirmed conversational thread.

## 9. Announce Before Acting (Project Rule)
Every tool call that isn't covered by rule #11 (e.g. bash/git commands, non-code file edits) must be preceded by a short plain-text explanation of what is about to happen and why. Do not silently act — state the intent first. (For source code files specifically, rule #11 applies instead and is stricter: no tool call at all, code given as text.)

## 10. No Task-Tool Progress Tracking (Project Rule)
Do not use the TaskCreate/TaskUpdate/TaskList tools in this project — the rendered task-list UI it produces is unwanted noise. Progress tracking for this mission lives in `CHECKLIST.md` (with 💾 commit markers) instead.

## 11. Never Write Code Files Directly (Project Rule — confirmed multiple times by user)
Never use Write/Edit on source code files (Java, gradle, yaml, sql, etc.) in this project. The user types/pastes every code change themselves. For every code change: state the target file path, state the reason/purpose in one line, then give the full code in a fenced code block. Do not ask again whether this rule applies — it always applies, no exceptions, until the user says otherwise.