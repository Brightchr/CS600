
package com.snhu.mscs.cs600.salesguru.contacts;

/**
 * Imports to support Spring JPA configuration
 */
import org.springframework.stereotype.Service;

/**
 * Imports to provide access to Java Collections
 */
import java.util.List;

/**
 * SalesGuruService -
 *
 * Purpose:
 *      The purpose of this class is to provide the necessary functionality
 *      to interact with the Repository interfaces for Spring Data JPA. This is
 *      the class that interacts with the application (or API controller(s))
 *      to provide access to a database.
 *
 *      This class is currently configured to interface with the following
 *      repositories:
 *
 *          CompanyRepository
 *          RepresentativeRepository
 *
 * Class Level Annotations:
 *      @Service - This tells the spring framework that this
 *      class is a Service implementation and enables the appropriate capabilities
 *      through the framework.
 *
 * Sample Output:
 *      This service utilizes the queries that are enabled in the Repository
 *      interface in order to return data to our application - in this case -
 *      the APIController
 *
 * Constraints: N/A
 *
 * Expected Results:
 *      Each query executed in the class will provide appropriate data as
 *      necessary to the calling function within the application.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 fjm            3-Oct-2025      Initial Creation
 *     2                 Chris Bright   17-Nov-2026     Reorganized package
 *                                                     and constructor injection
 */
@Service
public class SalesGuruService {

    private final CompanyRepository companyRepo;
    private final RepresentativeRepository representativeRepo;

    /**
     * Constructor - Provides access to the repositories used by this service.
     */
    public SalesGuruService(CompanyRepository cr, RepresentativeRepository rr) {
        this.companyRepo = cr;
        this.representativeRepo = rr;
    }

    /**
     * Returns all companies stored in the database.
     */
    public List<Company> listCompanies() {
        return companyRepo.listCompanies();
    }

    /**
     * Returns all representatives stored in the database.
     */
    public List<Representative> listRepresentatives() {
        return representativeRepo.listRepresentatives();
    }

    /**
     * Returns representatives associated with the specified company.
     */
    public List<Representative> listRepresentativesForCompany(int customer_number) {
        return representativeRepo.listRepresentativesForCompany(customer_number);
    }
}
