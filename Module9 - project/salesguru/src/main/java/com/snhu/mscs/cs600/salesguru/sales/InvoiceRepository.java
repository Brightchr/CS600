
package com.snhu.mscs.cs600.salesguru.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * InvoiceRepository -
 *
 * Purpose:
 *      Provides database access for invoices stored in SalesGuru.
 *      Spring Data JPA handles standard CRUD operations.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 Chris Bright   17-Nov-2026     Initial Creation
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

}
