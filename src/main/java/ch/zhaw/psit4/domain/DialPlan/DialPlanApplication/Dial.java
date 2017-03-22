package ch.zhaw.psit4.domain.DialPlan.DialPlanApplication;

import ch.zhaw.psit4.domain.DialPlan.interfaces.DialPlanApplication;
import ch.zhaw.psit4.domain.SipClient.SipClient;

import java.util.List;

/**
 * @author Jona Braun
 */
public class Dial implements DialPlanApplication {

    private Technology technology;
    private List<SipClient> sipClientList;
    private String timeout;


    /**
     * @param technology    the chanel driver (SIP, PSIP)
     * @param sipClientList the clients to call/dial
     * @param timeout       the number of seconds it is attempt to dial the specified clients
     */
    public Dial(Technology technology, List<SipClient> sipClientList, String timeout) {
        this.technology = technology;
        this.sipClientList = sipClientList;
        this.timeout = timeout;
    }


    /**
     * Puts together the asterisk application call.
     *
     * @return the asterisk application call
     */
    @Override
    public String getApplicationCall() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Dial(");
        for (int index = 1; index <= sipClientList.size(); index++) {

            SipClient sipClient = sipClientList.get(index - 1);

            if (technology == Technology.SIP) {
                stringBuilder.append("SIP/");
            } else if (technology == Technology.PSIP) {
                stringBuilder.append("PSIP/");
            }

            //TODO add String label to SipClient
            stringBuilder.append(sipClient.getUsername() + "-" + sipClient.getCompany());

            if (sipClientList.size() > index) {
                stringBuilder.append("&");
            }
        }
        stringBuilder.append(", ");
        stringBuilder.append(timeout);

        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public enum Technology {SIP, PSIP}
}
