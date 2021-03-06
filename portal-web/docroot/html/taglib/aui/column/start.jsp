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

<%@ include file="/html/taglib/aui/column/init.jsp" %>

<div class="aui-column <%= (columnWidth > 0) ? "aui-w" + columnWidth : StringPool.BLANK %> <%= cssClass %> <%= first ? "aui-column-first" : StringPool.BLANK %> <%= last ? "aui-column-last" : StringPool.BLANK %>" <%= InlineUtil.buildDynamicAttributes(dynamicAttributes) %>>
	<div class="aui-column-content <%= first ? "aui-column-content-first" : StringPool.BLANK %> <%= last ? "aui-column-content-last" : StringPool.BLANK %> <%= cssClasses %>">