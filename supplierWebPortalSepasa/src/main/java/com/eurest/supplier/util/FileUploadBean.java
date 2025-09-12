package com.eurest.supplier.util;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class FileUploadBean {
 
    private CommonsMultipartFile file;
    private CommonsMultipartFile fileTwo;
    private CommonsMultipartFile fileThree;
    private CommonsMultipartFile fileFour;
    private CommonsMultipartFile fileFive;
    private CommonsMultipartFile fileSix;
 
    public CommonsMultipartFile getFile() {
        return file;
    }
 
    public void setFile(CommonsMultipartFile file) {
        this.file = file;
    }

	public CommonsMultipartFile getFileTwo() {
		return fileTwo;
	}

	public void setFileTwo(CommonsMultipartFile fileTwo) {
		this.fileTwo = fileTwo;
	}

	public CommonsMultipartFile getFileThree() {
		return fileThree;
	}

	public void setFileThree(CommonsMultipartFile fileThree) {
		this.fileThree = fileThree;
	}

	public CommonsMultipartFile getFileFour() {
		return fileFour;
	}

	public void setFileFour(CommonsMultipartFile fileFour) {
		this.fileFour = fileFour;
	}

	public CommonsMultipartFile getFileFive() {
		return fileFive;
	}

	public void setFileFive(CommonsMultipartFile fileFive) {
		this.fileFive = fileFive;
	}

	public CommonsMultipartFile getFileSix() {
		return fileSix;
	}

	public void setFileSix(CommonsMultipartFile fileSix) {
		this.fileSix = fileSix;
	}
    
    
}
