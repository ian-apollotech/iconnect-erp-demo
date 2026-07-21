# Purchase Order Process v2 - Clarifications & Questions

Questions and clarifications needed before implementation, organized by spec section.

---

## A. Actors & Permissions

1. **High Value Merchandiser scope** - Can a High Value Merchandiser also perform all regular Merchandiser actions, or are these two distinct permission sets that must both be assigned? For example, can a High Value Merchandiser create a PO from scratch, or do they only add budget-category items to an existing PO?
**ANSWER** : The permission to create/modify PO for regular items and items with budget are two separate permissions. While a role can be configured to have both permissions, this will be at the discretion of the system admin. Either permission will have the capability to create PO from scratch or add/modify items according to the scope of their permission.

2. **Supervisor self-approval** - Can a user who has both Merchandiser and Supervisor permissions create and then approve their own PO? Or must the approver be a different user than the creator?
**ANSWER** : Yes, the same user can approve their own PO if they have the permissions set for their role.

3. **Permission granularity for cancel/close** - The spec says a Merchandiser can cancel a Draft and manually close an approved PO. Can any Merchandiser close/cancel any PO, or only the creator? Is there a separate permission for manual close vs. cancel?
**ANSWER** : Any user with permission to cancel or close a PO can do so regardless of ownership. The permissions to close or cancel PO are `cancel Draft`, `cancel Submitted`, and `manual close`.

** Additional detail on Roles: each organization will have their own set of roles that they can use.

---

## B. Create Purchase Order

### Header Fields

4. **Buyer-Seller relationship validity** - The spec mentions Buyer-Seller relationships determine terms. Can a Buyer-Seller relationship expire or become inactive? If yes, what happens to a Draft PO that references a now-expired relationship when the user returns to edit it?
**ANSWER** : Yes, a Buyer-Seller relationship can expire or become inactive in terms of its effective dates. It cannot become inactive by changing a status or state. A PO will always base its data and be validated against its `Order Date`. `Order Date` is not the same as `Created Date` of the PO — it is the document date that affects business processes both within the system and for the vendor. The minimum threshold between the Approval date and Order Date is configurable (different for each organization) by the system admin.

5. **Order Date constraints** - Are there restrictions on what dates can be set as the Order Date? Can it be a past date? Can it be far in the future? Is there a maximum allowed range?
**ANSWER** : `Order Date` should always be greater than the date the PO is approved. Maximum allowed range is configurable (different for each organization) by the system admin. For auto-approved POs (no approval required), the PO still has an approval date (the date it was auto-approved), and that date should not be greater than the Order Date — no additional constraint on the submission date is needed.

6. **Cancel if not Delivered by Date constraints** - Must this date be after the Order Date? Is there a minimum gap between Order Date and this date?
**ANSWER** : Yes, `Cancel if not Delivered by Date` must be a date after the `Order Date`. The minimum gap between them is configurable (different for each organization) by the system admin.

7. **Cross Dock allocation method** - When a Cross Dock Order is received, how is inventory allocated across the multiple facilities? Is this FIFO, proportional to ordered quantities per facility, or manually assigned? Is the allocation logic part of PO or part of Receiving?
**ANSWER** : Cross Dock Order receiving will be part of the receiving module and will not be covered in the PO module.

8. **Facility List changes after items added** - Can the Merchandiser change the Facility List after items have already been added with per-facility quantities? If yes, what happens to existing per-facility quantity splits?
**ANSWER** : Yes, they can be modified while the PO is at Draft status. Removing a facility will remove all allocated orders for that facility. This should trigger a validation and update of the total quantity for each item to ensure MOQ and order multiple are still followed. Allocated orders for an added facility will be set to 0. Adding or removing a facility should show a clear prompt describing the changes on the allocated orders for each item so that the user can confirm the action. On submission, facilities with no allocated order will be prompted for removal. If the user declines, submission is blocked — all facilities on a submitted PO must have at least one item ordered.

9. **Delivery Location vs. Facility List** - Can the Delivery Location be a facility that is NOT in the Facility List? The spec says "may be any facility under the selected company" which seems broader than the Facility List.
**ANSWER** : Yes. The delivery location may be a warehouse facility that will handle the crossdock for the facilities in the Facility List.

10. **Sales Order Fulfillment - partial coverage** - For SOF POs, must the PO cover ALL items in the referenced Sales Order, or can it cover a subset? Can multiple POs reference the same Sales Order?
**ANSWER** : SOF POs may cover only a subset of the referenced Sales Order. Multiple POs can reference the same Sales Order (e.g., supplement inventory on hand, items from different vendors). Pre-populated quantities are always the full Sales Order quantities — the user manually determines the appropriate quantity. Only items from the Sales Order that the Seller sells can be added into the PO. Ordering schedule will also be considered when determining if an item can be added.

11. **Changing Buyer or Seller on a Draft** - Can the Buyer or Seller be changed on an existing Draft PO that already has items? If yes, all items would presumably need to be cleared and re-added. Should the system warn or block this?
**ANSWER** : No, the system should block this if items are already added into the Draft PO. User can cancel the PO or remove all items first before making changes to the Buyer or Seller.

### Items

12. **Day-of-week schedule filtering** - If the Order Date is changed on a Draft PO that already has items, and some items are no longer valid for the new day of week, are those items automatically removed, flagged as errors, or does the system block the date change?
**ANSWER** : This should be flagged as errors. User can still save the draft with errors but submission should be blocked until resolved. Errors are displayed when the user navigates away from the PO Details page (triggering validation + save).

13. **UOM display and entry** - When a Merchandiser enters quantities, do they enter in the vendor's UOM or the company's SKU? The spec says "Quantity is based on the UOM set by the vendor" but also mentions "UOM/SKU ordering configuration" per Buyer-Seller relationship. Which unit does the user actually type into the form?
**ANSWER** : The quantity entry unit is configurable (different for each organization) by the system admin. If the company's SKU is used to enter the quantity, the vendor's UOM equivalent should be displayed in realtime to guide the encoder. When MOQ or order multiple warnings are triggered, they display the required value in vendor's UOM and the equivalent value in company's SKU. Reports (unless specifically stated) and received quantity will always use the vendor's UOM.

14. **Order multiple and minimum quantity - who defines values?** - Are minimum order quantity and order multiple defined per item per Buyer-Seller relationship, or globally per item per vendor? Can these values be date-sensitive like prices?
**ANSWER** : Per Buyer-Seller relationship. These data are date-sensitive.

15. **Budget calculation basis** - Is the budget compared against the cost (vendor cost) or the price (selling price) of items? How is cost determined - is it always from the Buyer-Seller relationship terms as of the Order Date?
**ANSWER** : Budget is compared against the vendor cost defined in the Buyer-Seller relationship terms as of the Order Date.

16. **Budget period definition** - How are budget periods defined? Monthly, quarterly, annually, or custom? Is this configurable per company, per category, or globally?
**ANSWER** : Budget periods are defined in the Budget Planner module, not covered by the PO. PO just needs to get the active budgets as of the Order Date of the PO.

17. **Budget - concurrent PO handling** - When multiple Merchandisers are creating POs simultaneously for the same budget category, is budget reserved when items are added to a Draft, or only validated at submission and consumed at approval? Between submission and approval, can another PO consume the remaining budget causing the first PO's approval to fail?
**ANSWER** : There is no reservation of budget. Budget is validated on submission and approval. Budget is consumed at successful PO approval.

18. **Item uniqueness** - Can the same item appear multiple times on the same PO (e.g., same product but different delivery dates or notes)? Or must each item be unique per PO?
**ANSWER** : Item should be unique per PO.

19. **Item-level fields** - Beyond quantity and per-facility quantity split, are there other fields at the item level? For example: item-level notes, requested delivery date per item, or discount/special pricing?
**ANSWER** : Add `item-level notes`.

20. **Cost display** - Is the item cost (unit cost, extended cost) visible to the Merchandiser during PO creation, or only to the Supervisor during approval? Are costs always from the Buyer-Seller relationship, or can they be overridden?
**ANSWER** : Yes, they are visible to anyone with access to view, create, or modify the PO. Costs are always from the Buyer-Seller relationship.

### Saving, Submitting, Cancelling

21. **Draft save validation** - Does saving a Draft PO (manually or via auto-save) trigger validation?
**ANSWER** : Validation runs on: (1) clicking the "Save Draft" button, (2) navigating away from the PO Details page (which also persists the current state), (3) PO Submission, and (4) PO Approval. Auto-save does NOT trigger validation. Session expiry does not trigger auto-save.

22. **Cloning - what about per-facility quantities?** - When a PO is cloned, are per-facility quantity splits also copied? If the original PO was a Cross Dock Order with facility-specific quantities, are those preserved in the clone?
**ANSWER** : Yes, per-facility quantity splits are also copied.

23. **Cloning - items no longer available** - If a cloned PO contains items that the vendor no longer sells to the buyer, are those items excluded from the clone, or included with validation errors?
**ANSWER** : Included with validation errors.

---

## C. Approving Purchase Order

24. **Approval validation scope** - What exactly does "system validation" at approval time include?
**ANSWER** : Buyer status, Seller status, Dates correctness (Order Date > approval date; Cancel if not Delivered by Date > Order Date if set), Selected facilities and Delivery Location belong to the Buyer, active Buyer-Seller relationships for the items ordered, Buyer-Seller relationship validity, item ordering schedule (day-of-week), MOQ, order multiples, budget, permissions.

25. **Partial approval** - Can a Supervisor approve some items and reject others in a single review? Or is approval always all-or-nothing at the PO level?
**ANSWER** : Approval is always all-or-nothing at the PO level.

26. **Approval configuration** - The spec mentions the system can be "configured to require approval." Is this a global setting, per-company, per-PO-type, or per-value-threshold?
**ANSWER** : The requirement for PO approval is configurable (different for each organization) by the system admin.

27. **Multi-level approval** - Is there ever a need for multiple levels of approval (e.g., supervisor approval for POs under a threshold, director approval above it)? Or is single-level approval sufficient?
**ANSWER** : Single-level approval is sufficient.

28. **Stale submitted PO** - Is there a timeout or escalation for POs that remain in Submitted status for too long? Can the Merchandiser withdraw (un-submit) a PO that hasn't been reviewed yet?
**ANSWER** : Notifications and dashboard should be enough to remind the approver to review submitted POs. Merchandiser cannot withdraw (un-submit) a PO.

---

## D. Rejecting Purchase Order

29. **Rejection mark workflow** - The spec says rejection marks are saved persistently as the supervisor reviews. Are these marks visible to the Merchandiser in real-time, or only after the supervisor formally submits the rejection?
**ANSWER** : Marks are visible only after the supervisor formally submits the rejection.

30. **Rejection history** - When a PO goes through multiple rejection/resubmission cycles, is the full history of all rejection notes preserved and visible? Or does each resubmission clear previous rejection notes?
**ANSWER** : Each resubmission clears previous rejection notes. Previous rejection notes can still be viewed in the Activity logs.

31. **Resubmission field highlighting** - The spec says "fields changed by the Merchandiser since the last rejection are highlighted." Does this include item-level changes (quantity changes, items added/removed), or only header field changes?
**ANSWER** : Includes item-level changes like quantity changes, items added/removed, etc.

---

## E. Closing Purchase Order

32. **Short Close threshold** - Is there a tolerance or threshold for "fully received"? For example, if 100 units were ordered and 99 received, does this count as Completed or would it be Short Closed?
**ANSWER** : Fully received means ordered quantity equals total received quantity. There is no tolerance or threshold.

33. **Manual close permissions** - Can a Supervisor also manually close a PO, or only a Merchandiser? The spec only mentions Merchandiser and High Value Merchandiser for this action.
**ANSWER** : Manual closing is a permission that can also be added to a supervisor. This can be configured by the system admin.

34. **Receiving in progress** - Can a PO be manually closed while a Receiving Advice is in progress (created but not finalized)? Should the system check for pending receiving documents?
**ANSWER** : Yes, the system should check for pending receiving documents associated to the PO. If pending receiving documents exist, the manual close is blocked. Definition of "pending" is part of the Receiving Module — skip until Receiving Module is defined.

---

## F. Referenced Documents

35. **Returns impact on PO** - When items are returned via a Returns Announcement, does this affect the PO item status or received quantities?
**ANSWER** : No. Association of the return to the PO is for reference and lookup purposes only. It will not have any changes on the PO other than providing a link to the linked returns.

---

## G. View of Purchase Order

36. **List/Search view** - Beyond the individual PO view, is there a PO list/search page?
**ANSWER** : Yes. Search filters: order date range, status, buyer, seller, facility, PO document ID. Default sort: Created Date, latest first. No search by item name or GTIN needed.

37. **Export** - Is there a requirement to export PO data (PDF for printing, CSV/Excel for reporting)?
**ANSWER** : Yes. Approved formats:
- **PDF**: Organization name/logo, Document ID, Status, Created By, Created Date, Approved By, Approved Date; Buyer and Seller details; PO Details (Order Date, PO Type, Delivery Location, Facility List, Cancel if not Delivered by Date, Referenced Sales Order if SOF, Notes); Item table (Line #, Product ID/GTIN, Product Name, Category, Vendor UOM, Unit Cost, Ordered Qty, Extended Cost — per-facility columns for Cross Dock); Totals; Signature block.
- **CSV**: One row per item line — PO Document ID, PO Status, Order Date, PO Type, Buyer, Seller, Delivery Location, Facility columns, Product ID, GTIN, Product Name, Category, Vendor UOM, Unit Cost, Ordered Qty, Extended Cost, Item Status, Cancel if not Delivered by Date, Created By, Created Date, Approved By, Approved Date.

38. **Audit trail / Activity log** - What actions are logged?
**ANSWER** : All field changes and status transitions are logged with acting user and timestamp. Details are only visible when activity log is expanded. Overview shows a short summary of the activity type. Events:
    - PO status changes: created, submitted, approved, rejected, cancelled, completed
    - PO Item status changes: created, submitted, approved, rejected, cancelled, completed, short closed
    - Modification of PO details
    - Modification of item details
    - PO manually closed
    - PO / PO Item rejected with reasons
    - System validation errors
    - PO cloned from [source PO Document ID] (recorded on the new cloned PO)

---

## H. Dashboard

39. **Dashboard scope** - Are the dashboard counts filtered by the logged-in user's assigned companies/facilities, or do they see counts across the entire organization?
**ANSWER** : Dashboard is visible across the entire organization for users who have permission to view the dashboard. Clicking a count drills down to the matching PO list.

40. **Dashboard items** - Confirmed dashboard items:
    1. Number of POs not submitted
    2. Number of POs needing approval
    3. Number of POs rejected and needing revisions
    4. Number of approved POs with no items received
    5. Number of approved POs partially received
    6. Number of approved POs nearing expiration of `Cancel if not Delivered by Date` (configurable lead time, default 5 days)
    7. Number of approved POs with `Cancel if not Delivered by Date` expired that are partially received (pending manual review/close)

**ANSWER** : No additional dashboard items beyond the above.

---

## I. Statuses & Transitions

41. **Approved to Cancelled directly** - Can an Approved PO be directly cancelled without going through manual close?
**ANSWER** : No direct cancel of an Approved PO. All close/cancel of Approved POs goes through manual close, which determines the outcome: Cancelled (no items received) or Completed (at least one item received).

42. **System auto-cancel scope** - What happens to partially received POs when `Cancel if not Delivered by Date` expires?
**ANSWER** : No action taken for partially received POs. These appear in dashboard item #7, and a notification is sent to users with the `manual close` permission.

---

## General / Cross-Cutting

43. **Notifications** - What notifications are needed and to whom?
**ANSWER** : Notifications sent in-app and by email. Email can be opted out per user (managed in user module). Events:
    - PO needing approval → users with approve PO permission
    - PO not yet submitted → users with create/modify/submit PO permission
    - PO rejected → users with create/modify PO permission
    - Approved PO nearing `Cancel if not Delivered by Date` → users with manual close permission
    - Partially received PO with expired `Cancel if not Delivered by Date` → users with manual close permission

    Note: Merchandiser is NOT notified when their PO is approved.

44. **Currency** - Is multi-currency supported?
**ANSWER** : Single currency per organization. Default currency is configurable by the system admin.

45. **Tax handling** - Are taxes calculated on the PO?
**ANSWER** : No tax handling on PO.

46. **Concurrent Draft POs for same Buyer-Seller** - Can multiple Draft POs exist for the same Buyer-Seller pair?
**ANSWER** : Yes. Warn the user (after setting PO details) if a Draft PO already exists for the same Buyer-Seller, PO type, and Order Date. Warning provides a link to the existing Draft PO.

47. **Edit lock timeout** - What is the session expiry / edit lock timeout?
**ANSWER** : Session expiry is part of the login module. Idle timeout is configurable per organization by the system admin. Logging out removes the edit lock. System admin can force-expire a user session.

48. **Order Date change on Draft - re-fetch behavior** - Are new prices shown vs. old values when Order Date changes?
**ANSWER** : No comparison needed. A warning prompt that changing the Order Date will affect PO data is sufficient.

---

## J. Post-Answer Review 1

49. **PO Type definitions (revised)** - The spec says setting multiple Facilities auto-sets the PO Type to Cross Dock. What is the complete revised definition for each PO type, including the Delivery Location relationship?
**ANSWER** : Revision and clarification:
    - `Inventory Replenishment`: can have no facility set — items are expected to be added to the inventory of the set Delivery Location. Or can have the same facility set on both Facility List and Delivery Location.
    - `Cross Dock Order`: multiple facilities set regardless of the delivery location. OR single facility on the Facility List but a different facility set as Delivery Location.

50. **SOF pre-populated quantities and existing fulfillment** - When multiple POs reference the same Sales Order, are pre-populated quantities for a new SOF PO reduced by quantities already covered by other active SOF POs on that Sales Order, or always the full Sales Order quantity?
**ANSWER** : Always the full Sales Order quantity. User will manually determine if there are available inventory to allocate and will set the appropriate quantity for the item.

51. **Notifications delivery mechanism** - Are notifications in-app only (dashboard alerts, bell icon, etc.), or are email notifications also sent? Is the notification channel configurable per user or per organization?
**ANSWER** : In-app and email notifications.

52. **PO search by item** - The PO list search filters include order date range, status, buyer, seller, facility, and document ID. Is searching/filtering by item name or GTIN also needed?
**ANSWER** : No need for searching/filtering by item name or GTIN.

53. **Facility with zero allocation at submission** - The answer to Q8 says facilities with no allocated orders are prompted for removal before submission. If the user declines to remove them, is submission blocked? Or is submission allowed with those zero-allocation facilities still on the PO?
**ANSWER** : Submission should be blocked. All facilities on a submitted PO must have at least one item ordered.

54. **Activity log visibility for in-progress reviews** - Section D.7 states that activity log entries from an in-progress review are visible only to supervisor-level users until formally completed. Does this also apply to auto-save events and draft modification logs that happen while the PO is under review?
**ANSWER** : Yes, this also applies to auto-save events and draft modification logs that happen while the PO is under review by the supervisor.

55. **Notification for rejected PO** - The answer to Q43 covers notifications for POs requiring action. Is a notification sent to the Merchandiser when their PO is rejected, prompting them to revise and resubmit?
**ANSWER** : Yes. The notification goes to users with create/modify PO permission.

56. **PO list default sort order** - On the PO list/search page, what is the default sort order?
**ANSWER** : Sort by Created Date, latest first.

---

## K. Post-Answer Review 2

57. **Spec contradiction — Draft save validation** - The answer to Q21 states "saving a Draft PO whether manually or automatic does not do any validation." However, the original spec (Section B.4) explicitly states "The system validates items against the Buyer-Seller relationship and item schedule when saving to Draft." Also, Q12's answer says items are flagged as errors when the Order Date changes. Please clarify: does saving a Draft run any validation?
**ANSWER** : Revision: Validation will only happen when manually saving Draft PO, Submitting PO, and Approving PO.

58. **Revised PO Type — Inventory Replenishment with no Facility List** - The revised PO Type definition (Q49) says Inventory Replenishment can have no facility set. Does this make the Facility List optional? If the user sets a Delivery Location but no Facility List entries, does the system treat the Delivery Location as the sole receiving facility for MOQ and order multiple validation?
**ANSWER** : Yes. Not adding a facility in the Facility List will automatically add the facility selected in Delivery Location into the Facility List.

59. **Revised PO Type — Cross Dock trigger for single facility + different Delivery Location** - The revised definition (Q49) says Cross Dock also applies to "single facility in Facility List but a different Delivery Location." Is the PO Type automatically switched to Cross Dock when the user selects a Delivery Location that differs from the single facility in the Facility List? Or must the user manually change the PO Type?
**ANSWER** : Yes. Include a prompt to the user to warn the change of PO Type.

60. **Revised PO Type — MOQ aggregate rule for single-facility Cross Dock** - For a Cross Dock Order with only one facility in the Facility List (but a different Delivery Location), the spec says MOQ and order multiple are "enforced on the aggregate total quantity across all facilities." With only one facility in the list, the aggregate equals that facility's quantity. Please confirm.
**ANSWER** : Clarification: The order is only for the facilities in the Facility List. It does not include the facility in Delivery Location. So a single-facility Cross Dock will only have the total ordered of the single facility in the Facility List.

61. **Pending Receiving Advice definition** - The answer to Q34 confirms blocking, but does not define what makes a Receiving Advice "pending." Is a pending Receiving Advice one that has been created but not yet finalized/posted in the receiving module?
**ANSWER** : Defining Pending Receiving Advice is not part of PO module but of Receiving Module. Skip this requirement first until Receiving Module is defined.

62. **Email notification opt-out** - The answer to Q51 confirms both in-app and email notifications. Can users configure their notification preferences (e.g., opt out of email for certain event types)?
**ANSWER** : Email notification can be opted out based on user settings. This is part of user module.

63. **Notifications for PO approved** - Should the Merchandiser be notified when their submitted PO is approved? This allows the Merchandiser to know the PO is active without checking the dashboard.
**ANSWER** : Revised: Merchandiser does not need to be notified when their submitted PO is approved.

---

## L. Post-Answer Review 3

64. **Manual save vs. auto-save — is there still an explicit Save button?** - With the revised rule that validation runs on manual save but not auto-save, is there still an explicit "Save Draft" button available to the user? Clarifying this determines when the user can intentionally trigger validation on a Draft.
**ANSWER** : Yes, maintain a "Save Draft" button.

65. **Error flagging on Order Date change — when are errors shown?** - Q12 says items are "flagged as errors" when the Order Date changes. Since validation only runs on manual save (not auto-save or real-time), are errors from an Order Date change only revealed when the user manually saves the Draft? Or does the system immediately flag affected items?
**ANSWER** : Validate on changes made in PO details. Assumption: page to edit item is different from page to edit PO details. User needs to navigate out of the PO details to do changes on the PO items. Navigating out of the PO detail when edited will trigger a validation of items and update the PO item page with the errors displayed resulted from the validation.

66. **Auto-add Delivery Location to Facility List — effect of subsequent Delivery Location change** - The answer to Q58 says the system auto-adds the Delivery Location to the Facility List when the Facility List is empty. If the user later changes the Delivery Location, does the Facility List auto-update? What if items have already been added?
**ANSWER** : Clarification and revision:
    - Empty Facility List but Delivery Location set → facility in Delivery Location added into Facility List
    - Single facility set in Facility List but empty Delivery Location → Block navigation out; Delivery Location is required
    - Changing Delivery Location when previous facility in Delivery Location and set facility in Facility List is the same → Prompt and confirm from user if facility in Facility List needs to be updated as well or kept as it is
    - Changing set facility in Facility List when previous facility is the same as the facility set in Delivery Location → Prompt and confirm from user if facility in Delivery Location needs to be updated as well or kept as it is

67. **Cross Dock PO Type auto-revert (reverse of Q59)** - The answer to Q59 says PO Type auto-switches to Cross Dock when Delivery Location ≠ single Facility List entry. Does the reverse apply: if the Delivery Location is changed to match the single facility in the Facility List, does the PO Type automatically revert to `Inventory Replenishment` (with a user prompt)?
**ANSWER** : Yes.

---

## M. Post-Answer Review 4

68. **Navigation-triggered validation — does it save the draft?** - The answer to Q65 says navigating away from the PO Details page triggers item validation. Does this navigation-triggered validation also save the draft (persisting the updated details to the database), or does it only run validation in-memory without saving?
**ANSWER** : Navigation-triggered validation also saves the draft, persisting the updated details to the database.

69. **Delivery Location required for all PO types** - The answer to Q66 says the system blocks navigation away from PO Details when Delivery Location is empty. Does this mean Delivery Location is a required field for ALL PO types before the user can proceed to add items?
**ANSWER** : Yes, Delivery Location is a required field for ALL PO types.

70. **Facility-Delivery Location sync with existing items** - The answer to Q66 describes prompts when the user updates a Delivery Location or Facility List entry that previously matched the other field. If the user confirms the update and items already have per-facility quantities assigned to the old facility, does the system follow the full facility-removal flow from Q8 (prompt showing per-item impact, quantities cleared for removed facility, new facility starts at 0)?
**ANSWER** : Yes, system will still follow that. Prompt to confirm changes between Delivery Location and Facility List is different from the prompt to inform changing Facility List showing per-item impact, quantities cleared for removed facility, new facility starts at 0.

---

## N. Post-Answer Review 5

71. **Edit lock scope — PO-level or page-level** - The spec states only one Merchandiser can actively edit a PO at a time, and only one Supervisor can actively review at a time. With PO Details and PO Items confirmed as separate pages, is the edit lock applied at the PO level (locking both pages together for the entire PO)?
**ANSWER** : Lock is at PO level.

72. **Items page real-time validation vs. deferred** - The spec says MOQ and order multiple violations "block the action" when entering quantities, implying real-time validation on the Items page. However, budget is "validated at submission." Please confirm which validations run in real-time on the Items page vs. which are deferred:
    - MOQ check → real-time (blocks input)?
    - Order multiple check → real-time (blocks input)?
    - Budget check → deferred to submission?
    - Ordering schedule check → real-time (prevents adding the item)?
    - Buyer-Seller relationship validity for item → real-time when adding, or deferred?
**ANSWER** : Rationale between real-time and deferred validation: Validation is expensive since it involves querying a lot of data, and PO items can be in the hundreds. Do real-time only on data that is small enough to be stored with the response so that computation and comparison can be done in script without needing to query from the backend. On manual save of Draft, submission, and approval will do all validations.

73. **Two-prompt flow cancellation** - The answer to Q70 confirms two sequential prompts when syncing Delivery Location and Facility List with existing items: (1) confirm sync, (2) confirm facility-removal item impact. If the user cancels at the second prompt, is the entire change rolled back, or does the Delivery Location change stand while the Facility List remains unchanged?
**ANSWER** : The two prompts are not necessarily dependent on each other. First prompt is to confirm if Delivery Location and Facility List should be kept in sync or not. Second prompt is triggered only when changes to the Facility List would have changes on items. Therefore, not changing Facility List will not need the second prompt since items are not affected.

---

## O. Post-Answer Review 6 (Open Questions)

74. **Two-prompt rollback — declining the Facility List change** - Building on Q73: if the user confirms Prompt 1 (yes, update the Facility List to match the new Delivery Location) and Prompt 2 is shown (per-item impact warning), but the user then **declines** Prompt 2, what is the outcome? Is the entire change (including the Delivery Location update) rolled back to original values? Or does the Delivery Location change stand while only the Facility List update is cancelled?
**ANSWER** : 
    - prompt 1 yes (yes, update the Facility List to match the new Delivery Location), prompt 2 no (retain current facility in Facility List) -> no changes in Facility List
    - prompt 1 yes (yes, update the Facility List to match the new Delivery Location), prompt 2 yes (retain current facility in Facility List) -> update Facility List


75. **Real-time validation — confirming specific behaviors based on Q72 principle** - The answer to Q72 establishes the principle: real-time for client-side computation from preloaded data; deferred (to Save Draft/Submission/Approval) for backend queries. Based on this principle, confirming the expected behaviors:
    - MOQ and order multiple → preloaded with item data when items page loads → real-time client-side block when entering quantity. Correct? **ANSWER** : Yes
    - Budget → requires backend query to get available balance → validated only at Save Draft/Submission/Approval. Correct?
    **ANSWER** : yes. latest budget needs to be fetched which is only possible through backend query.
    - Ordering schedule day-of-week → preloaded with item data → applied as a real-time filter when browsing/adding items (item cannot be added if not on schedule for the Order Date's day of week). Correct?
    **ANSWER** : No since retrieving items already needs backend query. the backend can already filter out the items to be added based on its schedule. For Rechecking of schedule, you can do realtime checking if item has schedule with it to validate the schedule.
    - Buyer-Seller relationship validity for a specific item → backend query → validated at Save Draft/Submission/Approval. Correct?
    **ANSWER** : yes

---

## P. Post-Answer Review 7 (Open Questions)

76. **Item search by GTIN/name when item is not on schedule** - The answer to Q75 says the backend filters items by ordering schedule when browsing, so unscheduled items do not appear in the add-item list. When a user searches for a specific item one-by-one (by product name or GTIN), and that item exists in the Buyer-Seller relationship but is NOT scheduled for the Order Date's day of week, what does the system show? Does the item appear in search results with a "not on schedule" indication, or does it simply not appear — leaving the user without an explanation for why their search returned no result?
**ANSWER** : item appear in search results but cannot be selected with a "not on schedule" indication
77. **SOF pre-populated quantities — display in configured entry unit** - The spec states that SOF item quantities are pre-populated by converting from the Sales Order's company SKU quantity to the vendor's UOM, then rounding up to satisfy MOQ and order multiple. Since the quantity entry unit is configurable per organization (Q13 — either vendor's UOM or company's SKU), should the pre-populated quantity be displayed to the user in the organization's configured entry unit? For example, if the org is configured to enter in company's SKU, should the system convert the pre-populated vendor UOM quantity back to company's SKU for display, or are SOF pre-populated quantities always shown in vendor's UOM regardless of the org's configured entry unit?
**ANSWER** : Revision: To make it easier for the user to trace and make decision, quantities encoded for PO of type SOF will be in company's SKU with a converversion to vendor's UOM (similar to how PO of type CrossDock is implemented). Furthermore, to make this process flexible across different user requirements, make this behavior configurable per organization by the system admin. 
    - `Round Up` : Items are prepopulated limited only to the items with Buyer-Seller relationship and schedule aligns with the set Order Date.  Quantity is set based on the quantities on the SO and will be rounded up to satisfy MOQ and order multiple. This is the default configuration.
    - `As Is` : Items are prepopulated limited only to the items with Buyer-Seller relationship and schedule aligns with the set Order Date.  Quantity is set based on the quantities on the SO but will not be rounded up to satisfy MOQ and order multiple. We will let PO validation on PO sumbission flag the quantities that does not satisfy the vendor's satisfy MOQ and order multiple.
    - `Manual` : No items is added into the PO. User will manually add the items items with Buyer-Seller relationship and schedule aligns with the set Order Date. Quantity of the added items will be set based on the quantities on the SO but will not be rounded up to satisfy MOQ and order multiple.

78. **Activity log granularity — per-field or per-save** - The activity log includes events "Modification of PO details" and "Modification of item details" (Q38). Does each log entry record the specific field changed with old and new values (e.g., "Order Date changed from 2024-05-01 to 2024-05-08"), or does it record a summary event (e.g., "PO details modified") without field-level detail? The answer to Q38 mentions "detailed description of state changes, errors, value changes" — confirming whether this means individual field-level entries or a grouped save-event entry.
**ANSWER** : Stored activity log entry should be stored per field. However, activity logs on display on the page will be simple and summarized enough to give the user an idea but not flood the whole page. Display only milestones of the PO and indication of changes made on PO details/items, etc.

79. **"PO not yet submitted" notification — trigger timing** - Q43 lists "PO not yet submitted → users with create/modify/submit PO permission" as a notification event. What triggers this notification? Is it: (a) time-based — a Draft PO not submitted after a configurable number of days; (b) a daily digest summarizing all open Draft POs; or (c) something else? Without a defined trigger, this notification cannot be implemented.
**ANSWER** : a daily digest summarizing all open Draft POs when the user logs in.

80. **SOF pre-population Q77 — four conflicts requiring resolution** - The Q77 answer introduces significant revisions to SOF behavior but contains internal contradictions and conflicts with earlier answers:

    **(a) Mode naming/description contradiction**: The `Round Up` mode description says "Quantity is set based on the quantities on the SO and will **not** be rounded up to satisfy MOQ and order multiple." However, the name `Round Up` implies the opposite. The `As Is` mode has a nearly identical description. These two modes appear to have their descriptions accidentally swapped, or one contains a typo. Please clarify which mode rounds quantities UP to satisfy MOQ and order multiple, and which takes quantities exactly as-is from the Sales Order.
    **ANSWER** : Revising definition for configuration `Round Up` : Items are prepopulated limited only to the items with Buyer-Seller relationship and schedule aligns with the set Order Date.  Quantity is set based on the quantities on the SO and will be rounded up to satisfy MOQ and order multiple. This is the default configuration.

    **(b) Conflict with Q10 and Q50**: Q10 and Q50 both state "pre-populated quantities are always the full Sales Order quantities." The `Manual` mode in Q77 pre-populates no items at all. Please confirm Q10 and Q50 are fully superseded by Q77's three-mode answer for SOF POs.
    **ANSWER** : Yes, Q10 and Q50 are fully superseded by Q77's three-mode answer for SOF POs.

    **(c) Conflict with original spec**: The original spec states SOF quantities "are adjusted upward to satisfy the minimum order quantity and order multiple for that item." All three Q77 modes say quantities are NOT rounded up by the system at pre-population. Please confirm the original spec behavior is superseded, and clarify which Q77 mode (if any) corresponds to the original rounded-up behavior.
    **ANSWER** : Original spec behavior is superseded by Q77. Revising definition for configuration `Round Up` : Items are prepopulated limited only to the items with Buyer-Seller relationship and schedule aligns with the set Order Date.  Quantity is set based on the quantities on the SO and will be rounded up to satisfy MOQ and order multiple. This is the default configuration.

    **(d) Manual mode quantity source**: In `Manual` mode, "No items is added into the PO. User will manually add items." When the user manually adds an item that is listed in the referenced Sales Order, is the quantity field pre-filled from the SO quantity (which the user can then edit), or does the user start from zero?
    **ANSWER** : user start from zero

81. **SOF Manual mode — quantity contradiction between Q77 and Q80(d)** - The Q77 answer for `Manual` mode states: "Quantity of the added items will be set based on the quantities on the SO but will not be rounded up to satisfy MOQ and order multiple." However, Q80(d) answers that "user start from zero." These are contradictory — one says quantities are seeded from the SO, the other says the user starts at zero. Please confirm which is correct: when a user manually adds an item from the referenced Sales Order in `Manual` mode, is the quantity field (a) pre-filled with the Sales Order quantity for that item, which the user can then edit, or (b) empty/zero, requiring the user to enter the quantity from scratch?
**ANSWER** : Clarification: Disregard Q80(d) and still follow Q77 answer for `Manual` mode states: "Quantity of the added items will be set based on the quantities on the SO but will not be rounded up to satisfy MOQ and order multiple."

82. **SOF entry unit — Q77 override vs. org-configured entry unit from Q13** - Q13 states the quantity entry unit is configurable per organization (vendor's UOM or company's SKU). Q77 states that for SOF POs, "quantities encoded for PO of type SOF will be in company's SKU with a conversion to vendor's UOM." This creates ambiguity: (a) does the SOF-specific rule in Q77 **override** the org's configured entry unit — meaning SOF POs always use company's SKU for entry regardless of the org setting? Or (b) does Q77 mean that for SOF POs, **both** the company's SKU and vendor's UOM quantities are always shown side-by-side (similar to Cross Dock dual-facility display), while the actual input field still follows the org's configured entry unit from Q13?
**ANSWER** : Revision: Q13 configuration only applies for for PO type `Inventory Replenishment`. `Cross Dock Order` and `Sales Order Fulfillment` **both** the company's SKU and vendor's UOM quantities are always shown side-by-side

---

## Section Q — Post-Answer Review 9

83. **Cross Dock and SOF dual-display — which field is the editable input?** - Q82 confirms that for `Cross Dock Order` and `Sales Order Fulfillment` POs, both the company's SKU quantity and the vendor's UOM quantity are always shown side-by-side. However, it does not specify which of the two is the actual **editable input field** the user types into. Q77 states "quantities encoded for PO of type SOF will be in company's SKU," suggesting company's SKU is the input. Please confirm: when entering quantities on a Cross Dock or SOF PO, does the user type into (a) the company's SKU field only — with vendor's UOM shown as a computed read-only conversion, (b) the vendor's UOM field only — with company's SKU shown as a computed read-only conversion, or (c) either field — with the other auto-converting in real time (i.e., both fields are editable)?
**ANSWER** : the company's SKU field only — with vendor's UOM shown as a computed read-only conversion

84. **IR entry unit when vendor's UOM is configured — is company's SKU shown?** - Q13 specifies only one direction: "If the company's SKU is used to enter the quantity, the vendor's UOM equivalent should be displayed in realtime to guide the encoder." Now that Q82/Q83 have established that Cross Dock and SOF always use company's SKU as the editable input, Q13's org-configurable entry unit applies **only** to `Inventory Replenishment` POs. The gap: when the org is configured to use **vendor's UOM** as the entry unit for IR POs, is the company's SKU equivalent shown as a computed read-only value alongside the input (symmetric to the company's SKU → vendor's UOM display described in Q13)? Or is only the vendor's UOM field shown with no SKU conversion displayed?
**ANSWER** : Yes. If vendor UOM is configured, viewing and editing is in vendor UOM. if company's SKU is configured, viewing and editing is in company's SKU but with vendor's UOM shown as a computed read-only conversion.

---

## Section R — Post-Answer Review 10

85. **Cross Dock per-facility quantity grid — SKU and UOM display per row** - Q82/Q83 confirm that for Cross Dock POs, the quantity is entered in company's SKU with vendor's UOM shown as a computed read-only conversion. For a Cross Dock PO with multiple facilities, the items page has a per-facility quantity allocation grid (each facility is a separate row or column with its own quantity). Does **each per-facility quantity** show both the company's SKU editable input and the vendor's UOM read-only conversion? Or is only one unit shown per facility row, with the conversion displayed only in the aggregate total row for that item?
**ANSWER** : each facility is a separate row or column with its own quantity in company's SKU. the conversion displayed only in the aggregate total row  in vendor UOM for that item.

86. **As Is / Manual SOF mode — does sub-MOQ pre-population hard-block submission?** - Q77 (`As Is` mode) states: "We will let PO validation on PO submission **flag** the quantities that do not satisfy the vendor's MOQ and order multiple." The word "flag" is ambiguous. Q57 states submission is "**hard-blocked** until all validation errors are resolved." These two descriptions conflict when applied to `As Is` (and `Manual`) mode, where items may be intentionally pre-populated at SO quantities that fall below the vendor's MOQ. Please clarify: for `As Is` and `Manual` SOF modes, when a pre-populated quantity is below MOQ or not a valid order multiple at the time of submission, is the submission (a) **hard-blocked** — the user cannot submit until all quantities are manually corrected to satisfy MOQ/order-multiple, or (b) **soft-warned** — the user sees a flagged warning but can still submit, and the supervisor sees the sub-MOQ quantities during review?
**ANSWER** : **hard-blocked** — the user cannot submit until all quantities are manually corrected to satisfy MOQ/order-multiple

---

## Section S — Post-Answer Review 11

87. **Changing PO Type away from SOF on a Draft** - The auto-switching rules in Q59 and Q67 only cover switching between `Inventory Replenishment` and `Cross Dock Order`. The spec states the Referenced Sales Order "cannot be changed after the PO is created." Two related gaps: (a) Can the user manually change the PO Type from `Sales Order Fulfillment` to `Inventory Replenishment` or `Cross Dock Order` on a Draft PO that already has a Referenced Sales Order set and items pre-populated? (b) If yes, what happens to the Referenced Sales Order field and the pre-populated items — are they cleared, flagged as errors, or preserved?
**ANSWER** : No, PO Type `Sales Order Fulfillment` cannot be changes to `Inventory Replenishment` or `Cross Dock Order` on a Draft PO that already has a Referenced Sales Order set and items pre-populated.

88. **SOF PO Type — remaining type-change edge cases** - The Q87 answer blocks the SOF type change only when both a Referenced SO **and** items are already set. Two remaining gaps: (a) If the Referenced SO is set on a Draft SOF PO but **no items** have been added yet, can the user change the PO Type — and if so, is the Referenced SO field cleared automatically? (b) Can a user change an existing Draft `Inventory Replenishment` or `Cross Dock Order` PO Type **to** `Sales Order Fulfillment`? If yes, what happens to the existing items — are they cleared (since SOF can only contain items from the referenced SO), or flagged as errors? 
**ANSWER** : the user change the PO Type for SOF if **no items** have been added yet. No `Inventory Replenishment` or `Cross Dock Order` cannot be changed PO Type **to** `Sales Order Fulfillment`

---

## Section T — Post-Answer Review 12

89. **Referenced SO when PO Type changes away from SOF (no items)** - Q88 confirms the PO Type can be changed away from `Sales Order Fulfillment` when no items are added yet. The spec states the Referenced Sales Order "cannot be changed after the PO is created." Two follow-up gaps: (a) When the user changes the PO Type away from SOF (no items), is the Referenced SO field automatically cleared — requiring the user to re-select a Referenced SO if they switch back to SOF? (b) What does "cannot be changed after the PO is created" mean precisely — does "created" mean after the **first save as Draft** (so Referenced SO is locked from first save), or after **items are added** (so Referenced SO can be re-set as long as no items exist, consistent with Q88's type-change rule)?
**ANSWER** : The Referenced SO field automatically cleared when the user changes the PO Type away from SOF (no items) — Yes, requiring the user to re-select a Referenced SO if they switch back to SOF? "Created" means after **items are added** (so Referenced SO can be re-set as long as no items exist, consistent with Q88's type-change rule)
** Revision to Q88: `Inventory Replenishment` or `Cross Dock Order` can be changed PO Type **to** `Sales Order Fulfillment` if no items are in the PO yet. To further clarify, a user may remove all items to be able to change types or other PO details since there are no items in the PO.

---

## Section U — Post-Answer Review 13

90. **SOF type when facility configuration matches Cross Dock trigger** - The Q89 revision to Q88 confirms any PO Type can be changed to any other when no items exist. A new conflict: the auto-switching rules (Q59, Q67) drive IR ↔ Cross Dock automatically based on facility/Delivery Location configuration. If a Draft has multiple facilities set (which normally forces Cross Dock) and no items, and the user manually changes the PO Type to `Sales Order Fulfillment`, does the facility-driven auto-switching logic immediately override the user's SOF selection back to Cross Dock? Or does the manual SOF selection take precedence and suppress the IR ↔ Cross Dock auto-switching while the PO Type is SOF?
**ANSWER** : Clarification on type change 
    - `Sales Order Fulfillment` -> `Cross Dock Order` : allowed if no items in PO. block otherwise.
    - `Sales Order Fulfillment` -> `Inventory Replenishment` : allowed if no items in PO. block otherwise.
    - `Inventory Replenishment` -> `Sales Order Fulfillment` : allowed if no items in PO. block otherwise.
    - `Inventory Replenishment` -> `Cross Dock Order` : no restriction with or without item. needs to change facility and delivery location should not be the same before change is allowed.
    - `Cross Dock Order` -> `Sales Order Fulfillment` : allowed if no items in PO. block otherwise.
    - `Cross Dock Order` -> `Inventory Replenishment` : no restriction with or without item. needs to remove all facilities in Facility List and  facility and delivery location should be the same before change is allowed.

91. **PO Type selector — manual vs. auto-determined for IR and Cross Dock** - The Q90 matrix shows IR ↔ Cross Dock transitions require specific facility/Delivery Location conditions ("facility and delivery location should not be the same" for Cross Dock; "single facility and delivery location should be the same" for IR). This implies IR ↔ Cross Dock is facility-driven (auto-set), not a manual user selection. However, the matrix also lists "SOF → IR" and "SOF → Cross Dock" as two separate transitions, suggesting the user explicitly picks one when leaving SOF. Please clarify: (a) For IR ↔ Cross Dock, is the PO Type always auto-determined by the current facility/Delivery Location configuration (user changes facilities, system updates the type), or can the user manually select IR or Cross Dock from a dropdown independent of facility setup? (b) When a user changes away from SOF (no items), is the resulting type auto-set based on the current facility configuration, or does the user explicitly select which type to switch to?
**ANSWER** : Yes, For IR ↔ Cross Dock, PO Type is always auto-determined by the current facility/Delivery Location configuration (user changes facilities, system updates the type), But the user manually select IR or Cross Dock from a dropdown if the facility setup is correct or none had been setup yet. User can set the type and the system will accept or block the change depending on the configured facility.

---

## Section V — Post-Answer Review 14

92. **SOF with multiple facilities — per-facility quantity distribution at pre-population** - Q90/Q91 confirm SOF POs can have multiple facilities. Q77 defines three pre-population modes (Round Up, As Is, Manual) that set item quantities based on the Referenced SO. However, Q77 was defined before multi-facility SOF was established and only describes the total quantity without addressing how it is distributed across facilities. For a SOF PO with multiple facilities already configured when items are pre-populated (Round Up or As Is mode): is the entire pre-populated quantity assigned to one facility (e.g., the Delivery Location) with all other facilities starting at 0 — requiring the user to manually split quantities across facilities afterward? Or is there a different distribution logic?
**ANSWER** : Clarification: SOF POs cannot have multiple facilities. SOF POs is restricted to a single facility only with delivery location set to the same facility

93. **Cross Dock → SOF type change — Q90 matrix incomplete due to Q92 facility constraint** - Q92 establishes that SOF requires a single facility with Delivery Location = same facility (identical to IR facility conditions). The Q90 matrix entry "Cross Dock → SOF: allowed if no items" is now incomplete: a Cross Dock Draft has multiple facilities or mismatched Delivery Location — both incompatible with SOF. Two related clarifications needed: (a) Is the Cross Dock → SOF transition actually blocked in two ways — both "no items" AND facility configuration must already match the IR/SOF single-facility condition? (b) When a Cross Dock Draft user attempts to switch to SOF and the facility configuration is incompatible, does the system block the selection and require the user to fix facilities first — or does selecting SOF automatically trigger the facility cleanup (prompt to remove extra facilities and align Delivery Location)?
**ANSWER** : Revision: `Cross Dock Order` -> `Sales Order Fulfillment` : allowed if no items in PO. needs to remove all facilities in Facility List and  facility and delivery location should be the same before change is allowed.

---

## Section W — Post-Answer Review 15

94. **`Notify Only` expiration — notification recipient scope** - The spec states that when the `Cancel if not Delivered by Date` is configured as `Notify Only` and the date is reached, it "notifies all users who have access to the PO." However, Q43 lists the notification for an expired `Cancel if not Delivered by Date` on a partially received PO as going to "users with manual close permission" — a narrower audience. Please clarify: when the `Notify Only` action fires at expiration (date reached, PO is Approved with no items or partially received), is the notification sent to (a) **all users who have access to the PO** (per the original spec wording), or (b) **only users with `manual close` permission** (consistent with Q43's notification audience)?
**ANSWER** : Notify only users with `manual close` permission

95. **"Nearing expiration" notification — frequency** - Q43 lists "Approved PO nearing `Cancel if not Delivered by Date` → users with `manual close` permission" as a notification event. The configurable lead time (default 5 days) defines the window. However, the notification frequency is unspecified: is this notification sent (a) **once** — only when the PO first enters the lead time window, (b) **daily** — once per day throughout the entire lead time window until the date is reached or the PO is closed, or (c) **login-based digest** — summarized and sent to each user at login (similar to Q79's "PO not yet submitted" digest)?
**ANSWER** : **login-based digest** — summarized and sent to each user at login (similar to Q79's "PO not yet submitted" digest)?

---

## Section X — Post-Answer Review 16

96. **Notification frequency for event-driven notifications** - Q79 and Q95 confirmed that "PO not yet submitted" and "nearing expiration" notifications are login-based digests (ongoing states). However, Q43 also lists two event-driven notifications that have not been given a frequency: (a) **"PO needing approval"** — sent to supervisors when a merchandiser submits a PO, and (b) **"PO rejected"** — sent to merchandisers when a supervisor rejects a PO. Are these also login-based digests (supervisor or merchandiser sees a summary at their next login), or are they sent **immediately** when the event occurs (submission or formal rejection submission)? A delayed rejection notification could significantly slow down the revision cycle.
**ANSWER** : Revision. All notifications for event driven notifications will be in 2 modes:
    - login-based digests (ongoing states) to remind user of what to do on login.
    - realtime for new notifications while user is still active

---

## Section Y — Post-Answer Review 17

97. **Activity log on source PO when cloned** - Q38 confirms the cloned PO gets an activity log entry "PO cloned from [source PO Document ID]." However, the spec does not mention any corresponding entry on the **source PO** when a clone is made from it. If a user views a Completed or Cancelled PO that has been cloned one or more times, would the source PO's activity log show any record of the cloning (e.g., "PO cloned to [new PO Document ID]")? Or is the clone relationship only visible from the cloned PO side?
**ANSWER** : Clone relationship only visible from the cloned PO side to log that the new PO was copied from an existing PO.

---

## Section Z — Post-Answer Review 18

98. **Document ID year-month component — Draft creation month vs. submission month** - The spec states the Draft ID (`DFTPO-'yyyyMM'XXXXXX`) is generated when the PO is first saved, and "upon submission the Draft ID is replaced by `PO-'yyyyMM'XXXXXX`." The `yyyyMM` in the PO ID is unspecified: if a Draft is created in one month (e.g., January) and submitted in a later month (e.g., February), does the PO Document ID use (a) the **submission month** (`PO-202502XXXXXX`) — reflecting when the PO was formally submitted, or (b) the **Draft creation month** (`PO-202501XXXXXX`) — carrying over the month from the original Draft?
**ANSWER** : the **submission month** (`PO-202502XXXXXX`) — reflecting when the PO was formally submitted

---

## Section AA — Post-Answer Review 19

99. **Item-level notes in export** - Q19 added `item-level notes` as a field on each PO line item. Q37 defined the PDF and CSV export formats before Q19 was answered, and neither format includes item-level notes: the PDF item table lists Line #, Product ID/GTIN, Product Name, Category, Vendor UOM, Unit Cost, Ordered Qty, Extended Cost; the CSV adds Item Status but also omits item-level notes. Should item-level notes be included in the PDF item table and/or the CSV export? If yes, where should they appear (e.g., as an additional column, or as a sub-row below the item line in the PDF)?
**ANSWER** : yes inclide it as an additional column for CSV and a sub row below the line item in PDF

100. **Rejection notes in export** - Q99 confirmed item-level notes are included in the PDF (as a sub-row) and CSV (as a column). By the same logic, Q37's export format also omits rejection notes, which are equally important for a complete document when a PO is in Rejected status. Two types exist: (a) **PO-level rejection note** (spec D.2) — the supervisor's overall reason for rejecting the PO; and (b) **per-item rejection note** (spec D.3) — the reason each flagged item was rejected. Should these appear in the PDF and/or CSV export? If yes, where — PO-level note in the PO Details section, and per-item note alongside the item-level notes sub-row?
**ANSWER** : Rejection notes are not included in the Report

---

## Section AB — Post-Answer Review 20

101. **PO General Notes in CSV export** - Q37's PDF export explicitly includes the PO-level **Notes** field (general instructions) in the PO Details section. However, Q37's CSV column list does not include it. Since the CSV has one row per item, the PO-level Notes would repeat on every item row (similar to other PO-level columns like PO Status, Buyer, Seller, etc.). Should PO General Notes be added as a column in the CSV export?
**ANSWER** : Yes include PO General Notes in CSV as additional column

---

## Section AC — Post-Answer Review 21

102. **Activity log entries for budget consumption and release** - Q38 defines all activity log events (status changes, field modifications, rejections, cloning, system validation errors) but does not include budget-related events. Budget is consumed at PO approval and released at PO close/cancel for unreceived items. Should the activity log include explicit entries for: (a) **budget consumed** — recorded at PO approval, with the amount consumed per budget category; and (b) **budget released** — recorded when a PO is manually closed or cancelled with unreceived budget-category items, with the amount released per category?
**ANSWER** : Budget comsumption and release is not part of the Activity log entries for the PO. But it will be included in the Activity log entries for the Budget which is part of Budget Planning module and not in PO module.

---

## Section AD — Post-Answer Review 22

103. **CSV export — per-facility column structure for Cross Dock** - Q37 specifies the CSV has "Facility columns" for Cross Dock POs, but the structure is undefined. A Cross Dock PO can have any number of facilities. For a flat CSV with one row per item, how should per-facility quantities be represented? Options: (a) **dynamic columns per facility** — one column per facility in the PO (e.g., "Qty - Facility A", "Qty - Facility B"), making the column count vary per exported PO; (b) **one row per facility per item** — multiple rows for the same item, each representing one facility's quantity, changing the CSV structure from "one row per item" to "one row per item per facility"; or (c) another approach?
**ANSWER** : dynamic columns per facility

---

## Section AE — Post-Answer Review 23

104. **PDF Totals section — contents and budget summary** - Q37's PDF export lists a "Totals" section without specifying its contents beyond what the spec implies (total ordered quantity, total cost). For POs containing budget-category items, should the PDF Totals section include a **budget summary** — showing, per category, the total cost of items ordered vs. the available budget at the time of export? Or should the Totals section contain only (a) total number of line items, (b) total ordered quantity (in vendor's UOM), and (c) total extended cost, with no budget breakdown?
**ANSWER** : total ordered quantity (in vendor's UOM)

---

## Section AF — Post-Answer Review 24

105. **PDF Totals section — complete list of fields** - Q104 confirmed total ordered quantity is shown in vendor's UOM with no budget breakdown. However, Q104's answer doesn't confirm whether total extended cost and total line-item count are also included. Q37's PDF item table lists "Extended Cost" per line — should the Totals section show a **grand total extended cost** (sum of all line item extended costs)? And should it show a **total number of line items**? Please confirm the complete list of fields in the PDF Totals section.
**ANSWER** : Totals section should show a **grand total extended cost** and **total number of line items**

---

## Section AG — Post-Answer Review 25

106. **Export quantity units after UOM/SKU clarifications (Q82–Q85)** - Q37 defined the PDF item table with a "Vendor UOM" label column and a single "Ordered Qty" column, and the CSV with a single "Ordered Qty" column. Q82–Q85 later established that Cross Dock and SOF POs always use company's SKU as the editable input with vendor's UOM shown as a read-only conversion; and Q85 specifically states that for Cross Dock, per-facility quantities are shown in company's SKU on-screen, with the conversion to vendor's UOM displayed only in the aggregate total row. These rules were defined for the on-screen edit view; the export formats were not updated to reflect them. Three related clarifications needed: (a) For the PDF item table and CSV "Ordered Qty" column, should quantities always be in **vendor's UOM** (since these are procurement documents also consumed by the vendor), or should they reflect the screen entry unit (company's SKU for Cross Dock and SOF)? (b) For Cross Dock PDFs and CSVs specifically, should the per-facility columns show quantities in **company's SKU** (matching Q85 screen behavior) with the aggregate "Ordered Qty" in vendor's UOM — or should the PDF/CSV normalize all quantities to vendor's UOM regardless of the screen display? (c) If the export retains company's SKU for per-facility Cross Dock columns and/or SOF quantities, should a **"Company SKU"** label column be added alongside the existing "Vendor UOM" label column to identify the unit in use?
**ANSWER** : quantities should always be in **vendor's UOM** (since these are procurement documents also consumed by the vendor). the PDF/CSV should  normalize all quantities to vendor's UOM regardless of the screen display

---

## Section AH — Post-Answer Review 26

107. **Cancelling a Rejected PO** - The spec (Section B.4) states the Merchandiser can cancel a Draft PO, and Section C.3 states the Supervisor can cancel a Submitted PO. The status transition summary in Section I explicitly shows only `Draft` or `Submitted` → `Cancelled` as paths to cancellation; `Rejected` is not listed. This creates a workflow dead-end: if a Merchandiser receives a rejected PO and decides not to resubmit it (e.g., the purchase is no longer needed, the vendor is unavailable, etc.), there is no defined way to cancel it — it would remain in `Rejected` status indefinitely. Two clarifications needed: (a) Can a `Rejected` PO be cancelled, and if so, by whom — the Merchandiser (the creator), any user with an existing cancel permission, or a new distinct permission? (b) If cancellation of a Rejected PO is allowed, does it use the existing `cancel Draft` or `cancel Submitted` permission, or does it require a new named permission (e.g., `cancel Rejected`)?
**ANSWER** : yes a rejected PO can be cancelled

---

## Section AI — Post-Answer Review 27

108. **Cancelling a Rejected PO — permission and actor** - Q107 confirms a Rejected PO can be cancelled but does not specify who can perform the action or which permission is required. The three existing cancel/close permissions are `cancel Draft`, `cancel Submitted`, and `manual close` (Q3). Since a Rejected PO is neither a Draft nor Submitted, none of the three existing permissions obviously cover it. Two clarifications needed: (a) Who can cancel a Rejected PO — is it the Merchandiser (user with create/modify PO permission), the Supervisor, or both? (b) Does cancelling a Rejected PO use one of the existing permissions (`cancel Draft` or `cancel Submitted`), or does it require a new named permission (e.g., `cancel Rejected`)?
**ANSWER** : cancelling a Rejected PO require a new named permission

---

## Section AJ — Post-Answer Review 28

109. **SOF auto-pre-population and High Value Merchandiser permission for budget-category items** - The spec (Section B.3) states: "Items belonging to a category with a set budget can only be added by an authorized High Value Merchandiser." For SOF POs in `Round Up` or `As Is` pre-population mode (Q77), items are automatically added by the system from the referenced Sales Order, limited to items with an active Buyer-Seller relationship and a matching order schedule. This system-driven pre-population may include items belonging to budget categories — items that normally require HV Merchandiser permission to add manually. There is no Q&A addressing whether auto-pre-population bypasses or respects this permission restriction. Two clarifications needed: (a) When an SOF PO in `Round Up` or `As Is` mode is created by a regular Merchandiser (without HV Merchandiser permission), and the referenced Sales Order contains budget-category items, are those budget-category items: (i) automatically pre-populated like all other items (permission check bypassed because it is a system action), (ii) silently excluded from pre-population (only non-budget items are added), or (iii) included with a validation error indicating the user lacks permission? (b) For `Manual` mode SOF POs: when a regular Merchandiser manually adds an item from the referenced Sales Order that belongs to a budget category, is this blocked the same way as adding a budget-category item on any other PO type?
**ANSWER** : When an SOF PO in `Round Up` or `As Is` mode is created by a regular Merchandiser included with a validation error indicating the user lacks permission. For `Manual` mode SOF POs: when a regular Merchandiser manually adds an item from the referenced Sales Order that belongs to a budget category this blocked the same way as adding a budget-category item on any other PO type.

---

## Section AK — Post-Answer Review 29

110. **CSV export — "Facility columns" structure for Inventory Replenishment and SOF POs** - Q37 defined the CSV column list with a "Facility columns" group between Delivery Location and Product ID. Q103 clarified that for Cross Dock POs, this is dynamic per-facility columns (one quantity column per facility in the PO). However, for `Inventory Replenishment` and `Sales Order Fulfillment` POs — both of which have only a single facility — the structure of "Facility columns" was never specified. Two options: (a) **Single facility column** — one column showing the single facility's ordered quantity (making the format structurally consistent across all PO types, with Cross Dock simply having more columns); or (b) **No facility columns** — for IR and SOF, the "Facility columns" group is absent and "Ordered Qty" alone represents the total quantity. Please confirm which structure applies for IR and SOF POs in the CSV export.
**ANSWER** : **No facility columns** — for IR and SOF, the "Facility columns" group is absent and "Ordered Qty" alone represents the total quantit

---

## Section AL — Post-Answer Review 30

111. **`submit PO` — distinct permission or bundled with create/modify?** - Q43's notification answer states the "PO not yet submitted" notification goes to "users with create/modify/submit PO permission," listing `submit` alongside `create/modify` as if it may be a separate permission. Q3 defines three named permissions (`cancel Draft`, `cancel Submitted`, `manual close`) but does not list a `submit PO` permission separately. Q1 addresses the create/modify split between regular items and budget items. Is `submit PO` a distinct, separately assignable permission — meaning a role could be configured to create and modify POs but not submit them for approval? Or is the ability to submit a PO always bundled with the create/modify permission and not separately grantable?
**ANSWER** : submit PO is part of create/modify permission

---

## Section AM — Post-Answer Review 31

112. **`manual close` permission and High Value Merchandiser for budget-category POs** - Spec Section E.3 states: "Only an authorized High Value Merchandiser can manually close a purchase order that contains items belonging to a category with a set budget." Q33 confirms `manual close` is a separately configurable permission (assignable to any role by the system admin). This creates an unresolved relationship between the two requirements. For manually closing a PO that contains budget-category items, does the user need: (a) **both** the `manual close` permission AND the High Value Merchandiser (budget-item) permission — requiring the system admin to configure both permissions on the role; or (b) **only** the High Value Merchandiser permission — meaning that for budget-category POs, the HV Merchandiser permission implicitly grants the ability to manually close, superseding the usual `manual close` permission requirement?
**ANSWER** : separate permission to manually close for budget-category POs, and a PO for regular items

---

## Section AN — Post-Answer Review 32

113. **Manual close for POs with mixed budget and non-budget items** - Q112 establishes two separate `manual close` permissions: one for regular (non-budget) POs and one for budget-category POs. The spec (Section E.3) triggers the budget restriction when a PO "contains items belonging to a category with a set budget," meaning a PO with even one budget-category item is subject to the stricter permission. When a PO contains a mix of budget-category items and regular items, which permission is required to manually close it: (a) only the **budget-category manual close** permission (since the PO "contains" budget items, the stricter permission applies and the regular manual close permission is not additionally required); or (b) **both** the regular and budget-category manual close permissions?
**ANSWER** : only the **budget-category manual close** permission (since the PO "contains" budget items, the stricter permission applies and the regular manual close permission is not additionally required)

---

## Section AO — Post-Answer Review 33

114. **SOF PO — Delivery Location change and forced Facility List sync** - Q66 describes prompts when the user changes the Delivery Location on a Draft PO: if the Delivery Location previously matched the single entry in the Facility List, the system prompts the user asking whether the Facility List should also be updated (Prompt 1). The user can decline, keeping the Facility List pointing to the old facility while the Delivery Location points to a new one. For `Inventory Replenishment` and `Cross Dock Order` POs this is valid. However, Q92 establishes that `Sales Order Fulfillment` POs are restricted to a **single facility with Delivery Location set to the same facility** — making Facility List ≠ Delivery Location an invalid state for SOF. When the user changes the Delivery Location on a Draft SOF PO (no items): does the system (a) **force-update the Facility List** to the new Delivery Location automatically without Prompt 1 (because the choice is irrelevant — they must always match for SOF); or (b) **still show Prompt 1** but block the user from declining (the "keep Facility List as-is" option is disabled for SOF)?
**ANSWER** : **still show Prompt 1** but block the user from declining. Add to the prompt the reason for the restriction

---

## Section AP — Post-Answer Review 34

115. **Complete named permission list** - The permission model has been incrementally expanded across multiple Q&As, with the initial list in Q3 now incomplete. Based on all answers to date, the known named permissions are: `cancel Draft` (Q3), `cancel Submitted` (Q3), `manual close` for regular POs (Q3/Q112), `manual close` for budget-category POs (Q112), `cancel Rejected` (Q108), plus the create/modify permissions for regular items and budget items (Q1), and the approve PO permission (implied by Section C). Please confirm the complete list of all named PO-module permissions so the implementation has a definitive reference. Specifically: (a) Are the two `manual close` permissions named distinctly (e.g., `manual close` and `manual close budget`)? (b) Are the create/modify permissions named (e.g., `create/modify PO` and `create/modify budget PO`)? (c) Is there a separate named `approve PO` permission, or is approval implied by the Supervisor role configuration? (d) Are there any other named permissions not yet identified?
**ANSWER** : see list of permissions for purchase order module
    - `purchaseorder_modify` : create and modify not budgeted PO
    - `purchaseorder_budgeted_modify` : create and modify budgeted PO
    - `purchaseorder_draft_cancel` : cancel not budgeted PO still in draft
    - `purchaseorder_budgeted_draft_cancel` : cancel budgeted PO still in draft
    - `purchaseorder_submitted_cancel` : cancel any PO already submitted
    - `purchaseorder_rejected_cancel` : cancel not budgeted PO that has been rejected
    - `purchaseorder_budgeted_rejected_cancel` : cancel budgeted PO that has been rejected
    - `purchaseorder_approve` : approve any PO that has been submitted
    - `purchaseorder_manual_close` : manually close not budgeted PO that has been approved
    - `purchaseorder_budgeted_manual_close` : manually close budgeted PO that has been approved

---

## Section AQ — Post-Answer Review 35

116. **View/read-only permission missing from Q115 list** - The Q115 permission list covers all create, modify, approve, cancel, and close actions, but does not include a view-only permission. The spec (Section H) states the dashboard is "viewable only for those who have permissions to view the PO or can perform the action being notified," explicitly indicating that view access is a permission-gated capability. Two clarifications needed: (a) Is there a `purchaseorder_view` (or equivalent) permission that grants read-only access to POs and the PO dashboard without granting any action capabilities — allowing, for example, an auditor or manager to view PO records without being able to modify, approve, or close them? (b) If no separate view permission exists, does having ANY of the Q115 permissions (e.g., `purchaseorder_modify`) automatically grant view access to all POs, or only to POs relevant to that permission scope?
**ANSWER** : yes add `purchaseorder_view` that will allow to view any PO beloging to any of the company under the organization.

---

## Section AR — Post-Answer Review 36

117. **`purchaseorder_view` scope — does it include Draft POs?** - Q116 confirms `purchaseorder_view` grants read access to "any PO belonging to any of the company under the organization." Draft POs (DFTPO- prefix) are in-progress work documents that have not yet been formally submitted. Does `purchaseorder_view` include Draft POs in its scope — meaning any user with this permission can read any Draft PO created by any other user in the organization? Or is view access to Draft POs restricted to: (a) the user who created the Draft, (b) users with `purchaseorder_modify` or `purchaseorder_budgeted_modify` (who can also edit Drafts), or (c) all Draft POs are excluded from `purchaseorder_view` until the PO is submitted?
**ANSWER** : no, `purchaseorder_view` will not be able to view Draft POs

---

## Section AS — Post-Answer Review 37

118. **Which permissions grant access to view Draft POs?** - Q117 confirms `purchaseorder_view` does not include Draft POs. Q71 confirms the edit lock is at PO level, implying that multiple users with Merchandiser-level permissions can navigate to and attempt to open the same Draft PO (the second user is placed in read-only mode). This means some permissions must grant visibility of Draft POs beyond just the creator. Please clarify: (a) Do `purchaseorder_modify` and `purchaseorder_budgeted_modify` grant access to view ALL Draft POs in the organization (not just those the user created themselves), allowing any Merchandiser to find and open any Draft? (b) Does `purchaseorder_approve` (the supervisor permission) also grant view access to Draft POs — for example, to monitor in-progress work before submission — or are supervisors limited to Submitted and later statuses?
**ANSWER** : `purchaseorder_modify` will be able to view not budgeted PO beloging to any of the company under the organization. `purchaseorder_budgeted_modify` will be able to view budgeted PO beloging to any of the company under the organization.

---

## Section AT — Post-Answer Review 38

119. **`purchaseorder_approve` — can supervisors view Draft POs?** - Q118 answered that `purchaseorder_modify` and `purchaseorder_budgeted_modify` grant Draft PO visibility. However, Q118 did not address whether `purchaseorder_approve` also grants Draft PO visibility. Dashboard item #1 ("Number of POs not submitted") counts in-progress Draft POs and is visible to users who can perform actions being notified (spec Section H). If a supervisor with only `purchaseorder_approve` clicks the count in dashboard item #1, can they drill into the list of individual Draft POs to view their details? Or is `purchaseorder_approve` restricted to viewing only Submitted and later-status POs — meaning Draft POs are hidden from supervisors unless they also hold `purchaseorder_modify`?
**ANSWER** : `purchaseorder_approve` is restricted to viewing only Submitted and later-status POs — meaning Draft POs are hidden from supervisors unless they also hold `purchaseorder_modify`

---

## Section AU — Post-Answer Review 39

120. **Activity log entry for system auto-cancel** - Q38 defines all activity log events. The spec (Section C.5) explicitly states that when a PO is auto-approved (no approval required), "An approval record is created in the activity log with an indication that the PO was system-approved." However, the spec does not state the equivalent for the daily-job system auto-cancel: when `Cancel if not Delivered by Date` expires with no items received and the action is set to `Automatic Cancel`, the system cancels the PO as part of the end-of-day job. Two clarifications needed: (a) Is a system-generated activity log entry created for this auto-cancel (e.g., "PO automatically cancelled — Cancel if not Delivered by Date expired"), and if so, what acting user is recorded (a system/bot user, or no user)? (b) Should `Automatic Cancel` be added to the Q38 activity log event list alongside the existing "PO status changes" events?
**ANSWER** : Yes a system-generated activity log entry is created when a PO had been auto-canceled with acting user as system. `Automatic Cancel` be added to the activity log list alongside the existing "PO status changes" events

---

## Section AV — Post-Answer Review 40

121. **Mixed-user Draft PO — view and edit access after budget items are added** - Q118 establishes that `purchaseorder_modify` can view non-budgeted POs and `purchaseorder_budgeted_modify` can view budgeted POs. A single Draft PO can contain items added by users with different permissions: a regular Merchandiser (`purchaseorder_modify`) creates the PO and adds non-budget items; then an HV Merchandiser (`purchaseorder_budgeted_modify`) adds budget-category items to the same Draft (using the edit lock in turn). Once budget items are added, the PO becomes a "budgeted PO." Two clarifications needed: (a) Can a regular Merchandiser (`purchaseorder_modify` only, no `purchaseorder_budgeted_modify`) still view a Draft PO that has become budgeted through another user's additions — and can they still edit the non-budget items on it? Or does their access terminate once the PO becomes budgeted? (b) Is this collaborative multi-user editing of a single Draft PO (one user adds regular items, another adds budget items) an intended workflow, or should adding budget items to a PO require the creator/editor to hold `purchaseorder_budgeted_modify`?
**ANSWER** : No the PO becomes a budgeted PO therefore can only be viewed and modified by a user with `purchaseorder_budgeted_modify` permission. the user can still view the budgeted PO if the user has `purchaseorder_view` permission. Adding budget items to a PO require the creator/editor to hold `purchaseorder_budgeted_modify`

---

## Section AW — Post-Answer Review 41

122. **`purchaseorder_draft_cancel` — does it grant Draft PO view access?** - Q117 confirms `purchaseorder_view` does not include Draft POs. Q118/Q121 establish that only `purchaseorder_modify` (non-budgeted) and `purchaseorder_budgeted_modify` (budgeted) grant view access to Draft POs. `purchaseorder_draft_cancel` and `purchaseorder_budgeted_draft_cancel` (Q115) allow cancelling Draft POs — but cancelling requires first being able to find and view the Draft. If these cancel permissions do NOT implicitly grant Draft view access, a user with only `purchaseorder_draft_cancel` would be unable to use it (they can't see the Drafts to cancel them). Please confirm: do `purchaseorder_draft_cancel` and `purchaseorder_budgeted_draft_cancel` implicitly grant read access to non-budgeted and budgeted Draft POs respectively — or must the holder also have `purchaseorder_modify` / `purchaseorder_budgeted_modify` to view the Drafts before cancelling them?
**ANSWER** : Permission List revision to streamline redundant permission
    - `purchaseorder_modify` : Create and modify non-budgeted POs; add/remove/edit non-budget-category items; submit for approval; save Draft; clone Completed/Cancelled non-budgeted POs; View non-budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Completed (POs that can be cloned) and Cancelled (POs that can be cloned).
    - `purchaseorder_budgeted_modify` : Create and modify budgeted POs; add/remove/edit both budget-category and non-budget-category; submit for approval; save Draft; clone Completed/Cancelled budgeted POs; View budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Completed (POs that can be cloned) and Cancelled (POs that can be cloned).
    - `purchaseorder_modify_cancel` : Review and cancel non-budgeted POs with status Draft and Rejected; View non-budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled).
    - `purchaseorder_budgeted_modify_cancel` : Review and cancel budgeted POs with status Draft and Rejected; View budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled).
    - `purchaseorder_approve` : Review, approve, reject or cancel all submitted POs both budgeted and non-budgeted.
    - `purchaseorder_manual_close` : Manually close non-budgeted Approved POs; View non-budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled).
    - `purchaseorder_budgeted_manual_close` : Manually close budgeted Approved POs; View budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled).
    - `purchaseorder_view` : Read-only view of POs with status Submitted, Approved, Rejected, Cancelled and Completed; dashboard view for POs having status Submitted, Approved, Rejected, Cancelled and Completed

---

## Section AX — Post-Answer Review 42

123. **`purchaseorder_modify` view scope excludes Submitted — can the submitting Merchandiser track their own PO after submission?** - The Q122 revised permission list explicitly defines `purchaseorder_modify` view access as: Draft, Rejected, Completed, and Cancelled only. `Submitted` is not listed. This means once a Merchandiser submits their Draft PO, it transitions to `Submitted` status — and unless they also hold `purchaseorder_view`, they can no longer see it. The same applies to `purchaseorder_budgeted_modify`. Two clarifications needed: (a) Is this intentional — meaning Merchandisers are expected to either also hold `purchaseorder_view` or lose visibility of their PO once submitted? Or should `purchaseorder_modify` and `purchaseorder_budgeted_modify` each implicitly include view access to Submitted status (at minimum for POs they created, or for all POs in their scope)? (b) Similarly, `purchaseorder_approve` is defined as "review, approve, reject or cancel all submitted POs" — does this grant view access only to Submitted-status POs, or also to Approved, Rejected, Completed, and Cancelled POs (consistent with the old Q119 answer: "Submitted and later-status POs")?
**ANSWER** : Agree this is not intentional. Permission List revision to streamline redundant permission
    - `purchaseorder_modify` : Create and modify non-budgeted POs; add/remove/edit non-budget-category items; submit for approval; save Draft; clone Completed/Cancelled non-budgeted POs; View non-budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for non-budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_budgeted_modify` : Create and modify budgeted POs; add/remove/edit both budget-category and non-budget-category; submit for approval; save Draft; clone Completed/Cancelled budgeted POs; View budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_modify_cancel` : Review and cancel non-budgeted POs with status Draft and Rejected; View non-budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Draft and Cancelled.
    - `purchaseorder_budgeted_modify_cancel` : Review and cancel budgeted POs with status Draft and Rejected; View budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Draft and Cancelled.
    - `purchaseorder_approve` : Review, approve, reject or cancel all submitted POs both budgeted and non-budgeted; View of all POs with status Submitted (POs to be approved), Cancelled (Review of cancelled POs), Rejected (Review of rejected POs), Rejected (Review of rejected POs); Dashboard view for POs having status Submitted, Approved, Rejected, and Cancelled.
    - `purchaseorder_manual_close` : Manually close non-budgeted Approved POs; View non-budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_budgeted_manual_close` : Manually close budgeted Approved POs; View budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_view` : Read-only view of POs with status Submitted, Approved, Rejected, Cancelled and Completed; dashboard view for POs having status Submitted, Approved, Rejected, Cancelled and Completed.

---

## Section AY — Post-Answer Review 43


124. **`purchaseorder_approve` view scope — Completed and Cancelled missing; dashboard-view conflict** - The Q123 revised permission list defines `purchaseorder_approve` view scope as: Submitted, Approved, and Rejected only. Two inconsistencies arise: (a) The dashboard scope for `purchaseorder_approve` explicitly includes Cancelled, yet the PO detail view scope does not include Cancelled. If a supervisor sees a Cancelled count on their dashboard and clicks through, there would be no accessible PO records to display — the dashboard count becomes non-drillable. Should Cancelled be added to `purchaseorder_approve`'s view scope to match its own dashboard? (b) A supervisor who cancels a Submitted PO (triggering Submitted → Cancelled) immediately loses sight of that PO since Cancelled is outside their view scope — this parallels the exact issue Q123 fixed for `purchaseorder_modify`. Should Cancelled (and Completed) also be added to `purchaseorder_approve`'s view scope? Additionally, there appears to be a typo in the Q123 answer: `purchaseorder_budgeted_manual_close` states "Dashboard view for **non-budgeted** POs having status Approved, Cancelled, and Completed" — should this read "budgeted"?
**ANSWER** : Agree this is not intentional. Permission List revision to streamline redundant permissions and additional corrections
    - `purchaseorder_modify` : Create and modify non-budgeted POs; add/remove/edit non-budget-category items; submit for approval; save Draft; clone Completed/Cancelled non-budgeted POs; View non-budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for non-budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_budgeted_modify` : Create and modify budgeted POs; add/remove/edit both budget-category and non-budget-category; submit for approval; save Draft; clone Completed/Cancelled budgeted POs; View budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_modify_cancel` : Review and cancel non-budgeted POs with status Draft and Rejected; View non-budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Draft and Cancelled.
    - `purchaseorder_budgeted_modify_cancel` : Review and cancel budgeted POs with status Draft and Rejected; View budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Draft and Cancelled.
    - `purchaseorder_approve` : Review, approve, reject or cancel all submitted POs both budgeted and non-budgeted; View of all POs with status Submitted (POs to be approved), Cancelled (Review of cancelled POs), Rejected (Review of rejected POs), Rejected (Review of rejected POs); Dashboard view for POs having status Submitted, Approved, Rejected, and Cancelled.
    - `purchaseorder_manual_close` : Manually close non-budgeted Approved POs; View non-budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_budgeted_manual_close` : Manually close budgeted Approved POs; View budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_view` : Read-only view of POs with status Submitted, Approved, Rejected, Cancelled and Completed; dashboard view for POs having status Submitted, Approved, Rejected, Cancelled and Completed.

---

## Section AZ — Post-Answer Review 44

125. **`purchaseorder_approve` view scope — "Approved" missing, "Rejected" duplicated** - The Q124 revised permission list defines `purchaseorder_approve` view scope as: "Submitted (POs to be approved), Cancelled (Review of cancelled POs), Rejected (Review of rejected POs), Rejected (Review of rejected POs)." The status Rejected appears twice and Approved is absent — yet the dashboard scope for `purchaseorder_approve` still explicitly includes Approved. This appears to be a copy-paste error where the second "Rejected" should read "Approved." Please confirm: should the `purchaseorder_approve` view scope be Submitted, Approved, Rejected, and Cancelled (with "Approved" replacing the duplicated "Rejected")?
**ANSWER** : Agree this is not intentional. Permission List revision to streamline redundant permissions and additional corrections
    - `purchaseorder_modify` : Create and modify non-budgeted POs; add/remove/edit non-budget-category items; submit for approval; save Draft; clone Completed/Cancelled non-budgeted POs; View non-budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for non-budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_budgeted_modify` : Create and modify budgeted POs; add/remove/edit both budget-category and non-budget-category; submit for approval; save Draft; clone Completed/Cancelled budgeted POs; View budgeted POs with status Draft (POs to be modified and submitted), Rejected (POs to be modified based on rejection notes and resubmitted), Submitted (Review of submitted POs), Completed (POs that can be cloned) and Cancelled (POs that can be cloned); Dashboard view for budgeted POs having status Draft, Rejected, Cancelled and Completed.
    - `purchaseorder_modify_cancel` : Review and cancel non-budgeted POs with status Draft and Rejected; View non-budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Draft and Cancelled.
    - `purchaseorder_budgeted_modify_cancel` : Review and cancel budgeted POs with status Draft and Rejected; View budgeted POs with status Draft (POs to be cancelled), Rejected (Review POs and its rejection notes for cancelation), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Draft and Cancelled.
    - `purchaseorder_approve` : Review, approve, reject or cancel all submitted POs both budgeted and non-budgeted; View of all POs with status Submitted (POs to be approved), Cancelled (Review of cancelled POs), Approved (Review of approved POs), Rejected (Review of rejected POs); Dashboard view for POs having status Submitted, Approved, Rejected, and Cancelled.
    - `purchaseorder_manual_close` : Manually close non-budgeted Approved POs; View non-budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for non-budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_budgeted_manual_close` : Manually close budgeted Approved POs; View budgeted POs with status Approved (POs to be manually closed), Completed (Review POs that had been completed), and Cancelled (Review POs that had been cancelled); Dashboard view for budgeted POs having status Approved, Cancelled, and Completed.
    - `purchaseorder_view` : Read-only view of POs with status Submitted, Approved, Rejected, Cancelled and Completed; dashboard view for POs having status Submitted, Approved, Rejected, Cancelled and Completed.

---

## Section BA — Post-Answer Review 45

126. **Cancellation notes — is a reason required when cancelling a PO?** - The spec (Section D) explicitly requires notes for rejection: a PO-level rejection note is required for header-level rejections (D.2), and per-item rejection notes are required for item-level rejections (D.3), both stored separately from the PO's general Notes field. However, the spec makes no mention of a required reason or note for cancellation. Three user-initiated cancellation paths exist: (a) Merchandiser cancels a Draft PO (`purchaseorder_modify_cancel` / `purchaseorder_budgeted_modify_cancel`); (b) Supervisor cancels a Submitted PO (`purchaseorder_approve`); (c) Manual close with no items received results in a Cancelled PO (`purchaseorder_manual_close` / `purchaseorder_budgeted_manual_close`). Is a cancellation reason/note required for any of these three paths? If so, is it stored as a dedicated cancellation note field (separate from the general Notes field), or appended to the general Notes field?
**ANSWER** : Add Cancellation notes at PO-level when cancelling the whole PO and Cancellation notes at per item when an item has been cancelled. This is stored separately from the PO's general Notes field. Automatic cancellation because of `Cancel if not Delivered by Date` with `Automatic Cancel` set in configuration will put cancellation notes on the items cancelled and on the PO-level if the whole PO was cancelled because of it.

---

## Section BB — Post-Answer Review 46

127. **Cancellation notes — required or optional, and scope for partial manual close** - Q126 confirms cancellation notes exist at PO-level (whole PO cancelled) and per-item (individual items cancelled), stored separately from the general Notes field. Two clarifications needed: (a) The spec explicitly says rejection notes are **required** (Section D.2: "A PO-level rejection note is required"). Are cancellation notes also required — meaning the user is hard-blocked from confirming the cancellation until a note is provided — or are they optional (the user may leave them blank and proceed)? This applies to all three user-initiated cancellation paths: Merchandiser cancels a Draft, Supervisor cancels a Submitted PO, and manual close with no items received. (b) When a PO is manually closed with some items received (PO status becomes `Completed`, not `Cancelled`), only the individual undelivered items receive `Cancelled` status — the PO itself is not cancelled. In this case, are per-item cancellation notes required for each `Cancelled` item, while no PO-level cancellation note applies?
**ANSWER** : Cancellation notes are required for items that will be cancelled and on the PO-level if the whole PO had been cancelled. A few clarifications: (a) If the whole PO is cancelled, a PO-level cancellation note will be enough. The items dont need to have individual cancellation notes. (b) Cancellation of whole PO from manual closing of PO will only require a PO level cancellation note. (c) Manual closing PO with some items received, resulting to the not received items to be cancelled, will need cancellation notes for each of the items that were not received. This can be the a message like "Manually closed due to vendor delivery limitations" applied to all of the items not received.

---

## Section BC — Post-Answer Review 47

128. **Conflict between Q126 and Q127 on auto-cancel notes** - Q126 states: "Automatic cancellation because of `Cancel if not Delivered by Date` with `Automatic Cancel` set will put cancellation notes on **the items cancelled** and on the PO-level if the whole PO was cancelled." Q127(a) establishes: when the whole PO is cancelled, a PO-level cancellation note is sufficient — items do not need individual cancellation notes. Since auto-cancel only triggers on POs with no items received and always cancels the whole PO (spec Section B: "Partially received POs are not affected"), these rules conflict: Q126 implies per-item notes for auto-cancel, while Q127(a) implies only PO-level. Which rule applies to auto-cancel: (a) PO-level note only (consistent with Q127(a) — whole PO cancelled, no per-item notes), or (b) both PO-level and per-item system-generated notes?
**ANSWER** : Revision: Cancellation notes are required for PO-level and items that were cancelled. `Cancel if not Delivered by Date` with `Automatic Cancel` will put message to the PO-level (when PO is cancelled) and all the items affected by the cancellation. When cancellation of entire PO is triggered by user, a single message for the PO-level can be specified by the user and will be inserted as item cancellation note unless user will specify specifc cancellation notes in some items. Similarly, when closing partially received PO, a single message can be given by the user as the cancellation notes for all of the items that were not received unless user will specify specifc cancellation notes in some items.

---

## Section BD — Post-Answer Review 48

129. **Cancellation and manual close UX flow — how and when are cancellation notes entered?** - Q128 establishes that cancellation notes are required at PO-level (when the whole PO is cancelled) and per item (for each cancelled item), with a bulk-apply shortcut. Spec Section E.2 currently describes the manual close confirmation as a single irreversibility prompt. Q128's note requirements imply a more detailed flow. Please clarify the UX for each path: (a) **Whole-PO user cancellation** (Draft cancel / Submitted PO cancel / manual close with no items received) — is the cancellation note collected in the same confirmation dialog (user enters one note → system applies it to PO and all items), and can the user then override per-item notes before finalising, or is the bulk note the only input? (b) **Partial manual close** (some items received → PO = Completed, undelivered items = Cancelled) — does the flow show the user a list of which items will be cancelled with a note field per item (pre-filled with the bulk message) before confirming, or is it still a single confirmation dialog with one note field that gets applied in bulk with no per-item review step?
**ANSWER** : still a single confirmation dialog with one note field that gets applied in bulk but with an option for the user to specify individual cancellation notes for each item, which will be done on a different user interface.

---

## Section BE — Post-Answer Review 49

130. **Per-item cancellation note override — before or after confirmation, and what triggers the separate UI?** - Q129 confirms that per-item cancellation note overrides are done on a "different user interface." Two clarifications needed: (a) **Timing** — does the per-item override UI appear as part of the cancellation flow before the final confirmation (i.e., the user can review and edit per-item notes, then confirms to cancel), or does it appear after the cancellation is already confirmed and the PO/items are already in Cancelled status? If it occurs after confirmation, cancellation notes on a Cancelled PO must remain editable, which should be clarified. (b) **Trigger** — how does the user access this separate UI? For example: an expandable section within the confirmation dialog, a separate "Review items" step before confirming, a link in the confirmation dialog that opens a secondary screen, or access via the PO's item list on the Cancelled PO record after the action completes?
**ANSWER** : UI flow will be: (1) user clicks cancel PO (2) system prompts confirmation of cancellation, provides text box for Cancellation note for the PO, a button/link to add cancellation for items (3.1) user enters cancellation note and clicks OK triggers cancelling of PO with specified cancellation note for PO-level and all items cancelled (3.2) user clicks button/link to add cancellation for items, system redirects to another page where they can specify cancellation notes to specific items and a general cancellation notes for the rest of the items and PO level.

---

## Section BF — Post-Answer Review 50

131. **Partial manual close UX flow — same two-path dialog or different flow?** - Q130's UX flow was described for whole-PO cancellation ("user clicks cancel PO"). Partial manual close (some items received → PO = `Completed`, undelivered items = `Cancelled`) is a different action triggered by "manual close" rather than "cancel." For partial manual close, there is no PO-level cancellation note (the PO becomes `Completed`, not `Cancelled`) — only per-item cancellation notes for the undelivered items. Does the partial manual close use the same two-path confirmation flow as Q130 but with the PO-level note field absent (since the PO is not being cancelled), or does manual close have a different confirmation flow? Specifically: in path 3.1 for partial manual close, is the bulk note applied only to the cancelled items (not to a PO-level note)? And in path 3.2, does the per-item page omit the "general cancellation notes for PO level" field?
**ANSWER** : flow can also be used when manual closing PO

---

## Section BG — Post-Answer Review 51

132. **Path 3.2 per-item notes page — final confirmation and note carry-over** - Q130 describes path 3.2 as: user clicks the "add cancellation for items" link in the confirmation dialog → system redirects to a per-item notes page. Two details not covered: (a) **Final confirmation** — what action on the per-item page actually triggers the cancellation or manual close? Is there a "Confirm" or "Submit" button on the per-item page, or does the user return to the original confirmation dialog after saving per-item notes and then click OK? (b) **Note carry-over** — in the initial confirmation dialog (step 2), the user may have already typed a PO-level note before clicking the "add per-item" link. When redirected to the per-item page, is that note pre-filled in the "general cancellation note" field on the per-item page, so the user does not have to retype it?
**ANSWER** : per-item page will have a confirm button to proceed with the cancellation or manual closing of items

---

## Section BH — Post-Answer Review 52

133. **Rejection and cancellation notes display — where do they appear on the PO view page?** - Spec Section D establishes PO-level rejection notes (D.2) and per-item rejection notes (D.3), both stored separately from the general Notes field. Q126–Q132 established the same two-level structure for cancellation notes. However, spec Section G (View of Purchase Order) lists the PO view components — Buyer details, Seller details, PO details, PO totals, activity log, item list — without mentioning where either type of note appears. Four clarifications needed: (a) Where is the PO-level rejection note displayed — e.g., in the Purchase Order details section near the general Notes field but visually distinct, or in a dedicated section? (b) Where are per-item rejection notes displayed — e.g., inline in the item list next to each rejected item, or in an expandable detail per item? (c) Where is the PO-level cancellation note displayed — same location as the PO-level rejection note, or a different section? (d) Where are per-item cancellation notes displayed — same pattern as per-item rejection notes, or differently?
**ANSWER** : PO-level Rejection and Cancellation note is displayed in a separate view component near the PO details. Item level rejection and cancellation note appear part of the item row display below the details.

---

## Section BI — Post-Answer Review 53

134. **Rejection notes on the PO view — retained across rejection/resubmission cycles?** - The PO-level and per-item rejection note component (Q133) shows the current rejection notes. Spec Section C.2 states that when a rejected PO is resubmitted, "fields changed by the Merchandiser since the last rejection are highlighted for the supervisor's convenience" — implying the supervisor needs to see the prior rejection notes alongside those highlights to understand what was fixed. Three clarifications needed: (a) After the Merchandiser resubmits a Rejected PO (status returns to `Submitted`), are the rejection notes from the prior rejection still visible in the rejection note component on the PO view, or are they cleared on resubmission? (b) If a PO undergoes multiple rejection and resubmission cycles, does the rejection note component show only the most recent rejection's notes, or all historical rejection notes organised by cycle? (c) At what point are rejection notes cleared from the rejection note component — e.g., when the PO is ultimately approved, or never (always retained as part of the PO record)? 
**ANSWER** : All rejection notes on the PO are removed after resubmission. They will, however be kept in the activity logs if the user wishes to review the rejection histor of the PO.

---

## Section BJ — Post-Answer Review 54

135. **"Fields changed since last rejection" highlighting — scope, baseline, and presentation** - Spec Section C.2 states: "When a rejected PO is resubmitted, fields changed by the Merchandiser since the last rejection are highlighted for the supervisor's convenience." This mechanism is not further defined. Four clarifications needed: (a) **Scope** — which elements are eligible for highlighting? For example: header fields (Order Date, Notes, Facility List, Delivery Location); items added or removed; quantity changes per item; per-facility quantity changes on multi-facility orders? (b) **Baseline** — "since the last rejection" means since the moment the supervisor formally submitted the rejection (spec D.6). Is the baseline snapshot taken at that moment, so that any change the Merchandiser makes after that point is highlighted? (c) **Presentation** — how is a changed field indicated? For example: a coloured border, a badge or icon, an inline "changed" label, or a tooltip showing the previous value? (d) **Persistence** — do the highlights disappear once the supervisor completes the current review (approves or rejects), or are they always shown when viewing the resubmitted PO in `Submitted` status regardless of who views it?
**ANSWER Scope** : All fields in that can be changed by a user in both PO header and items will be monitored for changes.
**ANSWER Baseline** : baseline snapshot taken at time the user submits rejection of PO
**ANSWER Presentation** : coloured border with a tooltip showing the previous value
**ANSWER Persistence** : on the UI, changes are removed when the user submits approval or rejection. rejecting means another snapshot is taken for another round of editing by the creator

---

## Section BK — Post-Answer Review 55

136. **Changed-field highlighting for added and removed items** - Q135 establishes that changed fields are highlighted with a coloured border and a tooltip showing the previous value. This presentation works for value changes (e.g., quantity changed from 100 to 150 — tooltip shows "100"). However, two structural changes cannot be expressed this way: (a) **Added items** — an item that did not exist at the time of rejection and was added by the Merchandiser before resubmission has no previous value to show in a tooltip. How is a newly added item indicated to the supervisor? For example, a "New" badge or tag on the item row? (b) **Removed items** — an item that existed at the time of rejection and was subsequently removed by the Merchandiser no longer has a row in the item list. How does the supervisor know an item was removed? For example, a ghost or greyed-out row showing the removed item with a "Removed" indicator, or a summary note at the top of the item list stating how many items were removed?
**ANSWER** : Added items will be highlighted the badge indicating it is new. Removed items will be a ghost or greyed-out row with a strike out and a badge indicating it was removed.

---

## Section BL — Post-Answer Review 56

137. **Can the supervisor add rejection marks to ghost rows (removed items)?** - Q136 establishes that items removed by the Merchandiser between rejection and resubmission appear as ghost/greyed-out rows with a strikethrough and "Removed" badge during the supervisor's review. Spec Section D.3 allows the supervisor to mark individual items as rejected with a per-item note. Can the supervisor apply a rejection mark to a ghost row — for example, to indicate that removing the item was wrong and the Merchandiser must re-add it? Or are ghost rows display-only and cannot receive rejection marks, meaning the supervisor must use the PO-level rejection note to communicate that a removed item should be reinstated?
**ANSWER** : Yes, a user with `purchaseorder_approve` may add rejection notes on a ghost row to indicate rejection of the removal. This implicates that the Merchandiser should be able to undo/re-add the item back to the PO when addressing the rejection.

146. **Ghost row re-add — who performs it and is there a UI shortcut?**
R1 confirmed supervisors can mark ghost rows as rejected. Who re-adds the item, and is there a UI shortcut?
**ANSWER** : The Merchandiser re-adds the item through the normal modify PO flow (add-item). A UI shortcut on the ghost row in the Rejected PO — an "Undo removal" button — may be provided to restore the item directly without requiring the Merchandiser to search for and re-add it from scratch. The shortcut uses the same underlying modify PO flow.

147. **`Do Nothing` configuration and partially received PO expiry**
C2 established that `Automatic Cancel` falls back to `Notify Only` for partially received POs. Does this fallback also apply when the configured action is `Do Nothing`?
**ANSWER** : No. When configured as `Do Nothing`, the system takes no action on a partially received PO with an expired `Cancel if not Delivered by Date`. The `Notify Only` fallback applies only when the configured action is `Automatic Cancel` (since auto-cancel cannot apply to partially received POs, the system notifies instead). Summary of all three cases for partially received POs: `Automatic Cancel` → falls back to `Notify Only`; `Notify Only` → notifies as normal; `Do Nothing` → no action.

---

## Section BM — Gaps Batch Review 1

138. **Two simultaneous highlight types — visually distinguished?**
A resubmitted PO may show two highlight types simultaneously: (a) "changed since rejection" — coloured border + tooltip showing previous value (Q135); (b) approval validation error highlights — errors highlighted and labeled with date last checked (spec C.1). Are these visually distinct from each other?
**ANSWER** : These are visually distinct, though all details can be listed in the same tooltip window.

139. **Item status during in-progress supervisor review**
Spec D.1 says rejection marks are saved persistently as the supervisor reviews. Does saving a rejection mark on an item immediately change that item's status from `Submitted` to `Rejected`, or do all item statuses remain `Submitted` until the supervisor formally submits the rejection (spec D.6)?
**ANSWER** : All item statuses remain `Submitted` until the supervisor formally submits the rejection.

140. **Edit lock applies to Rejected POs?**
Spec B.4 describes the edit lock for Draft POs: only one Merchandiser can actively edit at a time. Does the same edit lock mechanism apply to Rejected POs when the Merchandiser makes changes to resubmit?
**ANSWER** : Yes, the same edit lock mechanism applies to Rejected POs when a Merchandiser is making changes to resubmit.

141. **Auto-cancel system-generated note content**
Q126/Q128 confirm `Automatic Cancel` creates system-generated notes at PO-level and per-item. Is the note text a fixed standard message or configurable per organisation?
**ANSWER** : Fixed standard message.

142. **Partially received PO when `Automatic Cancel` date expires**
Spec B states auto-cancel only fires when no items have been received ("Partially received POs are not affected"). If a PO is partially received when `Cancel if not Delivered by Date` expires with `Automatic Cancel` configured, what happens?
**ANSWER** : The system falls back to `Notify Only` behaviour — notifying users — for a partially received PO with an expired `Cancel if not Delivered by Date`.

143. **SOF pre-population — original SO quantity or remaining unfulfilled quantity?**
Q10 confirms multiple POs can reference the same Sales Order. When a new SOF PO is created for an SO that already has other SOF POs referencing it, does pre-population use the original full SO quantity or the remaining unfulfilled quantity?
**ANSWER** : The original full SO quantity for each item is always used for pre-population, regardless of quantities already ordered on other SOF POs for the same SO.

144. **Per-facility zero quantity for specific items**
Q8 establishes that at submission, facilities with all items at zero quantity are prompted for removal. Can a facility have zero quantity for some items while non-zero for others without triggering the removal prompt?
**ANSWER** : Yes, a facility may have zero quantity for some items while non-zero for others. The submission removal prompt only fires when ALL items are zero for that facility.

145. **Returns Announcement documents — in scope for PO module?**
Spec Section F.4 lists Returns Announcement documents as a referenced document type linked from the PO. Is creation and management of Returns Announcements in scope for the PO module spec?
**ANSWER** : Returns Announcements are handled entirely by a separate Returns module. Only the reference link appears on the PO.
