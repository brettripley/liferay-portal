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

package com.liferay.portlet.documentlibrary.service;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.util.MethodCache;
import com.liferay.portal.kernel.util.ReferenceRegistry;

/**
 * The utility for the document library content local service. This utility wraps {@link com.liferay.portlet.documentlibrary.service.impl.DLContentLocalServiceImpl} and is the primary access point for service operations in application layer code running on the local server.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see DLContentLocalService
 * @see com.liferay.portlet.documentlibrary.service.base.DLContentLocalServiceBaseImpl
 * @see com.liferay.portlet.documentlibrary.service.impl.DLContentLocalServiceImpl
 * @generated
 */
public class DLContentLocalServiceUtil {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to {@link com.liferay.portlet.documentlibrary.service.impl.DLContentLocalServiceImpl} and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	* Adds the document library content to the database. Also notifies the appropriate model listeners.
	*
	* @param dlContent the document library content
	* @return the document library content that was added
	* @throws SystemException if a system exception occurred
	*/
	public static com.liferay.portlet.documentlibrary.model.DLContent addDLContent(
		com.liferay.portlet.documentlibrary.model.DLContent dlContent)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().addDLContent(dlContent);
	}

	/**
	* Creates a new document library content with the primary key. Does not add the document library content to the database.
	*
	* @param contentId the primary key for the new document library content
	* @return the new document library content
	*/
	public static com.liferay.portlet.documentlibrary.model.DLContent createDLContent(
		long contentId) {
		return getService().createDLContent(contentId);
	}

	/**
	* Deletes the document library content with the primary key from the database. Also notifies the appropriate model listeners.
	*
	* @param contentId the primary key of the document library content
	* @throws PortalException if a document library content with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public static void deleteDLContent(long contentId)
		throws com.liferay.portal.kernel.exception.PortalException,
			com.liferay.portal.kernel.exception.SystemException {
		getService().deleteDLContent(contentId);
	}

	/**
	* Deletes the document library content from the database. Also notifies the appropriate model listeners.
	*
	* @param dlContent the document library content
	* @throws SystemException if a system exception occurred
	*/
	public static void deleteDLContent(
		com.liferay.portlet.documentlibrary.model.DLContent dlContent)
		throws com.liferay.portal.kernel.exception.SystemException {
		getService().deleteDLContent(dlContent);
	}

	/**
	* Performs a dynamic query on the database and returns the matching rows.
	*
	* @param dynamicQuery the dynamic query
	* @return the matching rows
	* @throws SystemException if a system exception occurred
	*/
	@SuppressWarnings("rawtypes")
	public static java.util.List dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	* Performs a dynamic query on the database and returns a range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @return the range of matching rows
	* @throws SystemException if a system exception occurred
	*/
	@SuppressWarnings("rawtypes")
	public static java.util.List dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) throws com.liferay.portal.kernel.exception.SystemException {
		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	* Performs a dynamic query on the database and returns an ordered range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching rows
	* @throws SystemException if a system exception occurred
	*/
	@SuppressWarnings("rawtypes")
	public static java.util.List dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService()
				   .dynamicQuery(dynamicQuery, start, end, orderByComparator);
	}

	/**
	* Returns the number of rows that match the dynamic query.
	*
	* @param dynamicQuery the dynamic query
	* @return the number of rows that match the dynamic query
	* @throws SystemException if a system exception occurred
	*/
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	* Returns the document library content with the primary key.
	*
	* @param contentId the primary key of the document library content
	* @return the document library content
	* @throws PortalException if a document library content with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public static com.liferay.portlet.documentlibrary.model.DLContent getDLContent(
		long contentId)
		throws com.liferay.portal.kernel.exception.PortalException,
			com.liferay.portal.kernel.exception.SystemException {
		return getService().getDLContent(contentId);
	}

	/**
	* Returns a range of all the document library contents.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param start the lower bound of the range of document library contents
	* @param end the upper bound of the range of document library contents (not inclusive)
	* @return the range of document library contents
	* @throws SystemException if a system exception occurred
	*/
	public static java.util.List<com.liferay.portlet.documentlibrary.model.DLContent> getDLContents(
		int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getDLContents(start, end);
	}

	/**
	* Returns the number of document library contents.
	*
	* @return the number of document library contents
	* @throws SystemException if a system exception occurred
	*/
	public static int getDLContentsCount()
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getDLContentsCount();
	}

	/**
	* Updates the document library content in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	*
	* @param dlContent the document library content
	* @return the document library content that was updated
	* @throws SystemException if a system exception occurred
	*/
	public static com.liferay.portlet.documentlibrary.model.DLContent updateDLContent(
		com.liferay.portlet.documentlibrary.model.DLContent dlContent)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().updateDLContent(dlContent);
	}

	/**
	* Updates the document library content in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	*
	* @param dlContent the document library content
	* @param merge whether to merge the document library content with the current session. See {@link com.liferay.portal.service.persistence.BatchSession#update(com.liferay.portal.kernel.dao.orm.Session, com.liferay.portal.model.BaseModel, boolean)} for an explanation.
	* @return the document library content that was updated
	* @throws SystemException if a system exception occurred
	*/
	public static com.liferay.portlet.documentlibrary.model.DLContent updateDLContent(
		com.liferay.portlet.documentlibrary.model.DLContent dlContent,
		boolean merge)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().updateDLContent(dlContent, merge);
	}

	public static com.liferay.portlet.documentlibrary.model.DLContentDataBlobModel getDataBlobModel(
		java.io.Serializable primaryKey)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getDataBlobModel(primaryKey);
	}

	/**
	* Returns the Spring bean ID for this bean.
	*
	* @return the Spring bean ID for this bean
	*/
	public static java.lang.String getBeanIdentifier() {
		return getService().getBeanIdentifier();
	}

	/**
	* Sets the Spring bean ID for this bean.
	*
	* @param beanIdentifier the Spring bean ID for this bean
	*/
	public static void setBeanIdentifier(java.lang.String beanIdentifier) {
		getService().setBeanIdentifier(beanIdentifier);
	}

	public static com.liferay.portlet.documentlibrary.model.DLContent addContent(
		long companyId, java.lang.String portletId, long groupId,
		long repositoryId, java.lang.String path, java.lang.String version,
		byte[] bytes)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService()
				   .addContent(companyId, portletId, groupId, repositoryId,
			path, version, bytes);
	}

	public static com.liferay.portlet.documentlibrary.model.DLContent addContent(
		long companyId, java.lang.String portletId, long groupId,
		long repositoryId, java.lang.String path, java.lang.String version,
		java.io.InputStream inputStream, long size)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService()
				   .addContent(companyId, portletId, groupId, repositoryId,
			path, version, inputStream, size);
	}

	public static void deleteContent(long companyId,
		java.lang.String portletId, long repositoryId, java.lang.String path,
		java.lang.String version)
		throws com.liferay.portal.kernel.exception.PortalException,
			com.liferay.portal.kernel.exception.SystemException {
		getService()
			.deleteContent(companyId, portletId, repositoryId, path, version);
	}

	public static void deleteContents(long companyId,
		java.lang.String portletId, long repositoryId, java.lang.String path)
		throws com.liferay.portal.kernel.exception.SystemException {
		getService().deleteContents(companyId, portletId, repositoryId, path);
	}

	public static com.liferay.portlet.documentlibrary.model.DLContent getContent(
		long companyId, java.lang.String portletId, long repositoryId,
		java.lang.String path, java.lang.String version)
		throws com.liferay.portal.kernel.exception.SystemException,
			com.liferay.portlet.documentlibrary.NoSuchContentException {
		return getService()
				   .getContent(companyId, portletId, repositoryId, path, version);
	}

	public static java.util.List<com.liferay.portlet.documentlibrary.model.DLContent> getContentReferences(
		long companyId, long repositoryId, java.lang.String path)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getContentReferences(companyId, repositoryId, path);
	}

	public static java.util.List<com.liferay.portlet.documentlibrary.model.DLContent> getContents(
		long companyId, java.lang.String portletId, long repositoryId,
		java.lang.String path)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getContents(companyId, portletId, repositoryId, path);
	}

	public static boolean hasContent(long companyId,
		java.lang.String portletId, long repositoryId, java.lang.String path,
		java.lang.String version)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService()
				   .hasContent(companyId, portletId, repositoryId, path, version);
	}

	public static void updateDLContent(long companyId, long oldRepositoryId,
		long newRepositoryId, java.lang.String oldPath, java.lang.String newPath)
		throws com.liferay.portal.kernel.exception.SystemException {
		getService()
			.updateDLContent(companyId, oldRepositoryId, newRepositoryId,
			oldPath, newPath);
	}

	public static DLContentLocalService getService() {
		if (_service == null) {
			_service = (DLContentLocalService)PortalBeanLocatorUtil.locate(DLContentLocalService.class.getName());

			ReferenceRegistry.registerReference(DLContentLocalServiceUtil.class,
				"_service");
			MethodCache.remove(DLContentLocalService.class);
		}

		return _service;
	}

	public void setService(DLContentLocalService service) {
		MethodCache.remove(DLContentLocalService.class);

		_service = service;

		ReferenceRegistry.registerReference(DLContentLocalServiceUtil.class,
			"_service");
		MethodCache.remove(DLContentLocalService.class);
	}

	private static DLContentLocalService _service;
}