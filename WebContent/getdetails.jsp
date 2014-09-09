<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Get details from an account</title>
</head>
<body>
<form action="controller" method="post" >

Username: <input type="text" name="username"><br /><br />

<input type="hidden" name="action" value="get_details">
<input type="submit" value="Get Details">


</form>
${message }
</body>
</html>