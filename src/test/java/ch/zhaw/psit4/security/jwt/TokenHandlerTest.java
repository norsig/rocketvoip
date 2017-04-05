package ch.zhaw.psit4.security.jwt;

import ch.zhaw.psit4.data.jpa.entities.Admin;
import ch.zhaw.psit4.security.auxiliary.AdminDetails;
import ch.zhaw.psit4.security.jwt.mocks.UserDetailsServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static ch.zhaw.psit4.helper.matchers.AdminDetailsEqualTo.adminDetailsEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * @author Rafael Ostertag
 */
public class TokenHandlerTest {
    private TokenHandler tokenHandler;
    private UserDetailsService userDetailsServiceMock;
    private Admin admin;

    @Before
    public void setUp() throws Exception {
        admin = new Admin(null, "testfirstname", "testlastname", "test", "testpw", false);
        userDetailsServiceMock = UserDetailsServiceMock.makeMockForAdmin(admin);
        tokenHandler = new TokenHandler("testsecret", userDetailsServiceMock);
    }

    @Test
    public void parseUserFromToken() throws Exception {
        AdminDetails adminDetails = new AdminDetails(admin);
        String token = tokenHandler.createTokenForUser(adminDetails);

        UserDetails actual = tokenHandler.parseUserFromToken(token);

        assertThat((AdminDetails) actual, adminDetailsEqualTo(adminDetails));
        verify(userDetailsServiceMock, atLeastOnce()).loadUserByUsername(adminDetails.getUsername());
    }
}