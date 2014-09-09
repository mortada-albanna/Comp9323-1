<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Registration</title>
</head>
<body>
<H1>Create an Account</H1>
${message}
Please fill in details

<form action="controller" method="post" >

Username: <input type="text" name="username"><br /><br />
Password: <input type="password" name="password"><br /><br />

<input type="hidden" name="action" value="login">
<input type="submit" value="Login">


</form>
<form action="controller" method="post" >
<input type="hidden" name="action" value="register">
<input type="submit" value="Register">


</form>

</body>
</html>