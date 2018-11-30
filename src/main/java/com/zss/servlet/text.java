package com.zss.servlet;

public class text {

	public static void main(String[] args) throws InterruptedException {
        A00729997 a1 = new A00729997();
        A00729998 a2 = new A00729998();
        for (int i = 0; i <9; i++) {
			if (i%2==0) {
				Thread.sleep(20 * 1000);
				/*System.out.println(date);
				System.out.println(a.generatePlyEdr());
				System.out.println(date);
				System.out.println(a.generatePlyEdr());
				System.out.println(date);*/
				if (a2.sftpUpLoadFile()) {
					System.out.println("A00729998:-------第"+i+"次成功！");
				} else {
					System.out.println("A00729998:-------第"+i+"次失败！");
				}
				if (a1.generatePlyEdr()) {
					System.out.println("A00729997:-------第"+i+"次成功！");
				} else {
					System.out.println("A00729997;-------第"+i+"次失败！");
				}
			}
		
		}
	}

}
