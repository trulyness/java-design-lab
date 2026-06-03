# Splitwise: Design Notes

## Key Decisions

- Email is used as the immutable primary identifier for users in this design.
- User profile updates support changing mutable fields like name and phone number, but changing email is intentionally unsupported.
- In a real backend API, the email argument for profile updates represents authenticated user identity from request context or validated JWT claims, not arbitrary request input.
- For profile creation, email may either come from a verified signup flow or from an authenticated identity provider/JWT after login; this design assumes the email has already been verified by the auth layer.
- Group creation returns the generated group ID, which is the minimal value callers need for later group operations.
- Group membership changes require an authenticated email, and only an existing current group member may add or remove members.
- Group member emails must correspond to existing users before they can be added to a group.
- Expenses are group-scoped for the current implementation; direct non-group expenses are left as a future extension.
- Expense APIs require explicit participants from the caller. If the user selects the whole group, the frontend should send all group members rather than relying on an empty participant set.
- `SplitType` is stored for audit/history; public APIs such as equal, exact, and percentage expense creation determine the split behavior.

## Concurrency Approach

- To be filled in during implementation.

## Data Model Notes

- `User` is modeled as immutable: all fields are `final` and updates create a new `User` object with the same email.
- Profile updates will replace the existing `User` value in the in-memory data structures instead of mutating the existing object.
- `Group` is intentionally not fully immutable: membership is expected to change as users are added to or removed from a group.
- The `Group` members field is a final set reference, but the set contents are mutable through group membership operations.
- Initial group members are defensively copied during group creation so callers cannot mutate the group by holding on to the original input set.
- `Expense` stores both `createdBy` and `paidBy`: the authenticated user records the expense, while another group member may be the payer.
- `Expense.participantShares` stores final absolute amounts each participant owes, not raw percentage inputs.
- Money values use `BigDecimal` instead of integer or floating point types.
- Expense participants and exact-share maps are defensively copied before creating the stored expense.
- Exact splits require participant shares to be positive and add up to the total expense amount.
- Percentage splits require percentages to be positive and add up to `100`; raw percentages are converted into final absolute owed amounts before storing the expense.

## Known Tradeoffs

- Using email as the primary key keeps the initial design simple, but it assumes email never changes.
- If email changes need to be supported later, the design should move to a generated immutable user ID with email as a unique secondary attribute.
- Removing a member from a group affects future group participation only; historical expenses and balances should continue to reference the original participants.
- The design does not introduce group admins yet; any current group member may manage membership for the interview-scope implementation.
- Equal and percentage splits use straightforward two-decimal rounding per participant; tiny rounding residuals are accepted for now to keep the implementation simple.
- Removed group members are not granted separate historical group-expense access in this version; supporting that would require membership history or per-expense visibility rules.

## Potential Enhancements

- To be filled in during implementation.
