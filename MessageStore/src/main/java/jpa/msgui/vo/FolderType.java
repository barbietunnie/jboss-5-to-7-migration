package jpa.msgui.vo;

public enum FolderType {
	All, Received, Sent, Draft, Closed, Trash;

	public static FolderType getByName(String name) {
		for (FolderType type : FolderType.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid enum name (" + name + ") received.");
	}

}
