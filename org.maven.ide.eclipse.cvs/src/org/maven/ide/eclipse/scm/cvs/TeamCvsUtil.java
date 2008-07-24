/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs;

import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;

import org.maven.ide.eclipse.scm.ScmUrl;

/**
 * @author Eugene Kuleshov
 */
public class TeamCvsUtil {

  static ScmUrl getScmUrl(ICVSRemoteFolder remoteFolder) {
    String scmUrl = getUrl(remoteFolder);
    
    ICVSRemoteResource parent = remoteFolder.getRemoteParent();
    String scmParentUrl = null;
    if(parent!=null) {
      scmParentUrl = getUrl(parent);
    }
    
    return new ScmUrl(scmUrl, scmParentUrl);
  }
  
  static String getUrl(ICVSRemoteResource resource) {
    ICVSRepositoryLocation repository = resource.getRepository();
    IConnectionMethod method = repository.getMethod();
    String host = repository.getHost();
    int port = repository.getPort();
    String root = repository.getRootDirectory();
    String userName = repository.getUsername();

    String resourcePath = resource.getRepositoryRelativePath();
    return TeamCvsHandler.SCM_CVS_PREFIX + method.getName() //
        + ":" + userName + ":@" + host + ":" + (port == 0 ? "" : port) //
        + root + (resourcePath.length()==0 ? "" : ":" + resourcePath);
  }

  
}
