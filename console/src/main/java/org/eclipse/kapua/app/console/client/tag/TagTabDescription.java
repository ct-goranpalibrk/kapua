/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.client.tag;

import org.eclipse.kapua.app.console.client.ui.tab.EntityDescriptionTabItem;
import org.eclipse.kapua.app.console.shared.model.GwtGroupedNVPair;
import org.eclipse.kapua.app.console.shared.model.GwtTag;
import org.eclipse.kapua.app.console.shared.service.GwtTagService;
import org.eclipse.kapua.app.console.shared.service.GwtTagServiceAsync;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TagTabDescription extends EntityDescriptionTabItem<GwtTag> {

    public TagTabDescription() {
    }

    private static final GwtTagServiceAsync GWT_TAG_SERVICE = GWT.create(GwtTagService.class);

    @Override
    protected RpcProxy<ListLoadResult<GwtGroupedNVPair>> getDataProxy() {

        return new RpcProxy<ListLoadResult<GwtGroupedNVPair>>() {

            @Override
            protected void load(Object loadConfig, AsyncCallback<ListLoadResult<GwtGroupedNVPair>> callback) {
                GWT_TAG_SERVICE.getTagDescription(selectedEntity.getScopeId(), selectedEntity.getId(), callback);

            }
        };
    }
    
    @Override
    protected void onRender(Element parent, int index) {
    	super.onRender(parent, index);
    	setBorders(false);
    }
}
