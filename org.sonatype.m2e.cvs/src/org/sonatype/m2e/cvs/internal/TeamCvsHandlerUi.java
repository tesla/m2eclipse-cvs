/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.cvs.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.scm.ScmUrl;
import org.eclipse.m2e.scm.spi.ScmHandlerUi;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;

/**
 * An SCM handler UI for Team/CVS provider
 * 
 * @author Eugene Kuleshov
 */
public class TeamCvsHandlerUi extends ScmHandlerUi {
  private static Logger log = LoggerFactory.getLogger(TeamCvsHandlerUi.class);
  
  // capabilities
  
  public boolean canSelectUrl() {
    return true;
  }
  
  public boolean canSelectRevision() {
    return true;
  }
  
  // selection UI

  public ScmUrl selectUrl(Shell shell, ScmUrl scmUrl) {
    TeamCvsRepositorySelectionWizard wizard = new TeamCvsRepositorySelectionWizard();
    WizardDialog dialog = new WizardDialog(shell, wizard);
    if(dialog.open()==Window.OK) {
      return TeamCvsUtil.getScmUrl(wizard.getSelectedModule());
    }
    
    return null;
  }
  
  public String selectRevision(Shell shell, ScmUrl scmUrl, String scmRevision) {
    if(scmUrl==null || scmUrl.getUrl()==null || scmUrl.getUrl().length()==0) {
      return null;
    }
    
    String url = scmUrl.getUrl();
    int n1 = url.lastIndexOf(':');
    int n2 = url.lastIndexOf('|');
    int n = Math.max(n1, n2); // module name
    
    String location = url.substring(TeamCvsHandler.SCM_CVS_PREFIX.length(), n);
    final String moduleName = url.substring(n + 1);
    
    ICVSRepositoryLocation repository;
    try {
      repository = CVSRepositoryLocation.fromString(location, false);
    } catch(CVSException ex) {
      log.error("Exception reading location", ex);
      return null;
    }
    
    ICVSRemoteFolder remoteFolder = repository.getRemoteFolder(moduleName, CVSTag.DEFAULT);
    
    TagSource tagSource = TagSource.create(remoteFolder);

    String title = "Select Tag";
    String message = "Select tag to checkout";
    
    int includeFlags = TagSelectionDialog.INCLUDE_HEAD_TAG | TagSelectionDialog.INCLUDE_VERSIONS
        | TagSelectionDialog.INCLUDE_BRANCHES; // exclude dates
    
    TagSelectionDialog dialog = new TagSelectionDialog(shell, tagSource, title, message, includeFlags, false, null);
    if(dialog.open() == Window.OK) {
      CVSTag tag = dialog.getResult();
      return tag.getName();
    }
    
    return null;
  }
  
  // verification
  
  public boolean isValidUrl(String scmUrl) {
    if(scmUrl==null) {
      return false;
    }
    if(!scmUrl.startsWith(TeamCvsHandler.SCM_CVS_PREFIX)) {
      return false;
    }

    return true;
  }
  
  public boolean isValidRevision(ScmUrl scmUrl, String scmRevision) {
    return CVSTag.validateTagName(scmRevision).isOK();
  }
  
}

