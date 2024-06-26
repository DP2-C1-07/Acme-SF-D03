<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list navigable="true">
	<acme:list-column code="client.progressLog.list.label.recordId" path="recordId" width="20%"/>
	<acme:list-column code="client.progressLog.list.label.completeness" path="completeness" width="20%"/>
	<acme:list-column code="client.progressLog.list.label.registrationMoment" path="registrationMoment" width="20%"/>
	<acme:list-column code="client.progressLog.list.label.responsiblePerson" path="responsiblePerson" width="20%"/>
	<acme:list-column code="client.progressLog.list.label.draftMode" path="draftMode" width="20%"/>
</acme:list>

<acme:button test="${showCreate}" code="client.progressLog.list.button.create" action="/client/progress-log/create?masterId=${masterId}"/>