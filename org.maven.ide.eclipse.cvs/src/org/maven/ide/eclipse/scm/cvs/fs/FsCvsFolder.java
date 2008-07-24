/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs.fs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

/**
 * @author Eugene Kuleshov
 */
public class FsCvsFolder extends FsCvsResource implements ICVSFolder {

  public FsCvsFolder(File dest, FsCvsFolder parent, ICVSRepositoryLocation repository, String moduleName) {
    super(dest, parent, repository, moduleName);
  }

  public void mkdir() throws CVSException {
    dest.mkdir();
  }
  
  public boolean isCVSFolder() throws CVSException {
    return true;
  }
  
  public boolean isFolder() {
    return true;
  }
  
  
  public ICVSResource getChild(String path) throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }

  public ICVSFile getFile(String name) throws CVSException {
    return new FsCvsFile(new File(dest, name), this, repository, moduleName);
  }

  public ICVSFolder getFolder(String name) throws CVSException {
    return new FsCvsFolder(new File(dest, name), this, repository, (moduleName + "/" + getName()));
  }

  public ICVSResource[] members(int flags) throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }

  public ICVSResource[] fetchChildren(IProgressMonitor monitor) throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }


  public FolderSyncInfo getFolderSyncInfo() throws CVSException {
    return null;
  }
  
  public void setFolderSyncInfo(FolderSyncInfo info) throws CVSException {
  }

  
  public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
    // TODO Auto-generated method stub
    // visitor.visitFile(file);
    // visitor.visitFolder(folder);
  }

  public void run(ICVSRunnable job, IProgressMonitor monitor) throws CVSException {
    job.run(monitor);
  }
  
}

