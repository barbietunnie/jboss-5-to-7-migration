package jpa.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.googlecode.genericdao.search.Search;

import jpa.model.EmailAddress;

@Repository
public class EmailAddressDaoImpl extends BaseDao<EmailAddress, Integer> implements EmailAddressDao {

	@Override
	public List<EmailAddress> getEmailByAddress(String address) {
		return search(new Search().addFilterIn("address", address));
	}

}
