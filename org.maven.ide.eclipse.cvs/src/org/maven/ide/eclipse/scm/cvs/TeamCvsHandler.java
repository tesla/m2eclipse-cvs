/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scm.cvs;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.project.MavenProjectScmInfo;
import org.eclipse.m2e.core.scm.ScmHandler;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;


/**
 * An SCM handler for Eclipse Team CVS provider
 * 
 * @see http://maven.apache.org/scm/scm-url-format.html
 * 
 * @author Eugene Kuleshov
 */
public class TeamCvsHandler extends ScmHandler {

  public static final String SCM_CVS_PREFIX = "scm:cvs";

  public InputStream open(String url, String revision) throws CoreException {
    throw new RuntimeException("Not implemented");
  }

  public void checkoutProject(MavenProjectScmInfo info, //
      File dest, IProgressMonitor monitor) throws CoreException, InterruptedException {

    // :method:[[user][:password]@]hostname[:[port]]/path/to/repository
    // :method:[user[:password]@]hostname[#port]:/path/to/repository
    //   e.g. :pserver:username:password@hostname#port:D:\cvsroot
    // :method[;option=arg...]:other_connection_data
    //   e.g. :pserver;username=anonymous;hostname=localhost:/path/to/repository

    // scm:cvs<delimiter><method>[<delimiter>username_password_servername_port]<delimiter>path_to_repository<delimiter>module_name
    // scm:cvs<delimiter>local<delimiter>path_to_repository<delimiter>module_name
    // scm:cvs<delimiter>lserver<delimiter>[username@]servername[<delimiter>port]<delimiter>path_to_repository<delimiter>module_name
    // scm:cvs<delimiter>pserver<delimiter>[username[<delimiter>password]@]servername[<delimiter>port]<delimiter>path_to_repository<delimiter>module_name
    // scm:cvs<delimiter>ext<delimiter>[username@]servername<delimiter>path_to_repository<delimiter>module_name
    // scm:cvs<delimiter>sspi<delimiter>[username@]host<delimiter>path<delimiter>module

    String folderUrl = info.getFolderUrl();
    int n1 = folderUrl.lastIndexOf(':');
    int n2 = folderUrl.lastIndexOf('|');
    int n = Math.max(n1, n2); // module name
    
    String location = folderUrl.substring(SCM_CVS_PREFIX.length(), n);
    final String moduleName = folderUrl.substring(n + 1);
    
    ICVSRepositoryLocation repository = CVSRepositoryLocation.fromString(location, false);
    
    String projectName = dest.getName();
    
    // create temporary project to checkout using Team/CVS implementation
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    
    IProject tempProject = root.getProject(projectName);
    
    if(dest.getParentFile().equals(root.getLocation().toFile())) {
      // rename dir in workspace to match expected project name
      if(!dest.equals(root.getLocation().append(projectName).toFile())) {
        File newProject = new File(dest.getParent(), projectName);
        dest.renameTo(newProject);
      }
      tempProject.create(monitor);
    } else {
      IProjectDescription description = workspace.newProjectDescription(projectName);
      description.setLocation(new Path(dest.getAbsolutePath()));
      tempProject.create(description, monitor);
    }
    
    tempProject.open(monitor);

    ICVSFolder destFolder = CVSWorkspaceRoot.getCVSFolderFor(root);
    
    // XXX not completely implemented
    // ICVSFolder destFolder = new FsCvsFolderRoot(dest.getParentFile(), null, repository, moduleName, projectName);
    
    Session session = new Session(repository, destFolder, true);
    session.open(monitor, false /* read-only */);

    try {
      Command.LocalOption[] localOptions = new Command.LocalOption[] {
          // Checkout.makeDirectoryNameOption(tempProject.getName()),
          Checkout.makeDirectoryNameOption(projectName),
          Command.PRUNE_EMPTY_DIRECTORIES,
          Update.makeTagOption(getCvsTag(info)),
      };

      IStatus status = Command.CHECKOUT.execute(session, Command.NO_GLOBAL_OPTIONS, localOptions,
          new String[] {moduleName}, //
          null, // listener
          monitor);
      if(!status.isOK()) {
        throw new CoreException(status);
      } 
      
    } finally {
      session.close();
      
      tempProject.delete(false, true, monitor);
    }
  }

  private CVSTag getCvsTag(MavenProjectScmInfo info) {
    String revision = info.getRevision();
    if("HEAD".equals(revision)) {
      return CVSTag.DEFAULT;
    } else if(revision!=null && revision.trim().length()>0) {
      // TODO improve handling of the tag types, including CVSTag.BRANCH and CVSTag.DATE 
      return new CVSTag(revision, CVSTag.VERSION);
    }
    return CVSTag.DEFAULT;
  }

}
