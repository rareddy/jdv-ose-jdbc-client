/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class JDBCQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 8320524558076320976L;
	static final MessageFormat ERROR = new MessageFormat("<html><body>${0}</body></html>");
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/index.html");
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        boolean onlyFetch;
        String ds = req.getParameter("datasource");
        String sql = req.getParameter("sql");
        String showTimes = req.getParameter("timesonly");
        onlyFetch = showTimes != null && showTimes.equals("yes");
        PrintWriter out = resp.getWriter();
        if(ds == null || sql == null) {
            out.write(ERROR.format("Invalid Parameters Supplied"));
            return;
        }
        Connection conn = null;
        try {
            InitialContext ctx = new InitialContext();
            DataSource datasource = (DataSource)ctx.lookup(ds);
        	
            conn = datasource.getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            Statement stmt = conn.createStatement();
            long start = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(sql);
            writeResults(rs, out, metadata, start, onlyFetch);
            rs.close();
            stmt.close();
        }
        catch(Exception e){
            e.printStackTrace(out);
        } finally {
        	if(conn != null) {
        		try {conn.close();} catch(Exception exception2) { }
        	}
        }
    }

	String writePara(String msg) {
		return (new StringBuilder("<p>")).append(msg).append("</p>").toString();
	}

	void writeResults(ResultSet rs, PrintWriter out, DatabaseMetaData metadata, long start, boolean onlyFetch)
			throws SQLException {
		out.write("<html>");
		out.write("<body>");
		out.write(writePara(metadata.getDriverName()));
		out.write(writePara(metadata.getDriverVersion()));
		ResultSetMetaData md = rs.getMetaData();
		int colCount = md.getColumnCount();
		if (!onlyFetch) {
			out.write("<table cellspacing='1' cellpadding='1' align='left' border='1'>");
			out.write("<tr>");
			for (int i = 1; i <= colCount; i++) {
				writeColumn(md.getColumnName(i), out);
			}
			out.write("</tr>");
		}
		while (rs.next()) {
			if (!onlyFetch) {
				out.write("<tr>");
			}
			for (int i = 1; i <= colCount; i++) {
				Object obj = rs.getObject(i);
				if (!onlyFetch)
					writeColumn(obj, out);
			}
			if (!onlyFetch) {
				out.write("</tr>");
			}
		}
		if (!onlyFetch) {
			out.write("</table>");
		}
		out.write((new StringBuilder("Time Took: ")).append(System.currentTimeMillis() - start).append(" ms").toString());
		out.write("</body>");
		out.write("</html>");
	}

	void writeColumn(Object msg, PrintWriter out) {
		out.write("<td>");
		if (msg != null) {
			out.write(msg.toString());
		} else {
			out.write("null");
		}
		out.write("</td>");
	}
}
