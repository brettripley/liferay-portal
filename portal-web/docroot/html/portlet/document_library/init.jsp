<%--
/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/init.jsp" %>

<%@ page import="com.liferay.portal.DuplicateLockException" %>
<%@ page import="com.liferay.portal.InvalidRepositoryException" %>
<%@ page import="com.liferay.portal.NoSuchRepositoryException" %>
<%@ page import="com.liferay.portal.kernel.repository.model.FileEntry" %>
<%@ page import="com.liferay.portal.kernel.repository.model.FileVersion" %>
<%@ page import="com.liferay.portal.kernel.repository.model.Folder" %>
<%@ page import="com.liferay.portal.kernel.search.Document" %>
<%@ page import="com.liferay.portal.kernel.search.Hits" %>
<%@ page import="com.liferay.portal.kernel.search.Indexer" %>
<%@ page import="com.liferay.portal.kernel.search.IndexerRegistryUtil" %>
<%@ page import="com.liferay.portal.kernel.search.SearchContext" %>
<%@ page import="com.liferay.portal.kernel.search.SearchContextFactory" %>
<%@ page import="com.liferay.portal.repository.util.RepositoryFactoryUtil" %>
<%@ page import="com.liferay.portlet.asset.model.AssetEntry" %>
<%@ page import="com.liferay.portlet.asset.model.AssetRenderer" %>
<%@ page import="com.liferay.portlet.asset.service.AssetEntryServiceUtil" %>
<%@ page import="com.liferay.portlet.asset.service.persistence.AssetEntryQuery" %>
<%@ page import="com.liferay.portlet.documentlibrary.DuplicateFileException" %>
<%@ page import="com.liferay.portlet.documentlibrary.DuplicateFolderNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.DuplicateRepositoryNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.FileExtensionException" %>
<%@ page import="com.liferay.portlet.documentlibrary.FileNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.FileShortcutPermissionException" %>
<%@ page import="com.liferay.portlet.documentlibrary.FileSizeException" %>
<%@ page import="com.liferay.portlet.documentlibrary.FolderNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.NoSuchDirectoryException" %>
<%@ page import="com.liferay.portlet.documentlibrary.NoSuchFileEntryException" %>
<%@ page import="com.liferay.portlet.documentlibrary.NoSuchFileException" %>
<%@ page import="com.liferay.portlet.documentlibrary.NoSuchFolderException" %>
<%@ page import="com.liferay.portlet.documentlibrary.RepositoryNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.SourceFileNameException" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileEntry" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileEntryConstants" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileEntryMetadata" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileEntryType" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileShortcut" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFileVersion" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFolder" %>
<%@ page import="com.liferay.portlet.documentlibrary.model.DLFolderConstants" %>
<%@ page import="com.liferay.portlet.documentlibrary.search.EntriesChecker" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.DLAppServiceUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.DLFileEntryMetadataLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.DLFileEntryTypeServiceUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.permission.DLFileEntryTypePermission" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.permission.DLFileEntryPermission" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.permission.DLFileShortcutPermission" %>
<%@ page import="com.liferay.portlet.documentlibrary.service.permission.DLFolderPermission" %>
<%@ page import="com.liferay.portlet.documentlibrary.util.AudioProcessor" %>
<%@ page import="com.liferay.portlet.documentlibrary.util.DLUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.util.DocumentConversionUtil" %>
<%@ page import="com.liferay.portlet.documentlibrary.util.PDFProcessor" %>
<%@ page import="com.liferay.portlet.documentlibrary.util.VideoProcessor" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.model.DDMStructure" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.search.StructureSearch" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.search.StructureSearchTerms" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.storage.Fields" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.storage.StorageEngineUtil" %>
<%@ page import="com.liferay.portlet.dynamicdatamapping.util.DDMXSDUtil" %>
<%@ page import="com.liferay.portlet.usersadmin.search.GroupSearch" %>
<%@ page import="com.liferay.portlet.usersadmin.search.GroupSearchTerms" %>

<%
PortalPreferences portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(liferayPortletRequest);

PortletPreferences preferences = liferayPortletRequest.getPreferences();

String portletResource = ParamUtil.getString(request, "portletResource");

long rootFolderId = PrefsParamUtil.getLong(preferences, request, "rootFolderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);

Folder rootFolder = null;

String rootFolderName = StringPool.BLANK;

if (rootFolderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
	try {
		rootFolder = DLAppLocalServiceUtil.getFolder(rootFolderId);

		rootFolderName = rootFolder.getName();
	}
	catch (NoSuchFolderException nsfe) {
	}
}

boolean showFoldersSearch = PrefsParamUtil.getBoolean(preferences, request, "showFoldersSearch", true);
boolean showSubfolders = PrefsParamUtil.getBoolean(preferences, request, "showSubfolders", true);
int foldersPerPage = PrefsParamUtil.getInteger(preferences, request, "foldersPerPage", SearchContainer.DEFAULT_DELTA);

String defaultFolderColumns = "name,num-of-folders,num-of-documents";

String portletId = portletDisplay.getId();

if (portletId.equals(PortletKeys.PORTLET_CONFIGURATION)) {
	portletId = portletResource;
}

boolean showActions = PrefsParamUtil.getBoolean(preferences, request, "showActions");
boolean showAddFolderButton = false;
boolean showFolderMenu = PrefsParamUtil.getBoolean(preferences, request, "showFolderMenu");
boolean showTabs = PrefsParamUtil.getBoolean(preferences, request, "showTabs");

if (portletId.equals(PortletKeys.DOCUMENT_LIBRARY)) {
	showActions = true;
	showAddFolderButton = true;
	showFolderMenu = true;
	showTabs = true;
}

if (showActions) {
	defaultFolderColumns += ",action";
}

String allFolderColumns = defaultFolderColumns;

String[] folderColumns = StringUtil.split(PrefsParamUtil.getString(preferences, request, "folderColumns", defaultFolderColumns));

if (!showActions) {
	folderColumns = ArrayUtil.remove(folderColumns, "action");
}
else if (!portletId.equals(PortletKeys.DOCUMENT_LIBRARY) && !ArrayUtil.contains(folderColumns, "action")) {
	folderColumns = ArrayUtil.append(folderColumns, "action");
}

int fileEntriesPerPage = PrefsParamUtil.getInteger(preferences, request, "fileEntriesPerPage", SearchContainer.DEFAULT_DELTA);

String defaultFileEntryColumns = "name,size";

if (PropsValues.DL_FILE_ENTRY_READ_COUNT_ENABLED) {
	defaultFileEntryColumns += ",downloads";
}

defaultFileEntryColumns += ",locked";

if (showActions) {
	defaultFileEntryColumns += ",action";
}

String allFileEntryColumns = defaultFileEntryColumns;

String[] fileEntryColumns = StringUtil.split(PrefsParamUtil.getString(preferences, request, "fileEntryColumns", defaultFileEntryColumns));

if (!showActions) {
	fileEntryColumns = ArrayUtil.remove(fileEntryColumns, "action");
}
else if (!portletId.equals(PortletKeys.DOCUMENT_LIBRARY) && !ArrayUtil.contains(fileEntryColumns, "action")) {
	fileEntryColumns = ArrayUtil.append(fileEntryColumns, "action");
}

boolean enableCommentRatings = GetterUtil.getBoolean(preferences.getValue("enableCommentRatings", null), true);

Format dateFormatDateTime = FastDateFormatFactoryUtil.getDateTime(locale, timeZone);
%>

<%!
private static final String _getFileEntryImage(FileEntry fileEntry, ThemeDisplay themeDisplay) {
	StringBundler sb = new StringBundler(5);

	sb.append("<img style=\"border-width: 0; text-align: left;\" src=\"");
	sb.append(themeDisplay.getPathThemeImages());
	sb.append("/file_system/small/");
	sb.append(fileEntry.getIcon());
	sb.append(".png\">");

	return sb.toString();
}
%>