package ch.zhaw.psit4.domain.helper;

import ch.zhaw.psit4.data.jpa.entities.Company;
import ch.zhaw.psit4.domain.sipclient.SipClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for testing the domain specific sip client configuration.
 *
 * @author Jona Braun
 */
public class SipClientTestHelper {
    private static final String USERNAME = "Name";
    private static final String PHONE = "Phone";
    private static final String SECRET = "Secret";
    private static final String COMPANY = "acme";

    /**
     * Creates a jpa sip client
     *
     * @param number  the number for the phone and appended to "User" and "Secret"
     * @param company the company belonging the sip client
     * @return the jpa sip client
     */
    public ch.zhaw.psit4.data.jpa.entities.SipClient createSipClientEntity(int number, Company company) {
        return new ch.zhaw.psit4.data.jpa.entities.SipClient(company, USERNAME + number,
                PHONE + number, SECRET + number);
    }

    /**
     * Generates a sipClientConfig with the company prefix "acme".
     *
     * @param numberOfCompanies number of Companies in the sip config
     * @param numberOfClients   number of clients per company
     * @return the generated config
     */
    public String generateSipClientConfig(int numberOfCompanies, int numberOfClients) {
        String sipClientConfig = "";
        for (int i = 1; i <= numberOfCompanies; i++) {
            sipClientConfig += generateSipClientConfig(numberOfClients, COMPANY + i);
        }
        return sipClientConfig;
    }

    /**
     * Generates the expected config string of sip clients.
     *
     * @param number  the number of sip clients the config should be created
     * @param company the company the of the sip clients
     * @return the generated config
     */
    public String generateSipClientConfig(int number, String company) {
        String config = "";
        for (int i = 1; i <= number; i++) {
            config += "[" + USERNAME + i + "-" + company.replaceAll(" ", "-") + "]\n" +
                    "type=friend\n" +
                    "context=" + company.replaceAll(" ", "-") + "\n" +
                    "host=dynamic\n" +
                    "secret=" + SECRET + i + "\n\n";
        }
        return config;
    }

    /**
     * Generates a list of domain specific sip clients.
     *
     * @param number  the sip clients to create
     * @param company the company of the sip clients
     * @return the generated sip client list
     */
    public List<SipClient> generateSipClientList(int number, String company) {
        List<SipClient> sipClientList = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            SipClient sipClient = generateSipClient(i, company);
            sipClientList.add(sipClient);
        }
        return sipClientList;
    }

    /**
     * Generates one domain specific sip client.
     *
     * @param number  the number which is appended to the "User" and the "Secret" of the sip client.
     * @param company the company for the sip clients
     * @return the generated sip client
     */
    public SipClient generateSipClient(int number, String company) {
        SipClient sipClient = new SipClient();
        sipClient.setCompany(company);
        sipClient.setUsername(USERNAME + number);
        sipClient.setSecret(SECRET + number);
        sipClient.setPhoneNumber(PHONE + number);
        return sipClient;
    }
}