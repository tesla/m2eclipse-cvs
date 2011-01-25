/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs;

import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.core.MavenLogger;
import org.eclipse.m2e.core.scm.ScmTag;
import org.eclipse.m2e.core.scm.ScmUrl;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

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
    
    ScmTag scmTag = null;
    try {
      FolderSyncInfo syncInfo = remoteFolder.getFolderSyncInfo();
      if(syncInfo!=null) {
        CVSTag tag = syncInfo.getTag();
        if(tag!=null) {
          scmTag = new ScmTag(tag.getName(), getScmTagType(tag));
        }
      }
    } catch(CVSException ex) {
      String msg = "Can't retrieve CVS tag";
      MavenLogger.log(msg, ex);
      MavenPlugin.getDefault().getConsole().logError(msg + "; " + ex.getMessage());
    }
    
    return new ScmUrl(scmUrl, scmParentUrl, scmTag);
  }

  private static ScmTag.Type getScmTagType(CVSTag tag) {
    switch(tag.getType()) {
      case CVSTag.HEAD:
        return ScmTag.Type.HEAD;
      case CVSTag.BRANCH:
        return ScmTag.Type.BRANCH;
      case CVSTag.VERSION:
        return ScmTag.Type.TAG;
      case CVSTag.DATE:
        return ScmTag.Type.DATE;
    }
    return null;
  }
  
  static String getUrl(ICVSRemoteResource resource) {
    ICVSRepositoryLocation repository = resource.getRepository();
//    IConnectionMethod method = repository.getMethod();
//    String host = repository.getHost();
//    int port = repository.getPort();
//    String root = repository.getRootDirectory();
//    String userName = repository.getUsername();
    
    String location = repository.getLocation(true);

    String resourcePath = resource.getRepositoryRelativePath();
//    return TeamCvsHandler.SCM_CVS_PREFIX + method.getName() //
//        + ":" + userName + ":@" + host + ":" + (port == 0 ? "" : port) //
//        + root + (resourcePath.length()==0 ? "" : ":" + resourcePath);
    
    return TeamCvsHandler.SCM_CVS_PREFIX + location + (resourcePath.length() == 0 ? "" : ":" + resourcePath);
  }

  
}
