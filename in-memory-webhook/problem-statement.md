# Implement an In-Memory Webhook Delivery System 

Build a thread-safe webhook delivery service with the following requirements: 

## Required features 
- Submit a webhook event for asynchronous delivery.
- Multiple worker threads should process due events.
- If delivery fails, retry after a fixed delay. 
- Stop retrying after maxRetries. 
- Allow cancelling an event before it is delivered. 
- Allow querying current event status. 
- Ensure the same event is not processed concurrently more than once. 


## Problem statement 

Design and implement an in-memory webhook delivery system. 
- The system receives events that need to be delivered to external endpoints. 
- Each event should be processed asynchronously by worker threads. 
- If delivery fails, the system should retry the event after a delay, up to a maximum retry count. 
- Your system should be thread-safe and handle concurrent event creation, cancellation, status lookup, and worker execution. 

## Functional requirements 

Your system should support: 
- Create a webhook event for delivery
- Cancel a pending event 
- Query event status 
- Process due events asynchronously using worker threads 
- Retry failed events after a fixed delay 
- Stop retrying after max retries is reached 
- Ensure the same event is never processed concurrently by two workers 

## Concurrency expectations 

The system must safely handle: 
- multiple threads submitting events 
- multiple workers processing events 
- cancellation while an event is pending 
- no duplicate concurrent processing of the same event