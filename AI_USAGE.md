AI Used:
- ChatGPT
- GitHub Copilot

AI Mistake #1
Generated membership model without join/leave dates.

How I Found It:
Sam and Meera requirements failed.

Fix:
Added GroupMembership table.

AI Mistake #2
Treated settlement as expense.

Fix:
Created Settlement entity.

AI Mistake #3
Ignored missing currency values.

Fix:
Added anomaly detection.
