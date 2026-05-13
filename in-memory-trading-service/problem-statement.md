# In-Memory Trading System

Design and implement an efficient in-memory trading system similar to a stock exchange, where registered users can place, execute and cancel trades. The system should demonstrate synchronization and concurrency in a multi-threaded environment.


## Functional Requirements

Your system should support the following functionalities:

- A registered user can place, modify, and cancel his orders.
- A user should be able to query the status of his order
- The system should be able to execute trades based on matching buy and sell orders. A trade is executed when the buy and sell price of two different orders match/are equal. If multiple eligible orders can be matched with the same price, match the oldest orders first.
- Concurrent order placement, modification, cancellation, and execution should be handled appropriately.
- The system should maintain an order book per symbol, which holds all the current unexecuted orders.


Your system should store at least the following mentioned details.
- User details
    - User ID
    - User Name
    - Phone Number
    - Email Id
- Orders
    - Order ID
    - User ID
    - OrderType (Buy/Sell)
    - Stock Symbol (eg: AMZN, AAPL etc.)
    - Quantity
    - Price
    - Order Accepted Timestamp
    - Status (ACCEPTED, REJECTED, CANCELED)
- Trades
    - Trade ID
    - Trade Type (Buy/Sell)
    - Buyer Order Id
    - Seller Order Id
    - Stock Symbol
    - Quantity
    - Price
    - Trade Timestamp

- You should use the in-memory data structure of your preferred language to store the data but have the right abstractions so that other persistent stores can be plugged in.

## Additional Requirements

This is yet to be implemented: 

- Implement trade expiry. A trade should be automatically canceled if that trade is not executed within a specific time.