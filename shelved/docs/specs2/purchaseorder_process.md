# Purchase Order Process Flow

## A. Actors
1. Merchandiser : creates purchase order
2. High Value Merchandiser : Adds item in purchase order that is under a category with a limited budget.
3. Merchandise Supervisor : approves purchase order

## B. Create Purchase Order
1. Fields to set:
    - **Buyer** : Company that will be making the purchase. Determines the budget (for items that are under a budget) for the purchase order. Limits the Facilities that the order will be created for. This is a from a list of companies under the organization.
    - **Seller** : Vendor of the items to purchase. This is from a list of vendoers that the selected company has a relationship with. Relationship between the Company and Vendor determines the terms of the purchase order including cost, minimum order quantity, order multiples, schedule for each of the items the seller sells to the buyer.
    - **Order Date** : Date for the order. Date is also used as basis for budget, accounting and effectivity.
    - **Cancel if not Delivered by Date** : An optional fail over date when the order will be automatically canceled if the order did not arrive at the specified date.
    - **Type of Purchase Order** : 
        - `Inventory Replenishment` are orders to replenish the inventory of the selected Facility.
        - `Cross Dock Order` are orders to replenish the inventory of other Facilities. Inventory will automatically be allocated to be dispatched to the other Facilites.
        - `Sales Order Fulfillment` are orders specifically to fulfill a sales order. Inventory will automatically be allocated to the SalesOrder specified for the purchase order.
    - **Facility List** : The facilities are the recipients of the order. This is from the list of facilities that is under the selected company. Setting multiple Facilities will automatically set the Purchase Order Type to `Cross Dock Order`
    - **Delivery Location** : Facility location to deliver the order. This may be from any of the facilities that is under the selected company but the default is one of the facilities selected from the **Facility List**.
    - **Referenced Sales Order** : Related sales order when purchase order is set to type `Sales Order Fulfillment`
    - **Notes** : Additional notes related to the purchase order.
    - **Attached Files** : Documents related to the order that will be created.
2. Automatically set fields by the system :
    - **Document ID** : System generated human readable identifier for the purchase order. This will be the value documents and users will refer to a specific purchase order.
    - **Created By** : User who created the document.
    - **Created Date** : Date when the document was created.
    - **Updated By** : User who last updated the document.
    - **Updated Date** : Date when the document was last updated.
3. Add items for the Purchase Order
    - Items are from the list of items that the vendor sells to the company. This can be added one by one by searching with the product name, GTIN or product ID. Multiple items can be added by selecting a list of products from a selected category. Item is filtered by the schedule set by the vendor when it can be ordered. 
    - Items that belongs to a category with a set budget can only be added by an authorized High Value Merchandiser.
    - A purchase order of type `Sales Order Fulfillment` can only add items listed in the referenced sales order.
    - Quantity is based on set `unit of measure (UOM)` by the vendor for the product. System can be configured to order using this UOM or the set `stock keeping unit (SKU)` used by the company's facility. `minimum order quantity` and `order multiple` is set from the vendor's UOM.
    - Quantity to order is limited to the `minimum order quantity` and `order multiple` set by the vendor. System ensures that the quantity set no less than the `minimum order quantity` and only in multiples of the set `order multiple`
    - Quantity to order is also limited to the set budget for the category if the category has a defined budget. The total cost for all items ordered for each category should not exceed the set budget. Only Merchandiser authorized to order items under a category with budget can set the quantity to order for these kinds of items.
    - Quantity to order for each facility can be individually set if order is for multiple facility.
    - Total ordered quantity and cost of the purhase order is automatically updated for the purchase order.
4. Merchandiser submits the purchase order for approval after adding all the details. The purchase order can also be saved as draft if Merchandiser still needs time to complete the details of the purchase order. Alternately, the Merchandiser can cancel the purchase order to discontinue the processing of the created order.

## C. Approving Purchase Order
1. When the system is configured to require approval of purchase order, a Merchandiser supervisor reviews the submitted purchase order and approves it if everything is in order. 
2. Merchandiser supervisor can also reject the submitted purchase order (see Rejecting Purchase Order). Merchandiser checks the rejected purchase order, does the required changes and resubmits the purchase order for approval.
3. Merchandiser supervisor can also cancel the submitted purchase order to discontinue the processing of the submitted order.
4. If the system is configured not to require approval of purchase order, the system automatically approves the purchase order when it is submitted by the Merchandiser.

## D. Rejecting Purchase Order
1. Merchandiser supervisor can reject the submitted purchase order.
2. Rejecting a purchase order because of its detail or the purchase order as a whole updates the status of order to rejected but the status of the items remain as submitted. Rejection note is required to describe the reason for the rejection.
3. Rejecting a purchase order because of certain items will require marking each of the items as rejected with a note on each item to describe the reason for the rejection. The rest of the items with no issue will remain with submitted status. The status of the purchase order will be rejected.
4. Rejection on an item or PO detail can be removed by the supervisor. But all rejection are final and cannot be removed once the supervisor submits the rejection of the PO.
5. An activity log is created for each PO-detail and item rejection. Removing a rejection also creates an activity log.

## E. Completing Purchase Order
1. System automatically marks the purchase order as completed after all items have been received.
2. Merchandiser can manually mark a purchase order as completed even if not all items have been received.
3. Only authorized High Value Merchandiser can manually mark a purchase order as completed if the order contains items belonging to a category with set budget.

## F. Referenced Document
Referenced documents have a link from the purchase order to quickly access and view related document.
1. Attached files to the purchase order.
2. Sales Order referenced to the purchase order for type `Sales Order Fulfillment`.
3. Receiving Advice documents done to receive items in the purchase order.
4. Returns Annoucement documents done to return items in the purchase order.

## G. View of Purchase Order
1. Purchase order quick summary and next immediate action.
2. Buyer details
3. Seller details
4. Purchase order details
5. Purchase order totals
6. Activity log history
7. Item list (sortable, searchable)

## H. Dashboard
Viewable only for those who have permissions to view PO or can do the action being notified.
1. Number of PO in not submitted.
2. Number of PO needs approved.
3. Number of PO that were rejected and needs revisions.
4. Number of approved PO with no items received.
5. Number of approved PO patially received.
6. Number of approved PO nearing expiration of `Cancel if not Delivered by Date` (based on configured lead time)