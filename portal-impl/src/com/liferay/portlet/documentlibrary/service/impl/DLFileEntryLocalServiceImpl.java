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

package com.liferay.portlet.documentlibrary.service.impl;

import com.liferay.portal.ExpiredLockException;
import com.liferay.portal.InvalidLockException;
import com.liferay.portal.NoSuchLockException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.model.Lock;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.repository.liferayrepository.model.LiferayFileEntry;
import com.liferay.portal.repository.liferayrepository.model.LiferayFileVersion;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetLink;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.DuplicateFolderNameException;
import com.liferay.portlet.documentlibrary.FileNameException;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryException;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryMetadataException;
import com.liferay.portlet.documentlibrary.NoSuchFileException;
import com.liferay.portlet.documentlibrary.NoSuchFileVersionException;
import com.liferay.portlet.documentlibrary.NoSuchFolderException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntryConstants;
import com.liferay.portlet.documentlibrary.model.DLFileEntryMetadata;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.model.FileModel;
import com.liferay.portlet.documentlibrary.model.impl.DLFileEntryImpl;
import com.liferay.portlet.documentlibrary.service.base.DLFileEntryLocalServiceBaseImpl;
import com.liferay.portlet.documentlibrary.store.DLStoreUtil;
import com.liferay.portlet.documentlibrary.util.DLUtil;
import com.liferay.portlet.documentlibrary.util.comparator.RepositoryModelModifiedDateComparator;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngineUtil;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;

import java.io.InputStream;
import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The document library file entry local service.
 *
 * <p>
 * Due to legacy code, the names of some file entry properties are not
 * intuitive. Each file entry has both a name and title. The <code>name</code>
 * is a unique identifier for a given file and is generally numeric, whereas the
 * <code>title</code> is the actual name specified by the user (such as
 * &quot;Budget.xls&quot;).
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @author Harry Mark
 * @author Alexander Chow
 */
public class DLFileEntryLocalServiceImpl
	extends DLFileEntryLocalServiceBaseImpl {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DLFileEntry addFileEntry(
			long userId, long groupId, long repositoryId, long folderId,
			String mimeType, String title, String description, String changeLog,
			InputStream is, long size, ServiceContext serviceContext)
		throws PortalException, SystemException {

		// File entry

		User user = userPersistence.findByPrimaryKey(userId);
		folderId = dlFolderLocalService.getFolderId(
			user.getCompanyId(), folderId);
		String name = String.valueOf(
			counterLocalService.increment(DLFileEntry.class.getName()));
		String extension = getExtension(title, serviceContext);

		Long fileEntryTypeId = (Long)serviceContext.getAttribute(
			"fileEntryTypeId");

		if (fileEntryTypeId == null) {
			fileEntryTypeId = 0L;
		}

		Date now = new Date();

		validateFile(groupId, folderId, title, extension, is);

		long fileEntryId = counterLocalService.increment();

		DLFileEntry dlFileEntry = dlFileEntryPersistence.create(fileEntryId);

		dlFileEntry.setUuid(serviceContext.getUuid());
		dlFileEntry.setGroupId(groupId);
		dlFileEntry.setCompanyId(user.getCompanyId());
		dlFileEntry.setUserId(user.getUserId());
		dlFileEntry.setUserName(user.getFullName());
		dlFileEntry.setVersionUserId(user.getUserId());
		dlFileEntry.setVersionUserName(user.getFullName());
		dlFileEntry.setCreateDate(serviceContext.getCreateDate(now));
		dlFileEntry.setModifiedDate(serviceContext.getModifiedDate(now));
		dlFileEntry.setRepositoryId(repositoryId);
		dlFileEntry.setFolderId(folderId);
		dlFileEntry.setName(name);
		dlFileEntry.setExtension(extension);
		dlFileEntry.setMimeType(mimeType);
		dlFileEntry.setTitle(title);
		dlFileEntry.setDescription(description);
		dlFileEntry.setFileEntryTypeId(fileEntryTypeId);
		dlFileEntry.setVersion(DLFileEntryConstants.DEFAULT_VERSION);
		dlFileEntry.setSize(size);
		dlFileEntry.setReadCount(DLFileEntryConstants.DEFAULT_READ_COUNT);

		dlFileEntryPersistence.update(dlFileEntry, false);

		// Resources

		if (serviceContext.getAddGroupPermissions() ||
			serviceContext.getAddGuestPermissions()) {

			addFileEntryResources(
				dlFileEntry, serviceContext.getAddGroupPermissions(),
				serviceContext.getAddGuestPermissions());
		}
		else {
			addFileEntryResources(
				dlFileEntry, serviceContext.getGroupPermissions(),
				serviceContext.getGuestPermissions());
		}

		// File version

		DLFileVersion dlFileVersion = addFileVersion(
			user, dlFileEntry, serviceContext.getModifiedDate(now), extension,
			mimeType, title, description, null, StringPool.BLANK,
			fileEntryTypeId, DLFileEntryConstants.DEFAULT_VERSION, size,
			WorkflowConstants.STATUS_DRAFT, serviceContext);

		// Folder

		if (folderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
			DLFolder dlFolder = dlFolderPersistence.findByPrimaryKey(folderId);

			dlFolder.setLastPostDate(dlFileEntry.getModifiedDate());

			dlFolderPersistence.update(dlFolder, false);
		}

		// Asset

		updateAsset(
			userId, dlFileEntry, dlFileVersion,
			serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames(),
			serviceContext.getAssetLinkEntryIds());

		// DLApp

		dlAppHelperLocalService.addFileEntry(
			new LiferayFileEntry(dlFileEntry),
			new LiferayFileVersion(dlFileVersion), serviceContext);

		// File

		DLStoreUtil.addFile(
			user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
			dlFileEntry.getGroupId(), dlFileEntry.getDataRepositoryId(), name,
			false, serviceContext, is);

		// Index

		index(dlFileEntry, serviceContext);

		// Workflow

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			user.getCompanyId(), groupId, userId, DLFileEntry.class.getName(),
			dlFileVersion.getFileVersionId(), dlFileVersion, serviceContext);

		return dlFileEntry;
	}

	public void cancelCheckOut(long userId, long fileEntryId)
		throws PortalException, SystemException {

		boolean hasLock = hasFileEntryLock(userId, fileEntryId);

		if (!hasLock) {
			lockFileEntry(userId, fileEntryId);
		}

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		DLFileVersion dlFileVersion =
			dlFileVersionLocalService.getLatestFileVersion(fileEntryId, false);

		dlFileVersionPersistence.remove(dlFileVersion);

		try {
			DLStoreUtil.deleteFile(
				dlFileEntry.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
				dlFileEntry.getDataRepositoryId(), dlFileEntry.getName(),
				DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION);
		}
		catch (NoSuchFileException nsfe) {
		}

		lockLocalService.unlock(DLFileEntry.class.getName(), fileEntryId);
	}

	public void checkInFileEntry(
			long userId, long fileEntryId, boolean majorVersion,
			String changeLog, ServiceContext serviceContext)
		throws PortalException, SystemException {

		boolean hasLock = hasFileEntryLock(userId, fileEntryId);

		boolean isCheckedOut = isFileEntryCheckedOut(fileEntryId);

		if (!isCheckedOut) {
			return;
		}

		if (!hasLock) {
			lockFileEntry(userId, fileEntryId);
		}

		User user = userPersistence.findByPrimaryKey(userId);
		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		String version = getNextVersion(
			dlFileEntry, majorVersion, serviceContext.getWorkflowAction());

		DLFileVersion dlFileVersion =
			dlFileVersionLocalService.getLatestFileVersion(fileEntryId, false);

		dlFileVersion.setVersion(version);
		dlFileVersion.setChangeLog(changeLog);

		dlFileVersionPersistence.update(dlFileVersion, false);

		// Asset

		updateAsset(
			userId, dlFileEntry, dlFileVersion,
			dlFileVersion.getFileVersionId());

		// Folder

		if (dlFileEntry.getFolderId() !=
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {

			DLFolder dlFolder = dlFolderPersistence.findByPrimaryKey(
				dlFileEntry.getFolderId());

			dlFolder.setLastPostDate(dlFileEntry.getModifiedDate());

			dlFolderPersistence.update(dlFolder, false);
		}

		// File

		DLStoreUtil.updateFileVersion(
			user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
			dlFileEntry.getGroupId(), dlFileEntry.getDataRepositoryId(),
			dlFileEntry.getName(),
			DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION, version,
			dlFileEntry.getTitle(), serviceContext);

		// Index

		index(dlFileEntry, serviceContext);

		// Workflow

		if (serviceContext.getWorkflowAction() ==
				WorkflowConstants.ACTION_PUBLISH) {

			WorkflowHandlerRegistryUtil.startWorkflowInstance(
				user.getCompanyId(), dlFileEntry.getGroupId(), userId,
				DLFileEntry.class.getName(), dlFileVersion.getFileVersionId(),
				dlFileVersion, serviceContext);
		}

		lockLocalService.unlock(DLFileEntry.class.getName(), fileEntryId);
	}

	public void checkInFileEntry(long userId, long fileEntryId, String lockUuid)
		throws PortalException, SystemException {

		if (Validator.isNotNull(lockUuid)) {
			try {
				Lock lock = lockLocalService.getLock(
					DLFileEntry.class.getName(), fileEntryId);

				if (!lock.getUuid().equals(lockUuid)) {
					throw new InvalidLockException("UUIDs do not match");
				}
			}
			catch (PortalException pe) {
				if ((pe instanceof ExpiredLockException) ||
					(pe instanceof NoSuchLockException)) {
				}
				else {
					throw pe;
				}
			}
		}

		checkInFileEntry(
			userId, fileEntryId, false, StringPool.BLANK, new ServiceContext());
	}

	public DLFileEntry checkOutFileEntry(long userId, long fileEntryId)
		throws PortalException, SystemException {

		return checkOutFileEntry(
			userId, fileEntryId, StringPool.BLANK,
			DLFileEntryImpl.LOCK_EXPIRATION_TIME);
	}

	public DLFileEntry checkOutFileEntry(
			long userId, long fileEntryId, String owner, long expirationTime)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		boolean hasLock = hasFileEntryLock(userId, fileEntryId);

		if (!hasLock) {
			if ((expirationTime <= 0) ||
				(expirationTime > DLFileEntryImpl.LOCK_EXPIRATION_TIME)) {

				expirationTime = DLFileEntryImpl.LOCK_EXPIRATION_TIME;
			}

			lockLocalService.lock(
				userId, DLFileEntry.class.getName(), fileEntryId, owner,
				false, expirationTime);
		}

		User user = userPersistence.findByPrimaryKey(userId);

		dlFileEntryPersistence.update(dlFileEntry, false);

		DLFileVersion dlFileVersion =
			dlFileVersionLocalService.getLatestFileVersion(fileEntryId, false);

		long dlFileVersionId = dlFileVersion.getFileVersionId();

		String version = dlFileVersion.getVersion();

		if (!version.equals(
				DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION)) {

			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setCompanyId(user.getCompanyId());
			serviceContext.setUserId(userId);

			HashMap<Long, Fields> fieldsMap = new HashMap<Long, Fields>();

			serviceContext.setAttribute("fieldsMap", fieldsMap);

			dlFileVersion = addFileVersion(
				user, dlFileEntry, new Date(), dlFileVersion.getExtension(),
				dlFileVersion.getMimeType(), dlFileVersion.getTitle(),
				dlFileVersion.getDescription(), dlFileVersion.getChangeLog(),
				dlFileVersion.getExtraSettings(),
				dlFileVersion.getFileEntryTypeId(),
				DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION,
				dlFileVersion.getSize(), WorkflowConstants.STATUS_DRAFT,
				serviceContext);

			try {
				DLStoreUtil.deleteFile(
					dlFileEntry.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
					dlFileEntry.getDataRepositoryId(), dlFileEntry.getName(),
					DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION);
			}
			catch (NoSuchFileException nsfe) {
			}

			DLStoreUtil.copyFileVersion(
				user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
				dlFileEntry.getGroupId(), dlFileEntry.getDataRepositoryId(),
				dlFileEntry.getName(), version,
				DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION,
				dlFileVersion.getTitle(), serviceContext);

			copyFileEntryMetadata(
				dlFileEntry.getCompanyId(), dlFileVersion.getFileEntryTypeId(),
				fileEntryId, dlFileVersionId, dlFileVersion.getFileVersionId(),
				serviceContext);

			// Index

			index(dlFileEntry, serviceContext);

			// Asset

			updateAsset(userId, dlFileEntry, dlFileVersion, fileEntryId);
		}

		return dlFileEntry;
	}

	public void convertExtraSettings(String[] keys)
		throws PortalException, SystemException {

		int count = dlFileEntryFinder.countByExtraSettings();
		int pages = count / Indexer.DEFAULT_INTERVAL;

		for (int i = 0; i <= pages; i++) {
			int start = (i * Indexer.DEFAULT_INTERVAL);
			int end = start + Indexer.DEFAULT_INTERVAL;

			List<DLFileEntry> dlFileEntries =
				dlFileEntryFinder.findByExtraSettings(start, end);

			for (DLFileEntry dlFileEntry : dlFileEntries) {
				convertExtraSettings(dlFileEntry, keys);
			}
		}
	}

	public void deleteFileEntries(long groupId, long folderId)
		throws PortalException, SystemException {

		List<DLFileEntry> dlFileEntries = dlFileEntryPersistence.findByG_F(
			groupId, folderId);

		for (DLFileEntry dlFileEntry : dlFileEntries) {
			deleteFileEntry(dlFileEntry);
		}
	}

	public void deleteFileEntry(long fileEntryId)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = getFileEntry(fileEntryId);

		deleteFileEntry(dlFileEntry);
	}

	public void deleteFileEntry(long userId, long fileEntryId)
		throws PortalException, SystemException {

		boolean hasLock = hasFileEntryLock(userId, fileEntryId);

		if (!hasLock) {
			lockFileEntry(userId, fileEntryId);
		}

		try {
			deleteFileEntry(fileEntryId);
		}
		finally {
			unlockFileEntry(fileEntryId);
		}
	}

	public List<DLFileEntry> getExtraSettingsFileEntries(int start, int end)
		throws SystemException {

		return dlFileEntryFinder.findByExtraSettings(start, end);
	}

	public InputStream getFileAsStream(
			long userId, long fileEntryId, String version)
		throws PortalException, SystemException {

		return getFileAsStream(userId, fileEntryId, version, true);
	}

	public InputStream getFileAsStream(
			long userId, long fileEntryId, String version, boolean count)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		if (PropsValues.DL_FILE_ENTRY_READ_COUNT_ENABLED && count) {
			dlFileEntry.setReadCount(dlFileEntry.getReadCount() + 1);

			dlFileEntryPersistence.update(dlFileEntry, false);
		}

		dlAppHelperLocalService.getFileAsStream(
			userId, new LiferayFileEntry(dlFileEntry));

		return DLStoreUtil.getFileAsStream(
			dlFileEntry.getCompanyId(), dlFileEntry.getDataRepositoryId(),
			dlFileEntry.getName(), version);
	}

	public List<DLFileEntry> getFileEntries(int start, int end)
		throws SystemException {

		return dlFileEntryPersistence.findAll(start, end);
	}

	public List<DLFileEntry> getFileEntries(
			long groupId, long folderId, int start, int end,
			OrderByComparator obc)
		throws SystemException {

		return dlFileEntryPersistence.findByG_F(
			groupId, folderId, start, end, obc);
	}

	public int getFileEntriesCount() throws SystemException {
		return dlFileEntryPersistence.countAll();
	}

	public int getFileEntriesCount(long groupId, long folderId)
		throws SystemException {

		return dlFileEntryPersistence.countByG_F(groupId, folderId);
	}

	public DLFileEntry getFileEntry(long fileEntryId)
		throws PortalException, SystemException {

		return dlFileEntryPersistence.findByPrimaryKey(fileEntryId);
	}

	public DLFileEntry getFileEntry(long groupId, long folderId, String title)
		throws PortalException, SystemException {

		return dlFileEntryPersistence.findByG_F_T(groupId, folderId, title);
	}

	public DLFileEntry getFileEntryByName(
			long groupId, long folderId, String name)
		throws PortalException, SystemException {

		return dlFileEntryPersistence.findByG_F_N(groupId, folderId, name);
	}

	public DLFileEntry getFileEntryByUuidAndGroupId(String uuid, long groupId)
		throws PortalException, SystemException {

		return dlFileEntryPersistence.findByUUID_G(uuid, groupId);
	}

	public List<DLFileEntry> getGroupFileEntries(
			long groupId, int start, int end)
		throws SystemException {

		return getGroupFileEntries(
			groupId, start, end, new RepositoryModelModifiedDateComparator());
	}

	public List<DLFileEntry> getGroupFileEntries(
			long groupId, int start, int end, OrderByComparator obc)
		throws SystemException {

		return dlFileEntryPersistence.findByGroupId(groupId, start, end, obc);
	}

	public List<DLFileEntry> getGroupFileEntries(
			long groupId, long userId, int start, int end)
		throws SystemException {

		return getGroupFileEntries(
			groupId, userId, start, end,
			new RepositoryModelModifiedDateComparator());
	}

	public List<DLFileEntry> getGroupFileEntries(
			long groupId, long userId, int start, int end,
			OrderByComparator obc)
		throws SystemException {

		if (userId <= 0) {
			return dlFileEntryPersistence.findByGroupId(
				groupId, start, end, obc);
		}
		else {
			return dlFileEntryPersistence.findByG_U(
				groupId, userId, start, end, obc);
		}
	}

	public int getGroupFileEntriesCount(long groupId) throws SystemException {
		return dlFileEntryPersistence.countByGroupId(groupId);
	}

	public int getGroupFileEntriesCount(long groupId, long userId)
		throws SystemException {

		if (userId <= 0) {
			return dlFileEntryPersistence.countByGroupId(groupId);
		}
		else {
			return dlFileEntryPersistence.countByG_U(groupId, userId);
		}
	}

	public List<DLFileEntry> getNoAssetFileEntries() throws SystemException {
		return dlFileEntryFinder.findByNoAssets();
	}

	public List<DLFileEntry> getOrphanedFileEntries() throws SystemException {
		return dlFileEntryFinder.findByOrphanedFileEntries();
	}

	public boolean hasExtraSettings() throws SystemException {
		if (dlFileEntryFinder.countByExtraSettings() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean hasFileEntryLock(long userId, long fileEntryId)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = getFileEntry(fileEntryId);

		long folderId = dlFileEntry.getFolderId();

		boolean hasLock = lockLocalService.hasLock(
			userId, DLFileEntry.class.getName(), fileEntryId);

		if (!hasLock &&
			(folderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID)) {

			hasLock = dlFolderService.hasInheritableLock(folderId);
		}

		return hasLock;
	}

	public boolean isFileEntryCheckedOut(long fileEntryId)
		throws PortalException, SystemException {

		DLFileVersion dlFileVersion =
			dlFileVersionLocalService.getLatestFileVersion(fileEntryId, false);

		String version = dlFileVersion.getVersion();

		if (version.equals(
				DLFileEntryConstants.PRIVATE_WORKING_COPY_VERSION)) {

			return true;
		}
		else {
			return false;
		}
	}

	public DLFileEntry moveFileEntry(
			long userId, long fileEntryId, long newFolderId,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		boolean hasLock = hasFileEntryLock(userId, fileEntryId);

		if (!hasLock) {
			lockFileEntry(userId, fileEntryId);
		}

		try {
			return moveFileEntryImpl(
				userId, fileEntryId, newFolderId, serviceContext);
		}
		finally {
			if (!isFileEntryCheckedOut(fileEntryId)) {
				unlockFileEntry(fileEntryId);
			}
		}
	}

	public void revertFileEntry(
			long userId, long fileEntryId, String version,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		DLFileVersion dlFileVersion = dlFileVersionLocalService.getFileVersion(
			fileEntryId, version);

		if (!dlFileVersion.isApproved()) {
			return;
		}

		String sourceFileName = dlFileVersion.getTitle();
		String extension = dlFileVersion.getExtension();
		String mimeType = dlFileVersion.getMimeType();
		String title = dlFileVersion.getTitle();
		String description = dlFileVersion.getDescription();
		String changeLog = "Reverted to " + version;
		boolean majorVersion = true;
		String extraSettings = dlFileVersion.getExtraSettings();
		InputStream is = getFileAsStream(userId, fileEntryId, version);
		long size = dlFileVersion.getSize();

		updateFileEntry(
			userId, fileEntryId, sourceFileName, extension, mimeType, title,
			description, changeLog, majorVersion, extraSettings, is, size,
			serviceContext);
	}

	public AssetEntry updateAsset(
			long userId, DLFileEntry dlFileEntry, DLFileVersion dlFileVersion,
			long[] assetCategoryIds, String[] assetTagNames,
			long[] assetLinkEntryIds)
		throws PortalException, SystemException {

		boolean addDraftAssetEntry = false;

		if ((dlFileVersion != null) && !dlFileVersion.isApproved()) {
			String version = dlFileVersion.getVersion();

			if (!version.equals(DLFileEntryConstants.DEFAULT_VERSION)) {
				addDraftAssetEntry = true;
			}
		}

		boolean visible = true;

		if ((dlFileVersion != null) && !dlFileVersion.isApproved()) {
			visible = false;
		}

		return dlAppHelperLocalService.updateAsset(
			userId, new LiferayFileEntry(dlFileEntry),
			new LiferayFileVersion(dlFileVersion), assetCategoryIds,
			assetTagNames, assetLinkEntryIds, dlFileEntry.getMimeType(),
			addDraftAssetEntry, visible);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public DLFileEntry updateFileEntry(
			long userId, long fileEntryId, String sourceFileName,
			String mimeType, String title, String description, String changeLog,
			boolean majorVersion, InputStream is, long size,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		String extension = getExtension(title, serviceContext);

		String extraSettings = StringPool.BLANK;

		return updateFileEntry(
			userId, fileEntryId, sourceFileName, extension, mimeType, title,
			description, changeLog, majorVersion, extraSettings, is, size,
			serviceContext);
	}

	public DLFileEntry updateStatus(
			long userId, long fileVersionId, int status,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// File version

		User user = userPersistence.findByPrimaryKey(userId);

		DLFileVersion dlFileVersion = dlFileVersionPersistence.findByPrimaryKey(
			fileVersionId);

		dlFileVersion.setStatus(status);
		dlFileVersion.setStatusByUserId(user.getUserId());
		dlFileVersion.setStatusByUserName(user.getFullName());
		dlFileVersion.setStatusDate(new Date());

		dlFileVersionPersistence.update(dlFileVersion, false);

		// File entry

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			dlFileVersion.getFileEntryId());

		if (status == WorkflowConstants.STATUS_APPROVED) {
			if (DLUtil.compareVersions(
					dlFileEntry.getVersion(),
					dlFileVersion.getVersion()) <= 0) {

				dlFileEntry.setTitle(dlFileVersion.getTitle());
				dlFileEntry.setDescription(dlFileVersion.getDescription());
				dlFileEntry.setExtraSettings(dlFileVersion.getExtraSettings());
				dlFileEntry.setVersion(dlFileVersion.getVersion());
				dlFileEntry.setVersionUserId(dlFileVersion.getUserId());
				dlFileEntry.setVersionUserName(dlFileVersion.getUserName());
				dlFileEntry.setModifiedDate(dlFileVersion.getCreateDate());
				dlFileEntry.setSize(dlFileVersion.getSize());

				dlFileEntryPersistence.update(dlFileEntry, false);
			}

			// Indexer

			Indexer indexer = IndexerRegistryUtil.getIndexer(DLFileEntry.class);

			indexer.reindex(dlFileEntry);
		}
		else {

			// File entry

			if (dlFileEntry.getVersion().equals(dlFileVersion.getVersion())) {
				String newVersion = DLFileEntryConstants.DEFAULT_VERSION;

				List<DLFileVersion> approvedFileVersions =
					dlFileVersionPersistence.findByF_S(
						dlFileEntry.getFileEntryId(),
						WorkflowConstants.STATUS_APPROVED);

				if (!approvedFileVersions.isEmpty()) {
					newVersion = approvedFileVersions.get(0).getVersion();
				}

				dlFileEntry.setVersion(newVersion);

				dlFileEntryPersistence.update(dlFileEntry, false);
			}

			// Indexer

			if (dlFileVersion.getVersion().equals(
					DLFileEntryConstants.DEFAULT_VERSION)) {

				Indexer indexer = IndexerRegistryUtil.getIndexer(
					DLFileEntry.class);

				indexer.delete(dlFileEntry);
			}
		}

		// DLApp

		dlAppHelperLocalService.updateStatus(
			userId, new LiferayFileEntry(dlFileEntry),
			new LiferayFileVersion(dlFileVersion), status);

		return dlFileEntry;
	}

	public boolean verifyFileEntryCheckOut(long fileEntryId, String lockUuid)
		throws PortalException, SystemException {

		boolean lockVerified = false;

		try {
			Lock lock = lockLocalService.getLock(
				DLFileEntry.class.getName(), fileEntryId);

			if (lock.getUuid().equals(lockUuid)) {
				lockVerified = true;
			}
		}
		catch (PortalException pe) {
			if ((pe instanceof ExpiredLockException) ||
				(pe instanceof NoSuchLockException)) {

				DLFileEntry dlFileEntry = dlFileEntryLocalService.getFileEntry(
					fileEntryId);

				lockVerified = dlFolderService.verifyInheritableLock(
					dlFileEntry.getFolderId(), lockUuid);
			}
			else {
				throw pe;
			}
		}

		if (lockVerified && isFileEntryCheckedOut(fileEntryId)) {
			return true;
		}
		else {
			return false;
		}
	}

	protected void addFileEntryResources(
			DLFileEntry dlFileEntry, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addResources(
			dlFileEntry.getCompanyId(), dlFileEntry.getGroupId(),
			dlFileEntry.getUserId(), DLFileEntry.class.getName(),
			dlFileEntry.getFileEntryId(), false, addGroupPermissions,
			addGuestPermissions);
	}

	protected void addFileEntryResources(
			DLFileEntry dlFileEntry, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addModelResources(
			dlFileEntry.getCompanyId(), dlFileEntry.getGroupId(),
			dlFileEntry.getUserId(), DLFileEntry.class.getName(),
			dlFileEntry.getFileEntryId(), groupPermissions, guestPermissions);
	}

	protected void addFileEntryResources(
			long fileEntryId, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		addFileEntryResources(
			dlFileEntry, addGroupPermissions, addGuestPermissions);
	}

	protected void addFileEntryResources(
			long fileEntryId, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		addFileEntryResources(
			dlFileEntry, groupPermissions, guestPermissions);
	}

	protected DLFileVersion addFileVersion(
			User user, DLFileEntry dlFileEntry, Date modifiedDate,
			String extension, String mimeType, String title, String description,
			String changeLog, String extraSettings, long fileEntryTypeId,
			String version, long size, int status,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		long fileVersionId = counterLocalService.increment();

		DLFileVersion dlFileVersion = dlFileVersionPersistence.create(
			fileVersionId);

		long versionUserId = dlFileEntry.getVersionUserId();

		if (versionUserId <= 0) {
			versionUserId = dlFileEntry.getUserId();
		}

		String versionUserName = GetterUtil.getString(
			dlFileEntry.getVersionUserName(), dlFileEntry.getUserName());

		dlFileVersion.setGroupId(dlFileEntry.getGroupId());
		dlFileVersion.setCompanyId(dlFileEntry.getCompanyId());
		dlFileVersion.setUserId(versionUserId);
		dlFileVersion.setUserName(versionUserName);
		dlFileVersion.setCreateDate(modifiedDate);
		dlFileVersion.setRepositoryId(dlFileEntry.getRepositoryId());
		dlFileVersion.setFileEntryId(dlFileEntry.getFileEntryId());
		dlFileVersion.setExtension(extension);
		dlFileVersion.setMimeType(mimeType);
		dlFileVersion.setTitle(title);
		dlFileVersion.setDescription(description);
		dlFileVersion.setChangeLog(changeLog);
		dlFileVersion.setExtraSettings(extraSettings);
		dlFileVersion.setFileEntryTypeId(fileEntryTypeId);
		dlFileVersion.setVersion(version);
		dlFileVersion.setSize(size);
		dlFileVersion.setStatus(status);
		dlFileVersion.setStatusByUserId(user.getUserId());
		dlFileVersion.setStatusByUserName(user.getFullName());
		dlFileVersion.setStatusDate(dlFileEntry.getModifiedDate());
		dlFileVersion.setExpandoBridgeAttributes(serviceContext);

		dlFileVersionPersistence.update(dlFileVersion, false);

		Map<String, Fields> fieldsMap =
			(Map<String, Fields>)serviceContext.getAttribute("fieldsMap");

		if (fileEntryTypeId > 0) {
			dlFileEntryMetadataLocalService.updateFileEntryMetadata(
				fileEntryTypeId, dlFileEntry.getFileEntryId(), fileVersionId,
				fieldsMap, serviceContext);
		}

		return dlFileVersion;
	}

	protected void convertExtraSettings(
			DLFileEntry dlFileEntry, DLFileVersion dlFileVersion, String[] keys)
		throws PortalException, SystemException {

		UnicodeProperties extraSettingsProperties =
			dlFileVersion.getExtraSettingsProperties();

		ExpandoBridge expandoBridge = dlFileVersion.getExpandoBridge();

		convertExtraSettings(extraSettingsProperties, expandoBridge, keys);

		dlFileVersion.setExtraSettingsProperties(extraSettingsProperties);

		dlFileVersionPersistence.update(dlFileVersion, false);

		int status = dlFileVersion.getStatus();

		if ((status == WorkflowConstants.STATUS_APPROVED) &&
			(DLUtil.compareVersions(
				dlFileEntry.getVersion(), dlFileVersion.getVersion()) <= 0)) {

			Indexer indexer = IndexerRegistryUtil.getIndexer(DLFileEntry.class);

			indexer.reindex(dlFileEntry);
		}
	}

	protected void convertExtraSettings(DLFileEntry dlFileEntry, String[] keys)
		throws PortalException, SystemException {

		UnicodeProperties extraSettingsProperties =
			dlFileEntry.getExtraSettingsProperties();

		ExpandoBridge expandoBridge = dlFileEntry.getExpandoBridge();

		convertExtraSettings(extraSettingsProperties, expandoBridge, keys);

		dlFileEntry.setExtraSettingsProperties(extraSettingsProperties);

		dlFileEntryPersistence.update(dlFileEntry, false);

		List<DLFileVersion> dlFileVersions =
			dlFileVersionLocalService.getFileVersions(
				dlFileEntry.getFileEntryId(), WorkflowConstants.STATUS_ANY);

		for (DLFileVersion dlFileVersion : dlFileVersions) {
			convertExtraSettings(dlFileEntry, dlFileVersion, keys);
		}
	}

	protected void convertExtraSettings(
		UnicodeProperties extraSettingsProperties, ExpandoBridge expandoBridge,
		String[] keys) {

		for (String key : keys) {
			String value = extraSettingsProperties.remove(key);

			if (Validator.isNull(value)) {
				continue;
			}

			int type = expandoBridge.getAttributeType(key);

			Serializable serializable = ExpandoColumnConstants.getSerializable(
				type, value);

			expandoBridge.setAttribute(key, serializable);
		}
	}

	protected void copyFileEntryMetadata(
			long companyId, long fileEntryTypeId, long fileEntryId,
			long fromFileVersionId, long toFileVersionId,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		Map<String, Fields> fieldsMap = new HashMap<String, Fields>();

		List<DDMStructure> ddmStructures = null;

		if (fileEntryTypeId > 0) {
			DLFileEntryType dlFileEntryType =
				dlFileEntryTypeLocalService.getFileEntryType(fileEntryTypeId);

			ddmStructures = dlFileEntryType.getDDMStructures();

			for (DDMStructure ddmStructure : ddmStructures) {
				DLFileEntryMetadata dlFileEntryMetadata =
					dlFileEntryMetadataLocalService.getFileEntryMetadata(
						ddmStructure.getStructureId(), fromFileVersionId);

				Fields fields = StorageEngineUtil.getFields(
					dlFileEntryMetadata.getDDMStorageId());

				fieldsMap.put(ddmStructure.getStructureKey(), fields);
			}

			dlFileEntryMetadataLocalService.updateFileEntryMetadata(
				companyId, ddmStructures, fileEntryTypeId, fileEntryId,
				toFileVersionId, fieldsMap, serviceContext);
		}

		long classNameId = PortalUtil.getClassNameId(DLFileEntry.class);

		ddmStructures = ddmStructureLocalService.getClassStructures(
			classNameId);

		for (DDMStructure ddmStructure : ddmStructures) {
			try {
				DLFileEntryMetadata fileEntryMetadata =
					dlFileEntryMetadataLocalService.getFileEntryMetadata(
						ddmStructure.getStructureId(), fromFileVersionId);

				Fields fields = StorageEngineUtil.getFields(
					fileEntryMetadata.getDDMStorageId());

				fieldsMap.put(ddmStructure.getStructureKey(), fields);
			}
			catch (NoSuchFileEntryMetadataException nsfme) {
			}
		}

		dlFileEntryMetadataLocalService.updateFileEntryMetadata(
			companyId, ddmStructures, fileEntryTypeId, fileEntryId,
			toFileVersionId, fieldsMap, serviceContext);
	}

	protected void deleteFileEntry(DLFileEntry dlFileEntry)
		throws PortalException, SystemException {

		// File entry

		dlFileEntryPersistence.remove(dlFileEntry);

		// Resources

		resourceLocalService.deleteResource(
			dlFileEntry.getCompanyId(), DLFileEntry.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, dlFileEntry.getFileEntryId());

		// WebDAVProps

		webDAVPropsLocalService.deleteWebDAVProps(
			DLFileEntry.class.getName(), dlFileEntry.getFileEntryId());

		// File entry metadata

		dlFileEntryMetadataLocalService.deleteFileEntryMetadata(
			dlFileEntry.getFileEntryId());

		// File versions

		List<DLFileVersion> dlFileVersions =
			dlFileVersionPersistence.findByFileEntryId(
				dlFileEntry.getFileEntryId());

		for (DLFileVersion dlFileVersion : dlFileVersions) {
			dlFileVersionPersistence.remove(dlFileVersion);

			expandoValueLocalService.deleteValues(
				DLFileVersion.class.getName(),
				dlFileVersion.getFileVersionId());

			workflowInstanceLinkLocalService.deleteWorkflowInstanceLinks(
				dlFileEntry.getCompanyId(), dlFileEntry.getGroupId(),
				DLFileEntry.class.getName(), dlFileVersion.getFileVersionId());
		}

		// Expando

		expandoValueLocalService.deleteValues(
			DLFileEntry.class.getName(), dlFileEntry.getFileEntryId());

		// Lock

		lockLocalService.unlock(
			DLFileEntry.class.getName(), dlFileEntry.getFileEntryId());

		// DLApp

		dlAppHelperLocalService.deleteFileEntry(
			new LiferayFileEntry(dlFileEntry));

		// File

		try {
			DLStoreUtil.deleteFile(
				dlFileEntry.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
				dlFileEntry.getDataRepositoryId(), dlFileEntry.getName());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}
		}

		// Index

		FileModel fileModel = new FileModel();

		fileModel.setCompanyId(dlFileEntry.getCompanyId());
		fileModel.setFileName(dlFileEntry.getName());
		fileModel.setPortletId(PortletKeys.DOCUMENT_LIBRARY);
		fileModel.setRepositoryId(dlFileEntry.getDataRepositoryId());

		Indexer indexer = IndexerRegistryUtil.getIndexer(FileModel.class);

		indexer.delete(fileModel);
	}

	protected String getExtension(String title, ServiceContext serviceContext) {
		String sourceFileName = (String)serviceContext.getAttribute(
			"sourceFileName");

		if (Validator.isNull(sourceFileName)) {
			sourceFileName = title;
		}

		return FileUtil.getExtension(sourceFileName);
	}

	protected String getNextVersion(
			DLFileEntry dlFileEntry, boolean majorVersion, int workflowAction)
		throws PortalException, SystemException {

		String version = dlFileEntry.getVersion();

		try {
			DLFileVersion dlFileVersion =
				dlFileVersionLocalService.getLatestFileVersion(
					dlFileEntry.getFileEntryId(), true);

			version = dlFileVersion.getVersion();
		}
		catch (NoSuchFileVersionException nsfve) {
		}

		if (workflowAction == WorkflowConstants.ACTION_SAVE_DRAFT) {
			majorVersion = false;
		}

		int[] versionParts = StringUtil.split(version, StringPool.PERIOD, 0);

		if (majorVersion) {
			versionParts[0]++;
			versionParts[1] = 0;
		}
		else {
			versionParts[1]++;
		}

		return versionParts[0] + StringPool.PERIOD + versionParts[1];
	}

	protected void index(
			DLFileEntry dlFileEntry, ServiceContext serviceContext)
		throws SearchException {

		Indexer indexer = IndexerRegistryUtil.getIndexer(
			FileModel.class);

		FileModel fileModel = new FileModel();

		fileModel.setAssetCategoryIds(serviceContext.getAssetCategoryIds());
		fileModel.setAssetTagNames(serviceContext.getAssetTagNames());
		fileModel.setCompanyId(serviceContext.getCompanyId());
		fileModel.setFileEntryId(dlFileEntry.getFileEntryId());
		fileModel.setFileName(dlFileEntry.getName());
		fileModel.setGroupId(dlFileEntry.getGroupId());
		fileModel.setModifiedDate(dlFileEntry.getModifiedDate());
		fileModel.setPortletId(PortletKeys.DOCUMENT_LIBRARY);
		fileModel.setProperties(dlFileEntry.getLuceneProperties());
		fileModel.setRepositoryId(dlFileEntry.getDataRepositoryId());

		indexer.reindex(fileModel);
	}

	protected Lock lockFileEntry(long userId, long fileEntryId)
		throws PortalException, SystemException {

		return lockFileEntry(
			userId, fileEntryId, null, DLFileEntryImpl.LOCK_EXPIRATION_TIME);
	}

	protected Lock lockFileEntry(
			long userId, long fileEntryId, String owner, long expirationTime)
		throws PortalException, SystemException {

		if ((expirationTime <= 0) ||
			(expirationTime > DLFileEntryImpl.LOCK_EXPIRATION_TIME)) {

			expirationTime = DLFileEntryImpl.LOCK_EXPIRATION_TIME;
		}

		return lockLocalService.lock(
			userId, DLFileEntry.class.getName(), fileEntryId, owner, false,
			expirationTime);
	}

	protected DLFileEntry moveFileEntryImpl(
			long userId, long fileEntryId, long newFolderId,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);
		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		long oldDataRepositoryId = dlFileEntry.getDataRepositoryId();

		if (DLStoreUtil.hasFile(
				user.getCompanyId(),
				DLFolderConstants.getDataRepositoryId(
					dlFileEntry.getGroupId(), newFolderId),
				dlFileEntry.getName(), StringPool.BLANK)) {

			throw new DuplicateFileException(dlFileEntry.getName());
		}

		dlFileEntry.setFolderId(newFolderId);

		dlFileEntryPersistence.update(dlFileEntry, false);

		// File

		DLStoreUtil.updateFile(
			user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
			dlFileEntry.getGroupId(), oldDataRepositoryId,
			dlFileEntry.getDataRepositoryId(), dlFileEntry.getName());

		// Index

		Indexer indexer = IndexerRegistryUtil.getIndexer(
			FileModel.class);

		FileModel fileModel = new FileModel();

		fileModel.setCompanyId(user.getCompanyId());
		fileModel.setFileName(dlFileEntry.getName());
		fileModel.setPortletId(PortletKeys.DOCUMENT_LIBRARY);
		fileModel.setRepositoryId(oldDataRepositoryId);

		indexer.delete(fileModel);

		fileModel.setGroupId(dlFileEntry.getGroupId());
		fileModel.setRepositoryId(dlFileEntry.getDataRepositoryId());

		indexer.reindex(fileModel);

		return dlFileEntry;
	}

	protected void unlockFileEntry(long fileEntryId) throws SystemException {
		lockLocalService.unlock(DLFileEntry.class.getName(), fileEntryId);
	}

	protected AssetEntry updateAsset(
			long userId, DLFileEntry dlFileEntry, DLFileVersion dlFileVersion,
			long assetClassPk)
		throws PortalException, SystemException {

		long[] assetCategoryIds = assetCategoryLocalService.getCategoryIds(
			DLFileEntry.class.getName(), assetClassPk);
		String[] assetTagNames = assetTagLocalService.getTagNames(
			DLFileEntry.class.getName(), assetClassPk);

		AssetEntry assetEntry = assetEntryLocalService.getEntry(
			DLFileEntry.class.getName(), assetClassPk);

		List<AssetLink> assetLinks = assetLinkLocalService.getDirectLinks(
			assetEntry.getEntryId());

		long[] assetLinkIds = StringUtil.split(
			ListUtil.toString(assetLinks, "linkId"), 0L);

		return updateAsset(
			userId, dlFileEntry, dlFileVersion, assetCategoryIds, assetTagNames,
			assetLinkIds);
	}

	protected DLFileEntry updateFileEntry(
			long userId, long fileEntryId, String sourceFileName,
			String extension, String mimeType, String title, String description,
			String changeLog, boolean majorVersion, String extraSettings,
			InputStream is, long size, ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);
		DLFileEntry dlFileEntry = dlFileEntryPersistence.findByPrimaryKey(
			fileEntryId);

		boolean checkedOut = dlFileEntry.isCheckedOut();

		DLFileVersion dlFileVersion =
			dlFileVersionLocalService.getLatestFileVersion(
				fileEntryId, !checkedOut);

		boolean autoCheckIn = !checkedOut && dlFileVersion.isApproved();

		if (autoCheckIn) {
			dlFileEntry = checkOutFileEntry(userId, fileEntryId);
		}
		else if (!checkedOut) {
			lockFileEntry(userId, fileEntryId);
		}

		if (checkedOut || autoCheckIn) {
			dlFileVersion = dlFileVersionLocalService.getLatestFileVersion(
				fileEntryId, false);
		}

		try {
			if (Validator.isNull(extension)) {
				extension = dlFileEntry.getExtension();
			}

			if (Validator.isNull(mimeType)) {
				mimeType = dlFileEntry.getMimeType();
			}

			if (Validator.isNull(title)) {
				title = sourceFileName;

				if (Validator.isNull(title)) {
					title = dlFileEntry.getTitle();
				}
			}

			Long fileEntryTypeId = (Long)serviceContext.getAttribute(
				"fileEntryTypeId");

			if (fileEntryTypeId == null) {
				fileEntryTypeId = 0L;
			}

			Date now = new Date();

			validateFile(
				dlFileEntry.getGroupId(), dlFileEntry.getFolderId(),
				dlFileEntry.getFileEntryId(), extension, title, sourceFileName,
				is);

			// File version

			String version = dlFileVersion.getVersion();

			if (size == 0) {
				size = dlFileVersion.getSize();
			}

			updateFileVersion(
				user, dlFileVersion, sourceFileName, extension, mimeType, title,
				description, changeLog, extraSettings, fileEntryTypeId, version,
				size, dlFileVersion.getStatus(),
				serviceContext.getModifiedDate(now), serviceContext);

			// File

			if (is != null) {
				try {
					DLStoreUtil.deleteFile(
						user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
						dlFileEntry.getDataRepositoryId(),
						dlFileEntry.getName(), version);
				}
				catch (NoSuchFileException nsfe) {
				}

				DLStoreUtil.updateFile(
					user.getCompanyId(), PortletKeys.DOCUMENT_LIBRARY,
					dlFileEntry.getGroupId(), dlFileEntry.getDataRepositoryId(),
					dlFileEntry.getName(), dlFileEntry.getExtension(), false,
					version, sourceFileName, serviceContext, is);

				// Index

				index(dlFileEntry, serviceContext);
			}

			// Asset

			updateAsset(
				userId, dlFileEntry, dlFileVersion,
				serviceContext.getAssetCategoryIds(),
				serviceContext.getAssetTagNames(),
				serviceContext.getAssetLinkEntryIds());

			if (autoCheckIn) {
				checkInFileEntry(
					userId, fileEntryId, majorVersion, changeLog,
					serviceContext);
			}
		}
		catch (PortalException pe) {
			if (autoCheckIn) {
				cancelCheckOut(userId, fileEntryId);
			}

			throw pe;
		}
		catch (SystemException se) {
			if (autoCheckIn) {
				cancelCheckOut(userId, fileEntryId);
			}

			throw se;
		}
		finally {
			if (!autoCheckIn && !checkedOut) {
				unlockFileEntry(fileEntryId);
			}
		}

		return dlFileEntry;
	}

	protected void updateFileVersion(
			User user, DLFileVersion dlFileVersion, String sourceFileName,
			String extension, String mimeType, String title, String description,
			String changeLog, String extraSettings, long fileEntryTypeId,
			String version, long size, int status, Date statusDate,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		if (Validator.isNotNull(sourceFileName)) {
			dlFileVersion.setExtension(extension);
			dlFileVersion.setMimeType(mimeType);
		}

		dlFileVersion.setTitle(title);
		dlFileVersion.setDescription(description);
		dlFileVersion.setChangeLog(changeLog);
		dlFileVersion.setExtraSettings(extraSettings);
		dlFileVersion.setFileEntryTypeId(fileEntryTypeId);
		dlFileVersion.setVersion(version);
		dlFileVersion.setSize(size);
		dlFileVersion.setStatus(status);
		dlFileVersion.setStatusByUserId(user.getUserId());
		dlFileVersion.setStatusByUserName(user.getFullName());
		dlFileVersion.setStatusDate(statusDate);
		dlFileVersion.setExpandoBridgeAttributes(serviceContext);

		dlFileVersionPersistence.update(dlFileVersion, false);

		Map<String, Fields> fieldsMap =
			(Map<String, Fields>)serviceContext.getAttribute("fieldsMap");

		if (fileEntryTypeId > 0) {
			dlFileEntryMetadataLocalService.updateFileEntryMetadata(
				fileEntryTypeId, dlFileVersion.getFileEntryId(),
				dlFileVersion.getFileVersionId(), fieldsMap,
				serviceContext);
		}
	}

	protected void validateFile(
			long groupId, long folderId, long fileEntryId, String title)
		throws PortalException, SystemException {

		try {
			dlFolderLocalService.getFolder(groupId, folderId, title);

			throw new DuplicateFolderNameException(title);
		}
		catch (NoSuchFolderException nsfe) {
		}

		try {
			DLFileEntry dlFileEntry =
				dlFileEntryPersistence.findByG_F_T(groupId, folderId, title);

			if (dlFileEntry.getFileEntryId() != fileEntryId) {
				throw new DuplicateFileException(title);
			}
		}
		catch (NoSuchFileEntryException nsfee) {
		}
	}

	protected void validateFile(
			long groupId, long folderId, long fileEntryId, String extension,
			String title, String sourceFileName, InputStream is)
		throws PortalException, SystemException {

		if (Validator.isNotNull(sourceFileName)) {
			DLStoreUtil.validate(
				sourceFileName, extension, sourceFileName, true, is);
		}

		validateFileName(title);

		DLStoreUtil.validate(title, false);

		validateFile(groupId, folderId, fileEntryId, title);
	}

	protected void validateFile(
			long groupId, long folderId, String title, String extension,
			InputStream is)
		throws PortalException, SystemException {

		String fileName = title + StringPool.PERIOD + extension;

		validateFileName(fileName);

		DLStoreUtil.validate(fileName, true, is);

		validateFile(groupId, folderId, 0, title);
	}

	protected void validateFileName(String fileName) throws PortalException {
		if (fileName.contains(StringPool.SLASH)) {
			throw new FileNameException(fileName);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		DLFileEntryLocalServiceImpl.class);

}