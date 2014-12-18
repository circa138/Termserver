/*
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2014 FH Dortmund: Peter Haas, Robert Muetzner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhdo.gui.main.modules;

import de.fhdo.Definitions;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IUpdateData;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.TreeModel;
import de.fhdo.models.TreeNode;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemMetadataValue;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.MetadataParameter;
import types.termserver.fhdo.de.ValueSetMetadataValue;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PopupConcept extends Window implements AfterCompose, IUpdateData
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static enum EDITMODES
  {

    NONE, DETAILSONLY, CREATE, MAINTAIN, CREATE_NEW_VERSION
  }

  public static enum HIERARCHYMODE
  {

    NONE, SAME, SUB, ROOT
  }

  public static enum CONTENTMODE
  {

    CODESYSTEM, VALUESET
  }

  private EDITMODES editMode;
  private HIERARCHYMODE hierarchyMode;
  private CONTENTMODE contentMode;

  private boolean guiConceptMinimalVisible;
  private boolean guiConceptExpandableVisible;

  private CodeSystemEntityVersion csev;
  private CodeSystemConcept csc;
  private CodeSystemEntity cse;
  private CodeSystemVersionEntityMembership csvem;

  private long codeSystemEntityVersionId, codeSystemVersionId, valueSetVersionId;
  private long codeSystemId, valueSetId;

  GenericList genericListMetadata = null;
  GenericList genericListTranslation = null;

  private TreeNode tnSelected;
  //private CodeSystemEntityVersion csevAssociatedVersionId = null;
  private long csevAssociatedVersionId = 0;

  private List<MetadataParameter> listMetadata;
  private List<CodeSystemMetadataValue> listMetadataValuesCS;
  private List<ValueSetMetadataValue> listMetadataValuesVS;

  private IUpdateModal updateListener = null;

  public PopupConcept()
  {
    logger.debug("PopupConcept() - Konstruktor");

    // load arguments
    //id = ArgumentHelper.getWindowArgumentLong("Id");
    codeSystemEntityVersionId = ArgumentHelper.getWindowArgumentLong("VersionId");
    logger.debug("versionId: " + codeSystemEntityVersionId);

    codeSystemVersionId = ArgumentHelper.getWindowArgumentLong("CodeSystemVersionId");
    valueSetVersionId = ArgumentHelper.getWindowArgumentLong("ValueSetVersionId");

    codeSystemId = ArgumentHelper.getWindowArgumentLong("CodeSystemId");
    valueSetId = ArgumentHelper.getWindowArgumentLong("ValueSetId");

    Object o = ArgumentHelper.getWindowArgument("MetadataList");
    if (o != null)
      listMetadata = (List<MetadataParameter>) o;

    logger.debug("codeSystemVersionId: " + codeSystemVersionId);
    logger.debug("valueSetVersionId: " + valueSetVersionId);

    logger.debug("codeSystemId: " + codeSystemId);
    logger.debug("valueSetId: " + valueSetId);

    /*csev = (CodeSystemEntityVersion) ArgumentHelper.getWindowArgument("CSEV");
     if (csev != null)
     {
     csc = csev.getCodeSystemConcepts().get(0);
     cse = csev.getCodeSystemEntity();
     }*/
    contentMode = (CONTENTMODE) ArgumentHelper.getWindowArgument("ContentMode");
    logger.debug("contentMode: " + contentMode.name());

    editMode = EDITMODES.NONE;
    o = ArgumentHelper.getWindowArgument("EditMode");
    if (o != null)
    {
      try
      {
        editMode = (EDITMODES) o;
      }
      catch (NumberFormatException ex)
      {
        LoggingOutput.outputException(ex, PopupCodeSystem.class);
      }
    }
    logger.debug("Edit Mode: " + editMode.name());

    hierarchyMode = HIERARCHYMODE.NONE;
    o = ArgumentHelper.getWindowArgument("Association");
    if (o != null)
    {
      hierarchyMode = (HIERARCHYMODE) o;
    }
    logger.debug("hierarchyMode: " + hierarchyMode.name());

    o = ArgumentHelper.getWindowArgument("TreeNode");
    if (o != null)
      tnSelected = (TreeNode) o;

    //csevAssociatedVersionId = (CodeSystemEntityVersion) ArgumentHelper.getWindowArgument("CSEVAssociated"); // für assoziationen   
    csevAssociatedVersionId = ArgumentHelper.getWindowArgumentLong("CSEVAssociated"); // für assoziationen   

    initData();
  }

  public void afterCompose()
  {
    logger.debug("PopupConcept() - afterCompose()");

    setWindowTitle();
    showDetailsVisibilty();

    // fill domain values with selected codes
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbStatus"), de.fhdo.Definitions.DOMAINID_STATUS,
            csev == null ? "" : "" + csev.getStatusVisibility());

    // load data without bindings (dates, ...)
    if (csev.getStatusVisibilityDate() != null)
      ((Datebox) getFellow("dateBoxSD")).setValue(new Date(csev.getStatusVisibilityDate().toGregorianCalendar().getTimeInMillis()));
    if (csev.getInsertTimestamp() != null)
      ((Datebox) getFellow("dateBoxID")).setValue(new Date(csev.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
    if (csev.getEffectiveDate() != null)
      ((Datebox) getFellow("dateBoxReleasedAt")).setValue(new Date(csev.getEffectiveDate().toGregorianCalendar().getTimeInMillis()));

    ComponentHelper.setVisible("divId", editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN
            || editMode == EDITMODES.DETAILSONLY, this);

    initListMetadata();

    showComponents();
  }

  private void showComponents()
  {
    logger.debug("showComponents()");

    List<String> ignoreList = new LinkedList<String>();
    ignoreList.add("rowCSVStatus");  // immer readonly 
    ignoreList.add("dateBoxID");
    ignoreList.add("cbIsLeaf");
    ignoreList.add("buttonExpand");

    boolean readOnly = (editMode == EDITMODES.DETAILSONLY || editMode == EDITMODES.NONE);
    ComponentHelper.doDisableAll(getFellow("tabpanelDetails"), readOnly, ignoreList);

    logger.debug("version checked: " + (editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.CREATE ? "true" : "false"));

    ((Checkbox) getFellow("cbNewVersion")).setVisible(editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN);
    ((Checkbox) getFellow("cbNewVersion")).setChecked(editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.CREATE);
    ((Checkbox) getFellow("cbNewVersion")).setDisabled(editMode != EDITMODES.MAINTAIN);

    ComponentHelper.setVisible("bCreate", editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN, this);
  }

  private void initData()
  {
    // Properties
    guiConceptMinimalVisible = !PropertiesHelper.getInstance().isGuiConceptMinimal();
    guiConceptExpandableVisible = PropertiesHelper.getInstance().isGuiConceptExpandable();

    logger.debug("guiConceptMinimalVisible: " + guiConceptMinimalVisible);
    logger.debug("guiConceptExpandableVisible: " + guiConceptExpandableVisible);

    if (editMode == EDITMODES.CREATE)
    {
      logger.debug("new entry");
      // new entry
      csev = new CodeSystemEntityVersion();
      csc = new CodeSystemConcept();
      cse = new CodeSystemEntity();
      csvem = new CodeSystemVersionEntityMembership();

      csc.setIsPreferred(Boolean.TRUE);
      csev.getCodeSystemConcepts().add(csc);
      csev.setStatusVisibility(1); // TODO: 1 durch Konstante ersetzen
      csev.setIsLeaf(Boolean.TRUE);
      csvem.setIsAxis(Boolean.FALSE);

      if (hierarchyMode == HIERARCHYMODE.ROOT)
      {
        csvem.setIsMainClass(Boolean.TRUE);
        // TODO isAxis ?
      }
      else
      {
        this.setTitle(Labels.getLabel("popupConcept.createSubconcept"));
        csvem.setIsMainClass(Boolean.FALSE);
      }
    }
    else
    {
      logger.debug("load concept details");
      // load concept details
      ReturnConceptDetailsRequestType parameter = new ReturnConceptDetailsRequestType();
      parameter.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev_ws = new CodeSystemEntityVersion();
      csev_ws.setVersionId(codeSystemEntityVersionId);
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev_ws);

      if (SessionHelper.isUserLoggedIn())
      {
        parameter.setLoginToken(SessionHelper.getSessionId());
      }

      ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(parameter);

      // keine csev zurueckgekommen (wegen moeglicher Fehler beim WS)
      if (response.getCodeSystemEntity() == null)
        return;

      // load entities
      cse = response.getCodeSystemEntity();
      for (CodeSystemEntityVersion csev_db : cse.getCodeSystemEntityVersions())
      {
        if (csev_db.getVersionId().longValue() == cse.getCurrentVersionId())
        {
          csev = csev_db;
          csc = csev.getCodeSystemConcepts().get(0);
          break;
        }
      }

      for (CodeSystemVersionEntityMembership csvem_db : cse.getCodeSystemVersionEntityMemberships())
      {
        if (csvem_db.getId() != null && codeSystemVersionId == csvem_db.getId().getCodeSystemVersionId())
        {
          csvem = csvem_db;
          logger.debug("csvem found");
          break;
        }
      }

      // das Loeschen der cse aus der csev wieder rueckgaengig machen (war nur fuer die Anfrage an WS)
      //csev.setCodeSystemEntity(cse);
      // CodeSystemVersionEntityMembership nachladen
      /*if (response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().isEmpty() == false)
       {
       csvem = response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0);
       cse.getCodeSystemVersionEntityMemberships().clear();
       cse.getCodeSystemVersionEntityMemberships().add(csvem);
       }*/
      if (contentMode == CONTENTMODE.VALUESET)
      {
        ReturnValueSetConceptMetadataRequestType para = new ReturnValueSetConceptMetadataRequestType();
        para.setCodeSystemEntityVersionId(csev.getVersionId());
        para.setValuesetVersionId(valueSetVersionId);
//      listMetadata.setAttribute("valuesetVersionId", versionId);
//      listMetadata.setAttribute("codeSystemEntityVersionId", csev.getVersionId());
//      listMetadata.setAttribute("contentMode", contentMode);
//
//      listTranslations.setAttribute("cse", cse);
//      listTranslations.setAttribute("csev", csev);
//      listTranslations.setAttribute("csevm", csvem);

        ReturnValueSetConceptMetadataResponse.Return resp = WebServiceHelper.returnValueSetConceptMetadata(para);
//     TODO loadVsMetadata(resp, false);
      }
      else
      {
        // TODO
//      listMetadata.setAttribute("cse", cse);
//      listMetadata.setAttribute("csev", csev);
//      listMetadata.setAttribute("csevm", csvem);
//      listMetadata.setAttribute("contentMode", contentMode);
//      listMetadata.setAttribute("versionId", versionId);
//
//      listTranslations.setAttribute("cse", cse);
//      listTranslations.setAttribute("csev", csev);
//      listTranslations.setAttribute("csevm", csvem);
//
//      loadCsMetadata(response, false);
      }

    }

//    loadTranslations(response, false);
//    loadAssociations();
//
//    loadTbStatus();
  }

  private void setWindowTitle()
  {
    String title;

    switch (editMode)
    {
      case CREATE:
      case CREATE_NEW_VERSION:
        if (hierarchyMode == HIERARCHYMODE.SUB)
          title = Labels.getLabel("popupConcept.createSubconcept");
        else
          title = Labels.getLabel("popupConcept.newConcept");
        break;
      case DETAILSONLY:
        title = Labels.getLabel("popupConcept.showConcept");
        break;
      case MAINTAIN:
        title = Labels.getLabel("popupConcept.editConcept");
        break;

      default:
        //title = Labels.getLabel("common.details");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    this.setTitle(title);
  }

  private void showDetailsVisibilty()
  {
    logger.debug("showDetailsVisibilty()");

    // concept
    ComponentHelper.setVisible("buttonExpand", guiConceptExpandableVisible, this);

    ComponentHelper.setVisible("rMeaning", guiConceptMinimalVisible, this);
    ComponentHelper.setVisible("rowAbbrevation", guiConceptMinimalVisible, this);

    ComponentHelper.setVisible("rowCSVStatus", guiConceptMinimalVisible, this);
    ComponentHelper.setVisible("rowInsertedAt", guiConceptMinimalVisible, this);
    ComponentHelper.setVisible("rowPreferred", guiConceptMinimalVisible, this);
    ComponentHelper.setVisible("rowMainAxis", guiConceptMinimalVisible, this);
    ComponentHelper.setVisible("rowLeaf", guiConceptMinimalVisible, this);

    
    // Buttons
    Button buttonExpandConcept = (Button) getFellow("buttonExpand");
    
    if (guiConceptMinimalVisible)
      buttonExpandConcept.setImage("/rsc/img/symbols/collapse_16x16.png");
    else
      buttonExpandConcept.setImage("/rsc/img/symbols/expand_16x16.png");
  }

  public void onClickExpand()
  {
    logger.debug("Expand CS...");

    guiConceptMinimalVisible = !guiConceptMinimalVisible;

    showDetailsVisibilty();
    this.invalidate();
  }

  public void tabSelected()
  {
    logger.debug("Tab selected");
    Tabbox tabboxFilter = (Tabbox) getFellow("tabboxFilter");
    Tabpanel selPanel = tabboxFilter.getSelectedPanel();
    if (selPanel != null)
    {
      logger.debug("selPanel: " + selPanel.getId());

      if (selPanel.getId().equals("tabpanelMetadata"))
      {
        initListMetadata();
      }
      else if (selPanel.getId().equals("tabpanelTranslations"))
      {
        initListTranslation();
      }
    }

  }

  private void initListMetadata()
  {
    listMetadataValuesCS = new LinkedList<CodeSystemMetadataValue>();
    listMetadataValuesVS = new LinkedList<ValueSetMetadataValue>();

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("common.metadata"), 160, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.value"), 0, "", true, "String", true, true, editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.language"), 100, "", true, "String", true, true, false, false));

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (MetadataParameter mp : listMetadata)
    {
      String value = "";
      boolean gefunden = false;

      // load value
      if (contentMode == CONTENTMODE.VALUESET)
      {
        for (ValueSetMetadataValue meta : csev.getValueSetMetadataValues())
        {
          if (meta.getMetadataParameter().getId().longValue() == mp.getId())
          {
            value = meta.getParameterValue();
            listMetadataValuesVS.add(meta);
            gefunden = true;
            break;
          }
        }
      }
      else
      {
        for (CodeSystemMetadataValue meta : csev.getCodeSystemMetadataValues())
        {
          if (meta.getMetadataParameter().getId().longValue() == mp.getId())
          {
            value = meta.getParameterValue();
            listMetadataValuesCS.add(meta);
            gefunden = true;
            break;
          }
        }
      }

      if (gefunden == false)
      {
        if (contentMode == CONTENTMODE.VALUESET)
        {
          ValueSetMetadataValue meta = new ValueSetMetadataValue();
          meta.setParameterValue("");
          meta.setMetadataParameter(mp);
          listMetadataValuesVS.add(meta);
        }
        else
        {
          CodeSystemMetadataValue meta = new CodeSystemMetadataValue();
          meta.setParameterValue("");
          meta.setMetadataParameter(mp);
          listMetadataValuesCS.add(meta);
        }
      }

      GenericListRowType row = createRowFromMetadataParameter(value, mp);
      dataList.add(row);
    }
    /*if (contentMode == CONTENTMODE.VALUESET)
     {
     for (ValueSetMetadataValue meta : csev.getValueSetMetadataValues())
     {
     GenericListRowType row = createRowFromMetadataParameter(meta.getParameterValue(), meta.getMetadataParameter());
     dataList.add(row);
     }
     }
     else
     {
     for (CodeSystemMetadataValue meta : csev.getCodeSystemMetadataValues())
     {
     GenericListRowType row = createRowFromMetadataParameter(meta.getParameterValue(), meta.getMetadataParameter());
     dataList.add(row);
     }
     }*/

    // Liste initialisieren
    Include inc = (Include) getFellow("incListMetadata");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericListMetadata = (GenericList) winGenericList;
    genericListMetadata.setListId("metadata");

    //genericList.setListActions(this);
    genericListMetadata.setUpdateDataListener(this);
    genericListMetadata.setButton_new(false);
    genericListMetadata.setButton_edit(false);
    genericListMetadata.setButton_delete(false);
    genericListMetadata.setListHeader(header);
    genericListMetadata.setDataList(dataList);

  }

  private GenericListRowType createRowFromMetadataParameter(String value, MetadataParameter meta)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(meta.getParamName(), false, "");
    cells[1] = new GenericListCellType(value, false, "");
    cells[2] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_LANGUAGECODES, meta.getLanguageCd()), false, "");

    row.setData(meta);
    row.setCells(cells);

    return row;
  }

  private void initListTranslation()
  {
    if (genericListTranslation == null)
    {
      logger.debug("initListTranslation()");

      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType(Labels.getLabel("common.language"), 160, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(Labels.getLabel("common.translation"), 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(Labels.getLabel("common.abbrevation"), 150, "", true, "String", true, true, false, false));

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

      logger.debug("Anzahl: " + csc.getCodeSystemConceptTranslations().size());

      for (CodeSystemConceptTranslation data : csc.getCodeSystemConceptTranslations())
      {
        GenericListRowType row = createRowFromTranslation(data);
        dataList.add(row);
      }

      // Liste initialisieren
      Include inc = (Include) getFellow("incListTranslation");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListTranslation = (GenericList) winGenericList;
      genericListTranslation.setListId("translation");

      //genericList.setListActions(this);
      genericListTranslation.setButton_new(false);
      genericListTranslation.setButton_edit(false);
      genericListTranslation.setButton_delete(false);
      genericListTranslation.setListHeader(header);
      genericListTranslation.setDataList(dataList);
    }
  }

  private GenericListRowType createRowFromTranslation(CodeSystemConceptTranslation data)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_LANGUAGECODES, data.getLanguageCd()), false, "");
    cells[1] = new GenericListCellType(data.getTerm(), false, "");
    cells[2] = new GenericListCellType(data.getTermAbbrevation(), false, "");

    row.setData(data);
    row.setCells(cells);

    return row;
  }

  public void onOkClicked()
  {
    logger.debug("onOkClicked() - save data...");

    // save data without bindings (dates, ...)
    /*Date date = ((Datebox) getFellow("dateBoxED")).getValue();
     if (date != null)
     codeSystemVersion.setExpirationDate(DateTimeHelper.dateToXMLGregorianCalendar(date));
     else
     codeSystemVersion.setExpirationDate(null);*/
    // check mandatory fields
    if ((csc.getCode() == null || csc.getCode().length() == 0)
            || (csc.getTerm() == null || csc.getTerm().length() == 0))
    {
      Messagebox.show(Labels.getLabel("common.mandatoryFields"), Labels.getLabel("common.requiredField"), Messagebox.OK, Messagebox.EXCLAMATION);
      return;
    }

    // build structure for webservice
    cse.getCodeSystemVersionEntityMemberships().clear();
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    csev.getCodeSystemConcepts().clear();
    csev.getCodeSystemConcepts().add(csc);

    cse.getCodeSystemEntityVersions().clear();
    cse.getCodeSystemEntityVersions().add(csev);

    boolean success = false;
    logger.debug("editMode: " + editMode.name());

    try
    {
      // add metadata to request
      //genericListMetadata.
      //csev.getCodeSystemMetadataValues()
      if (contentMode == CONTENTMODE.VALUESET)
      {
        csev.getValueSetMetadataValues().clear();
        csev.getValueSetMetadataValues().addAll(listMetadataValuesVS);
      }
      else
      {
        csev.getCodeSystemMetadataValues().clear();
        csev.getCodeSystemMetadataValues().addAll(listMetadataValuesCS);
        
        for(CodeSystemMetadataValue mv : listMetadataValuesCS)
        {
          logger.debug("add metadata with id: " + mv.getMetadataParameter().getId() + ", value: " + mv.getParameterValue());
        }
      }

      // -> status date can't be updated manually
      switch (editMode)
      {
        case CREATE:
          success = save_Create();
          break;
        case MAINTAIN:
          success = save_MaintainVersion();
          break;
        case CREATE_NEW_VERSION:
          success = save_MaintainVersion();
          break;
      }
    }
    catch (Exception ex)
    {
      Messagebox.show(ex.getLocalizedMessage());
      LoggingOutput.outputException(ex, this);

      success = false;
    }

    if (updateListener != null
            && (editMode == EDITMODES.MAINTAIN
            || editMode == EDITMODES.CREATE_NEW_VERSION
            || editMode == EDITMODES.CREATE))
    {
      // update tree

      updateListener.update(cse, editMode == EDITMODES.MAINTAIN || editMode == EDITMODES.CREATE_NEW_VERSION);
    }

    if (success)
      this.detach();

  }

  public boolean save_MaintainVersion()
  {
    logger.debug("save_MaintainVersion()");

    Checkbox cbNewVersion = (Checkbox) getFellow("cbNewVersion");

    if (contentMode == CONTENTMODE.CODESYSTEM)
    {
      MaintainConceptRequestType parameter = new MaintainConceptRequestType();
      parameter.setCodeSystemVersionId(codeSystemVersionId);

      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      // Versioning 
      VersioningType versioning = new VersioningType();
      versioning.setCreateNewVersion(cbNewVersion.isChecked());
      parameter.setVersioning(versioning);

      // CSE
      parameter.setCodeSystemEntity(cse);

      //CSEV    
      csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      MaintainConceptResponseType response = WebServiceHelper.maintainConcept(parameter);

      csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

      // Meldung
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
          if (parameter.getVersioning().isCreateNewVersion())
            Messagebox.show(Labels.getLabel("popupConcept.newVersionSuccessfullyCreated"));
          else
            Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + response.getReturnInfos().getMessage());

        this.detach();
        //((ContentConcepts) this.getParent()).updateModel(true);  // TODO funktioniert nicht, zeigt Änderungen nicht an
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    else
    {
      // ValueSet
      /*TODO MaintainConceptValueSetMembershipRequestType parameter = new MaintainConceptValueSetMembershipRequestType();

       // Login
       parameter.setLoginToken(SessionHelper.getSessionId());

       CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
       codeSystemEntityVersion.getConceptValueSetMemberships().clear();
       ConceptValueSetMembership cvsmL = new ConceptValueSetMembership();

       cvsmL.setValueSetVersion(new ValueSetVersion());
       cvsmL.getValueSetVersion().setVersionId(cvsm.getId().getValuesetVersionId());
       cvsmL.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
       cvsmL.getCodeSystemEntityVersion().setVersionId(cvsm.getId().getCodeSystemEntityVersionId());
       cvsmL.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));

       cvsmL.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
       cvsmL.setIsStructureEntry(cbStructureEntry.isChecked());
       cvsmL.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
       cvsmL.setMeaning(tbBedeutung.getValue());
       cvsmL.setDescription(tbAwbeschreibung.getValue());
       cvsmL.setHints(tbHinweise.getValue());

       codeSystemEntityVersion.getConceptValueSetMemberships().add(cvsmL);
       parameter.setCodeSystemEntityVersion(codeSystemEntityVersion);

       // Versioning 
       MaintainConceptValueSetMembershipResponse.Return response = WebServiceHelper.maintainConceptValueSetMembership(parameter);

       try
       {
       if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
       Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
       ((ContentConcepts) this.getParent()).updateModel(true);
       this.detach();
       cvsm.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
       cvsm.setIsStructureEntry(cbStructureEntry.isChecked());
       cvsm.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
       cvsm.setMeaning(tbBedeutung.getValue());
       cvsm.setDescription(tbAwbeschreibung.getValue());
       cvsm.setHints(tbHinweise.getValue());

       }
       catch (Exception ex)
       {
       Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
       }*/
    }

    return true;
  }

  public boolean save_Create()
  {
    logger.debug("save_Create()");

    if (checkIfConceptExists(csc.getCode()))
      return false;

    // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
    CreateConceptRequestType parameter = new CreateConceptRequestType();

    // set parameter
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    parameter.setCodeSystem(new CodeSystem());
    parameter.getCodeSystem().setId(codeSystemId);
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setVersionId(codeSystemVersionId);
    //parameter.getCodeSystem().setId(id);
    parameter.getCodeSystem().getCodeSystemVersions().add(csv);

    parameter.setCodeSystemEntity(cse); // cse structure build before

    // WS aufruf
    CreateConceptResponse.Return response = WebServiceHelper.createConcept(parameter);

    // Message über Erfolg/Misserfolg                
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      //Messagebox.show(Labels.getLabel("popupCodeSystem.newCodeSystemsuccessfullyCreated"));
      // die neue cse(v) hat noch keine id. Für Assoziationen aber nötig => aus response auslesen
      csev.setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
      cse.setId(response.getCodeSystemEntity().getId());
      cse.setCurrentVersionId(csev.getVersionId());

      logger.debug("new Entity-ID: " + cse.getId());
      logger.debug("new Version-ID: " + csev.getVersionId());

      // TreeNode erstellen und danach update, damit das neue Konzept auch angezeigt wird                
      //TreeNode newTreeNode = new TreeNode(csev);
      // TODO irgendwie hier auslagern
      // In Root einhängen
      if (hierarchyMode == HIERARCHYMODE.ROOT)
      { // Root
        //Tree t = (Tree) this.getParent().getFellow("treeConcepts");
        //((TreeNode) ((TreeModel) t.getModel()).get_root()).getChildren().add(newTreeNode);
      }
      // Create Association für sub-konzepte und TreeNode einhängen
      else if (hierarchyMode == HIERARCHYMODE.SUB)
      {
        logger.debug("create association...");
        // sub-ebene
        // Assoziation erstellen; geht erst nachdem die neue CSE(V) erstell wurde und eine Id bekommen hat
        logger.debug("to id: " + csevAssociatedVersionId);

        CreateConceptAssociationResponse.Return responseAssociation
                = createAssociation(csevAssociatedVersionId, csev.getVersionId(), 
                        Definitions.ASSOCIATION_KIND.TAXONOMY.getCode(),
                        PropertiesHelper.getInstance().getAssociationTaxonomyDefaultVersionId());

        try
        {
          if (responseAssociation != null && responseAssociation.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.conceptassociation.Status.OK)
            Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
          else
          {
            if (responseAssociation.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.conceptassociation.OverallErrorCategory.INFO)
            {
              CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
              cseva.setLeftId(csevAssociatedVersionId);
              csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(cseva);
              //tnSelected.getChildren().add(newTreeNode);
            }
            else
            {
              Messagebox.show(Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
            }
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      //((ContentConcepts) this.getParent()).updateModel(true);

    }
    else
    {
      Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + response.getReturnInfos().getMessage());
    }

    return true;
  }

  private boolean checkIfConceptExists(String name)
  {
    logger.debug("checkIfConceptExists with code: " + name);

    if (contentMode == CONTENTMODE.VALUESET)
    {
      // TODO doppelte Werte erlaubt?
      return false;
    }
    else
    {
      ListCodeSystemConceptsRequestType request = new ListCodeSystemConceptsRequestType();
      request.setCodeSystem(new CodeSystem());
      CodeSystemVersion csv = new CodeSystemVersion();
      csv.setVersionId(codeSystemVersionId);
      request.getCodeSystem().getCodeSystemVersions().add(csv);

      request.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev_ws = new CodeSystemEntityVersion();
      CodeSystemConcept csc_ws = new CodeSystemConcept();
      csc_ws.setCode(name);
      csev_ws.getCodeSystemConcepts().add(csc_ws);
      request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev_ws);

      ListCodeSystemConceptsResponse.Return response = WebServiceHelper.listCodeSystemConcepts(request);
      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        for (CodeSystemEntity cse_ws : response.getCodeSystemEntity())
        {
          String code_ws = cse_ws.getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCode();
          if (code_ws == null)
            continue;

          if (code_ws.equalsIgnoreCase(name))
            return true;
        }
      }
      else
      {
        Messagebox.show(response.getReturnInfos().getMessage());
        return true;
      }
    }

    return false;
  }

  private CreateConceptAssociationResponse.Return createAssociation(long csev1_id, long csev2_id, int assoKind, long assoType_id)
  {
    CreateConceptAssociationRequestType parameterAssociation = new CreateConceptAssociationRequestType();
    CreateConceptAssociationResponse.Return responseAccociation = null;
    CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

    if (csev1_id > 0 && csev2_id > 0)
    {
      CodeSystemEntityVersion csev1 = new CodeSystemEntityVersion();
      csev1.setVersionId(csev1_id);
      CodeSystemEntityVersion csev2 = new CodeSystemEntityVersion();
      csev2.setVersionId(csev2_id);

      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
      cseva.setAssociationKind(assoKind); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
      cseva.setLeftId(csev1.getVersionId()); // immer linkes Element also csev1
      cseva.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
      cseva.getAssociationType().setCodeSystemEntityVersionId(assoType_id);

      // Login
      parameterAssociation.setLoginToken(SessionHelper.getSessionId());

      // Association
      parameterAssociation.setCodeSystemEntityVersionAssociation(cseva);

      // Call WS and prevent loops in SOAP Message        
      responseAccociation = WebServiceHelper.createConceptAssociation(parameterAssociation);
      //csev1.setCodeSystemEntity(new CodeSystemEntity());
      //csev1.getCodeSystemEntity().setId(cse1id);
      //csev2.setCodeSystemEntity(cse);
    }

    return responseAccociation;
  }

  public void onCellUpdated(int cellIndex, Object data, GenericListRowType row)
  {
    logger.debug("onCellUpdated()");
    
    if (cellIndex == 1 && row != null && row.getData() != null && row.getData() instanceof MetadataParameter
            && data != null && data instanceof String)
    {
      logger.debug("set value in list");
      
      MetadataParameter mp = (MetadataParameter) row.getData();
      
      if (contentMode == CONTENTMODE.VALUESET)
      {
        for(ValueSetMetadataValue mv : listMetadataValuesVS)
        {
          if(mv.getMetadataParameter().getId().longValue() == mp.getId())
          {
            mv.setParameterValue(data.toString());
            break;
          }
        }
      }
      else
      {
        for(CodeSystemMetadataValue mv : listMetadataValuesCS)
        {
          if(mv.getMetadataParameter().getId().longValue() == mp.getId())
          {
            mv.setParameterValue(data.toString());
            break;
          }
        }
      }
    }
  }

  /**
   * @return the csev
   */
  public CodeSystemEntityVersion getCsev()
  {
    return csev;
  }

  /**
   * @param csev the csev to set
   */
  public void setCsev(CodeSystemEntityVersion csev)
  {
    this.csev = csev;
  }

  /**
   * @return the csc
   */
  public CodeSystemConcept getCsc()
  {
    return csc;
  }

  /**
   * @param csc the csc to set
   */
  public void setCsc(CodeSystemConcept csc)
  {
    this.csc = csc;
  }

  /**
   * @return the cse
   */
  public CodeSystemEntity getCse()
  {
    return cse;
  }

  /**
   * @param cse the cse to set
   */
  public void setCse(CodeSystemEntity cse)
  {
    this.cse = cse;
  }

  /**
   * @return the csvem
   */
  public CodeSystemVersionEntityMembership getCsvem()
  {
    return csvem;
  }

  /**
   * @param csvem the csvem to set
   */
  public void setCsvem(CodeSystemVersionEntityMembership csvem)
  {
    this.csvem = csvem;
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdateModal updateListener)
  {
    this.updateListener = updateListener;
  }

}
