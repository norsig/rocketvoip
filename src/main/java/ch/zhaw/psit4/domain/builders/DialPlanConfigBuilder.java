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

package ch.zhaw.psit4.domain.builders;

import ch.zhaw.psit4.domain.beans.DialPlanContext;
import ch.zhaw.psit4.domain.beans.DialPlanExtension;
import ch.zhaw.psit4.domain.exceptions.InvalidConfigurationException;
import ch.zhaw.psit4.domain.exceptions.ValidationException;
import ch.zhaw.psit4.domain.interfaces.AsteriskApplicationInterface;
import ch.zhaw.psit4.domain.interfaces.AsteriskExtensionInterface;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Build a DialPlan suitable for ConfigWriter using a fluent API.
 * <p>
 * Please note that the builder modifies the instances passed. Do not modify the instances outside the builder once
 * they have been passed to the builder.
 *
 * Extensions in a context will have the priorities set to "1", "n", "n", ... using this builder. For instance
 *
 * <pre>
 *     [context1]
 *     exten => nr,1,...
 *     exten => nr,n,...
 *     exten => nr,n,...
 * </pre>
 * Priority numbering restarts from {@code 1} if the extension number changes within a context, for instance
 * <pre>
 *     [context]
 *     exten => 1,1,...
 *     exten => 1,n,...
 *     exten => 1,n,...
 *     ; Here, a new extension number is used and thus the priority numbering restarts from 1
 *     exten => 2,1,...
 *     exten => 2,n,...
 * </pre>
 * <p>
 * You most likely don't want to use this builder. Use specialized builders derived from it.
 *
 * @author Rafael Ostertag
 */
public class DialPlanConfigBuilder {
    public static final int USER_EXTENSION_ORDINAL_FACTOR = 100;
    public static final String PRIORITY_ONE = "1";
    private List<ContextWrapper> contexts;
    private ContextWrapper activeContext;
    private DialPlanExtension activeExtension;
    private boolean contextReactivated;

    public DialPlanConfigBuilder() {
        contexts = new LinkedList<>();

        // Will be used during build. Initial state has to be null.
        activeContext = null;
        // Will be used during build. Initial state has to be null.
        activeExtension = null;
        contextReactivated = false;
    }

    /**
     * Initialize with an existing builder.
     *
     * It will call {@code build()} on {@code dialPlanConfigBuilder}.
     *
     * @param dialPlanConfigBuilder existing DialPlanConfigBuilder
     */
    public DialPlanConfigBuilder(DialPlanConfigBuilder dialPlanConfigBuilder) {
        this();

        dialPlanConfigBuilder.build();
        contexts = dialPlanConfigBuilder.getContexts();
    }

    protected List<ContextWrapper> getContexts() {
        return contexts;
    }

    protected ContextWrapper getActiveContext() {
        return activeContext;
    }

    /**
     * Add a new Dialplan context.
     * <p>
     * Please note, that this method will set an empty dial plan extension list on the provided context, discarding
     * any extension added to the context by the caller
     *
     * @param context the new dialplan context.
     * @return the DialPlanConfigBuilder.
     * @throws InvalidConfigurationException when configuration is invalid.
     * @throws ValidationException           when validation of context, extension or app fails.
     */
    public DialPlanConfigBuilder addNewContext(DialPlanContext context) {
        if (context == null) {
            throw new InvalidConfigurationException("context must not be null");
        }

        if (activeContext != null) {
            saveActiveContext();
        }

        assert !contextReactivated;

        activeContext = new ContextWrapper(context);
        activeContext.getDialPlanContext().setDialPlanExtensionList(new ArrayList<>());

        return this;
    }

    /**
     * Activate a previously stored context. It will save the active context only if the context name was found.
     *
     * @param contextName name of the previously stored context
     * @return DialPlanConfigBuilder instance
     * @throws IllegalArgumentException      when contextName can't be found
     * @throws InvalidConfigurationException when configuration is invalid.
     * @throws ValidationException           when validation of context, extension or app fails.
     */
    public DialPlanConfigBuilder activateExistingContext(String contextName) {
        ContextWrapper needle = null;
        for (ContextWrapper context : contexts) {
            if (context.getDialPlanContext().getContextName().equals(contextName)) {
                needle = context;
            }
        }

        if (needle == null) {
            throw new IllegalArgumentException("Cannot find context '" + contextName + "' in contexts");
        }

        if (activeContext != null) {
            saveActiveContext();
        }

        activeContext = needle;
        contextReactivated = true;

        return this;
    }

    /**
     * Add a new dialplan extension to the active dialplan context.
     * <p>
     * You may not call this method twice or more in a row.
     * <p>
     *     This method will alter the ordinal of the extension. This is required to guarantee internal consistency of
     *     a given context.
     * </p>
     * It will also unconditionally modify the priority of the DialPlanExtension. It is thus not necessary for the
     * caller to set the priority of the DialPlanExtension.
     *
     * @param extension the new extension context
     * @return DialPlanConfigBuilder.
     * @throws InvalidConfigurationException when extension is null
     * @throws IllegalStateException         when no active context exists
     * @throws ValidationException           when validation of extension or app fails.
     */
    public DialPlanConfigBuilder addNewExtension(DialPlanExtension extension) {
        if (extension == null) {
            throw new InvalidConfigurationException("extension must not be null");
        }

        if (activeContext == null) {
            throw new IllegalStateException("No active context. Did you call addNewContext()?");
        }

        if (activeExtension != null) {
            assignActiveExtensionToActiveContext();
        }

        activeExtension = multiplyOrdinalByUserFactor((DialPlanExtension) setPriorityN(extension));

        return this;
    }

    /**
     * Multiply the ordinal by 100. If the ordinal is zero, it is set to 1 and then multiplied by 100. If the ordinal
     * is negative, take the absolute value and multiply by hundred.
     * <p>
     * We require this, in order to guarantee that we can prepend prologs in front of contexts. Suppose we require
     * two extensions added in front of the user defined extensions. By Asterisk requirements, they must have
     * increasing priorities starting with 1.
     * <p>
     * <pre>
     *     [contextN]
     *     exten => s,1,...
     *     exten => s,n,...
     * </pre>
     * <p>
     * If we allow user supplied ordinals to start with 1, a user might interfere with the prolog. For instance, we
     * might end up with this invalid context
     * <pre>
     *     [contextM]
     *     exten => s,1,...   ; this is a builder provided prolog extension
     *     exten => s,n,...   ; this is a user provided extension
     *     exten => s,2,...   ; this is a builder provided prolog extension
     * </pre>
     * This method prevents such interference by guaranteeing, that user supplied extensions always have an ordinal
     * >= 100.
     *
     * @param extension the extension to modify
     * @return {@code extension}
     */
    private DialPlanExtension multiplyOrdinalByUserFactor(DialPlanExtension extension) {
        if (extension.getOrdinal() == 0) {
            extension.setOrdinal(1);
        }

        if (extension.getOrdinal() < 0) {
            extension.setOrdinal(Math.abs(extension.getOrdinal()));
        }

        extension.setOrdinal(extension.getOrdinal() * USER_EXTENSION_ORDINAL_FACTOR);
        return extension;
    }

    /**
     * Set the Asterisk application on the active dialplan extension.
     * <p>
     * This method may be called multiple times in succession. Each call will overwrite the application set by the
     * previous one.
     *
     * @param app Asterisk application to be set.
     * @return DialPlanConfigBuilder
     * @throws InvalidConfigurationException when app is null
     * @throws IllegalStateException         when no active extension exists.
     * @throws ValidationException           when app cannot be validated.
     */
    public DialPlanConfigBuilder setApplication(AsteriskApplicationInterface app) {
        if (app == null) {
            throw new InvalidConfigurationException("extension must not be null");
        }

        if (activeExtension == null) {
            throw new IllegalStateException("No active extension found. Did you call addNewExtension()?");
        }

        activeExtension.setDialPlanApplication(app);

        return this;
    }

    /**
     * Build the list of contexts stored in this builder.
     *
     * @return the list with contexts.
     * @throws InvalidConfigurationException when no contexts have been added.
     */
    public List<DialPlanContext> build() {
        if (activeContext != null) {
            saveActiveContext();
        }

        if (contexts.isEmpty()) {
            throw new IllegalStateException("No configuration to build");
        }

        // Extract the DialPlanContexts from the Wrappers

        return contexts.stream()
                .map(ContextWrapper::getDialPlanContext)
                .collect(Collectors.toList());
    }

    private void saveActiveContext() {
        assert activeContext != null;

        assignActiveExtensionToActiveContext();

        activeContext.getDialPlanContext().validate();
        sortActiveExtension();
        setAsteriskPrioritiesOnActiveContext();

        // If we save an reactivated context, we must no re-add it to the list.
        if (!contextReactivated) {
            contexts.add(activeContext);
        }

        activeContext = null;
        contextReactivated = false;
    }

    /**
     * Set the Asterisk priority before the active context is stowed away in the contexts list.
     *
     * Priorities for extensions start with {@code 1} and continue with {@code n}. Expressed as Asterisk configuration:
     * <pre>
     * [context]
     * exten => 001,1,...
     * exten => 001,n,...
     * </pre>
     *
     * The priority numbering restarts if the extension number in a context changes. Expressed as Asterisk
     * configuration:
     * <pre>
     * [context]
     * exten => 001,1,...
     * exten => 001,n,...
     * exten => 001,n,...
     * ; Restart priority numbering
     * exten => 002,1,...
     * exten => 002,n,...
     * </pre>
     *
     * This method expects the extensions within a context to be sorted.
     */
    protected void setAsteriskPrioritiesOnActiveContext() {
        assert activeContext != null;

        activeContext
                .getDialPlanContext()
                .getDialPlanExtensionList()
                .forEach(new Consumer<AsteriskExtensionInterface>() {
                    private String lastNumber = "";

                    @Override
                    public void accept(AsteriskExtensionInterface extension) {
                        if (lastNumber.equals(extension.getPhoneNumber())) {
                            setPriorityN(extension);
                        } else {
                            lastNumber = extension.getPhoneNumber();
                            setPriorityOne(extension);
                        }
                    }
                });
    }

    private void assignActiveExtensionToActiveContext() {
        if (activeExtension == null) {
            throw new IllegalStateException("no active extension");
        }

        activeExtension.validate();
        activeContext.getDialPlanContext().getDialPlanExtensionList().add(activeExtension);

        activeExtension = null;
    }

    private void sortActiveExtension() {
        assert activeContext != null;
        activeContext.getDialPlanContext().getDialPlanExtensionList()
                .sort(
                        Comparator.comparingInt(AsteriskExtensionInterface::getOrdinal)
                );
    }

    /**
     * Unconditionally set the priority of the extension to {@code n}
     *
     * @param extension extension having the priority set to {@code 1}.
     * @return the same {@link AsteriskExtensionInterface} instance as passed in {@code extension}
     */
    protected AsteriskExtensionInterface setPriorityN(AsteriskExtensionInterface extension) {
        extension.setPriority("n");
        return extension;
    }

    /**
     * Unconditionally set the priority of the extension to {@code 1}
     *
     * @param extension extension having the priority set to {@code 1}
     * @return the same {@link AsteriskExtensionInterface} instance as passed in {@code extension}
     */
    protected AsteriskExtensionInterface setPriorityOne(AsteriskExtensionInterface extension) {
        extension.setPriority(PRIORITY_ONE);
        return extension;
    }

    /**
     * Wrap a DialPlanContext. It allows to provide meta information to contexts.
     *
     * Meta information can be added as KV pair, where the key is a string and the value a boolean.
     */
    protected class ContextWrapper {
        private DialPlanContext dialPlanContext;
        private Map<String, Boolean> metaInformation;

        public ContextWrapper() {
            metaInformation = new HashMap<>();
        }

        public ContextWrapper(DialPlanContext dialPlanContext) {
            this();
            this.dialPlanContext = dialPlanContext;
        }

        public void setMetaInformation(String key, boolean value) {
            metaInformation.put(key, value);
        }

        /**
         * Get meta information by key. If key is not found, {@code false} is returned.
         *
         * @param key name of the key
         * @return value of key, or {@code false} if key is not found.
         */
        public boolean getMetaInformation(String key) {
            return metaInformation.getOrDefault(key, false);
        }

        public DialPlanContext getDialPlanContext() {
            return dialPlanContext;
        }

    }
}
