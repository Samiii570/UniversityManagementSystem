package com.kuet.ums.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UmsIntegrationTest {
    @Autowired
    MockMvc mvc;

    private static final String TEACHER = """
            {"name":"Dr. Rahman","email":"rahman@kuet.ac.bd","username":"teacher1",
             "password":"teach123","department":"CSE","designation":"Professor"}""";
    private static final String STUDENT = """
            {"name":"Prottoy","email":"prottoy@stud.kuet.ac.bd","username":"student1",
             "password":"stud123","roll":"2207040","department":"CSE","batch":2022}""";

    @Test
    void adminCrudReturnsProperStatusCodes() throws Exception {
        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        mvc.perform(get("/api/students").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/students/1").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STUDENT.replace("Prottoy", "Prottoy Kumar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Prottoy Kumar"));

        mvc.perform(delete("/api/students/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/students/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void eachRoleCanAccessOnlyItsOwnResources() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admins").with(httpBasic("admin", "wrong")))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/teachers").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(TEACHER))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/teachers").with(httpBasic("teacher1", "teach123")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/students").with(httpBasic("teacher1", "teach123")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admins").with(httpBasic("teacher1", "teach123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/students").with(httpBasic("student1", "stud123")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/teachers").with(httpBasic("student1", "stud123")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admins").with(httpBasic("student1", "stud123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admins").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void loginCreatesSessionAndLogoutEndsIt() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());

        MockHttpSession session = (MockHttpSession) mvc.perform(post("/api/auth/login")
                        .param("username", "admin").param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andReturn().getRequest().getSession();

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));

        mvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
