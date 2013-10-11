package jpa.dao;

import java.util.List;

import jpa.model.EmailAddress;
import com.googlecode.genericdao.dao.jpa.GenericDAO;

public interface EmailAddressDao extends GenericDAO<EmailAddress, Integer> {

	public List<EmailAddress> getEmailByAddress(String address);
}
