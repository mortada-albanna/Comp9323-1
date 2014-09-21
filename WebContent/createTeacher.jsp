<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Create a teacher Account</title>
</head>
<body>
<H1>Create an Account</H1>
Please fill in details

<form action="controller" method="post" >


Given Name: <input type="text" name="given_name"><br /><br />
Surname: <input type="text" name="surname"><br /><br />
Email: <input type="text" name="email"><br /><br />
Password: <input type="password" name="password"><br /><br />
Password: <input type="password" name="password2"><br /><br />

<input type="hidden" name="action" value="create_teacher">
<input type="submit" value="Register">


</form>

</body>
</html>