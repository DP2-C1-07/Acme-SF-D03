<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>


<acme:form>
	<acme:input-textbox code="client.progress-log.form.label.recordId" path="recordId" placeholder="client.progress-log.form.recordId.placeholder"/>
	<acme:input-double code="client.progress-log.form.label.completeness" path="completeness" placeholder="client.progress-log.form.completeness.placeholder"/>
	<acme:input-textarea code="client.progress-log.form.label.comment" path="comment" placeholder="client.progress-log.form.comment.placeholder"/>
	<acme:input-moment code="client.progress-log.form.label.registrationMoment" path="registrationMoment" />
	<acme:input-textbox code="client.progress-log.form.label.responsiblePerson" path="responsiblePerson" placeholder="client.progress-log.form.responsiblePerson.placeholder"/>
	
	<jstl:choose>
	
		<jstl:when test="${acme:anyOf(_command, 'show|update|delete') && draftMode == true  }">
			<acme:submit code="client.progress-log.form.button.update" action="/client/progress-log/update"/>
			<acme:submit code="client.progress-log.form.button.delete" action="/client/progress-log/delete"/>
			<acme:submit code="client.progress-log.form.button.publish" action="/client/progress-log/publish"/>
		</jstl:when>
		<jstl:when test="${acme:anyOf(_command, 'create')}">
			<acme:submit code="client.progress-log.form.button.create" action="/client/progress-log/create?masterId=${masterId}"/>

		</jstl:when>
	</jstl:choose>

</acme:form>