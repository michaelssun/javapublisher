package com.tealium.publisher.ftp;


public interface SessionFactory<F> {

	Session<F> getSession();

}
