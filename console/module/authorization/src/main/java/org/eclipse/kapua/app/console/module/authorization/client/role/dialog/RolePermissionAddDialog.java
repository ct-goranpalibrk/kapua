/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.module.authorization.client.role.dialog;

import java.util.List;

import org.eclipse.kapua.app.console.module.api.client.ui.dialog.entity.EntityAddEditDialog;
import org.eclipse.kapua.app.console.module.api.client.ui.panel.FormPanel;
import org.eclipse.kapua.app.console.module.api.client.util.DialogUtils;
import org.eclipse.kapua.app.console.module.api.shared.model.GwtSession;
import org.eclipse.kapua.app.console.module.authorization.client.messages.ConsoleRoleMessages;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtDomain;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtGroup;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtPermission.GwtAction;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRole;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRolePermission;
import org.eclipse.kapua.app.console.module.authorization.shared.model.GwtRolePermissionCreator;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtDomainServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtGroupServiceAsync;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtRoleService;
import org.eclipse.kapua.app.console.module.authorization.shared.service.GwtRoleServiceAsync;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RolePermissionAddDialog extends EntityAddEditDialog {

    private final static ConsoleRoleMessages MSGS = GWT.create(ConsoleRoleMessages.class);
    private final static GwtDomainServiceAsync DOMAIN_SERVICE = GWT.create(GwtDomainService.class);
    private final static GwtGroupServiceAsync GWT_GROUP_SERVICE = GWT.create(GwtGroupService.class);
    private final static GwtDomainServiceAsync GWT_DOMAIN_SERVICE = GWT.create(GwtDomainService.class);

    private ComboBox<GwtDomain> domainsCombo;
    private SimpleComboBox<GwtAction> actionsCombo;
    private ComboBox<GwtGroup> groupsCombo;
    private CheckBoxGroup forwardableChecboxGroup;
    private CheckBox forwardableChecbox;

    private GwtRole selectedRole;

    private final GwtGroup allGroup;
    private final GwtDomain allDomain = new GwtDomain("ALL");
    private final GwtAction allAction = GwtAction.ALL;

    public RolePermissionAddDialog(GwtSession currentSession) {
        super(currentSession);
        DialogUtils.resizeDialog(this, 400, 250);
        allGroup = new GwtGroup();
        allGroup.setId(null);
        allGroup.setGroupName("ALL");
    }

    public void setSelectedRole(GwtRole selectedRole) {
        this.selectedRole = selectedRole;
    }

    @Override
    public void createBody() {
        FormPanel permissionFormPanel = new FormPanel(FORM_LABEL_WIDTH);

        //
        // Domain
        domainsCombo = new ComboBox<GwtDomain>();
        domainsCombo.setStore(new ListStore<GwtDomain>());
        domainsCombo.setEditable(false);
        domainsCombo.setTypeAhead(false);
        domainsCombo.setAllowBlank(false);
        domainsCombo.disable();
        domainsCombo.setFieldLabel(MSGS.permissionAddDialogDomain());
        domainsCombo.setTriggerAction(TriggerAction.ALL);
        domainsCombo.setEmptyText(MSGS.permissionAddDialogLoading());
        domainsCombo.setDisplayField("domainName");
        GWT_DOMAIN_SERVICE.findAll(new AsyncCallback<List<GwtDomain>>() {

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }

            @Override
            public void onSuccess(List<GwtDomain> result) {
                domainsCombo.getStore().add(allDomain);
                domainsCombo.getStore().add(result);
                domainsCombo.setValue(allDomain);
                domainsCombo.enable();
            }
        });

        domainsCombo.addSelectionChangedListener(new SelectionChangedListener<GwtDomain>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GwtDomain> se) {
                GWT_DOMAIN_SERVICE.findActionsByDomainName(se.getSelectedItem().getDomainName(), new AsyncCallback<List<GwtAction>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                        exitStatus = false;
                        hide();
                    }

                    @Override
                    public void onSuccess(List<GwtAction> result) {
                        actionsCombo.removeAll();
                        actionsCombo.add(allAction);
                        actionsCombo.add(result);
                        actionsCombo.setSimpleValue(allAction);
                        actionsCombo.enable();
                    }
                });

            }
        });
        permissionFormPanel.add(domainsCombo);

        //
        // Action
        actionsCombo = new SimpleComboBox<GwtAction>();
        actionsCombo.disable();
        actionsCombo.setTypeAhead(false);
        actionsCombo.setAllowBlank(false);
        actionsCombo.setFieldLabel(MSGS.permissionAddDialogAction());
        actionsCombo.setTriggerAction(TriggerAction.ALL);
        actionsCombo.setEmptyText(MSGS.permissionAddDialogLoading());

        permissionFormPanel.add(actionsCombo);

        // Groups
        groupsCombo = new ComboBox<GwtGroup>();
        groupsCombo.setStore(new ListStore<GwtGroup>());
        groupsCombo.setEditable(false);
        groupsCombo.setTypeAhead(false);
        groupsCombo.setAllowBlank(false);
        groupsCombo.setDisplayField("groupName");
        groupsCombo.setValueField("id");
        groupsCombo.setFieldLabel(MSGS.permissionAddDialogGroup());
        groupsCombo.setTriggerAction(TriggerAction.ALL);
        groupsCombo.setEmptyText(MSGS.permissionAddDialogLoading());
        groupsCombo.disable();
        GWT_GROUP_SERVICE.findAll(currentSession.getSelectedAccountId(), new AsyncCallback<List<GwtGroup>>() {

            @Override
            public void onFailure(Throwable caught) {
                exitMessage = MSGS.dialogAddError(caught.getLocalizedMessage());
                exitStatus = false;
                hide();
            }

            @Override
            public void onSuccess(List<GwtGroup> result) {
                groupsCombo.getStore().removeAll();
                groupsCombo.getStore().add(allGroup);
                groupsCombo.getStore().add(result);
                groupsCombo.setValue(allGroup);
                groupsCombo.enable();
            }
        });
        permissionFormPanel.add(groupsCombo);

        //
        // Forwardable
        forwardableChecbox = new CheckBox();
        forwardableChecbox.setBoxLabel("");

        forwardableChecboxGroup = new CheckBoxGroup();
        forwardableChecboxGroup.setFieldLabel(MSGS.permissionAddDialogForwardable());
        forwardableChecboxGroup.add(forwardableChecbox);
        permissionFormPanel.add(forwardableChecboxGroup);

        //
        // Add form panel to body
        bodyPanel.add(permissionFormPanel);

    }

    @Override
    public void submit() {
        GwtPermission permission = new GwtPermission();
        permission.setDomain(domainsCombo.getValue().getDomainName());
        permission.setAction(actionsCombo.getValue().getValue().toString());
        permission.setGroupId(groupsCombo.getValue().getId());
        permission.setTargetScopeId(currentSession.getSelectedAccountId());
        permission.setForwardable(forwardableChecboxGroup.getValue() != null);

        GwtRolePermissionCreator rolePermission = new GwtRolePermissionCreator();
        rolePermission.setScopeId(currentSession.getSelectedAccountId());
        rolePermission.setRoleId(selectedRole.getId());

        GwtRoleServiceAsync roleService = GWT.create(GwtRoleService.class);
        roleService.addRolePermission(xsrfToken, rolePermission, permission, new AsyncCallback<GwtRolePermission>() {

            @Override
            public void onSuccess(GwtRolePermission rolePermission) {
                exitStatus = true;
                exitMessage = MSGS.dialogAddConfirmation();
                hide();
            }

            @Override
            public void onFailure(Throwable cause) {
                exitStatus = false;
                exitMessage = MSGS.dialogAddError(cause.getLocalizedMessage());
            }
        });

    }

    @Override
    public String getHeaderMessage() {
        return MSGS.permissionAddDialogHeader();
    }

    @Override
    public String getInfoMessage() {
        return MSGS.permissionAddDialogMessage();
    }

}
