/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-tfs
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.epam.reportportal.extension.bugtracking.tfs.html;



/**
 * Img tag representation
 *
 * @author Andrei Varabyeu
 */
public class Img extends ComplexTag {

	private Img() {
		super("img");
	}

	public static Img src(String src, Tag body) {
		Img img = new Img();
		img.attribute("src", src);
		img.addChild(body);
		return img;
	}

	public static Img src(String src, String text) {
		Img img = new Img();
		img.attribute("src", src);
		img.addChild(TagUtils.text(text));
		return img;
	}

	public static Img src(String src, String height, String text) {
		Img img = new Img();
		img.attribute("src", src);
		img.attribute("height", height);
		img.addChild(TagUtils.text(text));
		return img;
	}
}