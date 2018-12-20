package wordcount.model;

public class FileModel {
	private final String fileName;
	private final String contentType;
	private final byte[] bytes;
	
	public FileModel(String originalFilename, String contentType, byte[] bytes) {
		this.fileName = originalFilename;
		this.contentType = contentType;
		this.bytes = bytes;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
