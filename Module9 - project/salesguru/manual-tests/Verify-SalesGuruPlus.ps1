
# Sales Guru+ verification
# Author: Chris Bright
# Tests the running REST API using HTTPS and database-backed authentication.

# PowerShell 7 is required for SkipCertificateCheck.
if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'PowerShell 7 or newer is required.'
}

$baseUrl = 'https://localhost:8443'

# Request credentials without displaying the password.
$password = Read-Host 'Password for sgUser' -AsSecureString
$credential = [pscredential]::new('sgUser', $password)

$results = [System.Collections.Generic.List[object]]::new()
$runId = Get-Date -Format 'yyyyMMdd-HHmmss'

# Sends requests and returns the HTTP status and response data.
function Request {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Payload = $null,
        [switch]$Anonymous
    )

    $options = @{
        Uri                  = "$baseUrl$Path"
        Method               = $Method
        SkipCertificateCheck = $true
        SkipHttpErrorCheck   = $true
        ErrorAction          = 'Stop'
    }

    if (-not $Anonymous) {
        $options.Authentication = 'Basic'
        $options.Credential = $credential
    }

    if ($null -ne $Payload) {
        $options.ContentType = 'application/json'
        $options.Body = ConvertTo-Json -InputObject $Payload -Depth 8
    }

    $response = Invoke-WebRequest @options

    $json = $null

    try {
        $json = ConvertFrom-Json -InputObject $response.Content
    }
    catch {
        # Some responses, such as Version-1.0, are plain text.
    }

    return [pscustomobject]@{
        Status  = [int]$response.StatusCode
        Content = $response.Content
        Json    = $json
    }
}

# Records and displays an individual test result.
function Check {
    param(
        [string]$Name,
        [bool]$Passed,
        [string]$Actual
    )

    $result = if ($Passed) { 'PASS' } else { 'FAIL' }

    $results.Add([pscustomobject]@{
        Test   = $Name
        Result = $result
        Actual = $Actual
    })

    Write-Host "[$result] $Name - $Actual"
}

try {

    # TEST GROUP 1 - Original functionality

    $version = Request GET '/api/v1/version' -Anonymous

    Check 'Public version endpoint' `
        ($version.Status -eq 200 -and $version.Content -eq 'Version-1.0') `
        "HTTP $($version.Status)"

    $anonymous = Request GET '/api/v1/companies' -Anonymous

    Check 'Anonymous access rejected' `
        ($anonymous.Status -eq 401) `
        "HTTP $($anonymous.Status)"

    $companies = Request GET '/api/v1/companies'

    Check 'Company list' `
        ($companies.Status -eq 200 -and @($companies.Json).Count -ge 2) `
        "HTTP $($companies.Status); $(@($companies.Json).Count) companies"

    $representatives = Request GET '/api/v1/representatives'

    Check 'Representative list' `
        ($representatives.Status -eq 200 -and @($representatives.Json).Count -ge 1) `
        "HTTP $($representatives.Status)"

    # Select an existing representative and their company.
    $rep = @($representatives.Json) | Select-Object -First 1

    if ($null -eq $rep) {
        throw 'No representatives available for invoice testing.'
    }

    $companyId = [int]$rep.customer_number
    $repId = [int]$rep.representative_id

    # Find another company for the invalid relationship test.
    $otherCompany = @($companies.Json) |
        Where-Object { $_.customer_number -ne $companyId } |
        Select-Object -First 1

    if ($null -eq $otherCompany) {
        throw 'A second company is required for the negative test.'
    }

    $filtered = Request GET "/api/v1/repsForCompany/$companyId"
    $filteredReps = @($filtered.Json)

    $validFilter = (
        $filtered.Status -eq 200 -and
        @($filteredReps | Where-Object {
            $_.customer_number -ne $companyId
        }).Count -eq 0 -and
        @($filteredReps | Where-Object {
            $_.representative_id -eq $repId
        }).Count -eq 1
    )

    Check 'Representatives filtered by company' `
        $validFilter `
        "HTTP $($filtered.Status); customer $companyId"


    # TEST GROUP 2 - Product persistence

    # Unique descriptions prevent confusion between test runs.
    $productA = Request POST '/api/v1/items' @{
        description = "TEST $runId - Office Supplies"
        cost = 12.50
        retail = 19.99
    }

    Check 'Create first product' `
        ($productA.Status -eq 201 -and $productA.Json.itemNumber -gt 0) `
        "HTTP $($productA.Status); item $($productA.Json.itemNumber)"

    $productB = Request POST '/api/v1/items' @{
        description = "TEST $runId - Printer Paper"
        cost = 4.25
        retail = 8.50
    }

    Check 'Create second product' `
        ($productB.Status -eq 201 -and $productB.Json.itemNumber -gt 0) `
        "HTTP $($productB.Status); item $($productB.Json.itemNumber)"

    if ($productA.Status -ne 201 -or $productB.Status -ne 201) {
        throw 'Product creation failed. Invoice tests cannot continue.'
    }


    # TEST GROUP 3 - Invoice creation

    $invoiceData = @{
        customerNumber = $companyId
        representativeId = $repId
        dateOfSale = (Get-Date -Format 'yyyy-MM-dd')

        lines = @(
            @{
                itemNumber = [int]$productA.Json.itemNumber
                qty = 2
                discount = 0
            },
            @{
                itemNumber = [int]$productB.Json.itemNumber
                qty = 5
                discount = 0
            }
        )
    }

    $created = Request POST '/api/v1/invoices' $invoiceData
    $createdLines = @($created.Json.lines)

    Check 'Create two-line invoice' `
        ($created.Status -eq 201 -and
         $created.Json.invoiceNumber -gt 0 -and
         $createdLines.Count -eq 2) `
        "HTTP $($created.Status); invoice $($created.Json.invoiceNumber)"

    if ($created.Status -ne 201) {
        throw 'Invoice creation failed. Retrieval cannot continue.'
    }

    $invoiceId = [int]$created.Json.invoiceNumber


    # TEST GROUP 4 - Invoice aggregation

    $retrieved = Request GET "/api/v1/invoices/$invoiceId"
    $retrievedLines = @($retrieved.Json.lines)

    $hasA = @($retrievedLines | Where-Object {
        $_.itemNumber -eq $productA.Json.itemNumber -and
        $_.qty -eq 2 -and
        $_.description -eq $productA.Json.description
    }).Count -eq 1

    $hasB = @($retrievedLines | Where-Object {
        $_.itemNumber -eq $productB.Json.itemNumber -and
        $_.qty -eq 5 -and
        $_.description -eq $productB.Json.description
    }).Count -eq 1

    Check 'Retrieve aggregated invoice' `
        ($retrieved.Status -eq 200 -and
         $retrievedLines.Count -eq 2 -and
         $hasA -and $hasB) `
        "HTTP $($retrieved.Status); 2 matching items"

    $allInvoices = Request GET '/api/v1/invoices'

    $invoiceExists = @($allInvoices.Json | Where-Object {
        $_.invoiceNumber -eq $invoiceId
    }).Count -eq 1

    Check 'Invoice appears in list' `
        ($allInvoices.Status -eq 200 -and $invoiceExists) `
        "HTTP $($allInvoices.Status); invoice $invoiceId"



    # TEST GROUP 5 - Invalid invoice requests


    $countBefore = @($allInvoices.Json).Count

    # A representative cannot be assigned to another company.
    $wrongCompanyData = $invoiceData.Clone()
    $wrongCompanyData.customerNumber = [int]$otherCompany.customer_number

    $wrongCompany = Request POST '/api/v1/invoices' $wrongCompanyData

    Check 'Reject representative from another company' `
        ($wrongCompany.Status -eq 400) `
        "HTTP $($wrongCompany.Status)"

    # Quantity must be greater than zero.
    $badQuantityData = $invoiceData.Clone()

    $badQuantityData.lines = @(
        @{
            itemNumber = [int]$productA.Json.itemNumber
            qty = 0
            discount = 0
        }
    )

    $badQuantity = Request POST '/api/v1/invoices' $badQuantityData

    Check 'Reject zero quantity' `
        ($badQuantity.Status -eq 400) `
        "HTTP $($badQuantity.Status)"

    # An invoice cannot reference a nonexistent product.
    $missingProductData = $invoiceData.Clone()

    $missingProductData.lines = @(
        @{
            itemNumber = 2147483647
            qty = 1
            discount = 0
        }
    )

    $missingProduct = Request POST '/api/v1/invoices' $missingProductData

    Check 'Reject nonexistent product' `
        ($missingProduct.Status -eq 400) `
        "HTTP $($missingProduct.Status)"

    # Confirm rejected requests did not create invoices.
    $afterInvalid = Request GET '/api/v1/invoices'

    Check 'Invalid requests leave invoice count unchanged' `
        ($afterInvalid.Status -eq 200 -and
         @($afterInvalid.Json).Count -eq $countBefore) `
        "Before $countBefore; after $(@($afterInvalid.Json).Count)"


    # TEST GROUP 6 - New endpoint security

    $anonymousInvoice = Request GET '/api/v1/invoices' -Anonymous

    Check 'New invoice endpoint requires authentication' `
        ($anonymousInvoice.Status -eq 401) `
        "HTTP $($anonymousInvoice.Status)"

    Write-Host ""
    Write-Host "Invoice created for UI verification: $invoiceId"
    Write-Host "Open https://localhost:8443/invoices/$invoiceId/view"

}
catch {
    Check 'Test execution completed' $false $_.Exception.Message
}
finally {

    # Generate a consolidated test report

    $passCount = @($results | Where-Object Result -eq 'PASS').Count
    $total = $results.Count

    $report = @(
        '# Sales Guru+ - End-to-End Verification Report'
        ''
        "Run: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')"
        'Environment: Spring Boot 3.5.6, PostgreSQL, HTTPS localhost:8443'
        'Certificate verification: Disabled for local development'
        "Result: $passCount / $total passed"
        ''
        '| Test | Result | Actual evidence |'
        '|---|---|---|'
    )

    foreach ($entry in $results) {

        $actual = $entry.Actual.Replace('|', '\|').
            Replace("`r", ' ').
            Replace("`n", ' ')

        $report += "| $($entry.Test) | $($entry.Result) | $actual |"
    }

    $reportPath = Join-Path $PSScriptRoot 'SalesGuruPlus-Test-Results.md'

    Set-Content -Path $reportPath -Value $report -Encoding utf8

    Write-Host ""
    Write-Host "RESULT: $passCount / $total tests passed"
    Write-Host "REPORT: $reportPath"

    # Print a consolidated table for one terminal screenshot.
    $results | Format-Table -AutoSize

    if ($passCount -ne $total) {
        Write-Warning "One or more tests failed. Review the report."
    }
}
