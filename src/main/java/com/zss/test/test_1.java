package com.zss.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;



public class test_1 {
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sft = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sft.parse("2018-12-05"));
		
	}

}
