package lt.creditco.cupa.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import lombok.extern.slf4j.Slf4j;

@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
class LegacySecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testFilterForwardsToIndex() throws Exception {
        log.info("testFilterForwardsToIndex. ServletName: {}", mockMvc.getDispatcherServlet().getServletName());
        mockMvc.perform(get("/")).andExpect(status().isFound()).andExpect(redirectedUrl("/ui/"));
    }

    @Test
    void shouldRedirectNotExistingPathToVaadinLoginPage() throws Exception {
        // Root should redirect to Vaadin UI
        mockMvc.perform(get("/not-existing-path")).andExpect(status().isFound()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void apiAuthenticateIsUnathorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void userShouldNotBeRedirectedToLoginForApiAuthenticate() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void testFilterDoesNotForwardAdminToIndexForV3ApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void testFilterDoesNotForwardUserToIndexForV3ApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }

    @Test
    void fileRequestRedicredtedToLoginPageForAnonymousUser() throws Exception {
        mockMvc.perform(get("/file.js")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void deepFileRequestRedirectedToLoginPageForAnonymousUser() throws Exception {
        mockMvc.perform(get("/ui/some-path/file.js")).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void getBackendEndpoint() throws Exception {
        mockMvc.perform(get("/test")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void forwardUnmappedFirstLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void forwardUnmappedSecondLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level/second-level")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void forwardUnmappedThirdLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level/second-level/third-level")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void forwardUnmappedDeepMapping() throws Exception {
        mockMvc.perform(get("/1/2/3/4/5/6/7/8/9/10")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void getUnmappedFirstLevelFile() throws Exception {
        mockMvc.perform(get("/foo.js")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    /**
     * This test verifies that any files that aren't permitted by Spring Security will be forbidden.
     * If you want to change this to return isNotFound(), you need to add a request mapping that
     * allows this file in SecurityConfiguration.
     */
    @Test
    void getUnmappedSecondLevelFile() throws Exception {
        mockMvc.perform(get("/foo/bar.js")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void getUnmappedThirdLevelFile() throws Exception {
        mockMvc.perform(get("/foo/another/bar.js")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }
}
