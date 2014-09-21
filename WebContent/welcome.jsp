<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	Login Successful
	Welcome ${user }
	you have ${groups } 
   	<c:forEach var="group" items="${groups}">
   	${group } 	
   	</c:forEach>access
   	Upload a file:
   	<form action="controller" method="post" enctype="multipart/form-data">
<input type="hidden" name="action" value="upload_file">

<input type="file" name="uploadedFile">

<input type="submit" value="Upload">

${message }

</form>
   	
</body>
</html>