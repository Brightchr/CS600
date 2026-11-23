
package com.snhu.mscs.cs600.salesguru.ui;

import com.snhu.mscs.cs600.salesguru.contacts.SalesGuruService;
import com.snhu.mscs.cs600.salesguru.sales.SalesGuruPlusService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

/**
 * DashboardController -
 *
 * Purpose:
 *      Provides the graphical interface for Sales Guru+.
 *      Retrieves company, representative, item, and invoice data
 *      through the existing service classes.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright   17-Nov-2026     Initial Creation
 */
@Controller
public class DashboardController {

  private final SalesGuruService salesGuruService;
  private final SalesGuruPlusService salesGuruPlusService;

  /**
   * Constructor injection for the application services.
   */
  public DashboardController(
      SalesGuruService salesGuruService,
      SalesGuruPlusService salesGuruPlusService) {

    this.salesGuruService = salesGuruService;
    this.salesGuruPlusService = salesGuruPlusService;
  }

  /**
   * Displays the main dashboard with company, representative,
   * product, and invoice information.
   */
  @GetMapping("/")
  public String dashboard(Model model, Principal principal) {

    model.addAttribute("username", principal.getName());

    model.addAttribute(
        "companies",
        salesGuruService.listCompanies()
    );

    model.addAttribute(
        "representatives",
        salesGuruService.listRepresentatives()
    );

    model.addAttribute(
        "items",
        salesGuruPlusService.listItems()
    );

    model.addAttribute(
        "invoices",
        salesGuruPlusService.listInvoices()
    );

    return "dashboard";
  }

  /**
   * Displays a selected invoice with its associated line items.
   */
  @GetMapping("/invoices/{id}/view")
  public String invoiceDetails(
      @PathVariable("id") Integer invoiceNumber,
      Model model) {

    model.addAttribute(
        "invoice",
        salesGuruPlusService.getInvoice(invoiceNumber)
    );

    return "invoice";
  }
}
