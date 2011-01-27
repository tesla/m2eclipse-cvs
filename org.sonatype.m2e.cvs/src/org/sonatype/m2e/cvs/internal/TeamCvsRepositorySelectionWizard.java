/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.cvs.internal;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.core.MavenLogger;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage;
import org.eclipse.team.internal.ccvs.ui.wizards.ICVSWizard;
import org.eclipse.team.internal.ccvs.ui.wizards.ModuleSelectionPage;
import org.eclipse.team.internal.ccvs.ui.wizards.NewLocationWizard;
import org.eclipse.team.internal.ccvs.ui.wizards.RepositorySelectionPage;

/**
 * @author Eugene Kuleshov
 */
public class TeamCvsRepositorySelectionWizard extends Wizard implements ICVSWizard {

  private RepositorySelectionPage locationPage;
  private ModuleSelectionPage modulePage;
  ConfigurationWizardMainPage createLocationPage;

  private ICVSRepositoryLocation location;
  private boolean isNewLocation;
  private ICVSRemoteFolder selectedModule;

  public TeamCvsRepositorySelectionWizard() {
    setWindowTitle("Select CVS location"); 
  }
  
  public void addPages() {
    locationPage = new RepositorySelectionPage("repositoryPage", "Select CVS Repository", null);
    locationPage.setDescription("Select an existing repository location or create a new location.");
    locationPage.setExtendedDescription("Select CVS repository:");
    addPage(locationPage);
    
    createLocationPage = new ConfigurationWizardMainPage("createLocationPage", "Enter Repository Location Information", null) {
      {
        setDialogSettings(NewLocationWizard.getLocationDialogSettings());
      }
    };
    createLocationPage.setDescription("Define the location and protocol required to connect with an existing CVS repository.");
    // createLocationPage.setShowValidate(isNewLocation)
    addPage(createLocationPage);
    
    modulePage = new ModuleSelectionPage("modulePage", "Select CVS module", null);
    modulePage.setDescription("Select the module to be checked out from CVS");
    modulePage.setSupportsMultiSelection(false);
    addPage(modulePage);
  }
  
  public boolean performCancel() {
    if (location != null && isNewLocation) {
      KnownRepositories.getInstance().disposeRepository(location);
      location = null;
    }
    return true;
  }
  
  public boolean performFinish() {
    selectedModule = modulePage.getSelectedModule();
    return true;
  }
  
  public ICVSRemoteFolder getSelectedModule() {
    return selectedModule;
  }

  public boolean canFinish() {
    if(getContainer().getCurrentPage()==modulePage) {
      return modulePage.getSelectedModule()!=null;
    }
    return false;
  }
  
  public IWizardPage getPreviousPage(IWizardPage page) {
    if (page == locationPage) {
      return null;
    }
    modulePage.setLocation(null);
    modulePage.setPageComplete(false);
    return locationPage;
  }
  
  public IWizardPage getNextPage(IWizardPage page, boolean aboutToShow) {
    if (page == locationPage) {
      if (locationPage.getLocation() == null) {
        modulePage.setLocation(null);
        return createLocationPage;
      } else {
        if (aboutToShow) {
          try {
            modulePage.setLocation(getLocation());
          } catch (TeamException ex) {
            MavenLogger.log(ex);
          }
        }
        return modulePage;
      }
    }
    
    if (page == createLocationPage) {
      if (aboutToShow) {
        try {
          ICVSRepositoryLocation l = getLocation();
          if (l != null) {
            modulePage.setLocation(l);
          }
        } catch (TeamException ex) {
          MavenLogger.log(ex);
        }
      }
      return modulePage;
    }
    
//    if (page == modulePage) {
//      ICVSRemoteFolder[] selectedModules = getSelectedModules();
//      if (selectedModules.length == 0) {
//        return null;
//      }
//      for (int i = 0; i < selectedModules.length; i++) {
//        ICVSRemoteFolder folder = selectedModules[i];
//        if (folder.isDefinedModule()) {
//          // No further configuration is possible for defined modules
//          return null;
//        }
//      }
//      if (aboutToShow) {
//        try {
//          boolean hasMetafile = true;
//          if (selectedModules.length == 1) {
//            // Only allow configuration if one module is selected
//            final ICVSRemoteFolder[] folders = new ICVSRemoteFolder[] {selectedModules[0]};
//            final boolean withName = CVSUIPlugin.getPlugin().isUseProjectNameOnCheckout();
//
//            // attempt to retrieve the project description depending on preferences
//            // this is a bit convoluted to batch the meta-file check and retrieval in one operation
//            final ICVSRemoteFolder[] folderResult = new ICVSRemoteFolder [1];
//            final boolean[] booleanResult = new boolean[] { true };
//            
//            getContainer().run(true, true, new IRunnableWithProgress() {
//              public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//                ProjectMetaFileOperation op = new ProjectMetaFileOperation(null /* part */, //
//                    new ICVSRemoteFolder[] {folders[0]}, withName);
//                op.run(monitor);
//                folderResult[0] = op.getUpdatedFolders()[0];
//                booleanResult[0] = op.metaFileExists();
//              }
//            });
//            hasMetafile = booleanResult[0];
//            if (withName && hasMetafile)
//              selectedModules[0] = folderResult[0];
//          }
//          resetSubwizard();
//          wizard = new CheckoutAsWizard(getPart(), selectedModules, ! hasMetafile /* allow configuration */);
//          wizard.addPages();
//          return wizard.getStartingPage();
//        } catch (InvocationTargetException e) {
//          // Show the error and fall through to return null as the next page
//          CVSUIPlugin.openError(getShell(), null, null, e);
//        } catch (InterruptedException e) {
//          // Canceled by user. Fall through and return null
//        }
//        return null;
//      } else {
//        if (wizard == null) {
//          return dummyPage;
//        } else {
//          return wizard.getStartingPage();
//        }
//      }
//    }

    return null;
  }  
  
//  private ICVSRemoteFolder[] getSelectedModules() {
//    if (modulePage == null) {
//      return null;
//    }
//    return modulePage.getSelectedModules();
//  }

  private ICVSRepositoryLocation getLocation() throws TeamException {
    // If the location page has a location, use it.
    if (locationPage != null) {
      ICVSRepositoryLocation newLocation = locationPage.getLocation();
      if (newLocation != null) {
        return recordLocation(newLocation);
      }
    }
    
    // Otherwise, get the location from the create location page
    final ICVSRepositoryLocation[] locations = new ICVSRepositoryLocation[1];
    final CVSException[] exception = new CVSException[1];
    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          locations[0] = createLocationPage.getLocation();
        } catch (CVSException e) {
          exception[0] = e;
        }
      }
    });
    if (exception[0] != null) {
      throw exception[0];
    }
    return recordLocation(locations[0]);
  }

  private ICVSRepositoryLocation recordLocation(ICVSRepositoryLocation newLocation) {
    if (newLocation == null) {
      return location;
    }
    
    if (location == null || !newLocation.equals(location)) {
      KnownRepositories knownRepositories = KnownRepositories.getInstance();
      if (location != null && isNewLocation) {
        // Dispose of the previous location
        knownRepositories.disposeRepository(location);
      }
      location = newLocation;
      isNewLocation = !knownRepositories.isKnownRepository(newLocation.getLocation(false));
      if (isNewLocation) {
        // Add the location silently so we can work with it
        location = knownRepositories.addRepository(location, false /* silently */);
      }
    }
    return location;
  }
  
}
