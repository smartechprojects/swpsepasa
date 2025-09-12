<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link rel="shortcut icon" href="resources/images/favicon.ico"/>
  <title>SEPASA&copy;-Portal de proveedores</title>

<link rel="stylesheet" type="text/css" href="${url}/ext-4.2.1/resources/css/ext-all-gray.css">
<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/ext-all.js"></script>	
<link rel="stylesheet" type="text/css" href="${url}/resources/css/app.css">
<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/locale/ext-lang-es.js"></script>	
<script type="text/javascript" charset="utf-8" src="${url}/app/app.js"></script>
<script type="text/javascript" charset="utf-8" src="${url}/resources/js/common.js"></script>
<!--  <script type="text/javascript" charset="utf-8" src="${url}/resources/js/Currency.js"></script>-->

<script>
	var userId = '<c:out value="${id}"/>';
	var displayName = '<c:out value="${name}"/>';
	var userName = '<c:out value="${userName}"/>';
	var role = '<c:out value="${role}"/>';
	var displayType = '<c:out value="${type}"/>';
	var userType = '<c:out value="${userType}"/>';
	var welcomeMessage = '<c:out value="${welcomeMessage}"/>';
	var addressNumber = '<c:out value="${userName}"/>';
	var userEmail = '<c:out value="${email}"/>';
	var invException = '<c:out value="${invException}"/>';
	var multipleRfc = '<c:out value="${multipleRfc}"/>';
	var approveNotif = '<c:out value="${approveNotif}"/>';
	var pendingDocs = '<c:out value="${pendingDocs}"/>';
	var osSupplier = '<c:out value="${osSupplier}"/>';
	var supplierWithoutOC = '<c:out value="${supplierWithoutOC}"/>';
	var supplierWithOC = '<c:out value="${supplierWithOC}"/>';  
	var dataSupplier = "";
	var supplierProfile = null;
	var tabChgn = "";
	
	Ext.Ajax.request({
	    url: 'supplier/getByAddressNumber.action',
	    method: 'POST',
	    params: {
	    	addressNumber : addressNumber
        },
	    success: function(fp, o) {
	    	var res = Ext.decode(fp.responseText);
	    	supplierProfile = Ext.create('SupplierApp.model.Supplier',res.data);
	    }
	}); 
	 
</script>


</head>
<body>
<div id="page-loader"></div>
<div id="content"></div>
</body>
</html>