package ch.zhaw.psit4.testsupport.fixtures.domain;

import ch.zhaw.psit4.domain.sipclient.SipClient;
import ch.zhaw.psit4.testsupport.fixtures.general.SipClientData;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for testing the domain specific sip client configuration.
 *
 * @author Jona Braun
 */
public class SipClientGenerator {

    /**
     * Create a domain sip client. The id will be 0 and has to be set by the caller.
     *
     * @param company name of the company
     * @param number  number of sip client
     * @return domain SipClient instance.
     */
    public static SipClient getSipClientDomain(String company, int number) {
        SipClient sipClient = new SipClient();
        sipClient.setCompany(company);
        sipClient.setPhoneNumber(SipClientData.getSipClientPhoneNumber(number));
        sipClient.setSecret(SipClientData.getSipClientSecret(number));
        sipClient.setUsername(SipClientData.getSipClientLabel(number));
        return sipClient;
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
            SipClient sipClient = getSipClientDomain(company, i);
            sipClientList.add(sipClient);
        }
        return sipClientList;
    }


}
