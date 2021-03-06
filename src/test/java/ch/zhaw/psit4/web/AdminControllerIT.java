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

package ch.zhaw.psit4.web;

import ch.zhaw.psit4.dto.AdminDto;
import ch.zhaw.psit4.dto.PasswordOnlyDto;
import ch.zhaw.psit4.services.implementation.AdminServiceImpl;
import ch.zhaw.psit4.testsupport.convenience.Json;
import ch.zhaw.psit4.testsupport.fixtures.database.BeanConfiguration;
import ch.zhaw.psit4.testsupport.fixtures.database.DatabaseFixtureBuilder;
import ch.zhaw.psit4.testsupport.fixtures.dto.AdminDtoGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static ch.zhaw.psit4.testsupport.matchers.AdminDtoEqualTo.adminDtoEqualTo;
import static ch.zhaw.psit4.testsupport.matchers.AdminDtoPartialMatcher.adminDtoAlmostEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jona Braun
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import(BeanConfiguration.class)
public class AdminControllerIT {
    private static final String V1_ADMINS_PATH = "/v1/admins";
    private static final int NON_EXISTING_ADMIN_ID = 100;

    @Autowired
    private WebApplicationContext wac;

    private DatabaseFixtureBuilder databaseFixtureBuilder1;
    private DatabaseFixtureBuilder databaseFixtureBuilder2;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        databaseFixtureBuilder1 = wac.getBean(DatabaseFixtureBuilder.class);
        databaseFixtureBuilder2 = wac.getBean(DatabaseFixtureBuilder.class);
    }

    @Test
    public void getAllAdminsEmpty() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.length()").value(equalTo(0))
        );
    }

    @Test
    public void getNonExistingAdmin() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH + "/{id}", NON_EXISTING_ADMIN_ID)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isNotFound()
        ).andExpect(
                jsonPath("$.reason").value(equalTo("Could not find admin with id " + NON_EXISTING_ADMIN_ID))
        );
    }

    @Test
    public void updateNonExistingAdmin() throws Exception {
        databaseFixtureBuilder1.addCompany(1).addCompany(2).build();
        AdminDto adminDto = AdminDtoGenerator.createAdminDto(databaseFixtureBuilder1.getCompanyList().values(), 1);

        mockMvc.perform(
                MockMvcRequestBuilders.put(V1_ADMINS_PATH + "/{id}", NON_EXISTING_ADMIN_ID)
                        .content(Json.toJson(adminDto))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isBadRequest()
        );
    }

    @Test
    public void createInvalidAdmin() throws Exception {
        AdminDto companyDto = new AdminDto();
        mockMvc.perform(
                MockMvcRequestBuilders.post(V1_ADMINS_PATH)
                        .content(Json.toJson(companyDto))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isBadRequest()
        );
    }

    @Test
    public void createAdmin() throws Exception {
        databaseFixtureBuilder1.addCompany(1).addCompany(2).build();
        AdminDto adminDto = AdminDtoGenerator.createAdminDto(databaseFixtureBuilder1.getCompanyList().values(), 1);

        String creationResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(V1_ADMINS_PATH)
                        .content(Json.toJson(adminDto))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isCreated()
        ).andExpect(
                jsonPath("$.id").value(not(equalTo(adminDto.getId())))
        ).andExpect(
                jsonPath("$.firstName").value(equalTo(adminDto.getFirstName()))
        ).andExpect(
                jsonPath("$.lastName").value(equalTo(adminDto.getLastName()))
        ).andExpect(
                jsonPath("$.userName").value(equalTo(adminDto.getUserName()))
        ).andExpect(
                jsonPath("$.password").doesNotExist()
        ).andReturn().getResponse().getContentAsString();

        AdminDto createdAdminDto = Json.toObjectTypeSafe(creationResponse, AdminDto.class);

        String response = mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH + "/{id}", createdAdminDto.getId())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isOk()
        ).andReturn().getResponse().getContentAsString();
        AdminDto actual = Json.toObjectTypeSafe(response, AdminDto.class);

        assertThat(adminDto, adminDtoAlmostEqualTo(actual));
    }

    @Test
    public void getAllAdmins() throws Exception {
        databaseFixtureBuilder1.addCompany(1).addCompany(2).addAdministrator(1).build();
        databaseFixtureBuilder2.addCompany(3).addAdministrator(2).build();

        String response = mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.length()").value(equalTo(2))
        ).andReturn().getResponse().getContentAsString();

        AdminDto createdAdminDto1 = AdminServiceImpl.adminEntityToAdminDto(
                databaseFixtureBuilder1.getAdminList().get(1)
        );

        AdminDto createdAdminDto2 = AdminServiceImpl.adminEntityToAdminDto(
                databaseFixtureBuilder2.getAdminList().get(2)
        );

        AdminDto[] actual = Json.toObjectTypeSafe(response, AdminDto[].class);

        assertThat(actual, arrayContainingInAnyOrder(
                adminDtoEqualTo(createdAdminDto1),
                adminDtoEqualTo(createdAdminDto2)
        ));


    }

    @Test
    public void updateAdmin() throws Exception {
        databaseFixtureBuilder1.addCompany(1).addCompany(2).addAdministrator(1).build();
        AdminDto existingAdmin = AdminServiceImpl.adminEntityToAdminDto(
                databaseFixtureBuilder1.getAdminList().get(1)
        );

        AdminDto updatedCompany = AdminDtoGenerator.createAdminDto(existingAdmin.getCompanyDtoList(), 2);
        updatedCompany.setId(existingAdmin.getId());

        String putResult = mockMvc.perform(
                MockMvcRequestBuilders.put(V1_ADMINS_PATH + "/{id}", existingAdmin.getId())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(Json.toJson(updatedCompany))
        ).andExpect(
                status().isOk()
        ).andReturn().getResponse().getContentAsString();

        AdminDto actual = Json.toObjectTypeSafe(putResult, AdminDto.class);
        assertThat(actual, adminDtoEqualTo(updatedCompany));

        String response = mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH + "/{id}", existingAdmin.getId())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.password").doesNotExist()
        ).andReturn().getResponse().getContentAsString();

        actual = Json.toObjectTypeSafe(response, AdminDto.class);

        assertThat(actual, adminDtoEqualTo(updatedCompany));
    }

    @Test
    public void deleteAdmin() throws Exception {

        databaseFixtureBuilder1.addCompany(1).addCompany(2).addAdministrator(1).build();
        AdminDto existingAdmin = AdminServiceImpl.adminEntityToAdminDto(
                databaseFixtureBuilder1.getAdminList().get(1)
        );

        mockMvc.perform(
                MockMvcRequestBuilders.delete(V1_ADMINS_PATH + "/{id}", existingAdmin.getId())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isNoContent()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.get(V1_ADMINS_PATH + "/{id}", existingAdmin.getId())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isNotFound()
        );
    }

    @Test
    public void updatePassword() throws Exception {
        databaseFixtureBuilder1
                .addCompany(1)
                .addCompany(2)
                .addAdministrator(1)
                .build();

        Long adminId = databaseFixtureBuilder1.getAdminList().get(1).getId();
        String initialPassword = databaseFixtureBuilder1.getAdminList().get(1).getPassword();

        PasswordOnlyDto passwordOnlyDto = new PasswordOnlyDto();
        passwordOnlyDto.setPassword("test1234");

        mockMvc.perform(
                MockMvcRequestBuilders.put(V1_ADMINS_PATH + "/{id}/password", adminId)
                        .content(Json.toJson(passwordOnlyDto))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isNoContent()
        );

        assertThat(databaseFixtureBuilder1.getAdminList().get(1).getPassword(), not(equalTo(initialPassword)));
        assertThat(databaseFixtureBuilder1.getAdminList().get(1).getPassword(), startsWith("$2a$"));
    }

    @Test
    public void updateNonExistingPassword() throws Exception {
        databaseFixtureBuilder1
                .addCompany(1)
                .addAdministrator(1)
                .build();

        PasswordOnlyDto passwordOnlyDto = new PasswordOnlyDto();
        passwordOnlyDto.setPassword("test1234");

        mockMvc.perform(
                MockMvcRequestBuilders.put(V1_ADMINS_PATH + "/{id}/password", NON_EXISTING_ADMIN_ID)
                        .content(Json.toJson(passwordOnlyDto))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isBadRequest()
        );
    }

}