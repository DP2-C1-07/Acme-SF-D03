
package acme.features.auditor.auditrecord;

import java.sql.Date;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.client.views.SelectChoices;
import acme.entities.audit_records.AuditRecord;
import acme.entities.codeaudits.CodeAudit;
import acme.entities.codeaudits.Mark;
import acme.features.auditor.codeaudit.AuditorCodeAuditRepository;
import acme.roles.Auditor;

@Service
public class AuditorAuditRecordCreateService extends AbstractService<Auditor, AuditRecord> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private AuditorAuditRecordRepository	auditorAuditRecordRepository;

	@Autowired
	private AuditorCodeAuditRepository		auditorCodeAuditRepository;


	// AbstractService interface ----------------------------------------------
	@Override
	public void authorise() {
		boolean status;
		int codeAuditId;
		Auditor auditor;
		CodeAudit codeAudit;

		codeAuditId = super.getRequest().getData("codeAuditId", int.class);
		codeAudit = this.auditorCodeAuditRepository.findOneById(codeAuditId);
		auditor = codeAudit.getAuditor();

		status = codeAudit != null && super.getRequest().getPrincipal().hasRole(auditor) && codeAudit.getAuditor().equals(auditor);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		AuditRecord object;
		CodeAudit codeAudit;
		int codeAuditId;

		codeAuditId = super.getRequest().getData("codeAuditId", int.class);
		codeAudit = this.auditorCodeAuditRepository.findOneById(codeAuditId);

		object = new AuditRecord();
		object.setCodeAudit(codeAudit);

		super.getBuffer().addData(object);
	}

	@Override
	public void bind(final AuditRecord object) {
		assert object != null;

		CodeAudit codeAudit;
		codeAudit = object.getCodeAudit();
		super.bind(object, "code", "periodBeginning", "periodEnd", "mark", "link");
		object.setCodeAudit(codeAudit);
	}

	@Override
	public void validate(final AuditRecord object) {
		assert object != null;

		if (!super.getBuffer().getErrors().hasErrors("periodEnd")) {
			long diffInMili;
			long diffInHour;

			super.state(object.getPeriodEnd().after(Date.valueOf("2000-1-1")) || object.getPeriodEnd().equals(Date.valueOf("2000-1-1")), "periodEnd", "auditor.code-audit.error.executionDate");

			if (object.getPeriodBeginning() != null) {
				diffInMili = object.getPeriodEnd().getTime() - object.getPeriodBeginning().getTime();
				diffInHour = TimeUnit.MILLISECONDS.toHours(diffInMili);
				super.state(diffInHour >= 1, "periodEnd", "auditor.audit-record.error.duration");
				super.state(object.getPeriodBeginning() != null && object.getPeriodBeginning().before(object.getPeriodEnd()), "periodEnd", "auditor.audit-record.error.consecutiveDates");
			}
		}

		if (!super.getBuffer().getErrors().hasErrors("periodBeginning"))
			super.state(object.getPeriodBeginning().after(Date.valueOf("2000-1-1")) || object.getPeriodBeginning().equals(Date.valueOf("2000-1-1")), "periodBeginning", "auditor.code-audit.error.executionDate");

		if (!super.getBuffer().getErrors().hasErrors("code")) {
			AuditRecord existing;

			existing = this.auditorAuditRecordRepository.findOneByCode(object.getCode());
			super.state(existing == null, "code", "auditor.audit-record.error.code");
		}
	}

	@Override
	public void perform(final AuditRecord object) {
		assert object != null;
		this.auditorAuditRecordRepository.save(object);
	}

	@Override
	public void unbind(final AuditRecord object) {
		assert object != null;

		SelectChoices choices;
		Dataset dataset;

		CodeAudit codeAudit;
		codeAudit = object.getCodeAudit();

		choices = SelectChoices.from(Mark.class, object.getMark());
		dataset = super.unbind(object, "code", "periodBeginning", "periodEnd", "link", "draftMode");
		dataset.put("mark", choices);
		dataset.put("codeAudit", codeAudit);
		super.getResponse().addData(dataset);
	}

	@Override
	public void unbind(final Collection<AuditRecord> objects) {
		assert objects != null;

		int codeAuditId;

		codeAuditId = super.getRequest().getData("codeAuditId", int.class);

		super.getResponse().addGlobal("codeAuditId", codeAuditId);
	}
}
