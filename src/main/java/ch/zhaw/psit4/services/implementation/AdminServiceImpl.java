package ch.zhaw.psit4.services.implementation;

import ch.zhaw.psit4.data.jpa.entities.Admin;
import ch.zhaw.psit4.data.jpa.entities.Company;
import ch.zhaw.psit4.data.jpa.repositories.AdminRepository;
import ch.zhaw.psit4.data.jpa.repositories.CompanyRepository;
import ch.zhaw.psit4.dto.AdminDto;
import ch.zhaw.psit4.dto.AdminWithPasswordDto;
import ch.zhaw.psit4.dto.CompanyDto;
import ch.zhaw.psit4.services.exceptions.*;
import ch.zhaw.psit4.services.interfaces.AdminServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ch.zhaw.psit4.services.implementation.CompanyServiceImpl.companyDtosToCompanyEntitiesWithId;

/**
 * @author Jona Braun
 */
@Service
public class AdminServiceImpl implements AdminServiceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceInterface.class);
    private AdminRepository adminRepository;
    private CompanyRepository companyRepository;

    public AdminServiceImpl(AdminRepository adminRepository, CompanyRepository companyRepository) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
    }

    public static AdminDto adminEntityToAdminDto(Admin admin) {
        AdminDto adminDto = new AdminDto();
        adminDto.setId(admin.getId());
        adminDto.setFirstName(admin.getFirstname());
        adminDto.setLastName(admin.getLastname());
        adminDto.setUserName(admin.getUsername());

        adminDto.setCompanyDtoList(
                CompanyServiceImpl.companyEntitiesToCompanyDtos(new ArrayList<>(admin.getCompany()))
        );

        return adminDto;
    }

    public static Admin adminDtoToAdminEntity(AdminWithPasswordDto adminDto) {
        return new Admin(companyDtosToCompanyEntitiesWithId(adminDto.getCompanyDtoList()),
                adminDto.getFirstName(),
                adminDto.getLastName(),
                adminDto.getUserName(),
                adminDto.getPassword(),
                false);
    }


    @Override
    public List<AdminDto> getAllAdmins() {
        List<AdminDto> adminDtos = new ArrayList<>();
        for (Admin admin : adminRepository.findAllBySuperAdminIsFalse()) {
            adminDtos.add(adminEntityToAdminDto(admin));
        }
        return adminDtos;
    }

    @Override
    public AdminDto createAdmin(AdminWithPasswordDto newAdmin) {
        try {
            Admin adminEntity = adminDtoToAdminEntity(newAdmin);
            adminEntity = adminRepository.save(adminEntity);
            return adminEntityToAdminDto(adminEntity);
        } catch (Exception e) {
            String message = "Could not create admin";
            LOGGER.error(message, e);
            throw new AdminCreationException(message, e);
        }
    }

    @Override
    public AdminDto updateAdmin(AdminDto adminDto) {
        try {
            Admin existingAdmin = adminRepository.findFirstByIdAndSuperAdminIsFalse(adminDto.getId());
            if (existingAdmin == null) {
                throw new AdminRetrievalException(String.format("Could not find admin with id %d", adminDto.getId()));
            }

            existingAdmin.setCompany(retrieveCompaniesByCompanyDtos(adminDto.getCompanyDtoList()));

            existingAdmin.setFirstname(adminDto.getFirstName());
            existingAdmin.setLastname(adminDto.getLastName());
            existingAdmin.setUsername(adminDto.getUserName());
            existingAdmin.setSuperAdmin(false);

            existingAdmin = adminRepository.save(existingAdmin);
            return adminEntityToAdminDto(existingAdmin);
        } catch (Exception e) {
            String message = String.format("Could not update admin with id %d", adminDto.getId());
            LOGGER.error(message, e);
            throw new AdminUpdateException(message, e);
        }
    }

    @Override
    public AdminDto getAdmin(long id) {
        Admin existingAdmin = adminRepository.findFirstByIdAndSuperAdminIsFalse(id);
        if (existingAdmin == null) {
            String message = String.format("Could not find admin with id %d", id);
            LOGGER.error(message);
            throw new AdminRetrievalException(message);
        }
        return adminEntityToAdminDto(existingAdmin);
    }

    @Override
    public void deleteAdmin(long id) {
        try {
            Admin existingAdmin = adminRepository.findFirstByIdAndSuperAdminIsFalse(id);
            adminRepository.delete(existingAdmin);
        } catch (Exception e) {
            String message = String.format("Could not delete admin with id %d", id);
            LOGGER.error(message, e);
            throw new AdminDeletionException(message, e);
        }
    }

    private List<Company> retrieveCompaniesByCompanyDtos(List<CompanyDto> companyDtos) {
        if (companyDtos == null) {
            return Collections.emptyList();
        }

        List<Long> companyIds = companyDtos
                .stream()
                .map(CompanyDto::getId)
                .collect(Collectors.toList());

        List<Company> companies = companyRepository.idIsIn(companyIds);
        assert companies != null;

        if (companyIds.size() != companies.size()) {
            // We have a simple message, intended for the exception which might leave the server, thus leaking
            // information and an exhaustive message intended for the log on the server.
            String simpleMessage = "Could not retrieve all companies specified in Dto List";

            String dtoIdList = companyDtos.stream().reduce(
                    "",
                    (a, b) -> a.isEmpty() ? a + b.getId() : a + "," + b.getId(),
                    (a, b) -> a + b
            );

            String foundCompanies = companies.stream().reduce(
                    "",
                    (a, b) -> a.isEmpty() ? a + b.getId() : a + "," + b.getId(),
                    (a, b) -> a + b
            );

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("%s: DTO IDs %s, found IDs %s", simpleMessage, dtoIdList, foundCompanies));
            }
            throw new CompanyRetrievalException(simpleMessage);
        }

        return companies;
    }
}
