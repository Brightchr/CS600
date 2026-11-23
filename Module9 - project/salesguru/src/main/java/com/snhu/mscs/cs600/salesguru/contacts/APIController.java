
package com.snhu.mscs.cs600.salesguru.contacts;

// Imports to support API Development
import org.springframework.web.bind.annotation.*;

// Import java collection capabilities
import java.util.List;

/**
 * APIController -
 *
 * Purpose:
 *      The purpose of this class is to provide the necessary controller
 *      mappings for a RESTful API. These will utilize additional classes
 *      and libraries as necessary to create and maintain the necessary
 *      API functionality for the Sales Guru Application.
 *
 * Class Level Annotations:
 *      @RestController - This tells the spring framework that this
 *      class is providing RESTful API endpoints for the application
 *      and enables those components in the framework.
 *
 *      @RequestMapping - This tells the spring framework what the
 *      root of the namespace is for our RESTful API.
 *
 * Sample Output:
 *      Accessing the 'version' endpoint provided by this application
 *      through the URL https://localhost:8443/api/v1/version should
 *      result in:
 *              Version-1.0
 *
 * Constraints: N/A
 *
 * Expected Results:
 *      Each API endpoint in this class should respond as defined and
 *      documented to appropriately formatted calls.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 fjm            3-Oct-2025      Initial Creation
 *     2                 Chris Bright   17-Nov-2026     Package reorganization
 *                                                     and constructor injection
 */
@RestController
@RequestMapping("/api/v1")
public class APIController {

    private final SalesGuruService salesGuruService;

    /**
     * APIController - Constructor used to provide access to the
     * SalesGuruService through Spring dependency injection.
     */
    public APIController(SalesGuruService service) {
        this.salesGuruService = service;
    }

    /**
     * getVersion - This method supports a simple return of what version
     * of the API is being provided. This should be changed when major
     * changes are made to this API.
     *
     * Annotations:
     *      @GetMapping - This annotation indicates the name of the
     *      endpoint. It is added to the end of the value of
     *      @RequestMapping that sets the namespace for this class.
     *
     * @return String indicating the version of the API being provided.
     */
    @GetMapping("/version")
    public String getVersion() {
        return "Version-1.0";
    }

    /**
     * companies - This method supports the display of a list of companies.
     * This version of the /companies endpoint calls for the data
     * directly from the Database.
     *
     * This endpoint provides validation for requirement #7 - "The application
     * shall provide a mechanism to display all companies in the system."
     *
     * Annotations:
     *      @GetMapping - This annotation indicates the name of the
     *      endpoint. It is added to the end of the value of
     *      @RequestMapping that sets the namespace for this class.
     *
     * @return A list of company objects serialized as JSON.
     */
    @GetMapping("/companies")
    public List<Company> listCompanies() {
        return salesGuruService.listCompanies();
    }

    /**
     * representatives - This method supports the display of a list of
     * representatives from the database. This only lists the representative
     * data, it does not map them to companies.
     *
     * Annotations:
     *      @GetMapping - This annotation indicates the name of the
     *      endpoint. It is added to the end of the value of
     *      @RequestMapping that sets the namespace for this class.
     *
     * @return A list of representative objects serialized as JSON.
     */
    @GetMapping("/representatives")
    public List<Representative> listRepresentatives() {
        return salesGuruService.listRepresentatives();
    }

    /**
     * repsForCompany/customer_number - This method supports the display
     * of a list of representatives from the database that represent
     * the company identified by the customer_number.
     *
     * This endpoint verifies requirement #6 - "The application shall provide
     * a mechanism to display all representatives for a given company."
     * and correspondingly requirement #3 - "The application shall provide
     * a mechanism to link representatives to a single company."
     *
     * This relationship is established through the customer_number
     * foreign key in the representative table and retrieved using
     * the parameterized REST endpoint.
     *
     * Annotations:
     *      @GetMapping - This annotation indicates the name of the
     *      endpoint. It is added to the end of the value of
     *      @RequestMapping that sets the namespace for this class.
     *
     *      @PathVariable - This annotation provides access to the
     *      customer_number supplied in the requested URL.
     *
     * @param customer_number The company ID used to filter representatives.
     * @return A list of representatives serialized as JSON.
     */
    @GetMapping("/repsForCompany/{customer_number}")
    public List<Representative> listRepresentativesForCompany(
        @PathVariable int customer_number) {

        return salesGuruService.listRepresentativesForCompany(customer_number);
    }
}
