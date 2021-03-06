/*
 * Copyright 2017 Jona Braun, Benedikt Herzog, Rafael Ostertag,
 *                Marcel Schöni, Marco Studerus, Martin Wittwer
 *
 * Redistribution and  use in  source and binary  forms, with  or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions  of  source code  must retain  the above  copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in  binary form must reproduce  the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation   and/or   other    materials   provided   with   the
 *    distribution.
 *
 * THIS SOFTWARE  IS PROVIDED BY  THE COPYRIGHT HOLDERS  AND CONTRIBUTORS
 * "AS  IS" AND  ANY EXPRESS  OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES  OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE  ARE DISCLAIMED. IN NO EVENT  SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL DAMAGES  (INCLUDING,  BUT  NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS  INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF  LIABILITY, WHETHER IN  CONTRACT, STRICT LIABILITY,  OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN  ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.zhaw.psit4.services.interfaces;

import ch.zhaw.psit4.dto.CompanyDto;
import ch.zhaw.psit4.services.exceptions.CompanyCreationException;
import ch.zhaw.psit4.services.exceptions.CompanyDeletionException;
import ch.zhaw.psit4.services.exceptions.CompanyRetrievalException;
import ch.zhaw.psit4.services.exceptions.CompanyUpdateException;

import java.util.List;

/**
 * Service handling companies.
 *
 * @author Jona Braun
 */
public interface CompanyServiceInterface {

    /**
     * Retrieves all companies form the data storage.
     *
     * @return all companies
     * @throws CompanyRetrievalException Implementations are expected to throw this exception on error.
     */
    List<CompanyDto> getAllCompanies();

    /**
     * Retrieves companies by id.
     *
     * @param ids list of company IDs
     * @return list of all Company, or an empty list if no companies are found
     * @throws CompanyRetrievalException Implementations are expected to throw this exception on error.
     */
    List<CompanyDto> getCompaniesById(List<Long> ids);

    /**
     * Creates a new company. The {$code id} attribute of {$code newCompany} will be ignored if set.
     *
     * @param newCompany the company to be created
     * @return The created company with the the unique id.
     * @throws CompanyCreationException Implementations are expected to throw this exception on error.
     */
    CompanyDto createCompany(CompanyDto newCompany);

    /**
     * Updates an existing company.
     *
     * @param companyDto the company to update
     * @return the updated company
     * @throws CompanyUpdateException Implementations are expected to throw this exception on error.
     */
    CompanyDto updateCompany(CompanyDto companyDto);

    /**
     * Retrieves a Company by id.
     *
     * @param id the id of the company to retrieve
     * @return the company
     * @throws CompanyRetrievalException Implementations are expected to throw this exception on error.
     */
    CompanyDto getCompany(long id);

    /**
     * Deletes a Company by id.
     *
     * @param id the id of the Company to delete.
     * @throws CompanyDeletionException Implementations are expected to throw this exception on error.
     */
    void deleteCompany(long id);

}
