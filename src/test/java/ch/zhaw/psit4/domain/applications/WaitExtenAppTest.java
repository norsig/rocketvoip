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

import ch.zhaw.psit4.domain.exceptions.ValidationException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Rafael Ostertag
 */
public class WaitExtenAppTest {
    WaitExtenApp waitExtenApp;

    @Before
    public void setUp() throws Exception {
        waitExtenApp = new WaitExtenApp(1);
    }

    @Test(expected = ValidationException.class)
    public void validateZeroSeconds() throws Exception {
        WaitExtenApp waitExtenApp = new WaitExtenApp(0);
        waitExtenApp.validate();
    }

    @Test(expected = ValidationException.class)
    public void validateNegativeSeconds() throws Exception {
        WaitExtenApp waitExtenApp = new WaitExtenApp(-1);
        waitExtenApp.validate();
    }

    @Test
    public void validate() throws Exception {
        waitExtenApp.validate();
    }

    @Test
    public void toApplicationCall() throws Exception {
        assertThat(waitExtenApp.toApplicationCall(), equalTo("WaitExten(1)"));
    }

    @Test
    public void requireAnswer() throws Exception {
        assertThat(waitExtenApp.requireAnswer(), equalTo(true));
    }

    @Test
    public void requireWaitExten() throws Exception {
        assertThat(waitExtenApp.requireWaitExten(), equalTo(false));
    }
}