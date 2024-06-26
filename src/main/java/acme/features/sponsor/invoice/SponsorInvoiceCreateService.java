
package acme.features.sponsor.invoice;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.accounts.Principal;
import acme.client.data.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractService;
import acme.entities.sponsorships.Invoice;
import acme.entities.sponsorships.Sponsorship;
import acme.roles.Sponsor;
import acme.utils.Validators;

@Service
public class SponsorInvoiceCreateService extends AbstractService<Sponsor, Invoice> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private SponsorInvoiceRepository	repository;

	@Autowired
	private Validators					validators;


	// AbstractService interface ----------------------------------------------
	@Override
	public void authorise() {
		Principal principal = super.getRequest().getPrincipal();
		Sponsor sponsor = this.repository.findSponsorById(principal.getActiveRoleId());

		int sponsorshipId = super.getRequest().getData("sponsorshipId", int.class);
		Sponsorship sponsorship = this.repository.findSponsorshipById(sponsorshipId);

		boolean authorised = super.getRequest().getPrincipal().hasRole(sponsor) && sponsorship.getSponsor().equals(sponsor) && !sponsorship.isPublished();
		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {
		int sponsorshipId = super.getRequest().getData("sponsorshipId", int.class);
		Sponsorship sponsorship = this.repository.findSponsorshipById(sponsorshipId);

		Invoice invoice = new Invoice();
		invoice.setSponsorship(sponsorship);
		invoice.setRegistrationTime(MomentHelper.getCurrentMoment());

		super.getBuffer().addData(invoice);
	}

	@Override
	public void bind(final Invoice object) {
		assert object != null;

		super.bind(object, "code", "dueDate", "quantity", "tax", "link");
	}

	@Override
	public void validate(final Invoice object) {
		assert object != null;

		if (!super.getBuffer().getErrors().hasErrors("*"))
			super.state(!object.getSponsorship().isPublished(), "*", "sponsor.invoice.form.error.sponsorship-published");

		if (!super.getBuffer().getErrors().hasErrors("code")) {
			Invoice existing = this.repository.findInvoiceByCode(object.getCode());
			super.state(existing == null || existing.equals(object), "code", "sponsor.invoice.form.error.duplicated-code");
		}

		if (!super.getBuffer().getErrors().hasErrors("quantity")) {
			super.state(object.getQuantity().getAmount() > 0, "quantity", "money.error.must-be-positive");
			super.state(object.getQuantity().getAmount() <= 1000000, "quantity", "money.error.exceeded-maximum");
			super.state(this.validators.moneyValidator(object.getQuantity().getCurrency()), "quantity", "money.error.unsupported-currency");
			super.state(object.getQuantity().getCurrency().equalsIgnoreCase(object.getSponsorship().getAmount().getCurrency()), "quantity", "sponsor.invoice.form.error.different-currency");
		}

		if (!super.getBuffer().getErrors().hasErrors("dueDate")) {
			super.state(object.getRegistrationTime().toInstant().plus(30, ChronoUnit.DAYS).isBefore(object.getDueDate().toInstant()), "dueDate", "sponsor.invoice.form.error.dueDate-one-month");
			Date maxDate = MomentHelper.parse("2200-12-31 23:59", "yyyy-MM-dd HH:mm");
			super.state(MomentHelper.isBeforeOrEqual(object.getDueDate(), maxDate), "dueDate", "moment.error.after-max-moment");
		}
	}

	@Override
	public void perform(final Invoice object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Invoice object) {
		assert object != null;

		Dataset dataset = super.unbind(object, "code", "registrationTime", "dueDate", "published", "quantity", "tax", "sponsorship.code");
		dataset.put("project.code", object.getSponsorship().getProject().getCode());
		dataset.put("totalAmount", object.getQuantity() == null ? null : object.getTotalAmount());

		super.getResponse().addData(dataset);
	}
}
