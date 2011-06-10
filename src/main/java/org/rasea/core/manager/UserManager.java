package org.rasea.core.manager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.rasea.core.domain.User;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.util.DateUtils;

@Domain("Users")
public class UserManager extends AbstractSimpleDBManager {

	// FIXME: variável não pode ser estática (vide documentação do DateUtils)
	public final DateUtils DATE_UTILS = new DateUtils();

	/**
	 * Cria a conta do usuário persistindo o id, name, email, password e
	 * activation que já estão preenchidos no objeto passado por parâmetro.
	 * 
	 * @param user
	 */
	public String createAccount(User user) {
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		attrs.add(new ReplaceableAttribute("email", user.getEmail(), true));
		attrs.add(new ReplaceableAttribute("password", user.getPassword(), true));

		String creationDateString = DATE_UTILS.formatIso8601Date(user.getCreation());
		attrs.add(new ReplaceableAttribute("creation", creationDateString, true));

		getSimpleDB().putAttributes(new PutAttributesRequest(getDomain(), user.getLogin(), attrs));

		return null;
	}

	/**
	 * Retorna o usuário com base no seu login.
	 * 
	 * @param login
	 * @return User
	 */
	public User findByLogin(String login) {
		User user = null;

		GetAttributesResult result = getSimpleDB().getAttributes(new GetAttributesRequest(getDomain(), login));
		if (result != null) {
			user = new User();
			user.setLogin(login);
			user = fillUserAttributes(user, result.getAttributes());
		}

		return user;
	}

	/**
	 * Retorna o usuário com base no seu e-mail.
	 * 
	 * @param email
	 * @return
	 */
	public User findByEmail(String email) {
		User user = null;

		// FIXME: essa expressão SQL-like não tá funfando!
		final String expr = "select * from `" + getDomain() + "` where email = '" + email + "'";

		SelectRequest request = new SelectRequest(expr);
		SelectResult result = getSimpleDB().select(request);

		if (result != null) {
			for (Item item : result.getItems()) {
				user = new User();

				user.setLogin(item.getName());
				user = fillUserAttributes(user, item.getAttributes());

				// TODO: o campo "email" será único no domínio, ver como informar no SimpleDB
				break;
			}
		}

		return user;
	}

	private User fillUserAttributes(User user, List<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty())
			return null;

		for (Attribute attr : attributes) {
			final String name = attr.getName();
			final String value = attr.getValue();

			if ("email".equals(name)) {
				user.setEmail(value);
			} else if ("password".equals(name)) {
				user.setPassword(value);
			} else if ("creation".equals(name)) {
				user.setCreation(parseDateValue(value));
			} else if ("activation".equals(name)) {
				user.setActivation(parseDateValue(value));
			}
		}

		return user;
	}

	private Date parseDateValue(final String value) {
		Date date = null;
		if (value != null && !"".equals(value)) {
			try {
				date = DATE_UTILS.parseIso8601Date(value);
			} catch (ParseException e) {
			}
		}
		return date;
	}

	public void deleteAccount(User user) {
		getSimpleDB().deleteAttributes(new DeleteAttributesRequest(getDomain(), user.getLogin()));
	}

	public void activateAccount(User user, String activationCode) {
		// TODO Auto-generated method stub
	}
}
