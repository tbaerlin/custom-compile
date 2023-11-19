/*
 * DependencyFeature.java
 *
 * Created on 08.10.2014 11:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
public class DependencyFeature {
    private final HasDependencyFeature owner;
    private final List<DependencyCommand> dependencies = new ArrayList<>();

    public DependencyFeature(HasDependencyFeature owner) {
        this.owner = owner;
    }

    public void release() {
        for (DependencyCommand dependencyCommand : this.dependencies) {
            if(dependencyCommand != null) {
                final BindFeature bindFeature = dependencyCommand.getBindFeature();
                if (bindFeature != null) {
                    bindFeature.release();
                }
            }
        }
    }

    public void addDependencyCommand(DependencyCommand dependencyCommand) {
        if(dependencyCommand instanceof SpsWidgetModificationCommand &&
                this.owner.asSpsWidget() != null) {
            ((SpsWidgetModificationCommand) dependencyCommand).setWidget(this.owner.asSpsWidget());
        }
        this.dependencies.add(dependencyCommand);
    }

}
