<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<h1 align="center">Add New Employee</h1>
	<form method="post" action="save">
		${message}<br>
		<br>

		<table align="center">
			<tr>
				<td>Htno :</td>
				<td><input type="text" name="htno" /></td>
			</tr>
			<tr>
				<td>Name :</td>
				<td><input type="text" name="name" /></td>
			</tr>
			<tr>
				<td>College Code :</td>
				<td><input type="text" name="collegecode" /></td>
			</tr>
			<tr>
				<td>Branchcode :</td>
				<td><input type="text" name="branchcode" required /></td>
			</tr>
			<tr>
				<td></td>
				<td><br>
				<input type="submit" value="Save" />&nbsp;&nbsp; <a
					href="viewstudent"><input type="button" value="View Employees" /></a>
					<a href="index.jsp"><input type="button" value="home" /></a>&nbsp;&nbsp;</td>
			</tr>
		</table>
	</form>
</body>
</html>