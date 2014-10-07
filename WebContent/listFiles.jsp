<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<form method="post" action="controller">
		<input type="hidden" name="action" value="list_files"> <input
			type="submit" value="View">
	</form>
	Here are the files:
	<table>
		<tr>
			<td>File Name</td>
			<td>Download Link</td>
		</tr>
		<c:forEach var="link" items="${links}">
			<tr>
				<td>${link.fileName}</td>
				<td><a href="${link.downloadLink}">link</a></td>
			</tr>
		</c:forEach>
	</table>

</body>
</html>