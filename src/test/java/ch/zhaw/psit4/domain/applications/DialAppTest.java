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

package ch.zhaw.psit4.domain.applications;

import ch.zhaw.psit4.domain.beans.SipClient;
import ch.zhaw.psit4.domain.exceptions.ValidationException;
import ch.zhaw.psit4.testsupport.fixtures.domain.DialAppCallGenerator;
import ch.zhaw.psit4.testsupport.fixtures.domain.SipClientGenerator;
import ch.zhaw.psit4.testsupport.matchers.SipClientEqualTo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Jona Braun
 */
public class DialAppTest {
    private List<SipClient> sipClientList;

    @Test
    public void factory1() throws Exception {
        SipClient sipClient = SipClientGenerator.getSipClient(1, 1);

        DialApp actual = DialApp.factory(DialApp.Technology.PSIP, sipClient, "1");
        assertThat(actual, is(not(nullValue())));

        assertThat(actual.getTechnology(), equalTo(DialApp.Technology.PSIP));
        assertThat(actual.getTimeout(), equalTo("1"));
        assertThat(actual.getSipClientList(), hasSize(1));
        assertThat(actual.getSipClientList(), hasItem(SipClientEqualTo.sipClientEqualTo(sipClient)));
    }

    @Test
    public void factory() throws Exception {
        SipClient sipClient = SipClientGenerator.getSipClient(1, 1);
        List<SipClient> list = new ArrayList<>();
        list.add(sipClient);

        DialApp actual = DialApp.factory(DialApp.Technology.PSIP, list, "1");
        assertThat(actual, is(not(nullValue())));

        assertThat(actual.getTechnology(), equalTo(DialApp.Technology.PSIP));
        assertThat(actual.getTimeout(), equalTo("1"));
        assertThat(actual.getSipClientList(), hasSize(1));
        assertThat(actual.getSipClientList(), hasItem(SipClientEqualTo.sipClientEqualTo(sipClient)));
    }

    @Test(expected = ValidationException.class)
    public void nullTechnology() throws Exception {
        DialApp dialApp = new DialApp(null,
                SipClientGenerator.generateSipClientList(1, 1),
                "1");
        dialApp.validate();
    }

    @Test(expected = ValidationException.class)
    public void nullSipClientList() throws Exception {
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP,
                null,
                "1");
        dialApp.validate();
    }

    @Test(expected = ValidationException.class)
    public void emptySipClientList() throws Exception {
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP,
                new ArrayList<>(),
                "1");
        dialApp.validate();
    }

    @Test(expected = ValidationException.class)
    public void nullTimeout() throws Exception {
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP,
                SipClientGenerator.generateSipClientList(1, 1),
                null);
        dialApp.validate();
    }

    @Test(expected = ValidationException.class)
    public void emptyTimeout() throws Exception {
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP,
                SipClientGenerator.generateSipClientList(1, 1),
                "");
        dialApp.validate();
    }

    @Test
    public void validate() throws Exception {
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP,
                SipClientGenerator.generateSipClientList(1, 1),
                "1");
        dialApp.validate();
    }

    @Test
    public void getApplicationCallOneSIPClient() throws Exception {

        sipClientList = SipClientGenerator.generateSipClientList(1, 1);

        DialApp dialApp = new DialApp(DialApp.Technology.SIP, sipClientList, DialAppCallGenerator.TIMEOUT);

        String dialAppCall = dialApp.toApplicationCall();
        String expected = DialAppCallGenerator.generateMultipleDialAppCalls(1, 1, "SIP");

        assertEquals(expected, dialAppCall);
    }

    @Test
    public void getApplicationCallMultipleSIPClients() throws Exception {

        sipClientList = SipClientGenerator.generateSipClientList(5, 1);

        DialApp dialApp = new DialApp(DialApp.Technology.SIP, sipClientList, DialAppCallGenerator.TIMEOUT);

        String dialAppCall = dialApp.toApplicationCall();
        String expected = DialAppCallGenerator.generateMultipleDialAppCalls(5, 1, "SIP");

        assertEquals(expected, dialAppCall);
    }

    @Test
    public void getApplicationCallMultiplePSIPClients() throws Exception {

        sipClientList = SipClientGenerator.generateSipClientList(2, 1);

        DialApp dialApp = new DialApp(DialApp.Technology.PSIP, sipClientList, DialAppCallGenerator.TIMEOUT);

        String dialAppCall = dialApp.toApplicationCall();
        String expected = DialAppCallGenerator.generateMultipleDialAppCalls(2, 1, "PSIP");

        assertEquals(expected, dialAppCall);
    }

    @Test
    public void requireAnswer() throws Exception {
        sipClientList = SipClientGenerator.generateSipClientList(1, 1);
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP, sipClientList, DialAppCallGenerator.TIMEOUT);

        assertThat(dialApp.requireAnswer(), equalTo(false));
    }


    @Test
    public void requireWaitExten() throws Exception {
        sipClientList = SipClientGenerator.generateSipClientList(1, 1);
        DialApp dialApp = new DialApp(DialApp.Technology.PSIP, sipClientList, DialAppCallGenerator.TIMEOUT);

        assertThat(dialApp.requireWaitExten(), equalTo(false));
    }
}