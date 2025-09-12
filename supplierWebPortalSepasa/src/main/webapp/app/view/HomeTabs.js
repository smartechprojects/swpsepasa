Ext.require('Ext.chart.*');
Ext.require(['Ext.layout.container.Fit', 'Ext.window.MessageBox']);
Ext.define('SupplierApp.view.HomeTabs', {
	extend : 'Ext.tab.Panel',
	alias : 'widget.homeTabs',
	frame : false,
	id : 'HomeTabs',
	cls : 'tabpanel',
	activeTab: 0,
    plain: false,
	border : false,
	height:520,
	style : 'background-color:#fff;font-color:#000;margin-top:10px;',
	tabBar: {
        width: 220,
        height: 30,
        defaults: {height:30},

    },
    listeners: {
    	'afterrender': function(component, eOpts) {
    		/*
	       if(role=='ROLE_ADMIN' ||  role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || (role=='ROLE_SUPPLIER' && supplierWithoutOC == 'TRUE')){
	    	   Ext.getCmp('mainPanelId').getComponent('fiscalDocumentsPanel').tab.show();
	       } else if(role == 'ROLE_SUPPLIER') {
	      		Ext.Ajax.request({
	    		    url: 'supplier/getCustomBroker.action',
	    		    method: 'POST',
	    		    params: {
	    		    	addressNumber : addressNumber
	    	        },
	    		    success: function(fp, o) {
	    		    	var res = Ext.decode(fp.responseText);
	    		    	var isCustomBroker = res.data;
	    		    	if(isCustomBroker == true){
	    		    		Ext.getCmp('mainPanelId').getComponent('fiscalDocumentsPanel').tab.show();
	    		    	} else {
	    		    		Ext.getCmp('mainPanelId').getComponent('fiscalDocumentsPanel').tab.hide();
	    		    	}
	    		    },
	    	        failure: function(fp, o) {
	    	        	Ext.getCmp('mainPanelId').getComponent('fiscalDocumentsPanel').tab.hide();
	    	        }
	       		});
	       } else {
	    	   Ext.getCmp('mainPanelId').getComponent('fiscalDocumentsPanel').tab.hide();
	       }
	       
	       if(role=='ROLE_ADMIN' || role=='ROLE_CXP_IMPORT' || role=='ROLE_PURCHASE_IMPORT' || supplierWithOC == 'TRUE'){
	    	    Ext.getCmp('mainPanelId').getComponent('purchaseOrderPanel').tab.show();
	    	    //Ext.getCmp('factVsJde').show();
	    	    Ext.getCmp('lblReplicacion').show();
	    		Ext.getCmp('fromDate').show();
		    	Ext.getCmp('toDate').show();
		    	Ext.getCmp('addressNumber').show();
		    	Ext.getCmp('orderNumber').show();
		    	Ext.getCmp('poLoadPurchases').show();
	       } else {
	    	   if(role == 'ROLE_SUPPLIER'){
					//Función de Complemento de Pago
	    		   	if(supplierProfile != null){
	    			   	if(supplierProfile.data.country != 'MX'){
	    			   		Ext.getCmp('poLoadCompl').hide();
	    			   	}
	    		   	}
	    	   } else {					
					//Función Import
		      		Ext.Ajax.request({
		    		    url: 'admin/udc/containsValue.action',
		    		    method: 'GET',
		    		    params: {
							query:'',
							udcSystem : 'FUNCTION',
							udcKey : 'PORTVSJDE',
							strValue1 : userName,
							strValue2 : ''							
		    	        },
		    		    success: function(fp, o) {
		    		    	var res = Ext.decode(fp.responseText);
		    		    	var isImportOCAllowed = res.data;
		    		    	if(isImportOCAllowed == true){
		    		    		Ext.getCmp('mainPanelId').getComponent('purchaseOrderPanel').tab.show();
		    		    		//Ext.getCmp('factVsJde').show();
		    		    	}
		    		    },
		    	        failure: function(fp, o) {
		    	        }
		       		});

		      		//Función Reporte de Facturas
		      		Ext.Ajax.request({
		      			url: 'admin/udc/containsValue.action',
		    		    method: 'GET',
		    		    params: {
							query:'',
							udcSystem : 'FUNCTION',
							udcKey : 'IMPORT',
							strValue1 : userName,
							strValue2 : ''
		    	        },
		    		    success: function(fp, o) {
		    		    	var res = Ext.decode(fp.responseText);
		    		    	var isInvReportAllowed = res.data;
		    		    	if(isInvReportAllowed == true){
		    		    		Ext.getCmp('mainPanelId').getComponent('purchaseOrderPanel').tab.show();
		    		    		Ext.getCmp('lblReplicacion').show();
		    		    		Ext.getCmp('fromDate').show();
		    			    	Ext.getCmp('toDate').show();
		    			    	Ext.getCmp('addressNumber').show();
		    			    	Ext.getCmp('orderNumber').show();
		    			    	Ext.getCmp('poLoadPurchases').show();
		    		    	}
		    		    },
		    	        failure: function(fp, o) {
		    	        }
		       		});
	    	   }
	       }
	       */
    	},
    
        'tabchange': function (tabPanel, tab) {	
            /*if(tab.id == 'udcTabPanel'){
            	debugger;
            	SupplierApp.Current.getController('Udc');
            	var obj = Ext.ComponentQuery.query('udcPanel')
            	tab.add({ xtype: 'udcPanel'});
            	tab.doLayout();
            	tabPanel.getUpdater().refresh();
            }
            */
        	/*var notice ='<div class="window-notice" id="window-notice">'+
		    '<div class="content">'+
	        '<div class="content-text" style="font-size:15px;text-align:center;color:red">¡Aviso Importante! </div><br>'+
	       ' <div style="font-size:15px;text-align:center;color:red"> <p>Usted actualizó sus datos de proveedor, por lo cual; su cuenta ha sido bloqueada temporalmente, una vez revisados y autorizados los cambios por SEPASA usted podrá ingresar nuevamente al portal.</p>'+
	        '	<br>'+
	        '	<p>¿Dudas? Favor de contactar a su comprador. Gracias</p>'+
	        '</div>'+
	        '<div class="content-buttons"><button style="height:30;width:60" onclick="closeNoLlogin()">Aceptar</button></div>'+
	    '</div>'+
		'</div>';*/
        	
        	if(tab.id == 'noticeTab'){
        		var controller = _AppGlobSupplierApp.getController('Notice');
        		var form = controller.getNoticeForm().getForm();
        		form.reset();
        		Ext.getCmp('suppliersNotice').setValue('');
        		Ext.getCmp('idNotice').setValue(Math.floor(1000000 + Math.random() * 900000));
        	}
        	
        	if(tab.id == 'supplierTab') tabChgn = 'suppliers';
        }
    },
	initComponent : function() {
		var me = this;
		this.noticeWindow = null;
		/*
		if(role == 'ROLE_SUPPLIER'){
    		Ext.Ajax.request({
				url : 'notice/noticeActivesBySupp.action',
				method : 'GET',
					params : {
						supp : userName
					},
					success : function(response,opts) {
						response = Ext.decode(response.responseText);
						//rec = Ext.create('SupplierApp.model.NoticeDTO');
						//rec.set(response.data);
			        	//form.loadRecord(rec);
			        	var fileList = response.data.docNotice;
			        	var hrefList ="";
			        	var r2 = [];
			        	var href ="";
			        	var fileHref="";

			        	var r1 = fileList.split("_FILE:");
			        	var inx = r1.length;
			        	for (var index = inx - 1; index >= 0; index--) {
			        		r2 = r1[index].split("_:_");
				        		if(r2[0]  != ""){
					        	href = "notice/openDocument.action?id=" + r2[0];
								fileHref = "<a href= '" + href + "' target='_blank'>" +  r2[1] + "</a>";
								hrefList = "<p>"  + hrefList + fileHref + "</p>";
			        		}
			        	} 
			        	//Ext.getCmp('hrefFileList').setValue(hrefList);
			        	
			        	var filePanel = Ext.create(
		    					'Ext.form.Panel',
		    					{
		    						width : 900,
		    						items : [
			    							{
												xtype : 'textfield',
												name : 'idNoticeDoc',
												hidden : true,
												margin:'20 0 0 10',
												value : response.data.idNotice 
											},{
		    									xtype : 'textfield',
		    									name : 'createdBy',
		    									hidden : true,
		    									margin:'20 0 0 10',
		    									value : userName 
		    								},{
		    									xtype : 'textfield',
		    									name : 'statusNotice',
		    									hidden : true,
		    									id : 'statusNotice',
		    								},{
	    										xtype : 'displayfield',
	    										value : hrefList,
	    										height:20,
	    										margin: '0 110 0 0',
	    										colspan:3,
	    										fieldStyle: 'font-weight:bold' 
	    									},
	        								{
	        									xtype : 'filefield',
	        									name : 'file',
	        									fieldLabel :SuppAppMsg.purchaseFile,
	        									labelWidth : 120,
	        									msgTarget : 'side',
	        									//allowBlank : false,
	        									allowBlank : response.data.docSupplier == true ?false:true,
	        									hidden: response.data.docSupplier == true ?false:true,
	        									margin:'10 0 30 0',
	        									anchor : '80%',
	        									buttonText : SuppAppMsg.suppliersSearch
	        								}
			    							],
			    							buttons : [ {
    			    							text : 'Aceptar',
    			    							margin:'10 0 0 0',
    			    							handler : function() {
    			    								Ext.getCmp('statusNotice').setValue('ACEPTADO');
    			    								var form = this.up('form').getForm();
    			    								if (form.isValid()) {
    			    									form.submit({
    			    												url : 'uploadFileNoticeSupplier.action',
    			    												waitMsg : SuppAppMsg.supplierLoadFile,
    			    												success : function(fp, o) {
    			    													noticeWindow.destroy();
    			    													if(noticeWindow){
    			    														noticeWindow.close();
    			    													}
    			    													
    			    												},       // If you don't pass success:true, it will always go here
    			    										        failure: function(fp, o) {
    			    										        	var res = o.response.responseText;
    			    										        	var result = Ext.decode(res);
    			    										        	var msgResp = result.message
    			    										        	
    			    										        	Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad, msg:  msgResp});
    			    										        	
    			    										        	
    			    										        	
    			    										        }
    			    											});
    			    								}
    			    							}
    			    						},{
    			    							text : 'Rechazar',
    			    							margin:'10 0 0 0',
    			    							handler : function() {
    			    								if(response.data.required == true){
    			    									location.href = "login.action";
    			    								}else{
    			    									var form = this.up('form').getForm();
        			    								Ext.getCmp('statusNotice').setValue('RECHAZADO');
        			    									form.submit({
        			    												url : 'uploadFileNoticeSupplier.action',
        			    												waitMsg : SuppAppMsg.supplierLoadFile,
        			    												success : function(fp, o) {
        			    													noticeWindow.destroy();
        			    													if(noticeWindow){
        			    														noticeWindow.close();
        			    													}
        			    													
        			    												},       // If you don't pass success:true, it will always go here
        			    										        failure: function(fp, o) {
        			    										        	noticeWindow.destroy();
        			    													if(noticeWindow){
        			    														noticeWindow.close();
        			    													}
        			    										        	
        			    										        	
        			    										        }
        			    											});
    			    								}
    			    							}
    			    						
    			    						} ]
		    					});
			        	
			        	
			        	this.noticeWindow = new Ext.Window({
    			    		layout : 'fit',
    			    		title : response.data.noticeTitle,
    			    		width : 600,
    			    		height : 200,
    			    		modal : true,
    			    		closeAction : 'destroy',
    			    		resizable : false,
    			    		closable: false,
    			    		minimizable : false,
    			    		maximizable : false,
    			    		plain : true,
    			    		items : [ filePanel ]
    			
    			    	});
    			    	this.noticeWindow.show();
        	
        	
    			}
    		});
    	}*/
		
		var supplierProfilePanel = new Ext.FormPanel({
		    title: SuppAppMsg.tabInfoSupplier,
		    hidden:role=='ROLE_SUPPLIER' ?false:true,
		    bodyStyle:'padding:5px 5px 0',
		    items: [{
		    	 xtype: 'supplierForm',
	           	 height:490,
	           	 id:'supplierFormId'
		    }]
		});
		
		if(supplierProfile != null){
			var form = Ext.getCmp('supplierFormId');
			form.loadRecord(supplierProfile);
			if(supplierProfile.data.country == 'MX'){
				Ext.getCmp('supRfc').setReadOnly(false);
				//Ext.getCmp('documentContainerForeingResidence').hide();
				Ext.getCmp('supRfc').allowBlank=false;
				Ext.getCmp('supRfc').show();
				Ext.getCmp('taxId').allowBlank=true;
				Ext.getCmp('taxId').hide();
				
				Ext.getCmp('documentContainerForeingResidence').hide();				
				Ext.getCmp('fldColonia').show();
				Ext.getCmp('fldColonia').allowBlank=false;
				
				Ext.getCmp('coloniaEXT').hide();
				Ext.getCmp('coloniaEXT').allowBlank=true;
				
				Ext.getCmp('fldMunicipio').show();
				Ext.getCmp('fldMunicipio').allowBlank=false;
				
				//Ext.getCmp('searchCP').hide();
				Ext.getCmp('codigoPostal').allowBlank=false;
			}else{
				Ext.getCmp('supRfc').setReadOnly(true);
				//Ext.getCmp('documentContainerForeingResidence').show();
				Ext.getCmp('taxId').allowBlank=false;
				Ext.getCmp('taxId').show();
				Ext.getCmp('supRfc').allowBlank=true;
				Ext.getCmp('supRfc').hide();
				
				Ext.getCmp('documentContainerForeingResidence').show();
				
				Ext.getCmp('coloniaEXT').allowBlank=false;
				Ext.getCmp('coloniaEXT').show();
				Ext.getCmp('coloniaEXT').setValue(supplierProfile.data.colonia);
				
				Ext.getCmp('fldColonia').hide();
				Ext.getCmp('fldColonia').allowBlank=true;
				
				Ext.getCmp('fldMunicipio').hide();
				Ext.getCmp('fldMunicipio').allowBlank=true;
				
				Ext.getCmp('searchCP').hide();
				Ext.getCmp('codigoPostal').allowBlank=true;
			}
			
			if(supplierProfile.data.fisicaMoral == 'F'){
				Ext.getCmp('curp').show();
    			Ext.getCmp('curp').allowBlank=false;
    			
    			Ext.getCmp('REPRESENTE_LEGAL').hide();
    			Ext.getCmp('tipoIdentificacion').allowBlank=true;
    			Ext.getCmp('tipoIdentificacion').hide();
    			Ext.getCmp('numeroIdentificacion').allowBlank=true;
    			Ext.getCmp('numeroIdentificacion').hide();
    			Ext.getCmp('nombreRL').allowBlank=true;
    			Ext.getCmp('nombreRL').hide();
    			Ext.getCmp('apellidoPaternoRL').allowBlank=true;
    			Ext.getCmp('apellidoPaternoRL').hide();
    			Ext.getCmp('apellidoMaternoRL').hide();
			}else{
				Ext.getCmp('curp').hide();
    			Ext.getCmp('curp').allowBlank=true;
    			
    			Ext.getCmp('REPRESENTE_LEGAL').show();
    			Ext.getCmp('tipoIdentificacion').allowBlank=false;
    			Ext.getCmp('tipoIdentificacion').show();
    			Ext.getCmp('numeroIdentificacion').allowBlank=false;
    			Ext.getCmp('numeroIdentificacion').show();
    			Ext.getCmp('nombreRL').allowBlank=false;
    			Ext.getCmp('nombreRL').show();
    			Ext.getCmp('apellidoPaternoRL').allowBlank=false;
    			Ext.getCmp('apellidoPaternoRL').show();
    			Ext.getCmp('apellidoMaternoRL').show();
			}
			
			Ext.getCmp('dataTax').hide();
			
			var fileList = supplierProfile.data.fileList;
	    	var hrefList ="";
	    	var r2 = [];
	    	var href ="";
	    	var fileHref="";
	
	    	var r1 = fileList.split("_FILE:");
	    	var inx = r1.length;
	    	for (var index = inx - 1; index >= 0; index--) {
	    		r2 = r1[index].split("_:_");
	        		if(r2[0]  != ""){
		        	href = "documents/openDocumentSupplier.action?id=" + r2[0];
					fileHref = "*** <a href= '" + href + "' target='_blank'>" +  r2[1] + "</a> ||" + r2[2];
					hrefList = "<p>"  + hrefList + fileHref + "</p>";
	    		}
	    	} 
	    	Ext.getCmp('hrefFileList').setValue(hrefList);
		}
		
		
		var store = Ext.create('Ext.data.JsonStore', {
		    fields: ['name', 'data'],
		    data: [
		        { 'name': 'Recibidas',   'data': 10 },
		        { 'name': 'Enviadas',   'data':  7 },
		        { 'name': 'Facturadas', 'data':  5 },
		        { 'name': 'Aprobadas',  'data':  2 },
		        { 'name': 'Pagadas',  'data': 27 },
		        { 'name': 'Con complemento',  'data': 43 }
		    ]
		});

		
		var chart = Ext.create('Ext.chart.Chart', {
		    width: 300,
		    height: 100,
		    animate: true,
		    store: store,
		    margin:'20 20 20 20',
		    theme: 'Base:gradients',
		    series: [{
		        type: 'pie',
		        angleField: 'data',
		        showInLegend: true,
		        tips: {
		            trackMouse: true,
		            width: 140,
		            height: 28,
		            renderer: function(storeItem, item) {
		                var total = 0;
		                store.each(function(rec) {
		                    total += rec.get('data');
		                });
		                this.setTitle(storeItem.get('name') + ': ' + Math.round(storeItem.get('data') / total * 100) + '%');
		            }
		        },
		        highlight: {
		            segment: {
		                margin: 20
		            }
		        },
		        label: {
		            field: 'name',
		            display: 'rotate',
		            contrast: true,
		            font: '14px Arial'
		        },
		       
		    }]
		});
		
		var homeTable=
			"<div style='margin-left:50px;margin-right:auto;'>" +
			"<table>"+
			'<tr>'+
			'<td colspan="2">'+
			'<div style="text-align: center; vertical-align: middle;"><IMG SRC="resources/images/contactos_saavi_titulo.png" style="margin-left:auto;margin-right:auto;""></div>' +
			"</td>"+
			"</tr>"+
			'<tr>'+
			/*"<td>"+
			'<div align="center"><IMG SRC="resources/images/fechas saavi.jpg" width="80%" height="80%" style="margin-left:auto;margin-right:auto;""></div>' +
			"</td>"+*/
			'<td style="text-align: center; vertical-align: top;">'+
			'<div><IMG SRC="resources/images/contactos_saavi_2.png"></div>' +
			"</td>"+
			'<td style="text-align: center; vertical-align: top;">'+
			'<div><IMG SRC="resources/images/contactos_saavi_1.png"></div>' +
			"</td>"+
			/*"<td>"+
			'<div style="text-align: center; vertical-align: middle;"><IMG SRC="resources/images/contactos_saavi.jpg" width="80%" height="50%" style="margin-left:auto;margin-right:auto;""></div>' +
			" <h1 style='font-size:15px;text-align:center'>En caso de dudas o aclaraciones y dependiendo de la razón social a la que esté<br>proporcionando servicios favor de contactar al personal de Compras correspondiente:</h1><br>" +
			"<table BORDER  style='font-size:10px;border:1px solid #000;margin-left:auto;margin-right:auto;' >" +
			"  <tr>" +
			 "   <th COLSPAN=3 style='background-color:#0781C2;'><b>Contactos Compras</b></th>" +
			 " </tr>" +
			"  <tr style='background-color:#7FD1FD;'>" +
			 "   <th><b>Razón Social</b></th>" +
			 "   <th>Comprador</th>" +
			 "   <th>Correo electronico</th>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td>Energía Azteca X, S.A. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;'rowspan='2'>Jose Angel Romero</td>" +
			 "   <td style='border-bottom:1px solid #000;' rowspan='2'>jose.romero@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Energía de Baja California, S. de R.L. de C.V.</td>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td>Energía San Luis de la Paz, S.A. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;' rowspan='2'>Jorge Duran</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>jorge.duran@saavienergia.com</td>" +
			 " </tr>" +
			  "<tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Energía Azteca VIII, S. de R.L. de C.V.</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;'>Energía Campeche, S.A. de C.V.</td>" +
			"	<td style='border-bottom:1px solid #000;'>Marco J. Romano</td>" +
			 " 	<td style='border-bottom:1px solid #000;'>Marco.Romano@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			  "  <td>Compresión Altamira, S.A. de C.V.</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>Delia Balboa</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>delia.balboa@saavienergia.com</td>" +
			 " </tr>" +
			  "<tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Compresión Bajío, S. de R.L. de C.V.</td>" +	    
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;'>Energía Chihuahua, S.A. de C.V.</td>" +
			 " <td style='border-bottom:1px solid #000;' >Ixchel Vazquez</td>" +
			 " <td style='border-bottom:1px solid #000;' >Ixchel.Vazquez@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td style='border-bottom:1px solid #000;'>GDD de Personal S. de R.L. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;'>Joel Carrera</td>" +
			 " 	<td style='border-bottom:1px solid #000;'>joel.carrera@saavienergia.com</td>" +
			 " </tr>" +
			"</table>" + 
			"<br>"+
			"<table style='font-size:10px;border:1px solid #000;margin-left:auto;margin-right:auto;' >" +
			"  <tr>" +
			 "   <th COLSPAN=2 style='background-color:#0781C2;'><b>Contactos Cuentas por pagar</b></th>" +
			 " </tr>" +
			 "  <tr style='background-color:#7FD1FD;'>" +
			 "   <th><b>Contacto</b></th>" +
			 "   <th>Correo electronico</th>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Enrique Sanchez</td>" +
			 " <td style='border-bottom:1px solid #000;' >Enrique.Sanchez@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Maria Cardenas</td>" +
			 " <td style='border-bottom:1px solid #000;' >Maria.Cardenas@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Tania De La Cruz</td>" +
			 " <td style='border-bottom:1px solid #000;' >Tania.DeLaCruz@saavienergia.com</td>" +
			 " </tr>" +
			"</table>" +*/ 
			"</td>"+
			"</tr>"+
			"</table>"+
			"</div>";
		
		/*var homeTable=
			"<div style='font-size:6px;  border-collapse: collapse;margin-top:3%;margin-left:auto;margin-right:auto;'>" + 
			" <h1>En caso de dudas o aclaraciones y dependiendo de la razón social a la que esté proporcionando<br>servicios favor de contactar al personal de Compras correspondiente:</h1><br>" +
			"<table BORDER  style='width:50%;font-size:10px;border:1px solid #000;margin-left:auto;margin-right:auto;' >" +
			"  <tr>" +
			 "   <th COLSPAN=3 style='background-color:#0781C2;'><b>Contactos Compras</b></th>" +
			 " </tr>" +
			"  <tr style='background-color:#7FD1FD;'>" +
			 "   <th><b>Razón Social</b></th>" +
			 "   <th>Comprador</th>" +
			 "   <th>Correo electronico</th>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td>Energía Azteca X, S.A. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;'rowspan='2'>Jose Angel Romero</td>" +
			 "   <td style='border-bottom:1px solid #000;' rowspan='2'>jose.romero@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Energía de Baja California, S. de R.L. de C.V.</td>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td>Energía San Luis de la Paz, S.A. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;' rowspan='2'>Jorge Duran</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>jorge.duran@saavienergia.com</td>" +
			 " </tr>" +
			  "<tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Energía Azteca VIII, S. de R.L. de C.V.</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;'>Energía Campeche, S.A. de C.V.</td>" +
			"	<td style='border-bottom:1px solid #000;'>Marco J. Romano</td>" +
			 " 	<td style='border-bottom:1px solid #000;'>Marco.Romano@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			  "  <td>Compresión Altamira, S.A. de C.V.</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>Delia Balboa</td>" +
			  "  <td style='border-bottom:1px solid #000;' rowspan='2'>delia.balboa@saavienergia.com</td>" +
			 " </tr>" +
			  "<tr>" +
			  "  <td style='border-bottom:1px solid #000;'>Compresión Bajío, S. de R.L. de C.V.</td>" +	    
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;'>Energía Chihuahua, S.A. de C.V.</td>" +
			 " <td style='border-bottom:1px solid #000;' >Ixchel Vazquez</td>" +
			 " <td style='border-bottom:1px solid #000;' >Ixchel.Vazquez@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 "   <td style='border-bottom:1px solid #000;'>GDD de Personal S. de R.L. de C.V.</td>" +
			 "   <td style='border-bottom:1px solid #000;'>Joel Carrera</td>" +
			 " 	<td style='border-bottom:1px solid #000;'>joel.carrera@saavienergia.com</td>" +
			 " </tr>" +
			"</table>" + 
			"<br>"+
			"<table style='width:25%;font-size:10px;border:1px solid #000;margin-left:auto;margin-right:auto;' >" +
			"  <tr>" +
			 "   <th COLSPAN=2 style='background-color:#0781C2;'><b>Contactos Cuentas por pagar</b></th>" +
			 " </tr>" +
			 "  <tr style='background-color:#7FD1FD;'>" +
			 "   <th><b>Contacto</b></th>" +
			 "   <th>Correo electronico</th>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Enrique Sanchez</td>" +
			 " <td style='border-bottom:1px solid #000;' >Enrique.Sanchez@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Maria Cardenas</td>" +
			 " <td style='border-bottom:1px solid #000;' >Maria.Cardenas@saavienergia.com</td>" +
			 " </tr>" +
			 " <tr>" +
			 " <td style='border-bottom:1px solid #000;' >Tania De La Cruz</td>" +
			 " <td style='border-bottom:1px solid #000;' >Tania.DeLaCruz@saavienergia.com</td>" +
			 " </tr>" +
			"</table>" + 
			"</div>";*/
			
		/*var homeTable="<table style='font-size:6px;  border-collapse: collapse;margin-top:10px;margin-left:auto;margin-right:auto;'><tr><td>" +
		//"<IMG SRC='resources/images/saavi-logo-gris.png' width='400' height='166' style='margin-top:30px;margin-right:30px;margin-left:30px;'><br />" +
		"</td>" +
		"<td style='padding:20px;'>" +
		" <h1>En caso de dudas o aclaraciones y dependiendo de la razón social a la que esté proporcionando servicios favor de contactar al personal de Compras correspondiente:</h1><br>" +
		"<table BORDER  style='width:95%;font-size:10px;border:1px solid #000;' >" +
		"  <tr>" +
		 "   <th COLSPAN=3 style='background-color:#0781C2;'><b>Contactos Compras</b></th>" +
		 " </tr>" +
		"  <tr style='background-color:#7FD1FD;'>" +
		 "   <th><b>Razón Social</b></th>" +
		 "   <th>Comprador</th>" +
		 "   <th>Correo electronico</th>" +
		 " </tr>" +
		 " <tr>" +
		 "   <td>Energía Azteca X, S.A. de C.V.</td>" +
		 "   <td style='border-bottom:1px solid #000;'rowspan='2'>Jose Angel Romero</td>" +
		 "   <td style='border-bottom:1px solid #000;' rowspan='2'>jose.romero@saavienergia.com</td>" +
		 " </tr>" +
		 " <tr>" +
		  "  <td style='border-bottom:1px solid #000;'>Energía de Baja California, S. de R.L. de C.V.</td>" +
		 " </tr>" +
		 " <tr>" +
		 "   <td>Energía San Luis de la Paz, S.A. de C.V.</td>" +
		 "   <td style='border-bottom:1px solid #000;' rowspan='2'>Jorge Duran</td>" +
		  "  <td style='border-bottom:1px solid #000;' rowspan='2'>jorge.duran@saavienergia.com</td>" +
		 " </tr>" +
		  "<tr>" +
		  "  <td style='border-bottom:1px solid #000;'>Energía Azteca VIII, S. de R.L. de C.V.</td>" +
		 " </tr>" +
		 " <tr>" +
		 " <td style='border-bottom:1px solid #000;'>Energía Campeche, S.A. de C.V.</td>" +
		"	<td style='border-bottom:1px solid #000;'>Marco J. Romano</td>" +
		 " 	<td style='border-bottom:1px solid #000;'>Marco.Romano@saavienergia.com</td>" +
		 " </tr>" +
		 " <tr>" +
		  "  <td>Compresión Altamira, S.A. de C.V.</td>" +
		  "  <td style='border-bottom:1px solid #000;' rowspan='2'>Delia Balboa</td>" +
		  "  <td style='border-bottom:1px solid #000;' rowspan='2'>delia.balboa@saavienergia.com</td>" +
		 " </tr>" +
		  "<tr>" +
		  "  <td style='border-bottom:1px solid #000;'>Compresión Bajío, S. de R.L. de C.V.</td>" +	    
		 " </tr>" +
		 " <tr>" +
		 " <td style='border-bottom:1px solid #000;'>Energía Chihuahua, S.A. de C.V.</td>" +
		 " <td style='border-bottom:1px solid #000;' >Ixchel Vazquez</td>" +
		 " <td style='border-bottom:1px solid #000;' >Ixchel.Vazquez@saavienergia.com</td>" +
		 " </tr>" +
		 " <tr>" +
		 "   <td style='border-bottom:1px solid #000;'>GDD de Personal S. de R.L. de C.V.</td>" +
		 "   <td style='border-bottom:1px solid #000;'>Joel Carrera</td>" +
		 " 	<td style='border-bottom:1px solid #000;'>joel.carrera@saavienergia.com</td>" +
		 " </tr>" +
		"</table>" + 
		"<br>"+
		"<table style='width:50%;font-size:10px;border:1px solid #000;margin-left:auto;margin-right:auto;' >" +
		"  <tr>" +
		 "   <th COLSPAN=2 style='background-color:#0781C2;'><b>Contactos Cuentas por pagar</b></th>" +
		 " </tr>" +
		 "  <tr style='background-color:#7FD1FD;'>" +
		 "   <th><b>Contacto</b></th>" +
		 "   <th>Correo electronico</th>" +
		 " </tr>" +
		 " <tr>" +
		 " <td style='border-bottom:1px solid #000;' >Enrique Sanchez</td>" +
		 " <td style='border-bottom:1px solid #000;' >Enrique.Sanchez@saavienergia.com</td>" +
		 " </tr>" +
		 " <tr>" +
		 " <td style='border-bottom:1px solid #000;' >Maria Cardenas</td>" +
		 " <td style='border-bottom:1px solid #000;' >Maria.Cardenas@saavienergia.com</td>" +
		 " </tr>" +
		 " <tr>" +
		 " <td style='border-bottom:1px solid #000;' >Tania De La Cruz</td>" +
		 " <td style='border-bottom:1px solid #000;' >Tania.DeLaCruz@saavienergia.com</td>" +
		 " </tr>" +
		"</table>" + 
		"</td>" +
		"</tr>" +
		"</table>";*/

       var confNotif = '';
       if(role == 'ROLE_SUPPLIER'){
    	   confNotif = ' <div style="text-align: justify; text-justify: inter-word;border:0px;padding:20px;"> ' +
           '<h2 style="text-align:center;">'+ SuppAppMsg.tabNoticePrivacy + '</h2>'+
           '<h2 style="text-align:center;"><a href="https://www.sepasa.com/es/politicas-de-privacidad" target="_blank""> ' + SuppAppMsg.tabNoticePrivacyMsg + '</a></h2>'+
           '<div align="center"><IMG SRC="resources/images/Hame-logo.png" width="200" height="200" style="margin-top:15px;"></div>' +
			' </div>';
       }else{ 
    	   confNotif = ' <div style="text-align: justify; text-justify: inter-word;border:0px;padding:20px;"> ' +
    		            '<h2 style="text-align:center;">'+ SuppAppMsg.tabNoticePrivacy + '</h2>'+
    		            '<h2 style="text-align:center;"><a href="https://www.sepasa.com/es/politicas-de-privacidad" target="_blank""> '+ SuppAppMsg.tabNoticePrivacyMsg + '</a></h2>'+
    		            '<div align="center"><IMG SRC="resources/images/Hame-logo.png" width="200" height="200" style="margin-top:15px;"></div>' +
    		            /*
						'“EL TRABAJADOR” reconoce que con motivo de su relación de trabajo con la “EMPRESA”'+
						'y durante el desempeño de sus funciones tendrá acceso a información, materiales,'+
						'sistemas y documentos propiedad exclusiva de la “EMPRESA” y/o de sus clientes,'+
						'proveedores, filiales y/o subsidiarias, incluyendo acceso a cualquier información o'+
						'documentación contenida en la plataforma conocida como “Portal de Proveedores”, misma'+
						'que es considerada como confidencial o reservada (en lo sucesivo la “Información'+
						'Confidencial”).'+ 
						' <br /><br />'+
						' Por tanto, “EL TRABAJADOR” se obliga a (i) manejar la Información Confidencial como'+
						'estrictamente reservada; (ii) no divulgar ni proporcionar a persona alguna, incluyendo a'+
						'familiares, la Información Confidencial, sin la autorización expresa previa y por escrito por'+
						'parte de la “EMPRESA”; (iii) utilizar la Información Confidencial exclusivamente para el'+
						'desempeño de sus funciones y cumplimiento específico de las tareas asignadas y para'+
						'ningún otro propósito; (iv) Nunca permitir a ningún tercero, incluyendo familiares, el acceso'+
						'a la Información Confidencial; (v) a no utilizar dicha Información Confidencial en beneficio'+
						'propio o de terceros; (vi) no reproducir, grabar, copiar Información Confidencial, ni'+
						'removerla de las instalaciones de la “EMPRESA” o de las instalaciones en donde la'+
						'“EMPRESA” preste servicios, excepto cuando se requiera para el desempeño de sus'+
						'labores para la “EMPRESA”, en cuyo caso “EL TRABAJADOR” deberá, en todo momento,'+
						'tomar las medidas necesarias para preservar su confidencialidad y prevenir su divulgación.'+ 
						'<br /><br />'+
						'“EL TRABAJADOR” se obliga además a devolver la Información Confidencial en el'+
						'momento en que la “EMPRESA” así lo requiera, así como al término de su relación laboral,'+
						'sin retener copia alguna de dicha información. “EL TRABAJADOR” deberá reportar'+
						'inmediatamente a la “EMPRESA” la pérdida de Información Confidencial con la finalidad de'+
						'que se tomen las medidas de seguridad correspondientes.'+ 
						'<br /><br />'+
						'El incumplimiento y/o violación por parte de “EL TRABAJADOR” de cualquiera de estas'+
						'obligaciones será considerado como causal de rescisión de su contrato de trabajo sin'+
						'responsabilidad alguna para la “EMPRESA”, independientemente de la responsabilidad civil'+
						'y penal en la que pudiera incurrir “EL TRABAJADOR” de conformidad con la Ley de'+
						'Propiedad Industrial, el Código Penal Federal, o cualquier otra legislación aplicable.'+ 
						'<br /><br />'+
						'Ratifico la obligación de confidencialidad que me corresponde frente a la “EMPRESA”, y'+
						'acepto que las obligaciones que asumo conforme al Aviso de Confidencialidad anterior'+
						'continuarán en vigor indefinidamente al término de mi contrato laboral, aún y cuando la'+
						'relación de trabajo termine por cualquier motivo. Asimismo, asumo el compromiso de no'+
						'destinar para fines personales la información o documentos mencionados en este Aviso'+
						' <br /><br />' +*/
						' </div>';	
       }
              
		Ext.apply(this, {
			id : 'mainPanelId',
			renderTo: Ext.getBody(), 
			items : [{
				xtype : 'panel',
				title : SuppAppMsg.tabInicio,
				html: "<IMG SRC='resources/images/Hame-logo.png' width='350' height='350' style='margin-top:30px;text-align:center;'><br /><br /><span style='font-size:16px;text-decoration: underline;' >" + welcomeMessage + "</span>",
				bodyStyle:{"background-color":"#fff"}
			    },{
					xtype : 'tokenPanel',
					title : window.navigator.language.startsWith("es", 0)? 'Nuevos Registros':'New Records',
					border : true,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_PURCHASE' || role=='ROLE_TAX'?false:true
				           
				},{
					xtype : 'supplierPanel',
					title : SuppAppMsg.tabProveedores,
					border : true,
					id: 'supplierTab',
					hidden:role=='ROLE_ADMIN' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_CXP' || role=='ROLE_CXP_IMPORT' || role=='ROLE_MANAGER' || role=='ROLE_TAX'?false:true
				},{
					xtype : 'outSourcingPanel',
					title : 'Servicios Especializados',
					border : true,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_3RDPARTY' || role=='ROLE_RH' || role=='ROLE_TAX' || role=='ROLE_LEGAL' || (role=='ROLE_SUPPLIER' && osSupplier == 'TRUE') || role=='ROLE_REPSE' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' ?false:true          
				},{
					xtype : 'codigosSATPanel',
					title : 'Claves SAT',
					hidden:true
				},{
					xtype : 'purchaseOrderPanel',
					itemId: 'purchaseOrderPanel',
					title : SuppAppMsg.tabPurchaseOrder,
					hidden: role=='ROLE_ADMIN' || role=='ROLE_TAX' ||  role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_SUPPLIER' || role=='ROLE_CXP_IMPORT'  ?false:true
				}, {
					xtype : 'fiscalDocumentsPanel',
					itemId: 'fiscalDocumentsPanel',
					title : SuppAppMsg.tabFiscalDocuments,					
					hidden: role=='ROLE_ADMIN' || role=='ROLE_TAX' ||  role=='ROLE_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_SUPPLIER' ?false:true
				}, {
					xtype : 'invoicesPanel',
					title : SuppAppMsg.tabInvoices,					
					hidden: role=='ROLE_ADMIN' || role=='ROLE_TAX' ||  role=='ROLE_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role == 'ROLE_ADMIN_PURCHASE' ?false:true
				}, {
					xtype : 'approvalSearchPanel',
					title : SuppAppMsg.tabSearchApproval,
					hidden:role=='ROLE_SUPPLIER' || role=='ROLE_RH' || role=='ROLE_LEGAL' || role=='ROLE_REPSE' || role=='ROLE_3RDPARTY'?true:false
				},{
					xtype : 'approvalPanel',
					title : SuppAppMsg.tabApproval,
					border : true,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_TAX' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_CXP' ||role=='ROLE_CXP_IMPORT' || role=='ROLE_MANAGER'?false:true
				           
				},{
					xtype : 'noticePanel',
					title : 'Avisos',
					border : true,
					//hidden:role=='ROLE_ADMIN' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_CXP'?false:true,
							hidden: true,
					id: 'noticeTab',
				},{
					xtype : 'deliverPurchaseOrderPanel',
					// disabled:true,
					title : "Liberación de OC",
					border : true,
					//hidden: role=='ROLE_ADMIN' || role=='ROLE_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_TAX'?false:true
					hidden:true
				           
				},{
					xtype : 'nonComplianceSupplierPanel',
					title : SuppAppMsg.tabNonComplianceSupplier,
					hidden:role=='ROLE_ADMIN' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_TAX'?false:true
				},{
					xtype : 'panel',
					// disabled:true,
					title : "Informes",
					border : true,
					hidden:true
				},{
					xtype : 'logDataPanel',
					// disabled:true,
					title : SuppAppMsg.tabLogs,
					border : true,
					hidden:true
				},{
					xtype : 'usersPanel',
					// disabled:true,
					title : SuppAppMsg.tabUsers,
					border : true,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_IT'?false:true
				}, {
					xtype : 'udcPanel',
					// disabled:true,
					id:'udcTabPanel',
					title : SuppAppMsg.tabUDC,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_IT'?false:true
				},
					supplierProfilePanel
				,
				{
					xtype : 'panel',
					title : SuppAppMsg.tabNoticePrivacy,
					margin:'50 100 100 50',
					html: confNotif,
					hidden:role=='ROLE_ADMIN' || role=='ROLE_SUPPLIER'?false:true
					 
				} ]
		});
		this.callParent(arguments);
	}
});