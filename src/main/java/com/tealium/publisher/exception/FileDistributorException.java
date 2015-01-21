package com.tealium.publisher.exception;

@SuppressWarnings("serial")
public class FileDistributorException extends RuntimeException {
	public enum Error {
		Unknown(10), IllegalState(1100), FileNotFound(1110), IOException(1120), FTPGeneric(
				2010), Login(2020), IllegalReply(2030), Aborted(2040), DataTransfer(
				2050), ListParse(2060), Connect(2070), TooManyRetrial(2080);
		private final int errorCode;

		Error(int errorCode) {
			this.errorCode = errorCode;
		}

		public int getErrorCode() {
			return this.errorCode;
		}

		public String toString() {
			return this.name() + "#ErrorCode" + this.errorCode;
		}
	};

	public FileDistributorException(Error error) {
		super("Error Code: " + error.getErrorCode());
	}

	public FileDistributorException(String errMsg, Error error) {
		super(errMsg + "[Error Code: " + error.getErrorCode() + "]");
	}

	public FileDistributorException(Throwable t, Error error) {
		super("Error Code: " + error.getErrorCode(), t);
	}

	public FileDistributorException(Throwable t, String errMsg, Error error) {
		super(errMsg + "[Error Code: " + error.getErrorCode() + "]", t);
	}

}
