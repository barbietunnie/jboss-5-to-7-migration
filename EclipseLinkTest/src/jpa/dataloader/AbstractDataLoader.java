package jpa.dataloader;

public interface AbstractDataLoader {
	public static final String LF = System.getProperty("line.separator", "\n");
	
	public void loadData();
}
