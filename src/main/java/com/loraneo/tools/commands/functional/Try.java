package com.loraneo.tools.commands.functional;

import java.util.Optional;

public class Try {

	public static <T> Optional<T> mapTry(Exceptional<T> runnable) {
		try {
			return Optional.of(runnable.run());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public interface Exceptional<T> {

		public T run() throws Exception;
	}
}
