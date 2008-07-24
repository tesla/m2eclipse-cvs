/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs.fs;

import java.io.File;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

/**
 * @author Eugene Kuleshov
 */
public class FsCvsFolderRoot extends FsCvsFolder {

  private final String rootName;

  public FsCvsFolderRoot(File dest, FsCvsFolder parent, ICVSRepositoryLocation repository, String moduleName, String rootName) {
    super(dest, parent, repository, moduleName);
    this.rootName = rootName;
  }

  public ICVSFile getFile(String name) throws CVSException {
    return new FsCvsFile(new File(dest, name), this, repository, moduleName);
  }

  public ICVSFolder getFolder(String name) throws CVSException {
    File folder = new File(dest, name);

    if(name.startsWith(rootName)) {
      String module = moduleName + name.substring(rootName.length());
      if(module.endsWith("/")) {
        module = module.substring(0, module.length() - 1);
      }
      
      int n = name.lastIndexOf("/");
      if(n>-1) {
        return new FsCvsFolder(folder, (FsCvsFolder) getFolder(name.substring(0, n)), repository, module);
      } else {
        return new FsCvsFolder(folder, this, repository, module);
      }
    } else {
      return new FsCvsFolder(folder, this, repository, moduleName + "/" + name);
    }
  }
  
}

