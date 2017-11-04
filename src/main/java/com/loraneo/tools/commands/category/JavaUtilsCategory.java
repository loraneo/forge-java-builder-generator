package com.loraneo.tools.commands.category;

import org.jboss.forge.addon.ui.metadata.UICategory;

public class JavaUtilsCategory implements UICategory {

	@Override
	public UICategory getSubCategory() {
		return null;
	}

	@Override
	public String getName() {
		return "Java utils";
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public static JavaUtilsCategory instance() {
		return new JavaUtilsCategory();
	}
}