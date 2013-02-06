package jpa.dataloader;

public class DataLoader {

	public static void main(String[] args) {
		DataLoader loader = new DataLoader();
		try {
			loader.loadAllTables();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void loadAllTables() {
		new ClientDataLoader().loadData();
		new IdTokensDataLoader().loadData();
		new EmailAddrLoader().loadData();
		new UserDataLoader().loadData();
		new CustomerDataLoader().loadData();
		new VariableDataLoader().loadData();
	}
}
