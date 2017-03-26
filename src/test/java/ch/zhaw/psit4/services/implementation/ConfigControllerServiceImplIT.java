package ch.zhaw.psit4.services.implementation;

import ch.zhaw.psit4.data.jpa.entities.Company;
import ch.zhaw.psit4.data.jpa.repositories.CompanyRepository;
import ch.zhaw.psit4.data.jpa.repositories.SipClientRepository;
import ch.zhaw.psit4.domain.exceptions.InvalidConfigurationException;
import ch.zhaw.psit4.domain.helper.DialPlanTestHelper;
import ch.zhaw.psit4.domain.helper.SipClientTestHelper;
import ch.zhaw.psit4.helper.ZipStreamTestHelper;
import ch.zhaw.psit4.services.interfaces.ConfigControllerServiceInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipInputStream;

/**
 * @author Jona Braun
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ConfigControllerServiceImplIT {

    private static final String COMPANY = "acme";
    private final ZipStreamTestHelper zipStreamTestHelper = new ZipStreamTestHelper();
    private final SipClientTestHelper sipClientTestHelper = new SipClientTestHelper();
    private DialPlanTestHelper dialPlanTestHelper = new DialPlanTestHelper();

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SipClientRepository sipClientRepository;

    @Autowired
    private ConfigControllerServiceInterface configControllerServiceInterface;

    @Test(expected = InvalidConfigurationException.class)
    public void getAsteriskConfigurationWithNoSipClients() throws Exception {
        configControllerServiceInterface.getAsteriskConfiguration();
    }

    @Test
    public void getAsteriskConfigurationWithOneSipClient() throws Exception {
        setupDatabase(1);

        ByteArrayOutputStream baos = configControllerServiceInterface.getAsteriskConfiguration();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));

        String[] expectedNames = {"sip.conf", "extension.conf"};
        String[] expectedContent = {sipClientTestHelper.generateSipClientConfig(1, COMPANY),
                dialPlanTestHelper.getSimpleDialPlan(1)};

        zipStreamTestHelper.testZipEntryContent(zipInputStream, expectedNames, expectedContent);

    }

    private void setupDatabase(int number) {
        Company company = new Company(COMPANY);
        companyRepository.save(company);
        for (int i = 1; i <= number; i++) {
            sipClientRepository.save(sipClientTestHelper.createSipClientEntity(i, company));
        }
    }

}