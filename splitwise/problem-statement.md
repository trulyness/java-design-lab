# Splitwise

Design and implement an in-memory expense sharing system similar to Splitwise.

## Functional Requirements

- Users should be able to create accounts and manage their profile information.
- Users should be able to create groups and add other users to those groups.
- Users should be able to add expenses within a group, specifying the amount, description, and participants.
- The system should automatically split expenses among participants based on their share.
- The system should support different split methods:
  - equal split
  - percentage split
  - exact amount split
- Users should be able to view group expenses.
- Users should be able to view their individual balances with other users.
- Users should be able to settle up balances.
- Users should be able to view transaction history.
- The system should handle concurrent transactions and ensure data consistency.

## Expected Data

- Users
  - user ID
  - name
  - phone number
  - email ID
- Groups
  - group ID
  - group name
  - members
- Expenses
  - expense ID
  - group ID
  - paid-by user ID
  - amount
  - description
  - participants
  - split method
  - created timestamp
- Settlements
  - settlement ID
  - from user ID
  - to user ID
  - amount
  - created timestamp

## Additional Requirements

- Use in-memory data structures with clear abstractions so another persistence layer can be plugged in later.
- Keep balance updates consistent when multiple expenses or settlements are created concurrently.
