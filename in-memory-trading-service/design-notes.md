# In-Memory Trading Service: Design Notes

## Key Decisions
- Per-symbol `OrderBook` is used to isolate matching state by stock symbol.
- Matching is exact-price only: buy/sell orders match only when `buy.price == sell.price`.
- FIFO is enforced at each price level using `Deque<Order>` (`addLast`, `peekFirst`, `pollFirst`) so oldest eligible order is matched first.
- Partial fills are supported via `remainingQuantity` and order status transitions (`ACCEPTED` -> `PARTIALLY_EXECUTED` -> `EXECUTED`).
- `modifyOrder` is implemented as cancel-and-replace:
  - existing order is canceled
  - a new order with a new order ID is created and placed
- Custom domain exceptions are used for core error paths:
  - `OrderNotFoundException`
  - `OrderBookNotFoundException`
  - `UnauthorizedAccessException`
- Stock symbols are normalized (`trim().toUpperCase()`) at order placement to avoid case/format fragmentation in books.

## Concurrency Approach
- `InMemoryStore` uses `ConcurrentHashMap` for users, orders, trades, and symbol books.
- Each `OrderBook` has a `ReentrantLock`; all mutating operations for that symbol (`place`, `modify`, `cancel`, matching) are executed under that lock.
- This keeps matching and order-state updates serialized per symbol while allowing parallelism across different symbols.
- Trade creation and order status/quantity updates happen inside the same lock scope for deterministic per-symbol behavior.

## Data Model Notes
- `Order` stores immutable identifiers/core attributes plus mutable runtime fields:
  - immutable: `orderId`, `userId`, `orderType`, `stockSymbol`, `quantity`, `price`, `acceptedTimestamp`
  - mutable: `remainingQuantity`, `orderStatus`
- `Trade` captures execution facts (`buyerOrderId`, `sellerOrderId`, `quantity`, `price`, `tradeTimestamp`).
- Store abstraction is explicit (`Store` interface), allowing non-memory persistence implementations later.

## Known Tradeoffs
- `modifyOrder` creating a new ID simplifies correctness but changes identity semantics for clients.
- Order lookup in `modifyOrder`/`cancelOrder` is done before acquiring the symbol lock; state is revalidated under lock before mutation.

## Potential Enhancements (Not Implemented Yet)
- Trade/order expiry with automatic cancellation for stale unexecuted orders.
- Explicit rejected-order flow that persists rejected orders with `REJECTED` status instead of only throwing validation exceptions.
- Additional concurrency tests for modify/cancel race scenarios.
