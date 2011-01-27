/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.cvs.internal.fs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.m2e.cvs.MavenTeamCvsPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;


/**
 * @author Eugene Kuleshov
 */
public abstract class FsCvsResource implements ICVSResource {

  protected final File dest;
  protected final FsCvsFolder parent;
  protected final ICVSRepositoryLocation repository;
  protected final String moduleName;

  public FsCvsResource(File dest, FsCvsFolder parent, ICVSRepositoryLocation repository, String moduleName) {
    this.dest = dest;
    this.parent = parent;
    this.repository = repository;
    this.moduleName = moduleName;
  }
  
  public File getFile() {
    return dest;
  }
  
  public final String getName() {
    return dest.getName();
  }
  
  public final void delete() throws CVSException {
    dest.delete();
  }

  public final boolean exists() throws CVSException {
    return dest.exists();
  }

  public final IResource getIResource() {
    return null;
  }

  public final ICVSFolder getParent() {
    return parent;
  }

  public final String getRelativePath(ICVSFolder ancestor) throws CVSException {
    try {
      FsCvsResource rootFolder;
      String result;
      rootFolder = (FsCvsResource) ancestor;
      result = Util.getRelativePath(rootFolder.getFile().getAbsolutePath(), dest.getAbsolutePath());
      if (result.length() == 0) {
        return Session.CURRENT_LOCAL_FOLDER;
      }
      return result;  
    } catch (ClassCastException e) {
      IStatus status = new Status(IStatus.ERROR, MavenTeamCvsPlugin.PLUGIN_ID, 
          -1, "Two different resource implementations are used", e);
      throw new CVSException(status); 
    }
  }

  public abstract boolean isFolder();
  
  
  public final String getRemoteLocation(ICVSFolder root) throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }

  public final String getRepositoryRelativePath() throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }

  
  public final ResourceSyncInfo getSyncInfo() throws CVSException {
    // TODO Auto-generated method stub
    return null;
  }

  public final boolean isIgnored() throws CVSException {
    // TODO Auto-generated method stub
    return false;
  }

  public final boolean isManaged() throws CVSException {
    // TODO Auto-generated method stub
    return false;
  }

  public final boolean isModified(IProgressMonitor monitor) throws CVSException {
    // TODO Auto-generated method stub
    return false;
  }

  public final void setIgnoredAs(String pattern) throws CVSException {
    // TODO Auto-generated method stub

  }

  public final void unmanage(IProgressMonitor monitor) throws CVSException {
    // TODO Auto-generated method stub

  }

  
  public final void accept(ICVSResourceVisitor visitor) throws CVSException {
    // TODO Auto-generated method stub

  }

  public final void accept(ICVSResourceVisitor visitor, boolean recurse) throws CVSException {
    // TODO Auto-generated method stub

  }

  protected void writeFile(File file, String line) throws CVSException {
    FileWriter w = null;
    try {
      w = new FileWriter(file, file.exists());
      w.write(line);
      w.flush();
    } catch(IOException ex) {
      throw new CVSException(ex.toString());
    } finally {
      IOUtil.close(w);
    }
  }
  
}
