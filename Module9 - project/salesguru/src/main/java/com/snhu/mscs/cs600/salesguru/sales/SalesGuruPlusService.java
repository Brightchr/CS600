
package com.snhu.mscs.cs600.salesguru.sales;

import com.snhu.mscs.cs600.salesguru.contacts.CompanyRepository;
import com.snhu.mscs.cs600.salesguru.contacts.Representative;
import com.snhu.mscs.cs600.salesguru.contacts.RepresentativeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SalesGuruPlusService -
 *
 * Purpose:
 *      Handles product and invoice operations for Sales Guru+.
 *      Validates incoming data, saves invoice records, and
 *      aggregates line items for display.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright   17-Nov-2026     Initial Creation
 */
@Service
public class SalesGuruPlusService {

  private final CompanyRepository companyRepository;
  private final RepresentativeRepository representativeRepository;
  private final ItemRepository itemRepository;
  private final InvoiceRepository invoiceRepository;
  private final LineItemRepository lineItemRepository;

  /**
   * Constructor injection for the repositories used by this service.
   */
  public SalesGuruPlusService(
      CompanyRepository companyRepository,
      RepresentativeRepository representativeRepository,
      ItemRepository itemRepository,
      InvoiceRepository invoiceRepository,
      LineItemRepository lineItemRepository) {

    this.companyRepository = companyRepository;
    this.representativeRepository = representativeRepository;
    this.itemRepository = itemRepository;
    this.invoiceRepository = invoiceRepository;
    this.lineItemRepository = lineItemRepository;
  }

  /**
   * Records define the data accepted and returned by the API.
   */
  public record ItemRequest(
      String description,
      Float cost,
      Float retail
  ) {}

  public record LineRequest(
      Integer itemNumber,
      Integer qty,
      Float discount
  ) {}

  public record InvoiceRequest(
      Integer customerNumber,
      Integer representativeId,
      LocalDate dateOfSale,
      List<LineRequest> lines
  ) {}

  public record LineView(
      Integer lineItemId,
      Integer itemNumber,
      String description,
      Integer qty,
      Float retail,
      Float discount
  ) {}

  public record InvoiceView(
      Integer invoiceNumber,
      Integer customerNumber,
      Integer representativeId,
      LocalDate dateOfSale,
      List<LineView> lines
  ) {}

  /**
   * Returns all products stored in the database.
   */
  @Transactional(readOnly = true)
  public List<Item> listItems() {
    return itemRepository.findAll();
  }

  /**
   * Returns all invoices stored in the database.
   */
  @Transactional(readOnly = true)
  public List<Invoice> listInvoices() {
    return invoiceRepository.findAll();
  }

  /**
   * Retrieves an invoice and its associated product details.
   */
  @Transactional(readOnly = true)
  public InvoiceView getInvoice(Integer invoiceNumber) {

    Invoice invoice = invoiceRepository.findById(invoiceNumber)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Invoice not found"
        ));

    List<LineView> details = new ArrayList<>();

    List<LineItem> invoiceLines =
        lineItemRepository.findByInvoiceNumberOrderByLineItemIdAsc(
            invoiceNumber
        );

    for (LineItem line : invoiceLines) {

      Item item = itemRepository.findById(line.getItemNumber())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Product not found"
          ));

      details.add(new LineView(
          line.getLineItemId(),
          line.getItemNumber(),
          item.getDescription(),
          line.getQty(),
          item.getRetail(),
          line.getDiscount()
      ));
    }

    return new InvoiceView(
        invoice.getInvoiceNumber(),
        invoice.getCustomerNumber(),
        invoice.getRepresentativeId(),
        invoice.getDateOfSale(),
        details
    );
  }

  /**
   * Validates and stores a new product.
   */
  @Transactional
  public Item createItem(ItemRequest request) {

    if (request == null
        || request.description() == null
        || request.description().isBlank()
        || request.description().length() > 128
        || request.cost() == null
        || !Float.isFinite(request.cost())
        || request.cost() < 0
        || request.retail() == null
        || !Float.isFinite(request.retail())
        || request.retail() < 0) {

      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Invalid product information"
      );
    }

    Item item = new Item();

    item.setDescription(request.description());
    item.setCost(request.cost());
    item.setRetail(request.retail());

    return itemRepository.save(item);
  }

  /**
   * Validates and saves an invoice with its line items.
   * All database changes are handled in one transaction.
   */
  @Transactional
  public InvoiceView createInvoice(InvoiceRequest request) {

    if (request == null
        || request.customerNumber() == null
        || request.representativeId() == null
        || request.dateOfSale() == null
        || request.lines() == null
        || request.lines().isEmpty()) {

      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Incomplete invoice information"
      );
    }

    // Verify that the selected company exists.
    if (!companyRepository.existsById(request.customerNumber())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Company does not exist"
      );
    }

    // Retrieve the representative associated with the sale.
    Representative representative = representativeRepository
        .findById(request.representativeId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Representative does not exist"
        ));

    // Prevent invoices from using a representative from another company.
    if (representative.getCustomer_number()
        != request.customerNumber()) {

      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Representative does not belong to this company"
      );
    }

    // Validate each line before creating database records.
    for (LineRequest line : request.lines()) {

      if (line == null
          || line.itemNumber() == null
          || line.qty() == null
          || line.qty() <= 0
          || !itemRepository.existsById(line.itemNumber())
          || (line.discount() != null
          && !Float.isFinite(line.discount()))) {

        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid invoice line item"
        );
      }
    }

    // Save the invoice first to obtain its generated invoice number.
    Invoice invoice = new Invoice();

    invoice.setCustomerNumber(request.customerNumber());
    invoice.setRepresentativeId(request.representativeId());
    invoice.setDateOfSale(request.dateOfSale());

    invoice = invoiceRepository.saveAndFlush(invoice);

    List<Integer> lineIds = new ArrayList<>();

    // Save each line item using the generated invoice number.
    for (LineRequest requestLine : request.lines()) {

      LineItem line = new LineItem();

      line.setInvoiceNumber(invoice.getInvoiceNumber());
      line.setItemNumber(requestLine.itemNumber());
      line.setQty(requestLine.qty());
      line.setDiscount(requestLine.discount());

      LineItem savedLine = lineItemRepository.saveAndFlush(line);

      lineIds.add(savedLine.getLineItemId());
    }

    // Store the generated line-item identifiers.
    invoice.setLineItems(lineIds.toArray(Integer[]::new));

    invoiceRepository.saveAndFlush(invoice);

    // Return the saved invoice with its aggregated product details.
    return getInvoice(invoice.getInvoiceNumber());
  }
}
