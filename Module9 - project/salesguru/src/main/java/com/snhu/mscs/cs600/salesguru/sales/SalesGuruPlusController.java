
package com.snhu.mscs.cs600.salesguru.sales;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SalesGuruPlusController -
 *
 * Purpose:
 *      Provides REST API endpoints for Sales Guru+ products
 *      and invoices. Business logic is handled by the
 *      SalesGuruPlusService class.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright   17-Nov-2026     Initial Creation
 */
@RestController
@RequestMapping("/api/v1")
public class SalesGuruPlusController {

  private final SalesGuruPlusService salesService;

  /**
   * Constructor injection for the Sales Guru+ service.
   */
  public SalesGuruPlusController(SalesGuruPlusService salesService) {
    this.salesService = salesService;
  }

  /**
   * Returns all products stored in the database.
   */
  @GetMapping("/items")
  public List<Item> listItems() {
    return salesService.listItems();
  }

  /**
   * Creates a new product using the supplied request data.
   */
  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public Item createItem(
      @RequestBody SalesGuruPlusService.ItemRequest request) {

    return salesService.createItem(request);
  }

  /**
   * Returns all invoices stored in the database.
   */
  @GetMapping("/invoices")
  public List<Invoice> listInvoices() {
    return salesService.listInvoices();
  }

  /**
   * Returns an invoice with its associated line items.
   */
  @GetMapping("/invoices/{id}")
  public SalesGuruPlusService.InvoiceView getInvoice(
      @PathVariable Integer id) {

    return salesService.getInvoice(id);
  }

  /**
   * Creates an invoice and its related line items.
   */
  @PostMapping("/invoices")
  @ResponseStatus(HttpStatus.CREATED)
  public SalesGuruPlusService.InvoiceView createInvoice(
      @RequestBody SalesGuruPlusService.InvoiceRequest request) {

    return salesService.createInvoice(request);
  }
}
