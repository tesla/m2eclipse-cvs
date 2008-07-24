/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.codehaus.plexus.util.IOUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.syncinfo.NotifyInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * @author Eugene Kuleshov
 */
public class FsCvsFile extends FsCvsResource implements ICVSFile {

  public FsCvsFile(File dest, FsCvsFolder parent, ICVSRepositoryLocation repository, String moduleName) {
    super(dest, parent, repository, moduleName);
  }

  public long getSize() {
    return dest.length();
  }
  
  public Date getTimeStamp() {
    return new Date(dest.lastModified());
  }

  public void setTimeStamp(Date date) throws CVSException {
    dest.setLastModified(date.getTime());
  }

  public boolean isFolder() {
    return false;
  }

  public InputStream getContents() throws CVSException {
    try {
      return new FileInputStream(dest);
    } catch(FileNotFoundException ex) {
      throw new CVSException(ex.toString());
    }
  }

  public void setContents(InputStream stream, int responseType, boolean keepLocalHistory, IProgressMonitor monitor)
      throws CVSException {
    FileOutputStream os = null;
    try {
      File parentDir = dest.getParentFile();
      if(!parentDir.exists()) {
        parentDir.mkdirs();
      }
      
      os = new FileOutputStream(dest);
      IOUtil.copy(stream, os);
    } catch(IOException ex) {
      throw new CVSException(ex.toString());
    } finally {
      IOUtil.close(os);
    }
  }

  
  public void checkedIn(String entryLine, boolean commit) throws CVSException {
  }

  public void copyTo(String filename) throws CVSException {
  }

  public void edit(int notifications, boolean notifyForWritable, IProgressMonitor monitor) throws CVSException {
  }

  public void unedit(IProgressMonitor monitor) throws CVSException {
  }

  public boolean isEdited() throws CVSException {
    return false;
  }

  public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws TeamException {
    return null;
  }

  public NotifyInfo getPendingNotification() throws CVSException {
    return null;
  }

  public void notificationCompleted() throws CVSException {
  }
  
  public byte[] getSyncBytes() throws CVSException {
    return null;
  }

  public void setSyncBytes(byte[] syncBytes, int modificationState) throws CVSException {
  }
  
  public void setSyncInfo(ResourceSyncInfo info, int modificationState) throws CVSException {
    File cvs = new File(dest.getParent(), "CVS");
    if(!cvs.exists()) {
      makeCvsFolders(parent);
    }
    
    File entries = new File(cvs, "Entries");
    writeFile(entries, info.getEntryLine()+"\n");
  }

  private void makeCvsFolders(FsCvsFolder folder) throws CVSException {
    if(folder.parent==null) {
      return;
    }
    
    makeCvsFolders(folder.parent);
    
    File file = folder.dest;
    File cvs = new File(file, "CVS");
    if(!cvs.exists()) {
      cvs.mkdirs();
      
      File root = new File(cvs, "Root");
      writeFile(root, repository.getLocation(false) + "\n");

      File repo = new File(cvs, "Repository");
      writeFile(repo, folder.moduleName + "\n");
      
      // add folder entry to the current CVS
      File parentCvs = new File(file.getParentFile(), "CVS");
      if(parentCvs.exists()) {
        // XXX tags?
        writeFile(new File(parentCvs, "Entries"), "D/" + file.getName() + "////\n");  
      }
    }
  }

  public boolean isExecutable() throws CVSException {
    return false;
  }

  public void setExecutable(boolean executable) throws CVSException {
  }
  
  public boolean isReadOnly() throws CVSException {
    return !dest.canWrite();
  }

  public void setReadOnly(boolean readOnly) throws CVSException {
  }

}
