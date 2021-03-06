    /**
     * DefaultScheduler.java - A default scheduling algorithm for market
     * orders that require timing.
     * 
     * Copyright (C) 2011 Josh Dorothy, Ben Harrington, Max Thayer 
     * 
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Affero General Public License as
     * published by the Free Software Foundation, either version 3 of the
     * License, or (at your option) any later version.
     * 
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU Affero General Public License for more details.
     * 
     * You should have received a copy of the GNU Affero General Public License
     * along with this program.  If not, see <http://www.gnu.org/licenses/>.
     */

package com.Bit4Tat;

import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

public class DefaultScheduler extends SchedulerGateway 
{
	private PriorityBlockingQueue<Transaction> transactions;
	private boolean stopped;
	private int pollInterval;
	private final static int BUY = 0;
	private final static int SELL = 1;
	
	public DefaultScheduler()
	{
		pollInterval = 6000;
		stopped = false;
		
		transactions = new PriorityBlockingQueue<Transaction>();
	}
	
	@Override
	public void doWork ()	
	{
		// TODO Auto-generated method stub
		
		while (stopped == false) {
			
			if (!transactions.isEmpty()) {
				
				Transaction t = transactions.peek();
				
				System.out.print("[");
				for (Transaction t2: transactions) {
					System.out.print(t2.amount + ", ");
				}
				System.out.println("]");
				
				System.out.println(t.triggerTime + " - ");
				System.out.println(System.currentTimeMillis() + " = ");
				System.out.println(t.triggerTime - System.currentTimeMillis());
				System.out.println(" -- " + t.expireTime);
				
				if (!t.isExpired()) {
					long curTime = System.currentTimeMillis();
					
					if (t.triggerTime - curTime <= 0) {
						t = transactions.poll();
						
						switch (t.action) {
						
							case BUY:							
								//t.w.getPayGate().service.buy(t.amount);
								//t.w.buy(t.amount, 0);
								//TODO: implement a way to do price too
							case SELL:
								//TODO argument change in sell
								//t.w.getPayGate().service.sell(t.amount);
						}
						
						// Poll the new balance after each completed transaction
						
						t.w = pollBalance(t.w);
					}
					else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else {
					System.out.println("\n  [expired: " + t.amount + "]  \n");
					transactions.poll();
				}
			}
			else {
				
				stopped = true;
				 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
	
	@Override
	public Wallet pollBalance(Wallet cred) 
	{
		// TODO Auto-generated method stub
		return cred;
		
	}

	@Override
	public void conditionalBuy(Wallet w, Object trigger, int action,
			double amount, Object expires) {
		// TODO Auto-generated method stub
		
		transactions.add(new Transaction(w, trigger, action, amount, expires));
	}

	@Override
	public void printScheduler() 
	{
		System.out.println("Default Simple Scheduler in use.");
		
	}

	@Override
	public void conditionalSell(Wallet cred, double amount) 
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @author Josh Dorothy
	 *
	 * Conditions have:
	 * 1) A trigger
	 *	 - A Date object (e.g. elapsed time)
	 *	 - A percentage (e.g. percentage drop/rise in value)
	 *   - Null (no trigger, e.g. immediate)
	 * 2) An action
	 * 	 - Buy
	 *	 - Sell
	 * 3) An amount
	 *	 - int amount - amount of actual Bitcoins
	 *	 - float amount - percentage of actual Bitcoins
	 * 4) An expiration date
	 *	 - A Date object
	 *	 - Null (no expiration)
	 */	
	
	class Transaction implements Comparable<Transaction> {
		
		public Transaction (Wallet w) {
			
		}
		
		public Transaction (Wallet w, Object trigger, int action, double amount, Object expires) {
			
			this.w = w;
			this.action = action;
			this.amount = amount;
			
			expireTime = 0;
			triggerTime = 0;
			
			// Set the trigger time of this object:
			// - If the object is a Date, use that Date's time
			// - Otherwise, this is a constant polling trigger (e.g. waiting
			//   for a percentage change in value) that we set to the current
			//   poll interval.
			
			if (trigger instanceof Date) {
				triggerTime = ((Date)trigger).getTime();				
			}
			else {
				triggerTime = System.currentTimeMillis() + pollInterval;
			}
			
			// Set the expiration time of this object:
			
			if (expires instanceof Date) {
				expireTime = ((Date)expires).getTime();
			}
		}				
		
		@Override
		public int compareTo(Transaction t) {
			// TODO Auto-generated method stub
			
			/*if (triggerTime < t.triggerTime)
				return -1;
			else if (triggerTime == t.triggerTime)
				return -1;
			else
				return 1;*/
			
			if (((Long)triggerTime).compareTo(t.triggerTime) < 1)
				return -1;
			else
				return 1;
			//return ((Long)triggerTime).compareTo(t.triggerTime);
		}		
		
		public boolean isExpired() {
			
			if (expireTime == 0)
				return false;

			long curTime = System.currentTimeMillis();
				
			if (expireTime - curTime <= 0)
				return true;
			else
				return false;
		}
			 
		Wallet w;		

		int     action;
		double  amount;
		
		long expireTime;
		long triggerTime;
	}

}
