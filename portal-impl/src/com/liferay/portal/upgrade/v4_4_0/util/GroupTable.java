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

package com.liferay.portal.upgrade.v4_4_0.util;

import java.sql.Types;

/**
 * @author	  Brian Wing Shun Chan
 * @generated
 */
public class GroupTable {

	public static final String TABLE_NAME = "Group_";

	public static final Object[][] TABLE_COLUMNS = {
		{"groupId", Types.BIGINT},
		{"companyId", Types.BIGINT},
		{"creatorUserId", Types.BIGINT},
		{"classNameId", Types.BIGINT},
		{"classPK", Types.BIGINT},
		{"parentGroupId", Types.BIGINT},
		{"liveGroupId", Types.BIGINT},
		{"name", Types.VARCHAR},
		{"description", Types.VARCHAR},
		{"type_", Types.INTEGER},
		{"typeSettings", Types.VARCHAR},
		{"friendlyURL", Types.VARCHAR},
		{"active_", Types.BOOLEAN}
	};

	public static final String TABLE_SQL_CREATE = "create table Group_ (groupId LONG not null primary key,companyId LONG,creatorUserId LONG,classNameId LONG,classPK LONG,parentGroupId LONG,liveGroupId LONG,name VARCHAR(75) null,description STRING null,type_ INTEGER,typeSettings STRING null,friendlyURL VARCHAR(100) null,active_ BOOLEAN)";

	public static final String TABLE_SQL_DROP = "drop table Group_";

	public static final String[] TABLE_SQL_ADD_INDEXES = {
		"create index IX_D0D5E397 on Group_ (companyId, classNameId, classPK)",
		"create index IX_5BDDB872 on Group_ (companyId, friendlyURL)",
		"create index IX_5AA68501 on Group_ (companyId, name)",
		"create index IX_16218A38 on Group_ (liveGroupId)"
	};

}