     /**
     * PaymentProcessorForMtGox.java - The trading interface for Mt. Gox.
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;

//import com.Bit4Tat.PaymentService.DefaultTrustManager;

public class PaymentProcessorForMtGox extends PaymentService
{
	private static final String CHECK_BALANCE 		= "https://mtgox.com/code/getFunds.php?";
	private static final String BUY_ORDER 			= "https://mtgox.com/code/buyBTC.php?";
	private static final String SELL_ORDER 			= "https://mtgox.com/code/sellBTC.php?";
	private static final String GET_ORDERS		 	= "https://mtgox.com/code/getOrders.php?";
	private static final String CANCEL_ORDER 		= "https://mtgox.com/code/cancelOrder.php?";
	private static final String SEND_BTC 			= "https://mtgox.com/code/withdraw.php?";
	private static final String CHECK_TICKER		= "https://mtgox.com/api/0/data/ticker.php";
	
	private ResponseContainer response;
	
	public PaymentProcessorForMtGox(String username, String password)
	{
		user = username;
		pass = password;
	}
	
	@Override
	public ResponseContainer checkBalance()
	{
		HttpsURLConnection conn = setupConnection(CHECK_BALANCE);
		
		try {
		// Construct data
		String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
		data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass.trim(), "UTF-8");
		StringBuffer returnString = new StringBuffer();

		try {
			// open up the output stream of the connection
			DataOutputStream wr = new DataOutputStream( conn.getOutputStream() );
			int queryLength = data.length();
			wr.writeBytes( data);
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
			//System.out.println("entering MtGox CheckBalance Parser");
			
			
			//Parse the string into a MtGox Container
			try
			{
				response = new ResponseMtGox();
				response.parseCheckBalance(rd);
			}catch(Exception ex){
				System.out.println("Error filling MtGox json in getResponse().");
				ex.printStackTrace();
			}
			
			//If the json is not parsed, this will print json string to the console.
			String line;
			while ((line = rd.readLine()) != null)
			{
				// Process line...
				System.out.println(line);
				returnString.append(line);
			}
			wr.close();
			rd.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
		conn.disconnect();
		//return a MtGox Response (child of abstract ResponseContainer)
		return response;
	}
	
	@Override
	public ResponseContainer checkTicker() 
	{
		StringBuffer returnString = new StringBuffer();
		
		try {
			
			URL url = new URL(CHECK_TICKER);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(url.openStream()));
		
			//System.out.println("before MtGox Ticker Parser");	
			//Parse the string into a MtGox Container
			/*
			try
			{
				response = new ResponseMtGox();
				response.parseTicker(rd);
			}catch(Exception ex){
				System.out.println("Error filling MtGox json in getResponse().");
				ex.printStackTrace();
			}
			*/
			
			//If the json is not parsed, this will print json string to the console.
			String line;
			while ((line = rd.readLine()) != null)
			{
				// Process line...
				System.out.println(line);
				returnString.append(line);
			}
			rd.close();
		
		
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		//return a MtGox Response (child of abstract ResponseContainer)
		return response;
	}
	
	@Override
	public void buy(double amount, double price) 
	{
		// Uses user credentials (user, pass) to purchase an amount of bitcoins.
		// Set up connection
		HttpsURLConnection conn = setupConnection(BUY_ORDER);
		try 
		{
			// Assemble the user, pass, amount, and price to tack onto conn
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
			data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + URLEncoder.encode(Double.toString(amount), "UTF-8");
			data += "&" + URLEncoder.encode("price", "UTF-8") + "=" + URLEncoder.encode(Double.toString(price), "UTF-8");
			// Pass to MtGox
			getResponse(data, conn);
		}
		// if there are errors, print a stack trace
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		// disconnect
		conn.disconnect();
		// needs to be parsed into a proper response class
	}
	
	@Override
	public void sell(double amount, double price) 
	{
		HttpsURLConnection conn = setupConnection(SELL_ORDER);
		try 
		{
			// Construct data
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
			data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + 
					URLEncoder.encode(new java.text.DecimalFormat("0.00").format(amount), "UTF-8");
			data += "&" + URLEncoder.encode("price", "UTF-8") + "=" + 
					URLEncoder.encode(new java.text.DecimalFormat("0.00").format(price), "UTF-8");
			getResponse(data, conn);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		conn.disconnect();
		// TODO figure out what to return
	}

	@Override
	public void cancelOrder(int orderID) 
	{
		HttpsURLConnection conn = setupConnection(CANCEL_ORDER);
		try 
		{
			// Construct data
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
			data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(orderID), "UTF-8");
			getResponse(data, conn);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		conn.disconnect();
		// TODO figure out what to return
	}

	@Override
	public void getOrders() 
	{
		HttpsURLConnection conn = setupConnection(GET_ORDERS);
		try 
		{
			// Construct data
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
			getResponse(data, conn);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		conn.disconnect();
		// TODO figure out what to return
	}
	// Read and return website responses
	String getResponse (String data, HttpsURLConnection conn) 
	{
		StringBuffer returnString = new StringBuffer();
		try {
			// open up the output stream of the connection
			DataOutputStream wr = new DataOutputStream( conn.getOutputStream() );
			int queryLength = data.length();
			wr.writeBytes( data);
				//OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			//wr.write(data);
			//wr.flush();
			// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null)
		{
		// Process line...
		System.out.println(line);
		returnString.append(line);
			}
		wr.close();
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// needs to be parsed into a proper response class
		return returnString.toString();
	}
	
	// Structure needed for authenticating the connection with MtGox of httpS
	static class DefaultTrustManager implements X509TrustManager
	{
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	
		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	
		@Override
		public X509Certificate[] getAcceptedIssuers() {return null;}
	}
	
	// Sets up a connection to a URL, this is common code for all MtGox API calls.
	HttpsURLConnection setupConnection(String urlString)
	{
		SSLContext ctx = null;

		try {
			ctx = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e1) {
		e1.printStackTrace();
		}
		try {
			ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		} catch (KeyManagementException e) {
		e.printStackTrace();
		}
		SSLContext.setDefault(ctx);


		URL url = null;

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
		e.printStackTrace();
		}
		HttpsURLConnection conn = null;

		try {
			conn = (HttpsURLConnection) url.openConnection();
		} catch (IOException e1) {
		e1.printStackTrace();
		}
		conn.setDoOutput(true);

		conn.setDoInput(true);

		conn.setHostnameVerifier(new HostnameVerifier() 
		{
			@Override
			public boolean verify(String arg0, SSLSession arg1) 
			{
				return true;
			}
		});
		return conn;
	}
}
