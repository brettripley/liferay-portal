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

<%@ include file="/html/portlet/journal/init.jsp" %>

<%
String langType = ParamUtil.getString(request, "langType");

String editorType = ParamUtil.getString(request, "editorType");

if (Validator.isNotNull(editorType)) {
	portalPreferences.setValue(PortletKeys.JOURNAL, "editor-type", editorType);
}
else {
	editorType = portalPreferences.getValue(PortletKeys.JOURNAL, "editor-type", "html");
}

boolean useEditorCodepress = editorType.equals("codepress");

String defaultContent = ContentUtil.get(PropsUtil.get(PropsKeys.JOURNAL_TEMPLATE_LANGUAGE_CONTENT, new Filter(langType)));
%>

<aui:form method="post" name="editorForm">
	<aui:fieldset>
		<aui:select name="editorType" onChange='<%= renderResponse.getNamespace() + "updateEditorType();" %>'>
			<aui:option label="plain" value="1" />
			<aui:option label="rich" selected="<%= useEditorCodepress %>" value="0" />
		</aui:select>

		<c:choose>
			<c:when test="<%= useEditorCodepress %>">
				 <aui:input cssClass="lfr-template-editor" inputCssClass="codepress html" label="" name="xslContent" type="textarea" wrap="off" />
			</c:when>
			<c:otherwise>
				<aui:input cssClass="lfr-template-editor" inputCssClass="lfr-textarea" label="" name="xslContent" onKeyDown="Liferay.Util.checkTab(this); Liferay.Util.disableEsc();" type="textarea" wrap="off" />
			</c:otherwise>
		</c:choose>
	</aui:fieldset>

	<aui:button-row>
		<aui:button onClick='<%= renderResponse.getNamespace() + "updateTemplateXsl();" %>' value="update" />

		<c:if test="<%= !useEditorCodepress %>">
			<aui:button onClick='<%= "Liferay.Util.selectAndCopy(document." + renderResponse.getNamespace() + "editorForm." + renderResponse.getNamespace() + "xslContent);" %>' value="select-and-copy" />
		</c:if>

		<aui:button type="cancel" />
	</aui:button-row>
</aui:form>

<c:if test="<%= useEditorCodepress %>">
	<script src="<%= themeDisplay.getPathContext() %>/html/js/editor/codepress/codepress.js" type="text/javascript"></script>
</c:if>

<aui:script>
	function <portlet:namespace />getEditorContent() {
		var openerAUI = Liferay.Util.getOpener().AUI();

		var xslContent = openerAUI.one('input[name=<portlet:namespace />xslContent]');

		if (xslContent) {
			var content = decodeURIComponent(xslContent.val());
		}

		if (!content) {
			content = "<%= UnicodeFormatter.toString(defaultContent) %>";
		}

		return content;
	}

	Liferay.provide(
		window,
		'<portlet:namespace />updateEditorType',
		function() {
			var A = AUI();

			<%
			String newEditorType = "codepress";

			if (useEditorCodepress) {
				newEditorType = "html";
			}
			%>

			Liferay.Util.switchEditor(
				{
					textarea: '<portlet:namespace />xslContent',
					uri: '<portlet:renderURL windowState="<%= LiferayWindowState.POP_UP.toString() %>"><portlet:param name="struts_action" value="/journal/edit_template_xsl" /><portlet:param name="langType" value="<%= langType %>" /><portlet:param name="editorType" value="<%= newEditorType %>" /></portlet:renderURL>'
				}
			);
		},
		['aui-base']
	);

	Liferay.provide(
		window,
		'<portlet:namespace />updateTemplateXsl',
		function() {
			var openingWindow = Liferay.Util.getOpener();

			var openerAUI = openingWindow.AUI();

			var xslContent = openerAUI.one('input[name=<portlet:namespace />xslContent]');
			var content = '';

			<c:choose>
				<c:when test="<%= useEditorCodepress %>">
					content = <portlet:namespace />xslContent.getCode();
				</c:when>
				<c:otherwise>
					content = document.<portlet:namespace />editorForm.<portlet:namespace />xslContent.value;
				</c:otherwise>
			</c:choose>

			xslContent.val(encodeURIComponent(content));

			var dialog = Liferay.Util.getWindow();

			if (dialog) {
				dialog.close();
			}
		},
		['aui-dialog']
	);

	Liferay.Util.resizeTextarea('<portlet:namespace />xslContent', <%= useEditorCodepress %>, true);
</aui:script>

<aui:script use="aui-base">
	var textarea = '#<portlet:namespace />xslContent';

	if (<%= useEditorCodepress %>) {
		textarea = '#<portlet:namespace />xslContent_cp';
	}

	A.on(
		'available',
		function(event) {
			A.one(textarea).val(<portlet:namespace />getEditorContent());
		},
		textarea
	);
</aui:script>