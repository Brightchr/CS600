
package com.snhu.mscs.cs600.salesguru.sales;

import com.snhu.mscs.cs600.salesguru.contacts.CompanyRepository;
import com.snhu.mscs.cs600.salesguru.contacts.Representative;
import com.snhu.mscs.cs600.salesguru.contacts.RepresentativeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SalesGuruPlusServiceTests -
 *
 * Purpose:
 *      Verifies product creation, invoice aggregation,
 *      and validation rules in SalesGuruPlusService.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright    18-Nov-2026     Initial Creation
 */
@ExtendWith(MockitoExtension.class)
class SalesGuruPlusServiceTests {

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private RepresentativeRepository representativeRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private InvoiceRepository invoiceRepository;

  @Mock
  private LineItemRepository lineItemRepository;

  @InjectMocks
  private SalesGuruPlusService service;

  /**
   * Test 1 - Verify a valid product is passed to the repository.
   */
  @Test
  void createItemWithValidData() {

    var request = new SalesGuruPlusService.ItemRequest(
        "Office Supplies", 12.50f, 19.99f
    );

    when(itemRepository.save(any(Item.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Item result = service.createItem(request);

    assertEquals("Office Supplies", result.getDescription());
    assertEquals(12.50f, result.getCost());
    assertEquals(19.99f, result.getRetail());

    verify(itemRepository).save(any(Item.class));
  }

  /**
   * Test 2 - Verify invoice aggregation includes product details.
   */
  @Test
  void aggregateInvoiceLineItems() {

    Invoice invoice = new Invoice();
    invoice.setInvoiceNumber(100);
    invoice.setCustomerNumber(1);
    invoice.setRepresentativeId(1);
    invoice.setDateOfSale(LocalDate.of(2026, 11, 18));

    LineItem lineOne = new LineItem();
    lineOne.setLineItemId(1);
    lineOne.setInvoiceNumber(100);
    lineOne.setItemNumber(10);
    lineOne.setQty(2);

    LineItem lineTwo = new LineItem();
    lineTwo.setLineItemId(2);
    lineTwo.setInvoiceNumber(100);
    lineTwo.setItemNumber(20);
    lineTwo.setQty(5);

    Item productOne = new Item();
    productOne.setItemNumber(10);
    productOne.setDescription("Office Supplies");
    productOne.setRetail(19.99f);

    Item productTwo = new Item();
    productTwo.setItemNumber(20);
    productTwo.setDescription("Printer Paper");
    productTwo.setRetail(8.50f);

    when(invoiceRepository.findById(100))
        .thenReturn(Optional.of(invoice));

    when(lineItemRepository
        .findByInvoiceNumberOrderByLineItemIdAsc(100))
        .thenReturn(List.of(lineOne, lineTwo));

    when(itemRepository.findById(10))
        .thenReturn(Optional.of(productOne));

    when(itemRepository.findById(20))
        .thenReturn(Optional.of(productTwo));

    var result = service.getInvoice(100);

    assertEquals(100, result.invoiceNumber());
    assertEquals(2, result.lines().size());

    assertEquals("Office Supplies",
        result.lines().get(0).description());
    assertEquals(2, result.lines().get(0).qty());

    assertEquals("Printer Paper",
        result.lines().get(1).description());
    assertEquals(5, result.lines().get(1).qty());
  }

  /**
   * Test 3 - Reject invoices where the representative
   * belongs to a different company.
   */
  @Test
  void rejectIncorrectRepresentativeCompany() {

    Representative representative = new Representative();
    representative.setCustomer_number(1);

    when(companyRepository.existsById(2)).thenReturn(true);

    when(representativeRepository.findById(1))
        .thenReturn(Optional.of(representative));

    var request = new SalesGuruPlusService.InvoiceRequest(
        2,
        1,
        LocalDate.now(),
        List.of(
            new SalesGuruPlusService.LineRequest(
                10, 1, null
            )
        )
    );

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.createInvoice(request)
    );

    assertEquals(HttpStatus.BAD_REQUEST,
        exception.getStatusCode());

    verifyNoInteractions(invoiceRepository, lineItemRepository);
  }

  /**
   * Test 4 - Reject invoices with a quantity of zero.
   */
  @Test
  void rejectInvalidQuantity() {

    Representative representative = new Representative();
    representative.setCustomer_number(1);

    when(companyRepository.existsById(1)).thenReturn(true);

    when(representativeRepository.findById(1))
        .thenReturn(Optional.of(representative));

    var request = new SalesGuruPlusService.InvoiceRequest(
        1,
        1,
        LocalDate.now(),
        List.of(
            new SalesGuruPlusService.LineRequest(
                10, 0, null
            )
        )
    );

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.createInvoice(request)
    );

    assertEquals(HttpStatus.BAD_REQUEST,
        exception.getStatusCode());

    verifyNoInteractions(invoiceRepository, lineItemRepository);
  }
}
