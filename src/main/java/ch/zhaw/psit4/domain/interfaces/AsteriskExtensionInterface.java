package ch.zhaw.psit4.domain.interfaces;

/**
 * Implementations provide a valid Asterisk dialplan extension configuration.
 *
 * @author Rafael Ostertag
 */
public interface AsteriskExtensionInterface extends Validatable {

    /**
     * Convert one Astersik dialplan extension configuration to a valid Asterisk dialplan extension configuration
     * fragment.
     * <p>
     * Implementations must guarantee, that the Asterisk dialplan extension configuration returned is terminated by a
     * newline character. This implicitly holds when implementations use DialPlanAppInterface to compose the fragment.
     * The fragment must not contain any other newline characters.
     * <p>
     * Implementations must guarantee, that multiple dialplan extension configurations strings can be used to compose
     * a valid Asterisk dialplan context configuration.
     *
     * @return String representing a valid Astersik dialplan extension configuration directive, terminated by a newline
     * character.
     */
    String toDialPlanExtensionConfiguration();

    /**
     * Ordinal of the Extension. This number is used to determine the order of extensions within a context.
     *
     * @return integer greater than 0.
     */
    int getOrdinal();

    /**
     * Get the Asterisk priority.
     */
    String getPriority();

    /**
     * Set the Asterisk priority.
     *
     * @param priority numerals such as '1', '2', etc. or 'n'
     */
    void setPriority(String priority);

    /**
     * Get the DialPlanApp.
     */
    AsteriskApplicationInterface getDialPlanApplication();

    /**
     * Get extension (phone)number.
     */
    String getPhoneNumber();
}