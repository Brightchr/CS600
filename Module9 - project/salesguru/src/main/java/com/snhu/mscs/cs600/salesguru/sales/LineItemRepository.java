
package com.snhu.mscs.cs600.salesguru.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LineItemRepository -
 *
 * Purpose:
 *      Provides database access for invoice line items.
 *      Supports retrieving all items associated with an invoice.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright   17-Nov-2026     Initial Creation
 */
@Repository
public interface LineItemRepository extends JpaRepository<LineItem, Integer> {

  /**
   * Retrieves all line items for an invoice, ordered by line item ID.
   *
   * @param invoiceNumber The invoice number to search for.
   * @return A list of line items associated with the invoice.
   */
  List<LineItem> findByInvoiceNumberOrderByLineItemIdAsc(Integer invoiceNumber);
}
