package ch.zhaw.psit4.domain.DialPlan.interfaces;

/**
 * Represents a dial plan application in asterisk.
 *
 * @author Jona Braun
 */
public interface DialPlanApplication {

    /**
     * Puts together the asterisk application call.
     *
     * @return the string representing the asterisk application calls
     */
    String getApplicationCall();
}
